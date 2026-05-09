import { useCallback, useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import { api } from '../../services/api';
import { formatSalario } from '../../utils/formatters';
import { Sesion, MensajeGlobal, Puesto } from '../../types';

interface Props {
  sesion: Sesion | null;
  onNavegar: (ruta: string) => void;
  onMensaje: (m: MensajeGlobal) => void;
}

function EmpresaPuestosPage({ sesion, onNavegar, onMensaje }: Props) {
  const [puestos, setPuestos] = useState<Puesto[]>([]);
  const [cargando, setCargando] = useState(true);

  const cargar = useCallback(() => {
    setCargando(true);
    api.getPuestosEmpresa()
      .then(setPuestos)
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [onMensaje]);

  useEffect(() => {
    if (sesion?.rol !== 'EMPRESA') return;
    let cancelled = false;

    (async () => {
      await Promise.resolve();
      if (cancelled) return;
      cargar();
    })();

    return () => { cancelled = true; };
  }, [sesion, cargar]);

  if (!sesion || sesion.rol !== 'EMPRESA') {
    return (
      <section className="container py-5">
        <div className="alert alert-warning">Acceso restringido a empresas autorizadas.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  const desactivar = async (id: number) => {
    if (!window.confirm('¿Desactivar este puesto?')) return;
    try {
      await api.desactivarPuesto(id);
      onMensaje({ tipo: 'success', texto: 'Puesto desactivado.' });
      cargar();
    } catch (e) {
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    }
  };

  const activar = async (id: number) => {
    try {
      await api.activarPuesto(id);
      onMensaje({ tipo: 'success', texto: 'Puesto activado.' });
      cargar();
    } catch (e) {
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    }
  };

  return (
    <section className="container py-5">
      <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <SectionTitle eyebrow="Empresa" title="Mis puestos" />
        <button className="btn btn-primary" onClick={() => onNavegar('/empresa/publicar')}>+ Publicar puesto</button>
      </div>

      {cargando ? <LoadingBlock /> : (
        <>
          {puestos.length === 0 ? (
            <p className="text-secondary">No has publicado puestos aún.</p>
          ) : (
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
                      {p.activo
                        ? <span className="badge bg-success">Sí</span>
                        : <span className="badge bg-secondary">No</span>}
                    </td>
                    <td className="d-flex gap-1 flex-wrap">
                      {p.activo ? (
                        <button className="btn btn-outline-danger btn-sm" onClick={() => desactivar(p.id)}>
                          Desactivar
                        </button>
                      ) : (
                        <button className="btn btn-outline-success btn-sm" onClick={() => activar(p.id)}>
                          Activar
                        </button>
                      )}
                      {p.activo ? (
                        <button className="btn btn-dark btn-sm"
                          onClick={() => onNavegar(`/empresa/puestos/${p.id}/candidatos`)}>
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
          )}
        </>
      )}
    </section>
  );
}

export default EmpresaPuestosPage;
