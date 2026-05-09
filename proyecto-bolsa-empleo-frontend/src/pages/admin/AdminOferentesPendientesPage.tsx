import { useCallback, useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import AccesoRestringido from '../../components/AccesoRestringido';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, OferentePendiente } from '../../types';

interface Props {
  sesion: Sesion | null;
  onNavegar: (ruta: string) => void;
  onMensaje: (m: MensajeGlobal) => void;
}

function AdminOferentesPendientesPage({ sesion, onNavegar, onMensaje }: Props) {
  const [oferentes, setOferentes] = useState<OferentePendiente[]>([]);
  const [cargando, setCargando] = useState(true);

  const cargar = useCallback(() => {
    setCargando(true);
    api.getOferentesPendientes()
      .then(setOferentes)
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
      await api.autorizarOferente(id);
      onMensaje({ tipo: 'success', texto: 'Oferente autorizado.' });
      cargar();
    } catch (e) { onMensaje({ tipo: 'danger', texto: (e as Error).message }); }
  };

  return (
    <AccesoRestringido sesion={sesion} rol="ADMIN" onNavegar={onNavegar}>
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
              ) : oferentes.map((o) => (
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
    </AccesoRestringido>
  );
}

export default AdminOferentesPendientesPage;
