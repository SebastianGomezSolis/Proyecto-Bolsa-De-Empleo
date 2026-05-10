// Componente del pie de página (footer) de la aplicación.
// Se muestra en la parte inferior de todas las páginas y contiene
// información de copyright y contacto del sistema.

function Footer() {
  return (
    <footer className="bg-dark text-white p-3 mt-auto" style={{ fontSize: '0.9rem' }}>
      <div className="container d-flex justify-content-between">
        {/* Información del sistema: nombre y empresa desarrolladora */}
        <div><b>Bolsa de Empleo</b><br />Total Soft Inc.</div>
        {/* Información de contacto y créditos */}
        <div>Contacto: info@bolsaempleo.local<br />Créditos: Equipo de desarrollo</div>
      </div>
    </footer>
  );
}

export default Footer;