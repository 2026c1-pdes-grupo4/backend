/**
 * E2E LOAD TEST — Flujo completo del Admin
 *
 *   1. Login como admin
 *   2. Ver todos los usuarios del sistema
 *   3. Ver todas las agencias
 *   4. Crear un nuevo usuario buyer
 *   5. Crear una nueva agencia bajo ese admin
 *   6. Ver todos los favoritos del sistema
 *   7. Ver todas las compras del sistema
 *   8. Consultar reportes: top-buyers
 *   9. Consultar reportes: top-ranked-properties
 *  10. Consultar reportes: top-agencies-sales
 *  11. Eliminar la agencia creada
 *  12. Eliminar el usuario creado
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

const errorRate       = new Rate('e2e_admin_error_rate');
const loginDuration   = new Trend('e2e_admin_login_duration',   true);
const reportDuration  = new Trend('e2e_admin_report_duration',  true);
const successUsers    = new Counter('e2e_admin_users_created');
const successAgencies = new Counter('e2e_admin_agencies_created');

export const options = {
  stages: [
    { duration: '10s', target: 2  },   // Warm-up (pocos admins)
    { duration: '30s', target: 5  },   // Carga media
    { duration: '40s', target: 5  },   // Carga sostenida
    { duration: '10s', target: 0  },   // Ramp-down
  ],
  thresholds: {
    e2e_admin_error_rate:      ['rate<0.05'],
    e2e_admin_login_duration:  ['p(95)<600'],
    e2e_admin_report_duration: ['p(95)<2000'],
    http_req_failed:           ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Admins del seed
const ADMINS = ['admin123', 'karina'];
const ADMIN_PASSWORDS = { 'admin123': 'admin123', 'karina': 'admin123' };

// Setup
export function setup() {
  const tokens = {};
  const adminIds = {};

  ADMINS.forEach((username) => {
    const res = http.post(
      `${BASE_URL}/auth/login`,
      JSON.stringify({ username, password: ADMIN_PASSWORDS[username] }),
      { headers: { 'Content-Type': 'application/json' } }
    );
    if (res.status === 200) {
      const body = JSON.parse(res.body);
      tokens[username] = body.token;
      // Intentar extraer el userId del token (JWT payload)
      try {
        const payload = JSON.parse(atob(body.token.split('.')[1]));
        adminIds[username] = payload.id ?? payload.sub;
      } catch {}
    } else {
      console.warn(`[setup] Login admin falló para ${username}: ${res.status}`);
    }
  });

  // Obtener lista de usuarios para extraer IDs de admins reales
  const firstAdmin = Object.keys(tokens)[0];
  if (firstAdmin) {
    const usersRes = http.get(`${BASE_URL}/users`, {
      headers: { 'Authorization': `Bearer ${tokens[firstAdmin]}` }
    });
    if (usersRes.status === 200) {
      try {
        const users = JSON.parse(usersRes.body);
        ADMINS.forEach((name) => {
          const found = users.find((u) => u.username === name);
          if (found) adminIds[name] = found.id ?? found.userId;
        });
      } catch {}
    }
  }

  console.log(`[setup] Admins autenticados: ${Object.keys(tokens).join(', ')}`);
  return { tokens, adminIds };
}

// Función ppal
export default function (data) {
  const adminNames = Object.keys(data.tokens);
  const adminName  = adminNames[(__VU - 1) % adminNames.length];
  let token = data.tokens[adminName];
  const adminId = data.adminIds[adminName] ?? 1;

  let authHeaders = {
    'Authorization': `Bearer ${token}`,
    'Content-Type':  'application/json',
  };

  let flowOk    = true;
  let newUserId = null;
  let newAgencyId = null;
  // Usar un sufijo único por VU+iteración para evitar conflictos de username/email
  const uniqueSuffix = `${__VU}_${__ITER}_${Date.now()}`;

  // PASO 1: Login
  group('1. Login admin', () => {
    const t0  = Date.now();
    const res = http.post(
      `${BASE_URL}/auth/login`,
      JSON.stringify({ username: adminName, password: ADMIN_PASSWORDS[adminName] }),
      { headers: { 'Content-Type': 'application/json' } }
    );
    loginDuration.add(Date.now() - t0);

    const ok = check(res, {
      '[Login Admin] status 200':     (r) => r.status === 200,
      '[Login Admin] token presente': (r) => { try { return !!JSON.parse(r.body).token; } catch { return false; } },
    });
    if (ok) {
      token = JSON.parse(res.body).token;
      authHeaders['Authorization'] = `Bearer ${token}`;
    }
    flowOk = flowOk && ok;
  });

  if (!flowOk) { errorRate.add(1); return; }
  sleep(0.3);

  // PASO 2: Ver todos los usuarios
  group('2. Listar usuarios', () => {
    const res = http.get(`${BASE_URL}/admin/users`, { headers: authHeaders });

    check(res, {
      '[Usuarios] status 200': (r) => r.status === 200,
      '[Usuarios] es array':   (r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
    });
  });

  sleep(0.3);

  // PASO 3: Ver todas las agencias
  group('3. Listar agencias', () => {
    const res = http.get(`${BASE_URL}/admin/agencies`, { headers: authHeaders });

    check(res, {
      '[Agencias] status 200': (r) => r.status === 200,
      '[Agencias] es array':   (r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
    });
  });

  sleep(0.3);

  // PASO 4: Crear nuevo usuario buyer
  group('4. Crear usuario buyer', () => {
    const payload = {
      username:    `load_buyer_${uniqueSuffix}`,
      email:       `load_buyer_${uniqueSuffix}@test.com`,
      password:    'test1234',
      profileType: 'BUYER',
    };

    const res = http.post(`${BASE_URL}/users`, JSON.stringify(payload), { headers: authHeaders });

    const ok = check(res, {
      '[Crear Usuario] status 201':  (r) => r.status === 201,
      '[Crear Usuario] tiene id':    (r) => { try { const b = JSON.parse(r.body); return b.id !== undefined || b.userId !== undefined; } catch { return false; } },
      '[Crear Usuario] no es 500':   (r) => r.status !== 500,
    });

    if (ok) {
      try {
        const body = JSON.parse(res.body);
        newUserId = body.id ?? body.userId;
        successUsers.add(1);
      } catch {}
    }
  });

  sleep(0.3);

  // PASO 5: Crear agencia
  group('5. Crear agencia', () => {
    const payload = {
      username:    `load_agency_${uniqueSuffix}`,
      email:       `load_agency_${uniqueSuffix}@test.com`,
      password:    'agencytest123',
      adminUserId: adminId,
    };

    const res = http.post(`${BASE_URL}/agencies`, JSON.stringify(payload), { headers: authHeaders });

    const ok = check(res, {
      '[Crear Agencia] status 201': (r) => r.status === 201,
      '[Crear Agencia] tiene id':   (r) => { try { const b = JSON.parse(r.body); return b.id !== undefined || b.agencyId !== undefined; } catch { return false; } },
      '[Crear Agencia] no es 500':  (r) => r.status !== 500,
    });

    if (ok) {
      try {
        const body = JSON.parse(res.body);
        newAgencyId = body.id ?? body.agencyId;
        successAgencies.add(1);
      } catch {}
    }
  });

  sleep(0.4);

  // PASO 6: Ver todos los favoritos
  group('6. Listar favoritos del sistema', () => {
    const res = http.get(`${BASE_URL}/admin/favorites`, { headers: authHeaders });

    check(res, {
      '[Favoritos Admin] status 200': (r) => r.status === 200,
      '[Favoritos Admin] es array':   (r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
    });
  });

  sleep(0.3);

  // PASO 7: Ver todas las compras
  group('7. Listar compras del sistema', () => {
    const res = http.get(`${BASE_URL}/admin/purchases`, { headers: authHeaders });

    check(res, {
      '[Compras Admin] status 200': (r) => r.status === 200,
      '[Compras Admin] es array':   (r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
    });
  });

  sleep(0.3);

  // PASO 8: Reporte Top Buyers
  group('8. Reporte top-buyers', () => {
    const t0  = Date.now();
    const res = http.get(`${BASE_URL}/admin/reports/top-buyers`, { headers: authHeaders });
    reportDuration.add(Date.now() - t0);

    check(res, {
      '[Top Buyers] status 200':     (r) => r.status === 200,
      '[Top Buyers] es array':       (r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
      '[Top Buyers] máx 5 items':    (r) => { try { return JSON.parse(r.body).length <= 5; } catch { return false; } },
    });
  });

  sleep(0.3);

  // PASO 9: Reporte Top Propiedades
  group('9. Reporte top-ranked-properties', () => {
    const t0  = Date.now();
    const res = http.get(`${BASE_URL}/admin/reports/top-ranked-properties`, { headers: authHeaders });
    reportDuration.add(Date.now() - t0);

    check(res, {
      '[Top Props] status 200':   (r) => r.status === 200,
      '[Top Props] es array':     (r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
      '[Top Props] máx 5 items':  (r) => { try { return JSON.parse(r.body).length <= 5; } catch { return false; } },
    });
  });

  sleep(0.3);

  // PASO 10: Reporte Top Agencias
  group('10. Reporte top-agencies-sales', () => {
    const t0  = Date.now();
    const res = http.get(`${BASE_URL}/admin/reports/top-agencies-sales`, { headers: authHeaders });
    reportDuration.add(Date.now() - t0);

    check(res, {
      '[Top Agencies] status 200':  (r) => r.status === 200,
      '[Top Agencies] es array':    (r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
      '[Top Agencies] máx 5 items': (r) => { try { return JSON.parse(r.body).length <= 5; } catch { return false; } },
    });
  });

  sleep(0.3);

  // PASO 11: Eliminar agencia
  if (newAgencyId) {
    group('11. Eliminar agencia (limpieza)', () => {
      const res = http.del(`${BASE_URL}/agencies/${newAgencyId}`, null, { headers: authHeaders });

      check(res, {
        '[Eliminar Agencia] 204 o 404': (r) => r.status === 204 || r.status === 404,
        '[Eliminar Agencia] no es 500': (r) => r.status !== 500,
      });
    });
    sleep(0.2);
  }

  // PASO 12: Eliminar usuario
  if (newUserId) {
    group('12. Eliminar usuario (limpieza)', () => {
      const res = http.del(`${BASE_URL}/users/${newUserId}`, null, { headers: authHeaders });

      check(res, {
        '[Eliminar Usuario] 204 o 404': (r) => r.status === 204 || r.status === 404,
        '[Eliminar Usuario] no es 500': (r) => r.status !== 500,
      });
    });
    sleep(0.2);
  }

  errorRate.add(!flowOk);
  sleep(1);
}

