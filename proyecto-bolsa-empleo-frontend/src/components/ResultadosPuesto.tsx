import { Puesto } from '../types';
import { formatSalario } from '../utils/formatters';

interface Props {
  // Lista de puestos obtenidos de la búsqueda
  puestos: Puesto[];
  // Texto opcional para mostrar cuando no hay resultados
  vacio?: string;
}

// Componente que muestra la lista de resultados de puestos encontrados
function ResultadosPuesto({ puestos, vacio = 'No se encontraron puestos.' }: Props) {
  // Mostrar mensaje de vacío si no hay resultados
  if (puestos.length === 0) {
    return <p className="text-muted">{vacio}</p>;
  }

  return (
    <>
      {puestos.map((p, i) => (
        // Tarjeta individual para cada puesto
        <div key={p.id} className="border rounded bg-white p-3 mb-3">
          {/* Encabezado con nombre de la empresa y badge de publicación privada */}
          <div className="d-flex justify-content-between align-items-start mb-1">
            <h6 className="mb-0 fw-bold">{p.empresa?.nombre}</h6>
            {p.tipoPublicacion === 'privado' && (
              <span className="badge text-bg-secondary" style={{ fontSize: '0.75rem' }}>Privado</span>
            )}
          </div>
          {/* Descripción del puesto */}
          <p className="text-muted mb-2" style={{ fontSize: '0.85rem' }}>{p.descripcion}</p>
          {/* Salario formateado con tipo de cambio */}
          <p className="mb-2">
            <strong>Salario:</strong> {formatSalario(p.salario, p.tipoCambio)}
            {!p.tipoCambio && <span className="text-muted ms-1" style={{ fontSize: '0.85rem' }}>(tipo de cambio no disponible)</span>}
          </p>
          {/* Sección colapsable de características requeridas */}
          {p.caracteristicas && p.caracteristicas.length > 0 && (
            <>
              <button className="btn btn-link text-dark text-decoration-none p-0" style={{ fontSize: '0.88rem' }}
                data-bs-toggle="collapse" data-bs-target={`#car_res_${i}`}>
                Ver características requeridas ▾
              </button>
              <div className="collapse mt-2" id={`car_res_${i}`}>
                <div className="d-flex flex-wrap gap-1">
                  {p.caracteristicas.map((pc) => (
                    <span key={pc.id} className="badge text-bg-light border" style={{ fontSize: '0.78rem' }}>
                      {pc.nombre} (niv. {pc.nivelRequerido})
                    </span>
                  ))}
                </div>
              </div>
            </>
          )}
        </div>
      ))}
    </>
  );
}

// Exportar el componente para usarlo en otros módulos
export default ResultadosPuesto;
