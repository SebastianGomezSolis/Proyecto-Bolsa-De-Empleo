// Componente para mostrar mensajes globales (toast/notificaciones) en la interfaz.
// Se usa para comunicar éxito, error, información o advertencias al usuario
// de forma no intrusiva y con posibilidad de cerrar manualmente.

import { MensajeGlobal } from '../types';

interface Props {
  // El mensaje a mostrar (null si no hay mensaje)
  mensaje: MensajeGlobal | null;
  // Función de callback para cuando el usuario cierra el mensaje
  onCerrar: () => void;
}

function AlertaMensaje({ mensaje, onCerrar }: Props) {
  // Si no hay mensaje, no renderizar nada
  if (!mensaje) return null;
  
  // Determinar la clase CSS basada en el tipo de mensaje
  // 'danger' se usa tanto para 'error' como para 'danger' (por compatibilidad con Bootstrap)
  const clase = mensaje.tipo === 'error' || mensaje.tipo === 'danger' ? 'danger' : 
                mensaje.tipo === 'success' ? 'success' : mensaje.tipo;

  return (
    <div className="container pt-3">
      {/* Alerta de Bootstrap con el estilo correspondiente al tipo de mensaje */}
      <div className={`alert alert-${clase} alert-dismissible fade show`} role="alert">
        {/* Texto del mensaje */}
        {mensaje.texto}
        {/* Botón para cerrar el mensaje */}
        <button type="button" className="btn-close" onClick={onCerrar}></button>
      </div>
    </div>
  );
}

export default AlertaMensaje;