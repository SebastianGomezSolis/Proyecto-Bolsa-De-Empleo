import { Puesto } from '../types';
import { formatSalario, formatFecha } from '../utils/formatters';

interface Props {
  puesto: Puesto;
  mostrarEmpresa?: boolean;
}

function CardPuesto({ puesto, mostrarEmpresa = true }: Props) {
  return (
    <div className="card shadow-sm border-0 h-100">
      <div className="card-body">
        <div className="d-flex justify-content-between align-items-start mb-2">
          <span className="badge bg-primary-subtle text-primary text-capitalize">{puesto.tipoPublicacion}</span>
          <small className="text-secondary">{formatFecha(puesto.fechaRegistro)}</small>
        </div>
        <p className="mb-2" style={{ fontSize: '0.9rem' }}>{puesto.descripcion?.substring(0, 120)}{(puesto.descripcion?.length ?? 0) > 120 ? '...' : ''}</p>
        <div className="fw-semibold text-primary">{formatSalario(puesto.salario, puesto.tipoCambio)}</div>
        {mostrarEmpresa && puesto.empresa && (
          <div className="text-secondary small mt-1">{puesto.empresa.nombre}</div>
        )}
      </div>
    </div>
  );
}

export default CardPuesto;
