// Página pública que lista todas las ofertas laborales disponibles.
// Incluye un campo de búsqueda para filtrar por descripción o nombre de empresa.

import { useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import { api } from '../../services/api';
import { formatSalario, formatFecha } from '../../utils/formatters';
import { MensajeGlobal, Puesto, PuestosResponse } from '../../types';

interface Props {
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
}

function PuestosPublicosPage({ onMensaje }: Props) {
  // Estado para almacenar todos los puestos públicos obtenidos del backend
  const [puestos, setPuestos] = useState<Puesto[]>([]);
  // Estado para el texto del filtro de búsqueda local
  const [filtro, setFiltro] = useState('');
  // Estado para indicar si se están cargando los puestos
  const [cargando, setCargando] = useState(true);

  // Effect que carga todos los puestos públicos al montar el componente
  useEffect(() => {
    api.getPuestosPublicos()
      .then((res: PuestosResponse) => {
        setPuestos(res.puestos ?? []);
      })
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [onMensaje]);

  // Filtra los puestos localmente por descripción o nombre de empresa
  const filtrados = puestos.filter((p) =>
    p.descripcion?.toLowerCase().includes(filtro.toLowerCase()) ||
    p.empresa?.nombre?.toLowerCase().includes(filtro.toLowerCase())
  );

  return (
    // Render del listado de puestos públicos con filtro de búsqueda
    <section className="container py-5">
      <SectionTitle
        eyebrow="Puestos disponibles"
        title="Ofertas laborales"
        description="Explorá todas las oportunidades laborales publicadas por empresas registradas."
      />

      {/* Campo de búsqueda para filtrar puestos por descripción o empresa */}
      <div className="mb-4">
        <input
          className="form-control"
          placeholder="Buscar por descripción o empresa..."
          value={filtro}
          onChange={(e) => setFiltro(e.target.value)}
        />
      </div>

      {cargando ? <LoadingBlock /> : (
        <>
          {filtrados.length === 0 ? (
            <p className="text-secondary">No se encontraron puestos.</p>
          ) : (
            <div className="row g-3">
              {filtrados.map((p) => (
                <div key={p.id} className="col-md-6 col-lg-4">
                  <div className="card shadow-sm border-0 h-100">
                    <div className="card-body">
                      <div className="d-flex justify-content-between align-items-start mb-2">
                        <span className="badge bg-primary-subtle text-primary text-capitalize">{p.tipoPublicacion}</span>
                        <small className="text-secondary">{formatFecha(p.fechaRegistro)}</small>
                      </div>
                      <p className="mb-2" style={{ fontSize: '0.9rem' }}>{p.descripcion}</p>
                      <div className="fw-semibold text-primary">{formatSalario(p.salario)}</div>
                      {p.empresa && <div className="text-secondary small mt-1">🏢 {p.empresa.nombre}</div>}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
          <div className="mt-3 text-secondary small">{filtrados.length} puesto(s) encontrado(s)</div>
        </>
      )}
    </section>
  );
}

export default PuestosPublicosPage;
