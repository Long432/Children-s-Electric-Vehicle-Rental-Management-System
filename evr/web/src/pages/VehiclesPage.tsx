import { useState } from 'react';
import { Badge, Card, Empty, Loading, Notice } from '../components/ui';
import { api } from '../lib/api';
import { dateTime, fullDate } from '../lib/format';
import { useAsync } from '../lib/hooks';
import type { Filters } from '../lib/types';

const INCIDENT_LIMIT = 100;

export function VehiclesPage({ filters }: { filters: Filters }) {
  const [vehicleId, setVehicleId] = useState<number | null>(null);
  const deps = [filters.from, filters.to, filters.userId] as const;
  const vehicles = useAsync(() => api.vehicles(filters), deps);
  const incidents = useAsync(() => api.incidents(filters, vehicleId, INCIDENT_LIMIT), [...deps, vehicleId]);

  return (
    <>
      <h1 className="page-title">Xe hỏng {fullDate(filters.from)} - {fullDate(filters.to)}</h1>
      <Card title="Theo từng xe">
        {vehicles.error && <Notice kind="error">{vehicles.error}</Notice>}
        {vehicles.loading && !vehicles.data && <Loading />}
        {vehicles.data && vehicles.data.length === 0 && <Empty>Không có xe hỏng trong khoảng thời gian này.</Empty>}
        {vehicles.data && vehicles.data.length > 0 && (
          <div className="table-wrap">
            <table className="clickable">
              <thead>
                <tr><th>Xe</th><th>Trạng thái hiện tại</th><th className="num">Số lần hỏng</th><th>Lần gần nhất</th></tr>
              </thead>
              <tbody>
                {vehicles.data.map((row) => (
                  <tr key={row.vehicle_id} className={row.vehicle_id === vehicleId ? 'selected' : undefined}
                    onClick={() => setVehicleId(row.vehicle_id === vehicleId ? null : row.vehicle_id)}>
                    <td><strong>{row.slug}</strong></td>
                    <td><Badge value={row.status} /></td>
                    <td className="num">{row.incident_count}</td>
                    <td>{dateTime(row.last_incident_at)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {vehicleId !== null && <p className="muted small">Đang lọc theo một xe. Bấm lại dòng đó để bỏ lọc.</p>}
      </Card>
      <Card title="Chi tiết từng lần hỏng">
        {incidents.error && <Notice kind="error">{incidents.error}</Notice>}
        {incidents.loading && !incidents.data && <Loading />}
        {incidents.data && incidents.data.length === 0 && <Empty>Không có dữ liệu.</Empty>}
        {incidents.data && incidents.data.length > 0 && (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Thời gian</th><th>Xe</th><th>Phiên</th><th>Vé</th><th>Người báo</th><th>Cách ghi nhận</th><th>Phụ trách phiên</th></tr>
              </thead>
              <tbody>
                {incidents.data.map((item) => (
                  <tr key={item.id}>
                    <td>{dateTime(item.created_at)}</td>
                    <td>{item.vehicle_slug}</td>
                    <td>{item.session_id ? <a className="link" href={`#/sessions/${item.session_id}`}>#{item.session_id}</a> : <span className="muted">Ngoài phiên</span>}</td>
                    <td>{item.ticket_no !== null ? `#${item.ticket_no}` : '-'}</td>
                    <td>{item.reported_by.username}</td>
                    <td><Badge value={item.source} /></td>
                    <td>{item.session_in_charge?.username ?? '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {incidents.data && incidents.data.length >= INCIDENT_LIMIT && <p className="muted small">Chỉ hiển thị {INCIDENT_LIMIT} lần gần nhất.</p>}
      </Card>
    </>
  );
}
