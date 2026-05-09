import { useCallback, useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import AccesoRestringido from '../../components/AccesoRestringido';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, EmpresaPendiente } from '../../types';

interface Props {
  sesion: Sesion | null;
  onNavegar: (ruta: string) => void;
  onMensaje: (m: MensajeGlobal) => void;
}

function AdminPendientesPage({ sesion, onNavegar, onMensaje }: Props) {
  const [empresas, setEmpresas] = useState<EmpresaPendiente[]>([]);
  const [cargando, setCargando] = useState(true);

  const cargar = useCallback(() => {
    setCargando(true);
    api.getEmpresasPendientes()
      .then(setEmpresas)
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [onMensaje]);

  useEffect(() => {
    if (sesion?.rol === 'ADMIN') {
      const timer = setTimeout(() => cargar(), 0);
      return () => clearTimeout(timer);
    }
  }, [sesion, cargar]);

  const autorizar = async (id: number) => {
    try {
      await api.autorizarEmpresa(id);
      onMensaje({ tipo: 'success', texto: 'Empresa autorizada.' });
      cargar();
    } catch (e) { onMensaje({ tipo: 'danger', texto: (e as Error).message }); }
  };

  return (
    <AccesoRestringido sesion={sesion} rol="ADMIN" onNavegar={onNavegar}>
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
    </AccesoRestringido>
  );
}

export default AdminPendientesPage;
