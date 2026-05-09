import { Caracteristica } from '../types';

interface NodoProps {
  nodo: Caracteristica;
  seleccionados: number[];
  onToggle: (id: number) => void;
  niveles?: Record<number, number>;
  onNivelChange?: (id: number, nivel: number) => void;
}

function NodoSelector({ nodo, seleccionados, onToggle, niveles, onNivelChange }: NodoProps) {
  const hijos = nodo.hijos ?? [];
  const esHoja = hijos.length === 0;

  if (esHoja) {
    return (
      <div className="d-flex align-items-center gap-3 mb-1">
        <input className="form-check-input" type="checkbox" id={`sel_${nodo.id}`}
          checked={seleccionados.includes(nodo.id)}
          onChange={() => onToggle(nodo.id)} />
        <label className="form-check-label flex-grow-1" htmlFor={`sel_${nodo.id}`} style={{ fontSize: '0.88rem' }}>
          {nodo.nombre}
        </label>
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
  raices: Caracteristica[];
  seleccionados: number[];
  onToggle: (id: number) => void;
  mostrarNiveles?: boolean;
  niveles?: Record<number, number>;
  onNivelChange?: (id: number, nivel: number) => void;
}

function SelectorCaracteristicas({ raices, seleccionados, onToggle, mostrarNiveles, niveles, onNivelChange }: Props) {
  return (
    <div className="row">
      {raices.map((raiz) => (
        <div key={raiz.id} className="col-md-3 mb-3">
          <div className="fw-semibold text-secondary mb-1" style={{ fontSize: '0.88rem' }}>{raiz.nombre}</div>
          {raiz.hijos?.map((hijo) => (
            <NodoSelector key={hijo.id} nodo={hijo} seleccionados={seleccionados}
              onToggle={onToggle} niveles={mostrarNiveles ? niveles : undefined} onNivelChange={mostrarNiveles ? onNivelChange : undefined} />
          ))}
        </div>
      ))}
    </div>
  );
}

export default SelectorCaracteristicas;
