// Página de inicio pública del sistema. Muestra los últimos puestos publicados.

import { useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, Puesto, PuestosResponse } from '../../types';
import { formatSalario, formatFecha } from '../../utils/formatters';

interface Props {
  // Sesión actual del usuario (null si no está autenticado)
  sesion: Sesion | null;
  // Función de navegación para redirigir al usuario a otras páginas
  onNavegar: (ruta: string) => void;
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
}

function HomePage({ sesion, onNavegar, onMensaje }: Props) {
  // Estado para almacenar los últimos puestos publicados
  const [puestos, setPuestos] = useState<Puesto[]>([]);
  // Estado para indicar si se están cargando los datos desde el backend
  const [cargando, setCargando] = useState(true);
  // Indica si el usuario tiene una sesión activa
  const logueado = Boolean(sesion);

  // Effect que carga los últimos puestos públicos al montar el componente
  useEffect(() => {
    api.getUltimosPuestosPublicos()
      .then((res: PuestosResponse) => {
        setPuestos((res.puestos ?? []).map((p: Puesto) => ({ ...p, tipoCambio: res.tipoCambio })));
      })
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [onMensaje]);

  return (
    <>
      <section className="container my-4">
        <SectionTitle
          eyebrow="Últimas ofertas"
          title="Bolsa de Empleo"
          description="Últimos 5 puestos públicos disponibles."
        />
        {cargando ? <LoadingBlock /> : (
          <div className="row g-3 align-items-start">
            {puestos.length === 0 ? (
              <div className="col-12"><p className="text-secondary">No hay puestos públicos registrados aún.</p></div>
            ) : puestos.map((p, i) => (
              <div key={p.id} className="col-md-4">
                <div className="card h-100">
                  <div className="card-body d-flex flex-column">
                    <h6 className="fw-bold mb-0">{p.empresa?.nombre}</h6>
                    <p className="text-muted mb-2">{p.descripcion}</p>
                    <p><strong>Salario:</strong> {formatSalario(p.salario, p.tipoCambio)}</p>
                    {!p.tipoCambio && <p className="text-muted small">Tipo de cambio no disponible</p>}
                    <div className="mt-auto">
                      <a className="btn btn-outline-dark w-100" data-bs-toggle="collapse"
                         href={`#detalle-${i}`} role="button">
                        Ver detalle
                      </a>
                      <div className="collapse mt-2 border rounded p-2 bg-white" id={`detalle-${i}`}>
                        <p className="fw-bold mb-1">{p.descripcion}</p>
                        {p.caracteristicas && p.caracteristicas.length > 0 ? (
                          <ul className="mb-0">
                            {p.caracteristicas.map((c) => (
                              <li key={c.id}>{c.nombre} (nivel {c.nivelRequerido})</li>
                            ))}
                          </ul>
                        ) : (
                          <p className="text-muted small mb-0">Sin características requeridas</p>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </>
  );
}

export default HomePage;
