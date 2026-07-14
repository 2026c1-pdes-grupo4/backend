/**
 * E2E STRESS TEST — Flujos completos bajo alta carga
 **
 *   Scenario "buyer_journey"   → Buyers: login → buscar → favoritar → comprar
 *   Scenario "agency_journey"  → Agencias: login → publicar → actualizar → ver ventas
 *   Scenario "admin_journey"   → Admins: login → reportes → gestionar usuarios
 *   Scenario "search_spike"    → Spike de búsquedas puras (operación más frecuente)
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

const buyerErrorRate   = new Rate('stress_buyer_error_rate');
const agencyErrorRate  = new Rate('stress_agency_error_rate');
const adminErrorRate   = new Rate('stress_admin_error_rate');
const globalErrorRate  = new Rate('stress_global_error_rate');

const loginDuration    = new Trend('stress_login_duration',    true);
const searchDuration   = new Trend('stress_search_duration',   true);
const purchaseDuration = new Trend('stress_purchase_duration', true);
const reportDuration   = new Trend('stress_report_duration',   true);

const successfulBuys   = new Counter('stress_successful_purchases');
const alreadySold      = new Counter('stress_already_sold');
const serverErrors     = new Counter('stress_server_errors_500');

export const options = {
  scenarios: {
    // Buyers haciendo flujo completo
    buyer_journey: {
      executor:          'ramping-vus',
      startVUs:          0,
      stages: [
        { duration: '15s', target: 10 },
        { duration: '40s', target: 30 },  // 30 buyers bajo stress
        { duration: '30s', target: 50 },  // Pico: 50 buyers
        { duration: '20s', target: 20 },
        { duration: '15s', target: 0  },
      ],
      exec: 'buyerFlow',
    },

    // Agencias publicando y gestionando
    agency_journey: {
      executor:          'ramping-vus',
      startVUs:          0,
      stages: [
        { duration: '15s', target: 3  },
        { duration: '40s', target: 8  },  // 8 agencias concurrentes
        { duration: '30s', target: 12 },  // Pico: 12 agencias
        { duration: '20s', target: 5  },
        { duration: '15s', target: 0  },
      ],
      exec: 'agencyFlow',
    },

    // Admins consultando reportes
    admin_journey: {
      executor:          'ramping-vus',
      startVUs:          0,
      stages: [
        { duration: '20s', target: 1  },
        { duration: '50s', target: 3  },  // 3 admins concurrentes
        { duration: '30s', target: 3  },
        { duration: '20s', target: 0  },
      ],
      exec: 'adminFlow',
    },

    search_spike: {
      executor:          'ramping-vus',
      startVUs:          0,
      stages: [
        { duration: '10s', target: 0   },
        { duration: '5s',  target: 50  },
        { duration: '30s', target: 80  },
        { duration: '20s', target: 20  },
        { duration: '15s', target: 0   },
      ],
      exec: 'searchSpike',
    },
  },

  thresholds: {
    stress_buyer_error_rate:  ['rate<0.10'],   // < 10% en buyers
    stress_agency_error_rate: ['rate<0.08'],   // < 8% en agencias
    stress_admin_error_rate:  ['rate<0.05'],   // < 5% en admins
    stress_global_error_rate: ['rate<0.10'],   // < 10% global

    stress_login_duration:    ['p(95)<1500'],  // Login < 1.5s bajo stress
    stress_search_duration:   ['p(95)<2000'],  // Búsqueda < 2s bajo stress
    stress_purchase_duration: ['p(95)<2500'],  // Compra < 2.5s bajo stress
    stress_report_duration:   ['p(95)<4000'],  // Reportes < 4s bajo stress

    http_req_failed:          ['rate<0.15'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const BUYERS   = ['buyer1', 'buyer2', 'buyer12', 'manuel', 'claudia', 'jorge', 'patricia'];
const AGENCIES = ['inmo1', 'inmo2', 'inmo3', 'ritondo_propiedades', 'nordelta_propiedades', 'sur_inversiones'];
const ADMINS   = ['admin123'];

const AGENCY_PROP_IDS = [3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18];

const SEARCH_QUERIES = [
  '?city=Buenos+Aires',
  '?city=C%C3%B3rdoba&propertyType=HOUSE',
  '?province=Buenos+Aires',
  '?rooms=3&priceMin=80000&priceMax=400000',
  '?city=Rosario',
  '?propertyType=APARTMENT&rooms=2',
  '?priceMin=100000&priceMax=600000',
  '?keyword=spacious',
  '?city=Buenos+Aires&propertyType=HOUSE&rooms=3',
];

// Setup: obtener todos los tokens
export function setup() {
  const tokens = { buyers: {}, agencies: {}, admins: {} };

  // Login buyers
  BUYERS.forEach((u) => {
    const res = loginAs(u, 'buyer123');
    if (res) tokens.buyers[u] = res;
  });

  // Login agencies
  AGENCIES.forEach((u) => {
    const res = loginAs(u, 'agency123');
    if (res) tokens.agencies[u] = res;
  });

  // Login admins
  ADMINS.forEach((u) => {
    const res = loginAs(u, 'admin123');
    if (res) tokens.admins[u] = res;
  });

  console.log(`[setup] Tokens: buyers=${Object.keys(tokens.buyers).length}, agencies=${Object.keys(tokens.agencies).length}, admins=${Object.keys(tokens.admins).length}`);
  return tokens;
}

// FLUJO BUYER — E2E completo
export function buyerFlow(data) {
  const buyerNames = Object.keys(data.buyers);
  if (buyerNames.length === 0) { buyerErrorRate.add(1); return; }

  const username = buyerNames[(__VU - 1) % buyerNames.length];
  let token = data.buyers[username];
  const apId = AGENCY_PROP_IDS[(__VU - 1) % AGENCY_PROP_IDS.length];
  let flowOk = true;

  const h = () => ({ 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' });

  // 1. Re-login
  group('[Buyer] 1. Login', () => {
    const t0  = Date.now();
    const res = http.post(`${BASE_URL}/auth/login`,
      JSON.stringify({ username, password: 'buyer123' }),
      { headers: { 'Content-Type': 'application/json' } }
    );
    loginDuration.add(Date.now() - t0);
    if (res.status === 200) {
      token = JSON.parse(res.body).token;
    } else {
      flowOk = false;
    }
    check(res, { '[Buyer Login] 200': (r) => r.status === 200 });
    if (res.status === 500) serverErrors.add(1);
  });
  if (!flowOk) { buyerErrorRate.add(1); globalErrorRate.add(1); return; }
  sleep(0.2);

  // 2. Buscar propiedades
  group('[Buyer] 2. Búsqueda', () => {
    const qs  = SEARCH_QUERIES[Math.floor(Math.random() * SEARCH_QUERIES.length)];
    const t0  = Date.now();
    const res = http.get(`${BASE_URL}/properties/search${qs}`, { headers: h() });
    searchDuration.add(Date.now() - t0);
    const ok = check(res, {
      '[Buyer Búsqueda] 200':       (r) => r.status === 200,
      '[Buyer Búsqueda] es array':  (r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
    });
    flowOk = flowOk && ok;
    if (res.status === 500) serverErrors.add(1);
  });
  sleep(0.2);

  // 3. Ver detalle
  group('[Buyer] 3. Ver detalle', () => {
    const res = http.get(`${BASE_URL}/agency-properties/${apId}`, { headers: h() });
    check(res, {
      '[Buyer Detalle] 200 o 404': (r) => r.status === 200 || r.status === 404,
      '[Buyer Detalle] no 500':    (r) => r.status !== 500,
    });
    if (res.status === 500) serverErrors.add(1);
  });
  sleep(0.2);

  // 4. Agregar favorito
  group('[Buyer] 4. Favorito', () => {
    const res = http.post(`${BASE_URL}/favorites`,
      JSON.stringify({ agencyPropertyId: apId, score: 7, comment: 'stress test' }),
      { headers: h() }
    );
    check(res, {
      '[Buyer Favorito] 201 o 409': (r) => r.status === 201 || r.status === 409,
      '[Buyer Favorito] no 500':    (r) => r.status !== 500,
    });
    if (res.status === 500) serverErrors.add(1);
  });
  sleep(0.2);

  // 5. Listar favoritos
  group('[Buyer] 5. Mis favoritos', () => {
    const res = http.get(`${BASE_URL}/favorites/me`, { headers: h() });
    check(res, {
      '[Buyer Mis Favs] 200':     (r) => r.status === 200,
      '[Buyer Mis Favs] es array':(r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
    });
    if (res.status === 500) serverErrors.add(1);
  });
  sleep(0.2);

  // 6. Ver historial de compras
  group('[Buyer] 6. Historial compras', () => {
    const t0  = Date.now();
    const res = http.get(`${BASE_URL}/purchases/me`, { headers: h() });
    purchaseDuration.add(Date.now() - t0);
    check(res, {
      '[Buyer Compras] 200':    (r) => r.status === 200,
      '[Buyer Compras] array':  (r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
    });
    if (res.status === 500) serverErrors.add(1);
  });
  sleep(0.2);

  // 7. Intentar compra
  group('[Buyer] 7. Comprar', () => {
    const t0  = Date.now();
    const res = http.post(`${BASE_URL}/purchases`,
      JSON.stringify({ agencyPropertyId: apId }),
      { headers: h() }
    );
    purchaseDuration.add(Date.now() - t0);
    check(res, {
      '[Buyer Compra] 201 o 400': (r) => r.status === 201 || r.status === 400,
      '[Buyer Compra] no 500':    (r) => r.status !== 500,
    });
    if (res.status === 201)      successfulBuys.add(1);
    if (res.status === 400) {
      try { if (JSON.parse(res.body).code === 'PROPERTY_ALREADY_SOLD') alreadySold.add(1); } catch {}
    }
    if (res.status === 500) serverErrors.add(1);
  });
  sleep(0.2);

  // 8. Verificar historial post-compra
  group('[Buyer] 8. Verificar post-compra', () => {
    const res = http.get(`${BASE_URL}/purchases/me`, { headers: h() });
    check(res, { '[Buyer Post-Compra] 200': (r) => r.status === 200 });
  });

  buyerErrorRate.add(!flowOk);
  globalErrorRate.add(!flowOk);
  sleep(0.5);
}

// FLUJO AGENCY — E2E completo
export function agencyFlow(data) {
  const agencyNames = Object.keys(data.agencies);
  if (agencyNames.length === 0) { agencyErrorRate.add(1); return; }

  const username = agencyNames[(__VU - 1) % agencyNames.length];
  let token = data.agencies[username];
  let flowOk = true;

  const h = () => ({ 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' });
  const uniqueSuffix = `${__VU}_${__ITER}_${Date.now()}`;

  let propertyId   = null;
  let agencyPropId = null;

  // 1. Login
  group('[Agency] 1. Login', () => {
    const t0  = Date.now();
    const res = http.post(`${BASE_URL}/auth/login`,
      JSON.stringify({ username, password: 'agency123' }),
      { headers: { 'Content-Type': 'application/json' } }
    );
    loginDuration.add(Date.now() - t0);
    if (res.status === 200) {
      token = JSON.parse(res.body).token;
    } else {
      flowOk = false;
    }
    check(res, { '[Agency Login] 200': (r) => r.status === 200 });
    if (res.status === 500) serverErrors.add(1);
  });
  if (!flowOk) { agencyErrorRate.add(1); globalErrorRate.add(1); return; }
  sleep(0.2);

  // 2. Ver mis publicaciones
  group('[Agency] 2. Mis publicaciones', () => {
    const res = http.get(`${BASE_URL}/agency-properties/agency/me`, { headers: h() });
    check(res, {
      '[Agency Mis Pubs] 200':    (r) => r.status === 200,
      '[Agency Mis Pubs] array':  (r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
    });
    if (res.status === 500) serverErrors.add(1);
  });
  sleep(0.2);

  // 3. Crear propiedad
  group('[Agency] 3. Crear propiedad', () => {
    const res = http.post(`${BASE_URL}/properties`,
      JSON.stringify({
        propertyType: __ITER % 2 === 0 ? 'HOUSE' : 'APARTMENT',
        price:        180000 + (__VU * 5000),
        address:      `Stress St ${uniqueSuffix}`,
        city:         'Buenos Aires',
        province:     'Buenos Aires',
        areaSq:       80,
        rooms:        3,
        description:  `Stress test VU-${__VU}`,
      }),
      { headers: h() }
    );
    const ok = check(res, {
      '[Agency Crear] 201':    (r) => r.status === 201,
      '[Agency Crear] no 500': (r) => r.status !== 500,
    });
    if (ok) {
      try { const b = JSON.parse(res.body); propertyId = b.id ?? b.propertyId; } catch {}
    }
    flowOk = flowOk && ok;
    if (res.status === 500) serverErrors.add(1);
  });
  if (!propertyId) { agencyErrorRate.add(1); globalErrorRate.add(1); return; }
  sleep(0.2);

  // 4. Publicar propiedad
  group('[Agency] 4. Publicar', () => {
    const res = http.post(`${BASE_URL}/agency-properties`,
      JSON.stringify({ propertyId, listedPrice: 170000 + (__VU * 4500) }),
      { headers: h() }
    );
    check(res, {
      '[Agency Publicar] 201 o 409': (r) => r.status === 201 || r.status === 409,
      '[Agency Publicar] no 500':    (r) => r.status !== 500,
    });
    if (res.status === 201) {
      try { const b = JSON.parse(res.body); agencyPropId = b.id ?? b.agencyPropertyId; } catch {}
    }
    if (res.status === 500) serverErrors.add(1);
  });
  sleep(0.2);

  // 5. Actualizar precio
  if (agencyPropId) {
    group('[Agency] 5. Actualizar precio', () => {
      const res = http.put(`${BASE_URL}/agency-properties/${agencyPropId}`,
        JSON.stringify({ listedPrice: 160000 + (__VU * 4000) }),
        { headers: h() }
      );
      check(res, {
        '[Agency Update] 200 o 404': (r) => r.status === 200 || r.status === 404,
        '[Agency Update] no 500':    (r) => r.status !== 500,
      });
      if (res.status === 500) serverErrors.add(1);
    });
    sleep(0.2);
  }

  // 6. Ver ventas de mi agencia
  group('[Agency] 6. Ventas', () => {
    const res = http.get(`${BASE_URL}/purchases/agency/me`, { headers: h() });
    check(res, {
      '[Agency Ventas] 200':   (r) => r.status === 200,
      '[Agency Ventas] array': (r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
    });
    if (res.status === 500) serverErrors.add(1);
  });
  sleep(0.2);

  // 7. Limpieza: eliminar publicación creada
  if (agencyPropId) {
    group('[Agency] 7. Limpiar publicación', () => {
      const res = http.del(`${BASE_URL}/agency-properties/${agencyPropId}`, null, { headers: h() });
      check(res, {
        '[Agency Limpiar] 204 o 400 o 404': (r) => r.status === 204 || r.status === 400 || r.status === 404,
        '[Agency Limpiar] no 500':          (r) => r.status !== 500,
      });
    });
  }

  agencyErrorRate.add(!flowOk);
  globalErrorRate.add(!flowOk);
  sleep(0.5);
}

// FLUJO ADMIN — Reportes y gestión
export function adminFlow(data) {
  const adminNames = Object.keys(data.admins);
  if (adminNames.length === 0) { adminErrorRate.add(1); return; }

  const username = adminNames[(__VU - 1) % adminNames.length];
  let token = data.admins[username];
  let flowOk = true;

  const h = () => ({ 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' });
  const uniqueSuffix = `${__VU}_${__ITER}_${Date.now()}`;

  // 1. Login
  group('[Admin] 1. Login', () => {
    const t0  = Date.now();
    const res = http.post(`${BASE_URL}/auth/login`,
      JSON.stringify({ username, password: 'admin123' }),
      { headers: { 'Content-Type': 'application/json' } }
    );
    loginDuration.add(Date.now() - t0);
    if (res.status === 200) {
      token = JSON.parse(res.body).token;
    } else {
      flowOk = false;
    }
    check(res, { '[Admin Login] 200': (r) => r.status === 200 });
  });
  if (!flowOk) { adminErrorRate.add(1); globalErrorRate.add(1); return; }
  sleep(0.3);

  // 2–3. Listar usuarios y agencias
  group('[Admin] 2-3. Listar usuarios y agencias', () => {
    const [usersRes, agenciesRes] = http.batch([
      ['GET', `${BASE_URL}/admin/users`,    null, { headers: h() }],
      ['GET', `${BASE_URL}/admin/agencies`, null, { headers: h() }],
    ]);
    check(usersRes,    { '[Admin Usuarios] 200':  (r) => r.status === 200 });
    check(agenciesRes, { '[Admin Agencias] 200':  (r) => r.status === 200 });
    if (usersRes.status === 500 || agenciesRes.status === 500) serverErrors.add(1);
  });
  sleep(0.3);

  // 4–5. Consultar favoritos y compras globales
  group('[Admin] 4-5. Favoritos y compras globales', () => {
    const [favsRes, purchasesRes] = http.batch([
      ['GET', `${BASE_URL}/admin/favorites`, null, { headers: h() }],
      ['GET', `${BASE_URL}/admin/purchases`, null, { headers: h() }],
    ]);
    check(favsRes,      { '[Admin Favs] 200':     (r) => r.status === 200 });
    check(purchasesRes, { '[Admin Purchases] 200': (r) => r.status === 200 });
  });
  sleep(0.3);

  // 6–8. Los 3 reportes
  group('[Admin] 6-8. Reportes (batch)', () => {
    const t0 = Date.now();
    const [r1, r2, r3] = http.batch([
      ['GET', `${BASE_URL}/admin/reports/top-buyers`,             null, { headers: h() }],
      ['GET', `${BASE_URL}/admin/reports/top-ranked-properties`,  null, { headers: h() }],
      ['GET', `${BASE_URL}/admin/reports/top-agencies-sales`,     null, { headers: h() }],
    ]);
    reportDuration.add(Date.now() - t0);
    check(r1, { '[Admin Top Buyers] 200':     (r) => r.status === 200 });
    check(r2, { '[Admin Top Props] 200':      (r) => r.status === 200 });
    check(r3, { '[Admin Top Agencies] 200':   (r) => r.status === 200 });
    if (r1.status === 500 || r2.status === 500 || r3.status === 500) serverErrors.add(1);
  });
  sleep(0.3);

  // 9. Crear y eliminar usuario
  let tempUserId = null;
  group('[Admin] 9. Crear usuario temporal', () => {
    const res = http.post(`${BASE_URL}/users`,
      JSON.stringify({
        username:    `stress_user_${uniqueSuffix}`,
        email:       `stress_${uniqueSuffix}@test.com`,
        password:    'testpass123',
        profileType: 'BUYER',
      }),
      { headers: h() }
    );
    check(res, {
      '[Admin Crear User] 201 o 409': (r) => r.status === 201 || r.status === 409,
      '[Admin Crear User] no 500':    (r) => r.status !== 500,
    });
    if (res.status === 201) {
      try { const b = JSON.parse(res.body); tempUserId = b.id ?? b.userId; } catch {}
    }
    if (res.status === 500) serverErrors.add(1);
  });
  sleep(0.2);

  if (tempUserId) {
    group('[Admin] 10. Eliminar usuario temporal', () => {
      const res = http.del(`${BASE_URL}/users/${tempUserId}`, null, { headers: h() });
      check(res, { '[Admin Del User] 204 o 404': (r) => r.status === 204 || r.status === 404 });
    });
  }

  adminErrorRate.add(!flowOk);
  globalErrorRate.add(!flowOk);
  sleep(1);
}

// FLUJO SEARCH SPIKE (búsquedas masivas)
export function searchSpike(data) {
  const buyerNames = Object.keys(data.buyers);
  if (buyerNames.length === 0) { globalErrorRate.add(1); return; }

  const token = data.buyers[buyerNames[(__VU - 1) % buyerNames.length]];
  const qs    = SEARCH_QUERIES[Math.floor(Math.random() * SEARCH_QUERIES.length)];
  const headers = { 'Authorization': `Bearer ${token}` };

  const t0  = Date.now();
  const res = http.get(`${BASE_URL}/properties/search${qs}`, { headers });
  searchDuration.add(Date.now() - t0);

  const ok = check(res, {
    '[Spike Búsqueda] 200':       (r) => r.status === 200,
    '[Spike Búsqueda] no 500':    (r) => r.status !== 500,
    '[Spike Búsqueda] < 3000ms':  (r) => r.timings.duration < 3000,
  });

  globalErrorRate.add(!ok);
  if (res.status === 500) serverErrors.add(1);
  sleep(0.1);
}

// Helper
function loginAs(username, password) {
  const res = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ username, password }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  if (res.status !== 200) {
    console.warn(`[setup] Login falló para ${username}: ${res.status}`);
    return null;
  }
  return JSON.parse(res.body).token;
}

