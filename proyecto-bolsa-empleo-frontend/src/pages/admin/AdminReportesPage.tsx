import { useState } from 'react';
import SectionTitle from '../../components/SectionTitle';

const MESES = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

interface MensajeGlobal {
  tipo: 'success' | 'error' | 'info' | 'warning' | 'danger';
  texto: string;
}

interface Props {
  onMensaje: (m: MensajeGlobal) => void;
}

function AdminReportesPage({ onMensaje }: Props) {
  const [mes, setMes] = useState(new Date().getMonth() + 1);
  const [anio, setAnio] = useState(new Date().getFullYear());

  const verPDF = (e: React.FormEvent) => {
    e.preventDefault();
    (async () => {
      const res = await fetch(`http://localhost:8080/api/admin/reportes/pdf?mes=${mes}&anio=${anio}`, { headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) });
      if (!res.ok) { onMensaje({ tipo: 'danger', texto: await res.text() }); return; }
      const blob = await res.blob();
      window.open(URL.createObjectURL(blob), '_blank');
    })();
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
