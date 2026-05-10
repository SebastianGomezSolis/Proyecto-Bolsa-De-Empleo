import { useCallback, useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import AccesoRestringido from '../../components/AccesoRestringido';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, OferentePendiente } from '../../types';

interface Props {
  // Sesión actual del usuario (se usa para verificar rol de administrador)
  sesion: Sesion | null;
  // Función de navegación para redirigir al usuario a otras páginas
  onNavegar: (ruta: string) => void;
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
}

function AdminOferentesPendientesPage({ sesion, onNavegar, onMensaje }: Props) {
  // Estado para almacenar la lista de oferentes pendientes de aprobación
  const [oferentes, setOferentes] = useState<OferentePendiente[]>([]);
  // Estado para indicar si se está cargando datos desde el backend
  const [cargando, setCargando] = useState(true);

  // Función memoizada para cargar la lista de oferentes pendientes
  // useCallback evita recrear la función en cada render
  const cargar = useCallback(() => {
    setCargando(true); // Indicar que comenzó la carga
    api.getOferentesPendientes()
      .then(setOferentes) // Actualizar estado con los datos recibidos
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message })) // Manejar errores
      .finally(() => setCargando(false)); // Siempre finalizar el estado de carga
  }, [onMensaje]);

  // Effect que se ejecuta cuando cambia la sesión o la función cargar
  // Se encarga de cargar inicialmente la lista de oferentes pendientes
  useEffect(() => {
    // Solo proceder si el usuario es administrador
    if (sesion?.rol === 'ADMIN') {
      // Usar setTimeout para evitar bloquear la renderización inicial
      const timer = setTimeout(() => cargar(), 0);
      // Limpiar el timeout cuando el componente se desmonte o cambien las dependencias
      return () => clearTimeout(timer);
    }
  }, [sesion, cargar]);

  // Función para autorizar un oferente pendiente
  const autorizar = async (id: number) => {
    try {
      // Llamada al backend para autorizar el oferente
      await api.autorizarOferente(id);
      // Mostrar mensaje de éxito
      onMensaje({ tipo: 'success', texto: 'Oferente autorizado.' });
      // Recargar la lista para mostrar los cambios
      cargar();
    } catch (e) { 
      // Mostrar mensaje de error si falla la autorización
      onMensaje({ tipo: 'danger', texto: (e as Error).message }); 
    }
  };

  return (
    // Envolver el contenido en AccesoRestringido para verificar permisos de administrador
    <AccesoRestringido sesion={sesion} rol="ADMIN" onNavegar={onNavegar}>
      <section className="container py-5">
        <SectionTitle eyebrow="Administración" title="Oferentes pendientes" />

        {cargando ? <LoadingBlock /> : (
          <table className="table table-hover">
            <thead>
              <tr><th>Nombre</th><th>Correo</th><th>Acción</th></tr>
            </thead>
            <tbody>
              {/* Mostrar mensaje cuando no hay oferentes pendientes */}
              {oferentes.length === 0 ? (
                <tr><td colSpan={3} className="text-muted text-center">No hay oferentes pendientes de aprobación.</td></tr>
              ) : 
                // Mapear cada oferente pendiente y crear una fila en la tabla
                oferentes.map((o) => (
                  <tr key={o.id}>
                    {/* Mostrar nombre completo del oferente */}
                    <td className="align-middle">{o.nombre} {o.primerApellido}</td>
                    {/* Mostrar correo electrónico del oferente */}
                    <td className="align-middle">{o.usuario?.correo}</td>
                    {/* Botón para aprobar el oferente */}
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