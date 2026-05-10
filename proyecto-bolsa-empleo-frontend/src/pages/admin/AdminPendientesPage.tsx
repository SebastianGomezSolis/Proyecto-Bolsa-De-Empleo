// Página de administración para gestionar empresas pendientes de aprobación.
// Permite visualizar empresas registradas y autorizarlas generando una clave de acceso.

import { useCallback, useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import AccesoRestringido from '../../components/AccesoRestringido';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, EmpresaPendiente } from '../../types';

interface Props {
  // Sesión actual del usuario (se usa para verificar rol de administrador)
  sesion: Sesion | null;
  // Función de navegación para redirigir al usuario a otras páginas
  onNavegar: (ruta: string) => void;
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
}

function AdminPendientesPage({ sesion, onNavegar, onMensaje }: Props) {
  // Estado para almacenar la lista de empresas pendientes de aprobación
  const [empresas, setEmpresas] = useState<EmpresaPendiente[]>([]);
  // Estado para indicar si se está cargando datos desde el backend
  const [cargando, setCargando] = useState(true);

  // Función memoizada para obtener la lista de empresas pendientes desde el backend
  const cargar = useCallback(() => {
    setCargando(true);
    api.getEmpresasPendientes()
      .then(setEmpresas)
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [onMensaje]);

  // Effect que se ejecuta al montar el componente para cargar empresas pendientes
  useEffect(() => {
    if (sesion?.rol === 'ADMIN') {
      const timer = setTimeout(() => cargar(), 0);
      return () => clearTimeout(timer);
    }
  }, [sesion, cargar]);

  // Función para autorizar una empresa pendiente y generar su clave de acceso
  const autorizar = async (id: number) => {
    try {
      await api.autorizarEmpresa(id);
      onMensaje({ tipo: 'success', texto: 'Empresa autorizada.' });
      cargar(); // Recargar la lista después de autorizar
    } catch (e) { onMensaje({ tipo: 'danger', texto: (e as Error).message }); }
  };

  return (
    <AccesoRestringido sesion={sesion} rol="ADMIN" onNavegar={onNavegar}>
      <section className="container py-5">
        <SectionTitle eyebrow="Administración" title="Empresas pendientes" />

        {cargando ? <LoadingBlock /> : (
        /* Tabla con la lista de empresas pendientes y botón de aprobación */
        <table className="table table-hover">
          <thead>
            <tr><th>Correo</th><th>Nombre</th><th>Acción</th></tr>
          </thead>
          <tbody>
            {empresas.length === 0 ? (
              /* Mensaje cuando no hay empresas pendientes */
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
