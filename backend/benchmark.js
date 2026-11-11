import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const BASE = __ENV.BASE_URL || 'http://localhost:8080/api/v1';
const EVENT_ID = __ENV.EVENT_ID;
const TICKET_TYPE = __ENV.TICKET_TYPE || 'VIP';

const sold = new Counter('tickets_reserved');
const rejected = new Counter('tickets_rejected');
const unexpected = new Counter('unexpected_status');

export const options = {
  scenarios: {
    high_traffic_spike: {
      executor: 'shared-iterations',
      vus: 500,
      iterations: 500,
      maxDuration: '30s',
    },
  },
  thresholds: {
    unexpected_status: ['count==0'],
  },
};

export function setup() {
  const res = http.post(`${BASE}/auth/login`,
    JSON.stringify({ email: 'admin@ticketmania.com', password: 'admin123' }),
    { headers: { 'Content-Type': 'application/json' } });

  if (res.status !== 200) {
    throw new Error(`Login falhou: ${res.status}`);
  }
  return { token: res.json('token') };
}

export default function (data) {
  const res = http.post(`${BASE}/orders`,
    JSON.stringify({ eventId: EVENT_ID, ticketType: TICKET_TYPE, quantity: 1 }),
    { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${data.token}` } });

  const statusCount = new Counter('status_count');

  statusCount.add(1, { status: String(res.status) });
  if (res.status === 201) sold.add(1);
  else if (res.status === 409) rejected.add(1);
  else {
    unexpected.add(1);
    if (__ITER < 5) console.log(`status=${res.status} error=${res.error} body=${res.body}`);
  }

  check(res, { 'status is 201 or 409': (r) => r.status === 201 || r.status === 409 });
}