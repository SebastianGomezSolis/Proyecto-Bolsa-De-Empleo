import { useCallback, useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import { api } from '../../services/api';
import { formatSalario } from '../../utils/formatters';
import { Sesion, MensajeGlobal, Puesto } from '../../types';

interface Props {
  // Sesión actual del usuario (se usa para verificar rol de empresa)
  sesion: Sesion | null;
  // Función de navegación para redirigir al usuario a otras páginas
  onNavegar: (ruta: string) => void;
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
}

// Componente principal para listar los puestos publicados por la empresa
// Permite activar, desactivar y ver candidatos de cada puesto
function EmpresaPuestosPage({ sesion, onNavegar, onMensaje }: Props) {
  // Estado para almacenar la lista de puestos de la empresa
  const [puestos, setPuestos] = useState<Puesto[]>([]);
  // Estado para indicar si se está cargando datos desde el backend
  const [cargando, setCargando] = useState(true);

  // Función memoizada para cargar la lista de puestos de la empresa
  // useCallback evita recrear la función en cada render
  const cargar = useCallback(() => {
    setCargando(true); // Indicar que comenzó la carga
    api.getPuestosEmpresa()
      .then(setPuestos) // Actualizar estado con los datos recibidos
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message })) // Manejar errores
      .finally(() => setCargando(false)); // Siempre finalizar el estado de carga
  }, [onMensaje]);

  // Effect que se ejecuta al montar el componente o cuando cambia la sesión
  // Se encarga de cargar inicialmente los puestos de la empresa
  useEffect(() => {
    // Solo proceder si el usuario es una empresa
    if (sesion?.rol !== 'EMPRESA') return;
    let cancelled = false;

    (async () => {
      await Promise.resolve();
      if (cancelled) return;
      cargar();
    })();

    // Limpiar la bandera cancelled cuando el componente se desmonte
    return () => { cancelled = true; };
  }, [sesion, cargar]);

  // Verificar si el usuario tiene sesión activa y rol de empresa
  // Si no cumple, mostrar mensaje de acceso restringido con botón para volver
  if (!sesion || sesion.rol !== 'EMPRESA') {
    return (
      <section className="container py-5">
        <div className="alert alert-warning">Acceso restringido a empresas autorizadas.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  // Función para desactivar un puesto (lo oculta de las búsquedas)
  const desactivar = async (id: number) => {
    if (!window.confirm('¿Desactivar este puesto?')) return;
    try {
      await api.desactivarPuesto(id);
      onMensaje({ tipo: 'success', texto: 'Puesto desactivado.' });
      cargar(); // Recargar la lista para reflejar el cambio
    } catch (e) {
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    }
  };

  // Función para activar un puesto desactivado
  const activar = async (id: number) => {
    try {
      await api.activarPuesto(id);
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
        <button className="btn btn-primary" onClick={() => onNavegar('/empresa/publicar')}>+ Publicar puesto</button>
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
            </>
          )}
        </>
      )}
    </section>
  );
}

export default EmpresaPuestosPage;
