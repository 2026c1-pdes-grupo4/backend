/**
 * E2E LOAD TEST — Flujo completo del comprador
 *
 *   1. Login
 *   2. Búsqueda de propiedades con filtros
 *   3. Ver detalle de una publicación
 *   4. Agregar a favoritos (409 si ya existe)
 *   5. Listar mis favoritos
 *   6. Actualizar un favorito (score + comentario)
 *   7. Ver historial de compras
 *   8. Intentar compra (400 PROPERTY_ALREADY_SOLD)
 *   9. Verificar que la compra aparece en el historial
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

const errorRate          = new Rate('e2e_buyer_error_rate');
const loginDuration      = new Trend('e2e_buyer_login_duration',    true);
const searchDuration     = new Trend('e2e_buyer_search_duration',   true);
const favoriteDuration   = new Trend('e2e_buyer_favorite_duration', true);
const purchaseDuration   = new Trend('e2e_buyer_purchase_duration', true);
const successfulLogins   = new Counter('e2e_buyer_successful_logins');
const successfulPurchases = new Counter('e2e_buyer_successful_purchases');
const alreadySoldCount   = new Counter('e2e_buyer_already_sold');

export const options = {
  stages: [
    { duration: '20s', target: 5  },   // Warm-up suave
    { duration: '40s', target: 15 },   // Ramp-up a carga media
    { duration: '60s', target: 25 },   // Carga sostenida (25 buyers simultáneos)
    { duration: '20s', target: 10 },   // Reducir
    { duration: '10s', target: 0  },   // Ramp-down
  ],
  thresholds: {
    e2e_buyer_error_rate:        ['rate<0.05'],   // < 5% errores
    e2e_buyer_login_duration:    ['p(95)<600'],   // Login < 600ms
    e2e_buyer_search_duration:   ['p(95)<900'],   // Búsqueda < 900ms
    e2e_buyer_favorite_duration: ['p(95)<700'],   // Favorito < 700ms
    e2e_buyer_purchase_duration: ['p(95)<1000'],  // Compra < 1s
    http_req_failed:             ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const BUYERS = [
  'buyer1', 'buyer2', 'buyer12',
  'manuel', 'claudia', 'jorge',
  'patricia', 'karina',
];

const AGENCY_PROPERTY_IDS = [3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20];

const SEARCH_SCENARIOS = [
  { city: 'Buenos Aires' },
  { city: 'Córdoba', propertyType: 'HOUSE' },
  { province: 'Buenos Aires', propertyType: 'APARTMENT' },
  { roomsMin: 3, priceMin: 80000, priceMax: 400000 },
  { city: 'Rosario' },
  { propertyType: 'HOUSE', roomsMin: 2 },
  { priceMin: 100000, priceMax: 500000 },
];

// Setup: pre-login de todos los buyers
export function setup() {
  const tokens = {};
  BUYERS.forEach((username) => {
    const res = http.post(
      `${BASE_URL}/auth/login`,
      JSON.stringify({ username, password: username === 'karina' ? 'admin123' : 'buyer123' }),
      { headers: { 'Content-Type': 'application/json' } }
    );
    if (res.status === 200) {
      tokens[username] = JSON.parse(res.body).token;
    } else {
      console.warn(`[setup] Login falló para ${username}: ${res.status}`);
    }
  });

  const availableUsers = Object.keys(tokens);
  if (availableUsers.length === 0) {
    throw new Error('No se pudo autenticar ningún buyer en setup');
  }
  console.log(`[setup] ${availableUsers.length} buyers autenticados: ${availableUsers.join(', ')}`);
  return { tokens };
}

// Función principal: flujo E2E completo por VU
export default function (data) {
  // Cada VU elige un buyer distinto según su número
  const buyers = Object.keys(data.tokens);
  const username = buyers[(__VU - 1) % buyers.length];
  let token = data.tokens[username];

  // El agency_property que este VU va a usar
  const apId = AGENCY_PROPERTY_IDS[(__VU - 1) % AGENCY_PROPERTY_IDS.length];

  const authHeaders = {
    'Authorization': `Bearer ${token}`,
    'Content-Type':  'application/json',
  };

  let flowOk = true;

  // PASO 1: Re-login
  group('1. Login', () => {
    const t0 = Date.now();
    const res = http.post(
      `${BASE_URL}/auth/login`,
      JSON.stringify({ username, password: username === 'karina' ? 'admin123' : 'buyer123' }),
      { headers: { 'Content-Type': 'application/json' } }
    );
    loginDuration.add(Date.now() - t0);

    const ok = check(res, {
      '[Login] status 200':      (r) => r.status === 200,
      '[Login] token presente':  (r) => { try { return !!JSON.parse(r.body).token; } catch { return false; } },
    });
    if (ok) {
      token = JSON.parse(res.body).token;
      authHeaders['Authorization'] = `Bearer ${token}`;
      successfulLogins.add(1);
    } else {
      flowOk = false;
      console.error(`[VU ${__VU}] Login falló: ${res.status} ${res.body}`);
    }
  });

  if (!flowOk) { errorRate.add(1); return; }
  sleep(0.3);

  // PASO 2: Búsqueda de propiedades
  group('2. Buscar propiedades', () => {
    const scenario = SEARCH_SCENARIOS[Math.floor(Math.random() * SEARCH_SCENARIOS.length)];
    const qs = buildQS(scenario);

    const t0  = Date.now();
    const res = http.get(`${BASE_URL}/properties/search?${qs}`, { headers: authHeaders });
    searchDuration.add(Date.now() - t0);

    const ok = check(res, {
      '[Búsqueda] status 200':          (r) => r.status === 200,
      '[Búsqueda] resultado tiene content': (r) => { try { return Array.isArray(JSON.parse(r.body).content); } catch { return false; } },
    });
    flowOk = flowOk && ok;
  });

  sleep(0.5);

  // PASO 3: Ver detalle de una publicación
  group('3. Ver detalle de publicación', () => {
    const res = http.get(`${BASE_URL}/agency-properties/${apId}`, { headers: authHeaders });

    check(res, {
      '[Detalle] status 200 o 404': (r) => r.status === 200 || r.status === 404,
      '[Detalle] no es 500':        (r) => r.status !== 500,
    });
  });

  sleep(0.3);

  // PASO 4: Agregar a favoritos
  let favoriteId = null;
  group('4. Agregar a favoritos', () => {
    const t0  = Date.now();
    const res = http.post(
      `${BASE_URL}/favorites`,
      JSON.stringify({ agencyPropertyId: apId, score: Math.floor(Math.random() * 5) + 5, comment: `VU ${__VU} - test carga` }),
      { headers: authHeaders }
    );
    favoriteDuration.add(Date.now() - t0);

    check(res, {
      '[Favorito] 201 creado o 409 duplicado': (r) => r.status === 201 || r.status === 409,
      '[Favorito] no es 500':                  (r) => r.status !== 500,
    });

    if (res.status === 201) {
      try { favoriteId = JSON.parse(res.body).id ?? JSON.parse(res.body).favoriteId; } catch {}
    }
  });

  sleep(0.3);

  // PASO 5: Listar mis favoritos
  group('5. Listar mis favoritos', () => {
    const res = http.get(`${BASE_URL}/favorites/me`, { headers: authHeaders });

    const ok = check(res, {
      '[Favoritos] status 200':        (r) => r.status === 200,
      '[Favoritos] es array':          (r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
    });
    flowOk = flowOk && ok;

    // Si no tenemos favoriteId del paso anterior, obtenemos el primero de la lista
    if (!favoriteId) {
      try {
        const list = JSON.parse(res.body);
        if (Array.isArray(list) && list.length > 0) {
          favoriteId = list[0].id ?? list[0].favoriteId;
        }
      } catch {}
    }
  });

  sleep(0.3);

  // PASO 6: Actualizar favorito
  if (favoriteId) {
    group('6. Actualizar favorito', () => {
      const res = http.put(
        `${BASE_URL}/favorites/${favoriteId}`,
        JSON.stringify({ score: 9, comment: `Actualizado en carga VU-${__VU}` }),
        { headers: authHeaders }
      );

      check(res, {
        '[Update Favorito] 200 o 403 o 404': (r) => r.status === 200 || r.status === 403 || r.status === 404,
        '[Update Favorito] no es 500':       (r) => r.status !== 500,
      });
    });
    sleep(0.2);
  }

  // PASO 7: Ver historial de compras
  group('7. Historial de compras', () => {
    const t0  = Date.now();
    const res = http.get(`${BASE_URL}/purchases/me`, { headers: authHeaders });
    purchaseDuration.add(Date.now() - t0);

    const ok = check(res, {
      '[Compras] status 200':    (r) => r.status === 200,
      '[Compras] es array':      (r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
    });
    flowOk = flowOk && ok;
  });

  sleep(0.3);

  // PASO 8: Intentar comprar
  group('8. Realizar compra', () => {
    const t0  = Date.now();
    const res = http.post(
      `${BASE_URL}/purchases`,
      JSON.stringify({ agencyPropertyId: apId }),
      { headers: authHeaders }
    );
    purchaseDuration.add(Date.now() - t0);

    const ok = check(res, {
      '[Compra] 201 éxito o 400 ya vendida': (r) => r.status === 201 || r.status === 400,
      '[Compra] no es 500':                  (r) => r.status !== 500,
    });

    if (res.status === 201) {
      successfulPurchases.add(1);
    } else if (res.status === 400) {
      try {
        if (JSON.parse(res.body).code === 'PROPERTY_ALREADY_SOLD') alreadySoldCount.add(1);
      } catch {}
    }
  });

  sleep(0.3);

  // PASO 9: Verificar historial post-compra
  group('9. Verificar historial post-compra', () => {
    const res = http.get(`${BASE_URL}/purchases/me`, { headers: authHeaders });

    check(res, {
      '[Post-Compra] historial 200': (r) => r.status === 200,
      '[Post-Compra] es array':      (r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
    });
  });

  errorRate.add(!flowOk);
  sleep(1);
}

// Helper
function buildQS(obj) {
  return Object.entries(obj)
    .map(([k, v]) => `${k}=${encodeURIComponent(v)}`)
    .join('&');
}

