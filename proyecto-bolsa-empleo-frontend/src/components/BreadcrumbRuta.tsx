// Componente de barra de navegación (breadcrumb) para mostrar la ubicación actual
// en el árbol de características. Permite navegar hacia arriba en el árbol
// y muestra la característica actual seleccionada.

interface Caracteristica {
  id: number;
  nombre: string;
  padreId: number | null;
  hijos: Caracteristica[];
}

interface Props {
  // Array de características que forman el camino desde la raíz hasta el padre
  // de la característica actual (excluyendo la actual)
  ruta: Caracteristica[];
  // Característica actualmente seleccionada (puede ser null si estamos en una raíz)
  actual: Caracteristica | null;
  // Callback para navegar a la raíz del árbol (limpiar selección)
  onIrARaiz: () => void;
  // Callback para navegar a un nodo específico del árbol
  onIrANodo: (nodo: Caracteristica) => void;
}

function BreadcrumbRuta({ ruta, actual, onIrARaiz, onIrANodo }: Props) {
  return (
    <div className="mb-2">
      {/* Etiqueta estática */}
      <span className="fw-semibold">Ruta:</span>
      <span className="ms-1">
        {/* Botón para ir a la raíz del árbol */}
        <button className="badge text-bg-light border text-dark border-0 bg-transparent"
          style={{ cursor: 'pointer' }} onClick={onIrARaiz}>Raíces</button>
        {/* Mapear cada característica en el camino y crear un botón para cada una */}
        {ruta.map((nodo) => (
          <span key={nodo.id}>
            {/* Separador entre nodos */}
            <span className="text-muted mx-1">/</span>
            {/* Botón para navegar a este nodo */}
            <button className="badge text-bg-light border text-dark border-0 bg-transparent"
              style={{ cursor: 'pointer' }} onClick={() => onIrANodo(nodo)}>
              {nodo.nombre}
            </button>
          </span>
        ))}
      </span>
      {/* Mostrar la característica actual si existe (no estamos en una raíz) */}
      {actual && (
        <div className="text-muted mt-1" style={{ fontSize: '0.88rem' }}>
          Subcategorías de: <strong>{actual.nombre}</strong>
        </div>
      )}
    </div>
  );
}

export default BreadcrumbRuta;