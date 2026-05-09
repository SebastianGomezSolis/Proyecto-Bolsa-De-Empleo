interface Props {
  eyebrow?: string;
  title: string;
  description?: string;
}

function SectionTitle({ eyebrow, title, description }: Props) {
  return (
    <div className="mb-4">
      {eyebrow && <div className="text-primary fw-semibold text-uppercase small mb-2">{eyebrow}</div>}
      <h2 className="fw-bold mb-2">{title}</h2>
      {description && <p className="text-secondary mb-0">{description}</p>}
    </div>
  );
}

export default SectionTitle;
