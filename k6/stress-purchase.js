import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const errorRate       = new Rate('error_rate');
const purchaseDuration = new Trend('purchase_duration', true);

/**
 * Stress test: Escala rápidamente a alta carga para identificar el punto de quiebre.
 * Prueba GET /purchases/me (operación que requiere auth + query a DB).
 */
export const options = {
  stages: [
    { duration: '10s', target: 10  }, // Ramp-up rápido
    { duration: '20s', target: 30  }, // Carga media
    { duration: '20s', target: 50  }, // Carga alta (stress)
    { duration: '10s', target: 100 }, // Pico extremo
    { duration: '20s', target: 50  }, // Reducir
    { duration: '10s', target: 0   }, // Ramp-down
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'], // Bajo stress permitimos hasta 1s
    error_rate: ['rate<0.10'],         // Hasta 10% de error en stress
    http_req_failed: ['rate<0.10'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
  const loginBuyer1 = loginAs('buyer1', 'buyer123');
  const loginBuyer2 = loginAs('buyer2', 'buyer123');

  return {
    tokens: [loginBuyer1, loginBuyer2],
  };
}

export default function (data) {
  const token   = data.tokens[Math.floor(Math.random() * data.tokens.length)];
  const headers = { 'Authorization': `Bearer ${token}` };

  const start   = Date.now();
  const resList = http.get(`${BASE_URL}/purchases/me`, { headers });
  purchaseDuration.add(Date.now() - start);

  const ok = check(resList, {
    'GET /purchases/me → 200': (r) => r.status === 200,
    'respuesta es array':      (r) => {
      try { return Array.isArray(JSON.parse(r.body)); }
      catch { return false; }
    },
  });

  errorRate.add(!ok);
  sleep(0.2);
}

// Helpers
function loginAs(username, password) {
  const res = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ username, password }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  if (res.status !== 200) {
    throw new Error(`Login falló para ${username}: ${res.status}`);
  }
  return JSON.parse(res.body).token;
}

