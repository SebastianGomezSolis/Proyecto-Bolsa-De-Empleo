import { Caracteristica } from '../types';

interface Props {
  ruta: Caracteristica[];
  actual: Caracteristica | null;
  onIrARaiz: () => void;
  onIrANodo: (nodo: Caracteristica) => void;
}

function BreadcrumbRuta({ ruta, actual, onIrARaiz, onIrANodo }: Props) {
  return (
    <div className="mb-2">
      <span className="fw-semibold">Ruta:</span>
      <span className="ms-1">
        <button className="badge text-bg-light border text-dark border-0 bg-transparent"
          style={{ cursor: 'pointer' }} onClick={onIrARaiz}>Raíces</button>
        {ruta.map((nodo) => (
          <span key={nodo.id}>
            <span className="text-muted mx-1">/</span>
            <button className="badge text-bg-light border text-dark border-0 bg-transparent"
              style={{ cursor: 'pointer' }} onClick={() => onIrANodo(nodo)}>
              {nodo.nombre}
            </button>
          </span>
        ))}
      </span>
      {actual && (
        <div className="text-muted mt-1" style={{ fontSize: '0.88rem' }}>
          Subcategorías de: <strong>{actual.nombre}</strong>
        </div>
      )}
    </div>
  );
}

export default BreadcrumbRuta;
