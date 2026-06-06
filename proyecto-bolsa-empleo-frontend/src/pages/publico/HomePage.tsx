import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import { formatSalario } from '../../utils/formatters';

interface MensajeGlobal {
  tipo: 'success' | 'error' | 'info' | 'warning' | 'danger';
  texto: string;
}

interface Puesto {
  id: number;
  descripcion: string;
  salario: number;
  tipoPublicacion: string;
  empresa: { id: number; nombre: string; usuarioCorreo: string };
  activo: boolean;
  fechaRegistro: string;
  caracteristicas: { id: number; nombre: string; nivelRequerido: number }[];
  tipoCambio?: { compra: number; venta: number; fecha: string };
}

interface PuestosResponse {
  puestos: Puesto[];
  tipoCambio?: { compra: number; venta: number; fecha: string };
}

interface Props {
  onMensaje: (m: MensajeGlobal) => void;
}

function HomePage({ onMensaje }: Props) {
  const navigate = useNavigate();
  const [puestos, setPuestos] = useState<Puesto[]>([]);
  const [cargando, setCargando] = useState(true);

  // Effect que carga los últimos puestos públicos al montar el componente
  useEffect(() => {
    fetch("http://localhost:8080/api/publico/puestos/ultimos", { headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) })
        .then(async (res) => {
        if (res.ok) return res.json();
        // Intentar obtener mensaje de error del cuerpo de la respuesta
        let mensaje = "No se pudieron cargar las últimas ofertas";
        try {
          const data = await res.json();
          if (data && typeof data.message === 'string') {
            mensaje = data.message;
          }
        } catch (e) {
          // Mantener mensaje genérico si el parseo falla
        }
        throw new Error(mensaje);
      })
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
