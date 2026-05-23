interface TipoCambio {
  compra: number;
  venta: number;
  fecha: string;
}

// Formatea un rol de usuario para mostrarlo en la interfaz.
export function formatRol(rol?: string): string {
  if (!rol) return 'Invitado';
  const mapa: Record<string, string> = { 
    ADMIN: 'Administrador', 
    EMPRESA: 'Empresa', 
    OFERENTE: 'Oferente' 
  };
  return mapa[rol] || rol;
}

// Formatea una fecha en formato ISO o similar a una representación legible.
export function formatFecha(fecha?: string): string {
  if (!fecha) return 'Sin fecha';
  const date = new Date(fecha);
  // Si la fecha es inválida, retornamos el valor original
  return Number.isNaN(date.getTime()) ? fecha : date.toLocaleString('es-CR');
}

// Formatea un salario para mostrarlo en ambas monedas: USD y CRC.
export function formatSalario(salario?: number | null, tipoCambio?: TipoCambio | null): string {
  if (salario == null) return '—';
  const usd = Number(salario).toLocaleString('en-US', { style: 'currency', currency: 'USD' });
  if (tipoCambio?.venta) {
    // Convertir de USD a CRC usando el tipo de cambio de venta
    const colones = (Number(salario) * tipoCambio.venta).toLocaleString('es-CR', { style: 'currency', currency: 'CRC' });
    return `${usd} / ${colones}`;
  }
  return usd;
}