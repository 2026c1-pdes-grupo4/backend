import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const errorRate    = new Rate('error_rate');
const searchDuration = new Trend('search_duration', true);

export const options = {
  stages: [
    { duration: '10s', target: 5  },  // Ramp-up
    { duration: '40s', target: 15 },  // Carga sostenida
    { duration: '10s', target: 0  },  // Ramp-down
  ],
  thresholds: {
    http_req_duration: ['p(95)<800'],
    error_rate: ['rate<0.05'],
    http_req_failed: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const searchScenarios = [
  { city: 'Buenos Aires' },
  { province: 'Córdoba' },
  { propertyType: 'HOUSE' },
  { rooms: 3 },
  { priceMin: 50000, priceMax: 150000 },
  { city: 'Buenos Aires', propertyType: 'HOUSE', rooms: 3 },
  { keyword: 'spacious' },
];

export function setup() {
  const res = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ username: 'buyer1', password: 'buyer123' }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  if (res.status !== 200) {
    throw new Error(`Login falló en setup: ${res.status} ${res.body}`);
  }

  return { token: JSON.parse(res.body).token };
}

export default function (data) {
  const scenario = searchScenarios[Math.floor(Math.random() * searchScenarios.length)];
  const params   = buildQueryParams(scenario);
  const headers  = {
    'Authorization': `Bearer ${data.token}`,
    'Content-Type':  'application/json',
  };

  const start = Date.now();
  const res   = http.get(`${BASE_URL}/properties/search?${params}`, { headers });
  searchDuration.add(Date.now() - start);

  const ok = check(res, {
    'status es 200':             (r) => r.status === 200,
    'respuesta es array':        (r) => Array.isArray(JSON.parse(r.body)),
  });

  errorRate.add(!ok);
  sleep(0.5);
}

function buildQueryParams(scenario) {
  return Object.entries(scenario)
    .map(([k, v]) => `${k}=${encodeURIComponent(v)}`)
    .join('&');
}

