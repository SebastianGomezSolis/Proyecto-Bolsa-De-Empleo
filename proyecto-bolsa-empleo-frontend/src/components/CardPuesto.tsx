// Componente para mostrar una tarjeta de puesto de trabajo.
// Presenta la información clave de un puesto en formato de tarjeta visual,
// incluyendo tipo de publicación, fecha, descripción, salario y empresa.

interface Puesto {
  id: number;
  descripcion: string;
  salario: number;
  tipoPublicacion: string;
  empresa: { id: number; nombre: string; usuarioCorreo: string };
  activo: boolean;
  fechaRegistro: string;
  caracteristicas: { id: number; nombre: string; nivelRequerido: number }[];
  tipoCambio?: { compra: number; venta: number; fecha: string };
}
import { formatSalario, formatFecha } from '../utils/formatters';

interface Props {
  // El puesto a mostrar en la tarjeta
  puesto: Puesto;
  // Indica si se debe mostrar la información de la empresa (por defecto: true)
  mostrarEmpresa?: boolean;
}

function CardPuesto({ puesto, mostrarEmpresa = true }: Props) {
  const tieneCaracteristicas = puesto.caracteristicas && puesto.caracteristicas.length > 0;

  return (
    <div className="card shadow-sm border-0 h-100 position-relative tarjeta-puesto">
      <div className="card-body">
        {/* Información superior: tipo de publicación y fecha */}
        <div className="d-flex justify-content-between align-items-start mb-2">
          {/* Badge con el tipo de publicación (publico/privado) */}
          <span className="badge bg-primary-subtle text-primary text-capitalize">{puesto.tipoPublicacion}</span>
          {/* Fecha de publicación formateada */}
          <small className="text-secondary">{formatFecha(puesto.fechaRegistro)}</small>
        </div>
        {/* Descripción del puesto (truncada a 120 caracteres con puntos suspensivos si es mayor) */}
        <p className="mb-2" style={{ fontSize: '0.9rem' }}>{puesto.descripcion?.substring(0, 120)}{(puesto.descripcion?.length ?? 0) > 120 ? '...' : ''}</p>
        {/* Salario formateado (en USD y opcionalmente en CRC si hay tipo de cambio) */}
        <div className="fw-semibold text-primary">{formatSalario(puesto.salario, puesto.tipoCambio)}</div>
        {/* Información de la empresa (si se requiere mostrarla y existe) */}
        {mostrarEmpresa && puesto.empresa && (
          <div className="text-secondary small mt-1">{puesto.empresa.nombre}</div>
        )}
      </div>
      {/* Hover overlay con detalle de características requeridas */}
      {tieneCaracteristicas && (
        <div className="position-absolute top-0 start-0 w-100 h-100 bg-dark bg-opacity-75 text-white p-3 rounded overlay-hover">
          <small className="fw-bold text-uppercase text-warning">Características requeridas</small>
          <ul className="list-unstyled small mt-2 mb-0">
            {puesto.caracteristicas.map((c, i) => (
              <li key={i} className="d-flex justify-content-between">
                <span>{c.nombre}</span>
                <span className="text-info">Nivel {c.nivelRequerido}/5</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

export default CardPuesto;