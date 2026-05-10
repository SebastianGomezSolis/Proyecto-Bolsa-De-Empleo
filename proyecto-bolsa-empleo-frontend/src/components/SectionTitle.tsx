// Componente para mostrar títulos de sección con formato consistente.
// Se usa en todas las páginas para presentar secciones con un estilo visual uniforme.

interface Props {
  // Texto pequeño que aparece arriba del título (opcional)
  // Ej: "Últimas ofertas", "Administración"
  eyebrow?: string;
  // Título principal de la sección
  title: string;
  // Descripción o subtítulo explicativo (opcional)
  description?: string;
}

function SectionTitle({ eyebrow, title, description }: Props) {
  return (
    <div className="mb-4">
      {/* Mostrar el eyebrow si se proporciona */}
      {eyebrow && <div className="text-primary fw-semibold text-uppercase small mb-2">{eyebrow}</div>}
      {/* Título principal: grande y en negrita */}
      <h2 className="fw-bold mb-2">{title}</h2>
      {/* Mostrar la descripción si se proporciona */}
      {description && <p className="text-secondary mb-0">{description}</p>}
    </div>
  );
}

export default SectionTitle;