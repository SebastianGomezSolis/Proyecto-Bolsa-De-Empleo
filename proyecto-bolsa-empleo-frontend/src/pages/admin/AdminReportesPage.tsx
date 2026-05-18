// Página de administración para generar reportes de puestos publicados.
// Permite seleccionar mes y año para descargar un PDF con los puestos del período.

import { useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import { API_BASE, obtenerToken } from '../../services/api';
import { Sesion } from '../../types';

const MESES = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

interface Props {
  // Sesión actual del usuario (se usa para verificar rol de administrador)
  sesion: Sesion | null;
  // Función de navegación para redirigir al usuario a otras páginas
  onNavegar: (ruta: string) => void;
}

function AdminReportesPage({ sesion, onNavegar }: Props) {
  // Estado para el mes seleccionado en el formulario (valor por defecto: mes actual)
  const [mes, setMes] = useState(new Date().getMonth() + 1);
  // Estado para el año seleccionado en el formulario (valor por defecto: año actual)
  const [anio, setAnio] = useState(new Date().getFullYear());

  // Redirección para usuarios no autenticados o que no son administradores
  if (!sesion || sesion.rol !== 'ADMIN') {
    return (
      <section className="container py-5">
        <div className="alert alert-danger">Acceso restringido a administradores.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  // Manejador del formulario: abre el PDF del reporte en una nueva pestaña
  const verPDF = (e: React.FormEvent) => {
    e.preventDefault(); // Prevenir envío tradicional del formulario
    const token = obtenerToken();
    if (!token) return;
    window.open(`${API_BASE}/api/admin/reportes/pdf?mes=${mes}&anio=${anio}&token=${token}`, '_blank');
  };

  return (
    <section className="container py-5">
      <SectionTitle eyebrow="Administración" title="Reportes" description="Generá reportes de puestos publicados por mes y año en formato PDF." />

      {/* Formulario para seleccionar mes y año del reporte */}
      <div className="border rounded p-4 bg-white" style={{ maxWidth: '450px' }}>
        <h6 className="fw-bold mb-3">Reporte de puestos por mes</h6>
        <p className="text-muted mb-3" style={{ fontSize: '0.88rem' }}>
          Seleccioná el mes y el año para descargar el reporte en PDF con todos los puestos publicados en ese período.
        </p>
        <form onSubmit={verPDF}>
          <div className="row g-3 mb-3">
            <div className="col">
              <label className="form-label fw-semibold">Mes</label>
              <select className="form-select" value={mes} onChange={(e) => setMes(Number(e.target.value))} required>
                {MESES.map((m, i) => <option key={i + 1} value={i + 1}>{m}</option>)}
              </select>
            </div>
            <div className="col">
              <label className="form-label fw-semibold">Año</label>
              <input type="number" className="form-control" value={anio} min={2000} max={2099}
                onChange={(e) => setAnio(Number(e.target.value))} required />
            </div>
          </div>
          <button type="submit" className="btn btn-dark">Ver PDF</button>
        </form>
      </div>
    </section>
  );
}

export default AdminReportesPage;
