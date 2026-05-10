// Componente para mostrar una tarjeta de puesto de trabajo.
// Presenta la información clave de un puesto en formato de tarjeta visual,
// incluyendo tipo de publicación, fecha, descripción, salario y empresa.

import { Puesto } from '../types';
import { formatSalario, formatFecha } from '../utils/formatters';

interface Props {
  // El puesto a mostrar en la tarjeta
  puesto: Puesto;
  // Indica si se debe mostrar la información de la empresa (por defecto: true)
  mostrarEmpresa?: boolean;
}

function CardPuesto({ puesto, mostrarEmpresa = true }: Props) {
  return (
    <div className="card shadow-sm border-0 h-100">
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
    </div>
  );
}

export default CardPuesto;