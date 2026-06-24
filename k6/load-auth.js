import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const errorRate = new Rate('error_rate');
const loginDuration = new Trend('login_duration', true);

export const options = {
  stages: [
    { duration: '15s', target: 5  },  // Ramp-up a 5 VUs
    { duration: '30s', target: 20 },  // Ramp-up a 20 VUs
    { duration: '30s', target: 20 },  // Sostener 20 VUs
    { duration: '15s', target: 0  },  // Ramp-down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% de requests < 500ms
    error_rate: ['rate<0.05'],        // Tasa de error < 5%
    http_req_failed: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const users = [
  { username: 'buyer1',  password: 'buyer123'  },
  { username: 'buyer2',  password: 'buyer123'  },
  { username: 'admin123', password: 'admin123' },
];

export default function () {
  const user = users[Math.floor(Math.random() * users.length)];

  const payload = JSON.stringify({ username: user.username, password: user.password });
  const params  = { headers: { 'Content-Type': 'application/json' } };

  const start = Date.now();
  const res   = http.post(`${BASE_URL}/auth/login`, payload, params);
  loginDuration.add(Date.now() - start);

  const ok = check(res, {
    'status es 200':        (r) => r.status === 200,
    'token en respuesta':   (r) => JSON.parse(r.body).token !== undefined,
  });

  errorRate.add(!ok);
  sleep(1);
}

