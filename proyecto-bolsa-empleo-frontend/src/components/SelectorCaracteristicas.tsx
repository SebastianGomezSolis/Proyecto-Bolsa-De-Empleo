interface Caracteristica {
  id: number;
  nombre: string;
  padreId: number | null;
  hijos: Caracteristica[];
}

interface NodoProps {
  // Nodo (característica) a renderizar, puede ser hoja o tener hijos
  nodo: Caracteristica;
  // IDs de las características seleccionadas actualmente
  seleccionados: number[];
  // Función para alternar la selección de una característica
  onToggle: (id: number) => void;
  // Mapa opcional de niveles por característica (para selector con nivel)
  niveles?: Record<number, number>;
  // Callback opcional cuando cambia el nivel de una característica
  onNivelChange?: (id: number, nivel: number) => void;
}

// Componente recursivo que renderiza un nodo del árbol de características
function NodoSelector({ nodo, seleccionados, onToggle, niveles, onNivelChange }: NodoProps) {
  const hijos = nodo.hijos ?? [];
  const esHoja = hijos.length === 0;

  // Si es hoja, mostrar checkbox con opción de nivel
  if (esHoja) {
    return (
      <div className="d-flex align-items-center gap-3 mb-1">
        <input className="form-check-input" type="checkbox" id={`sel_${nodo.id}`}
          checked={seleccionados.includes(nodo.id)}
          onChange={() => onToggle(nodo.id)} />
        <label className="form-check-label flex-grow-1" htmlFor={`sel_${nodo.id}`} style={{ fontSize: '0.88rem' }}>
          {nodo.nombre}
        </label>
        {/* Selector de nivel visible solo cuando la característica está seleccionada */}
        {niveles !== undefined && onNivelChange !== undefined && seleccionados.includes(nodo.id) && (
          <div className="d-flex align-items-center gap-1">
            <label className="text-muted mb-0" style={{ fontSize: '0.8rem' }}>Nivel:</label>
            <select className="form-select form-select-sm" style={{ width: '70px' }}
              value={niveles[nodo.id] ?? 1}
              onChange={(e) => onNivelChange(nodo.id, Number(e.target.value))}>
              {[1, 2, 3, 4, 5].map((n) => <option key={n} value={n}>{n}</option>)}
            </select>
          </div>
        )}
      </div>
    );
  }

  // Si no es hoja, mostrar nombre del grupo y renderizar hijos recursivamente
  return (
    <div className="mb-2">
      <div className="fw-semibold text-secondary mb-1" style={{ fontSize: '0.88rem', fontStyle: 'italic' }}>{nodo.nombre}</div>
      <div className="ms-3">
        {hijos.map((hijo) => (
          <NodoSelector key={hijo.id} nodo={hijo} seleccionados={seleccionados}
            onToggle={onToggle} niveles={niveles} onNivelChange={onNivelChange} />
        ))}
      </div>
    </div>
  );
}

interface Props {
  // Lista de categorías raíz del árbol de características
  raices: Caracteristica[];
  // IDs de las características seleccionadas actualmente
  seleccionados: number[];
  // Función para alternar la selección de una característica
  onToggle: (id: number) => void;
  // Si es true, se muestra el selector de nivel para cada característica
  mostrarNiveles?: boolean;
  // Mapa de niveles por característica
  niveles?: Record<number, number>;
  // Callback cuando cambia el nivel de una característica
  onNivelChange?: (id: number, nivel: number) => void;
}

// Componente principal que muestra el selector jerárquico de características
function SelectorCaracteristicas({ raices, seleccionados, onToggle, mostrarNiveles, niveles, onNivelChange }: Props) {
  return (
    <div className="row">
      {raices.map((raiz) => (
        <div key={raiz.id} className="col-md-3 mb-3">
          {/* Nombre de la categoría raíz */}
          <div className="fw-semibold text-secondary mb-1" style={{ fontSize: '0.88rem' }}>{raiz.nombre}</div>
          {/* Hijos de la raíz renderizados recursivamente */}
          {raiz.hijos?.map((hijo) => (
            <NodoSelector key={hijo.id} nodo={hijo} seleccionados={seleccionados}
              onToggle={onToggle} niveles={mostrarNiveles ? niveles : undefined} onNivelChange={mostrarNiveles ? onNivelChange : undefined} />
          ))}
        </div>
      ))}
    </div>
  );
}

// Exportar el componente para usarlo en otros módulos
export default SelectorCaracteristicas;
