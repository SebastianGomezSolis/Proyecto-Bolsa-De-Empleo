// Componente para mostrar un bloque de carga (spinner) con texto opcional.
// Se usa para indicar al usuario que se está realizando una operación
// en segundo plano (como cargar datos desde el backend).

interface Props {
  // Texto opcional para mostrar junto al spinner
  // Por defecto: "Cargando información..."
  text?: string;
}

function LoadingBlock({ text = 'Cargando información...' }: Props) {
  return (
    <div className="d-flex align-items-center gap-3 py-4">
      {/* Spinner de Bootstrap (animación de carga) */}
      <div className="spinner-border text-primary" role="status"></div>
      {/* Texto descriptivo de lo que se está cargando */}
      <span>{text}</span>
    </div>
  );
}

export default LoadingBlock;