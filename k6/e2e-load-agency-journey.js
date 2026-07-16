/**
 * E2E LOAD TEST — Flujo completo de la Agencia
 *
 *   1. Login como agency
 *   2. Ver mis publicaciones actuales
 *   3. Crear una nueva propiedad
 *   4. Publicar la propiedad (agency-property)
 *   5. Verificar que aparece en mis publicaciones
 *   6. Actualizar el precio de la publicación
 *   7. Verificar el precio actualizado
 *   8. Ver ventas de mi agencia
 *   9. Buscar propiedades disponibles
 *  10. Eliminar la publicación creada
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

const errorRate         = new Rate('e2e_agency_error_rate');
const loginDuration     = new Trend('e2e_agency_login_duration',   true);
const publishDuration   = new Trend('e2e_agency_publish_duration', true);
const salesDuration     = new Trend('e2e_agency_sales_duration',   true);
const successPublished  = new Counter('e2e_agency_published_ok');
const successPriceUpd   = new Counter('e2e_agency_price_updated');

export const options = {
  stages: [
    { duration: '15s', target: 3  },   // Warm-up (pocas agencias)
    { duration: '40s', target: 8  },   // Carga media
    { duration: '40s', target: 10 },   // Carga sostenida (10 agencias simultáneas)
    { duration: '15s', target: 0  },   // Ramp-down
  ],
  thresholds: {
    e2e_agency_error_rate:       ['rate<0.05'],
    e2e_agency_login_duration:   ['p(95)<600'],
    e2e_agency_publish_duration: ['p(95)<1000'],
    e2e_agency_sales_duration:   ['p(95)<800'],
    http_req_failed:             ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const AGENCIES = [
  'inmo1', 'inmo2', 'inmo3',
  'ritondo_propiedades', 'nordelta_propiedades',
  'puerto_madero_brokers', 'sur_inversiones',
  'agro_bienes_raices', 'patagonia_bienes',
];

const CITIES = [
  { city: 'Buenos Aires', province: 'Buenos Aires' },
  { city: 'Córdoba',      province: 'Córdoba'      },
  { city: 'Rosario',      province: 'Santa Fe'     },
  { city: 'La Plata',     province: 'Buenos Aires' },
  { city: 'Mendoza',      province: 'Mendoza'      },
];

const PROPERTY_TYPES = ['HOUSE', 'APARTMENT'];

// Setup: pre-login de todas las agencias
export function setup() {
  const tokens = {};
  AGENCIES.forEach((username) => {
    const res = http.post(
      `${BASE_URL}/auth/login`,
      JSON.stringify({ username, password: 'agency123' }),
      { headers: { 'Content-Type': 'application/json' } }
    );
    if (res.status === 200) {
      tokens[username] = JSON.parse(res.body).token;
    } else {
      console.warn(`[setup] Login falló para ${username}: ${res.status}`);
    }
  });

  const availableAgencies = Object.keys(tokens);
  if (availableAgencies.length === 0) {
    throw new Error('No se pudo autenticar ninguna agencia en setup');
  }
  console.log(`[setup] ${availableAgencies.length} agencias autenticadas: ${availableAgencies.join(', ')}`);
  return { tokens };
}

// Función principal: flujo E2E completo por VU
export default function (data) {
  const agencies = Object.keys(data.tokens);
  const agencyName = agencies[(__VU - 1) % agencies.length];
  let token = data.tokens[agencyName];
  let authHeaders = {
    'Authorization': `Bearer ${token}`,
    'Content-Type':  'application/json',
  };

  const location = CITIES[(__VU - 1) % CITIES.length];
  const propType = PROPERTY_TYPES[__ITER % PROPERTY_TYPES.length];

  let flowOk       = true;
  let propertyId   = null;
  let agencyPropId = null;

  // PASO 1: Login como agency
  group('1. Login agencia', () => {
    const t0  = Date.now();
    const res = http.post(
      `${BASE_URL}/auth/login`,
      JSON.stringify({ username: agencyName, password: 'agency123' }),
      { headers: { 'Content-Type': 'application/json' } }
    );
    loginDuration.add(Date.now() - t0);

    const ok = check(res, {
      '[Login Agencia] status 200':     (r) => r.status === 200,
      '[Login Agencia] token presente': (r) => { try { return !!JSON.parse(r.body).token; } catch { return false; } },
    });
    if (ok) {
      token = JSON.parse(res.body).token;
      authHeaders['Authorization'] = `Bearer ${token}`;
    }
    flowOk = flowOk && ok;
  });

  if (!flowOk) { errorRate.add(1); return; }
  sleep(0.3);

  // PASO 2: Ver mis publicaciones actuales
  group('2. Ver mis publicaciones', () => {
    const res = http.get(`${BASE_URL}/agency-properties/agency/me`, { headers: authHeaders });

    check(res, {
      '[Mis Publicaciones] status 200': (r) => r.status === 200,
      '[Mis Publicaciones] es array':   (r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
    });
  });

  sleep(0.3);

  // PASO 3: Crear nueva propiedad
  group('3. Crear propiedad', () => {
    const payload = {
      propertyType: propType,
      price:        200000 + (__VU * 10000) + (Math.floor(Math.random() * 50000)),
      address:      `Calle de Carga ${__VU}-${__ITER} N°${Math.floor(Math.random() * 999) + 1}`,
      city:         location.city,
      province:     location.province,
      areaSq:       60 + (__VU * 5),
      rooms:        ((__VU % 3) + 1),
      description:  `Propiedad de test E2E - VU ${__VU} iteración ${__ITER}`,
    };

    const res = http.post(`${BASE_URL}/properties`, JSON.stringify(payload), { headers: authHeaders });

    const ok = check(res, {
      '[Crear Prop] status 201':    (r) => r.status === 201,
      '[Crear Prop] tiene id':      (r) => { try { return JSON.parse(r.body).id !== undefined || JSON.parse(r.body).propertyId !== undefined; } catch { return false; } },
      '[Crear Prop] no es 500':     (r) => r.status !== 500,
    });

    if (ok) {
      try {
        const body = JSON.parse(res.body);
        propertyId = body.id ?? body.propertyId;
      } catch {}
    }
    flowOk = flowOk && ok;
  });

  if (!propertyId) {
    errorRate.add(1);
    console.error(`[VU ${__VU}] No se obtuvo propertyId tras crear propiedad`);
    return;
  }
  sleep(0.3);

  // PASO 4: Publicar la propiedad
  group('4. Publicar propiedad', () => {
    const listedPrice = 190000 + (__VU * 9000);
    const t0  = Date.now();
    const res = http.post(
      `${BASE_URL}/agency-properties`,
      JSON.stringify({ propertyId, listedPrice }),
      { headers: authHeaders }
    );
    publishDuration.add(Date.now() - t0);

    const ok = check(res, {
      '[Publicar] status 201':                    (r) => r.status === 201,
      '[Publicar] tiene agencyPropertyId':        (r) => { try { return JSON.parse(r.body).id !== undefined || JSON.parse(r.body).agencyPropertyId !== undefined; } catch { return false; } },
      '[Publicar] 201 o 409 (ya publicada)':      (r) => r.status === 201 || r.status === 409,
      '[Publicar] no es 500':                     (r) => r.status !== 500,
    });

    if (res.status === 201) {
      successPublished.add(1);
      try {
        const body = JSON.parse(res.body);
        agencyPropId = body.id ?? body.agencyPropertyId;
      } catch {}
    }
    flowOk = flowOk && (res.status === 201 || res.status === 409);
  });

  sleep(0.4);

  // PASO 5: Verificar que aparece en mis publicaciones
  group('5. Verificar publicaciones post-creación', () => {
    const res = http.get(`${BASE_URL}/agency-properties/agency/me`, { headers: authHeaders });

    check(res, {
      '[Verificar Pub] status 200':      (r) => r.status === 200,
      '[Verificar Pub] al menos 1 item': (r) => { try { return JSON.parse(r.body).length > 0; } catch { return false; } },
    });
  });

  sleep(0.3);

  // PASO 6: Actualizar precio de la publicación
  if (agencyPropId) {
    group('6. Actualizar precio publicación', () => {
      const newPrice = 175000 + (__VU * 8000);
      const res = http.put(
        `${BASE_URL}/agency-properties/${agencyPropId}`,
        JSON.stringify({ listedPrice: newPrice }),
        { headers: authHeaders }
      );

      const ok = check(res, {
        '[Update Precio] status 200':      (r) => r.status === 200,
        '[Update Precio] no es 500':       (r) => r.status !== 500,
      });
      if (ok) successPriceUpd.add(1);
    });
    sleep(0.3);

    // PASO 7: Verificar precio actualizado
    group('7. Verificar precio actualizado', () => {
      const res = http.get(`${BASE_URL}/agency-properties/${agencyPropId}`, { headers: authHeaders });

      check(res, {
        '[Precio actualizado] 200':          (r) => r.status === 200,
        '[Precio actualizado] precio nuevo': (r) => {
          try {
            const body = JSON.parse(r.body);
            const expectedPrice = 175000 + (__VU * 8000);
            return body.listedPrice === expectedPrice;
          } catch { return false; }
        },
      });
    });
    sleep(0.3);
  }

  // PASO 8: Ver ventas de mi agencia
  group('8. Ver ventas de mi agencia', () => {
    const t0  = Date.now();
    const res = http.get(`${BASE_URL}/purchases/agency/me`, { headers: authHeaders });
    salesDuration.add(Date.now() - t0);

    check(res, {
      '[Ventas Agencia] status 200': (r) => r.status === 200,
      '[Ventas Agencia] es array':   (r) => { try { return Array.isArray(JSON.parse(r.body)); } catch { return false; } },
    });
  });

  sleep(0.3);

  // PASO 9: Buscar propiedades disponibles
  group('9. Buscar propiedades disponibles', () => {
    const res = http.get(
      `${BASE_URL}/properties/search?city=${encodeURIComponent(location.city)}&propertyType=${propType}`,
      { headers: authHeaders }
    );

    check(res, {
      '[Buscar Disponibles] status 200':   (r) => r.status === 200,
      '[Buscar Disponibles] tiene content': (r) => { try { return Array.isArray(JSON.parse(r.body).content); } catch { return false; } },
    });
  });

  sleep(0.3);

  // PASO 10: Eliminar la publicación
  if (agencyPropId) {
    group('10. Eliminar publicación (limpieza)', () => {
      const res = http.del(
        `${BASE_URL}/agency-properties/${agencyPropId}`,
        null,
        { headers: authHeaders }
      );

      check(res, {
        '[Eliminar Pub] 204 o 400 (ya vendida)': (r) => r.status === 204 || r.status === 400,
        '[Eliminar Pub] no es 500':              (r) => r.status !== 500,
      });
    });
    sleep(0.2);
  }

  errorRate.add(!flowOk);
  sleep(1);
}

