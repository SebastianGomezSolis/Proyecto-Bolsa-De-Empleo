// Página de administración para gestionar el árbol jerárquico de características.
// Permite a los administradores navegar por el árbol, ver características,
// crear nuevas características y ver la ruta de navegación actual.

import { useCallback, useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, Caracteristica } from '../../types';

interface Props {
  // Sesión actual del usuario (se usa para verificar rol de administrador)
  sesion: Sesion | null;
  // Función de navegación para redirigir al usuario a otras páginas
  onNavegar: (ruta: string) => void;
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
}

function AdminCaracteristicasPage({ sesion, onNavegar, onMensaje }: Props) {
  // Estado para almacenar las características hijas (subcategorías) del nodo actual
  const [subcategorias, setSubcategorias] = useState<Caracteristica[]>([]);
  // Estado para almacenar la ruta de navegación desde la raíz hasta el padre del nodo actual
  const [ruta, setRuta] = useState<Caracteristica[]>([]);
  // Estado para almacenar la característica actualmente seleccionada (null si estamos en una raíz)
  const [actual, setActual] = useState<Caracteristica | null>(null);
  // Estado para almacenar el nombre de la nueva característica a crear
  const [nombre, setNombre] = useState('');
  // Estado para almacenar todas las características del sistema (usado en el selector de padre)
  const [todas, setTodas] = useState<Caracteristica[]>([]);
  // Estado para indicar si se está cargando datos desde el backend
  const [cargando, setCargando] = useState(true);
  // Estado para indicar si se está navegando entre nodos (evita doble clic)
  const [navegando, setNavegando] = useState(false);

  // Función memoizada para cargar un nodo del árbol (raíz o hijo)
  // actualId: ID de la característica padre (null para raíz)
  const cargarNodo = useCallback(async (actualId: number | null = null) => {
    try {
      // Llamada al backend para obtener características y metadata de navegación
      const resp = await api.getCaracteristicasAdmin(actualId ?? undefined);
      // Actualizar estados con los datos recibidos
      setSubcategorias(resp.subcategorias ?? []);
      setActual(resp.actual ?? null);
      setRuta(resp.ruta ?? []);
      setTodas(resp.todas ?? []);
    } catch (e) {
      // Mostrar mensaje de error si falla la llamada al backend
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    }
  }, [onMensaje]);

  // Effect que se ejecuta cuando cambia la sesión o la función cargarNodo
  // Se encarga de cargar inicialmente el árbol de características
  useEffect(() => {
    let cancelled = false; // Flag para evitar actualizaciones en componentes desmontados

    (async () => {
      // Verificar que el usuario sea administrador antes de proceder
      if (!sesion || sesion.rol !== 'ADMIN') return;
      await Promise.resolve();
      if (cancelled) return;

      setCargando(true);
      try {
        // Cargar inicialmente el árbol desde la raíz
        await cargarNodo(null);
      } finally {
        // Actualizar estado de carga solo si el componente no fue cancelado
        if (!cancelled) setCargando(false);
      }
    })();

    // Limpiar el flag de cancelación cuando el efecto se limpie
    return () => { cancelled = true; };
  }, [sesion, cargarNodo]);

  // Redirección para usuarios no autenticados o que no son administradores
  if (!sesion || sesion.rol !== 'ADMIN') {
    return (
      <section className="container py-5">
        <div className="alert alert-danger">Acceso restringido a administradores.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  // Función para navegar hacia abajo en el árbol (entrar a una característica)
  const entrar = async (sub: Caracteristica) => {
    if (navegando) return; // Prevenir doble clic
    setNavegando(true);
    setNombre(''); // Limpiar campo de nombre
    await cargarNodo(sub.id); // Cargar los hijos de la característica seleccionada
    setNavegando(false);
  };

  // Función para navegar hacia arriba hasta la raíz del árbol
  const irARaiz = async () => {
    setNombre(''); // Limpiar campo de nombre
    await cargarNodo(null); // Cargar desde la raíz
  };

  // Función para navegar hacia abajo a un nodo específico
  const irANodo = async (nodo: Caracteristica) => {
    setNombre(''); // Limpiar campo de nombre
    await cargarNodo(nodo.id); // Cargar los hijos del nodo seleccionado
  };

  // Función para crear una nueva característica
  const crear = async (e: React.FormEvent) => {
    e.preventDefault(); // Prevenir envío tradicional del formulario
    if (!nombre.trim()) return; // Validar que el nombre no esté vacío
    try {
      // Llamada al backend para crear la característica
      await api.crearCaracteristica({ nombre: nombre.trim(), padreId: actual?.id ?? null });
      onMensaje({ tipo: 'success', texto: 'Característica creada.' }); // Mostrar éxito
      setNombre(''); // Limpiar campo de nombre
      // Recargar la lista de características para mostrar la nueva creada
      await cargarNodo(actual?.id ?? null);
    } catch (e) {
      // Mostrar mensaje de error si falla la creación
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    }
  };

  return (
    <section className="container py-5">
      <SectionTitle eyebrow="Administración" title="Características" />

      {cargando ? <LoadingBlock /> : (
        <div className="row g-4">
          {/* Columna izquierda: navegador de árbol y lista de subcategorías */}
          <div className="col-md-6">
            <div className="border rounded p-3 bg-white">
              {/* Barra de navegación (breadcrumb) */}
              <div className="mb-2">
                <span className="fw-semibold">Ruta:</span>
                <span className="ms-1">
                  {/* Botón para ir a la raíz */}
                  <button className="badge text-bg-light border text-dark border-0 bg-transparent"
                    style={{ cursor: 'pointer' }} onClick={irARaiz}>Raíces</button>
                  {/* Mapear cada característica en la ruta y crear un botón para navegar a ella */}
                  {ruta.map((nodo) => (
                    <span key={nodo.id}>
                      <span className="text-muted mx-1">/</span>
                      <button className="badge text-bg-light border text-dark border-0 bg-transparent"
                        style={{ cursor: 'pointer' }} onClick={() => irANodo(nodo)}>
                        {nodo.nombre}
                      </button>
                    </span>
                  ))}
                </span>
              </div>
              {/* Mostrar la característica actual o indicar que estamos en las raíces */}
              <p className="text-muted mb-3" style={{ fontSize: '0.88rem' }}>
                Subcategorías de: <strong>{actual ? actual.nombre : 'raíces'}</strong>
              </p>
              {/* Lista de características hijas (subcategorías) */}
              {subcategorias.map((sub) => (
                <div key={sub.id} className="d-flex justify-content-between align-items-center border rounded px-3 py-2 mb-2 bg-light">
                  <span className="fw-semibold">{sub.nombre}</span>
                  {/* Botón para entrar a esta característica y ver sus hijos */}
                  <button className="btn btn-outline-secondary btn-sm" disabled={navegando} onClick={() => entrar(sub)}>Entrar</button>
                </div>
              ))}
              {/* Mensaje cuando no hay subcategorías */}
              {subcategorias.length === 0 && (
                <div className="text-muted" style={{ fontSize: '0.88rem' }}>Esta categoría no tiene subcategorías.</div>
              )}
            </div>
          </div>

          {/* Columna derecha: formulario para crear nueva característica */}
          <div className="col-md-4">
            <div className="border rounded p-3 bg-white">
              <h6 className="fw-bold mb-3">Agregar Característica</h6>
              <form onSubmit={crear}>
                <div className="mb-3">
                  <label className="form-label">Nombre</label>
                  <input type="text" className="form-control" value={nombre}
                    onChange={(e) => setNombre(e.target.value)} required />
                </div>
                <div className="mb-3">
                  <label className="form-label">Padre</label>
                  <select className="form-select" value={actual?.id ?? ''}
                    onChange={(e) => {
                      const val = e.target.value;
                      if (!val) { setActual(null); } // Seleccionar "(sin padre)"
                      else {
                        // Buscar la característica padre seleccionada
                        const found = todas.find((c) => c.id === Number(val));
                        if (found) setActual(found);
                      }
                    }}>
                    <option value="">(sin padre — raíz)</option>
                    {/* Opciones para cada característica disponible como padre */}
                    {todas.map((c) => (
                      <option key={c.id} value={c.id}>{c.nombre}</option>
                    ))}
                  </select>
                </div>
                <button type="submit" className="btn btn-dark">Crear</button>
              </form>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

export default AdminCaracteristicasPage;