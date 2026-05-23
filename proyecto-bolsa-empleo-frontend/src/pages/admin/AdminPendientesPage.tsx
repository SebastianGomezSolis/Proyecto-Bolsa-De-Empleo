import { useCallback, useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';

interface MensajeGlobal {
  tipo: 'success' | 'error' | 'info' | 'warning' | 'danger';
  texto: string;
}

interface EmpresaPendiente {
  id: number;
  nombre: string;
  localizacion: string;
  telefono: string;
  descripcion: string;
  usuario: { id: number; correo: string };
}

interface Props {
  onMensaje: (m: MensajeGlobal) => void;
}

function AdminPendientesPage({ onMensaje }: Props) {
  const [empresas, setEmpresas] = useState<EmpresaPendiente[]>([]);
  const [cargando, setCargando] = useState(true);

  const cargar = useCallback(() => {
    setCargando(true);
    fetch("http://localhost:8080/api/admin/empresas/pendientes", { headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) })
      .then(async (res) => { if (res.ok) setEmpresas(await res.json()); else throw new Error(await res.text()); })
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [onMensaje]);

  useEffect(() => {
    const timer = setTimeout(() => cargar(), 0);
    return () => clearTimeout(timer);
  }, [cargar]);

  const autorizar = async (id: number) => {
    try {
      const res = await fetch(`http://localhost:8080/api/admin/empresas/${id}/autorizar`, { method: "POST", headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) });
      if (!res.ok) throw new Error(await res.text());
      onMensaje({ tipo: 'success', texto: 'Empresa autorizada.' });
      cargar();
    } catch (e) { onMensaje({ tipo: 'danger', texto: (e as Error).message }); }
  };

  return (
    <section className="container py-5">
      <SectionTitle eyebrow="Administración" title="Empresas pendientes" />

      {cargando ? <LoadingBlock /> : (
      <table className="table table-hover">
        <thead>
          <tr><th>Correo</th><th>Nombre</th><th>Acción</th></tr>
        </thead>
        <tbody>
          {empresas.length === 0 ? (
            <tr><td colSpan={3} className="text-muted text-center">No hay empresas pendientes de aprobación.</td></tr>
          ) : empresas.map((e) => (
            <tr key={e.id}>
              <td className="align-middle">{e.usuario?.correo}</td>
              <td className="align-middle">{e.nombre}</td>
              <td>
                <button className="btn btn-success btn-sm" onClick={() => autorizar(e.id)}>
                  Aprobar y generar clave
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    )}
  </section>
  );
}

export default AdminPendientesPage;
