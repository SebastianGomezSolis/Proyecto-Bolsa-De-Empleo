import { MensajeGlobal } from '../types';

interface Props {
  mensaje: MensajeGlobal | null;
  onCerrar: () => void;
}

function AlertaMensaje({ mensaje, onCerrar }: Props) {
  if (!mensaje) return null;
  const clase = mensaje.tipo === 'error' || mensaje.tipo === 'danger' ? 'danger' : mensaje.tipo === 'success' ? 'success' : mensaje.tipo;

  return (
    <div className="container pt-3">
      <div className={`alert alert-${clase} alert-dismissible fade show`} role="alert">
        {mensaje.texto}
        <button type="button" className="btn-close" onClick={onCerrar}></button>
      </div>
    </div>
  );
}

export default AlertaMensaje;
