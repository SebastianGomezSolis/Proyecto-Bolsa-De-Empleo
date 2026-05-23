import { useCallback, useEffect, useState } from 'react';
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

interface Props {
  onMensaje: (m: MensajeGlobal) => void;
}

function EmpresaPuestosPage({ onMensaje }: Props) {
  const navigate = useNavigate();
  const [puestos, setPuestos] = useState<Puesto[]>([]);
  const [cargando, setCargando] = useState(true);

  const cargar = useCallback(() => {
    setCargando(true);
    fetch("http://localhost:8080/api/empresa/puestos", { headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) })
      .then(async (res) => { if (res.ok) setPuestos(await res.json()); else throw new Error(await res.text()); })
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [onMensaje]);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      await Promise.resolve();
      if (cancelled) return;
      cargar();
    })();
    return () => { cancelled = true; };
  }, [cargar]);

  const desactivar = async (id: number) => {
    if (!window.confirm('¿Desactivar este puesto?')) return;
    try {
      const res = await fetch(`http://localhost:8080/api/empresa/puestos/${id}/desactivar`, { method: 'POST', headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) });
      if (!res.ok) throw new Error(await res.text());
      onMensaje({ tipo: 'success', texto: 'Puesto desactivado.' });
      cargar(); // Recargar la lista para reflejar el cambio
    } catch (e) {
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    }
  };

  // Función para activar un puesto desactivado
  const activar = async (id: number) => {
    try {
      const res = await fetch(`http://localhost:8080/api/empresa/puestos/${id}/activar`, { method: 'POST', headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) });
      if (!res.ok) throw new Error(await res.text());
      onMensaje({ tipo: 'success', texto: 'Puesto activado.' });
      cargar(); // Recargar la lista para reflejar el cambio
    } catch (e) {
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    }
  };

  return (
    <section className="container py-5">
      <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <SectionTitle eyebrow="Empresa" title="Mis puestos" />
        {/* Botón para navegar a la página de publicación de nuevo puesto */}
        <button className="btn btn-primary" onClick={() => navigate('/empresa/publicar')}>+ Publicar puesto</button>
      </div>

      {cargando ? <LoadingBlock /> : (
        <>
          {puestos.length === 0 ? (
            <p className="text-secondary">No has publicado puestos aún.</p>
          ) : (
            <>
            {/* Tabla con la lista de puestos publicados */}
            <table className="table table-hover">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Descripción</th>
                  <th>Salario</th>
                  <th>Activo</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {puestos.map((p) => (
                  <tr key={p.id}>
                    <td>{p.id}</td>
                    <td>{p.descripcion}</td>
                    <td>{formatSalario(p.salario)}</td>
                    <td>
                      {/* Indicador visual del estado activo/inactivo del puesto */}
                      {p.activo
                        ? <span className="badge bg-success">Sí</span>
                        : <span className="badge bg-secondary">No</span>}
                    </td>
                    <td className="d-flex gap-1 flex-wrap">
                      {/* Botón para activar o desactivar el puesto según su estado actual */}
                      {p.activo ? (
                        <button className="btn btn-outline-danger btn-sm" onClick={() => desactivar(p.id)}>
                          Desactivar
                        </button>
                      ) : (
                        <button className="btn btn-outline-success btn-sm" onClick={() => activar(p.id)}>
                          Activar
                        </button>
                      )}
                      {/* Botón para buscar candidatos (solo disponible si el puesto está activo) */}
                      {p.activo ? (
                        <button className="btn btn-dark btn-sm"
                          onClick={() => navigate(`/empresa/puestos/${p.id}/candidatos`)}>
                          Buscar candidatos
                        </button>
                      ) : (
                        <button className="btn btn-secondary btn-sm" disabled>Buscar candidatos</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            </>
          )}
        </>
      )}
    </section>
  );
}

export default EmpresaPuestosPage;
