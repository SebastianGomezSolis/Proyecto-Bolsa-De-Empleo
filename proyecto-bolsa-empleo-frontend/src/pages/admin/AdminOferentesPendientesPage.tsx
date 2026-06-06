import { useCallback, useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';

interface MensajeGlobal {
  tipo: 'success' | 'error' | 'info' | 'warning' | 'danger';
  texto: string;
}

interface OferentePendiente {
  id: number;
  identificacion: string;
  nombre: string;
  primerApellido: string;
  telefono: string;
  lugarResidencia: string;
  usuario: { id: number; correo: string };
}

interface Props {
  onMensaje: (m: MensajeGlobal) => void;
}

function AdminOferentesPendientesPage({ onMensaje }: Props) {
  const [oferentes, setOferentes] = useState<OferentePendiente[]>([]);
  const [cargando, setCargando] = useState(true);

  const cargar = useCallback(() => {
    setCargando(true);
    fetch("http://localhost:8080/api/admin/oferentes/pendientes", { headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) })
      .then(async (res) => { if (res.ok) setOferentes(await res.json()); else throw new Error("No se pudieron cargar los oferentes pendientes"); })
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [onMensaje]);

  useEffect(() => {
    const timer = setTimeout(() => cargar(), 0);
    return () => clearTimeout(timer);
  }, [cargar]);

  const autorizar = async (id: number) => {
    try {
      const res = await fetch(`http://localhost:8080/api/admin/oferentes/${id}/autorizar`, { method: "POST", headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) });
      if (!res.ok) {
        throw new Error("No se pudo autorizar el oferente");
      }
      onMensaje({ tipo: 'success', texto: 'Oferente autorizado.' });
      cargar();
    } catch (e) { 
      onMensaje({ tipo: 'danger', texto: (e as Error).message }); 
    }
  };

  return (
    <section className="container py-5">
      <SectionTitle eyebrow="Administración" title="Oferentes pendientes" />

      {cargando ? <LoadingBlock /> : (
        <table className="table table-hover">
          <thead>
            <tr><th>Nombre</th><th>Correo</th><th>Acción</th></tr>
          </thead>
          <tbody>
            {oferentes.length === 0 ? (
              <tr><td colSpan={3} className="text-muted text-center">No hay oferentes pendientes de aprobación.</td></tr>
            ) : 
              oferentes.map((o) => (
                <tr key={o.id}>
                  <td className="align-middle">{o.nombre} {o.primerApellido}</td>
                  <td className="align-middle">{o.usuario?.correo}</td>
                  <td>
                    <button className="btn btn-success btn-sm" onClick={() => autorizar(o.id)}>Aprobar</button>
                  </td>
                </tr>
              ))}
          </tbody>
        </table>
      )}
    </section>
  );
}

export default AdminOferentesPendientesPage;