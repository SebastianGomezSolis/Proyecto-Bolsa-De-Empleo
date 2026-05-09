import { TipoCambio } from '../types';

export function formatRol(rol?: string): string {
  if (!rol) return 'Invitado';
  const mapa: Record<string, string> = { ADMIN: 'Administrador', EMPRESA: 'Empresa', OFERENTE: 'Oferente' };
  return mapa[rol] || rol;
}

export function formatFecha(fecha?: string): string {
  if (!fecha) return 'Sin fecha';
  const date = new Date(fecha);
  return Number.isNaN(date.getTime()) ? fecha : date.toLocaleString('es-CR');
}

export function formatSalario(salario?: number | null, tipoCambio?: TipoCambio | null): string {
  if (salario == null) return '—';
  const usd = Number(salario).toLocaleString('en-US', { style: 'currency', currency: 'USD' });
  if (tipoCambio?.venta) {
    const colones = (Number(salario) * tipoCambio.venta).toLocaleString('es-CR', { style: 'currency', currency: 'CRC' });
    return `${usd} / ${colones}`;
  }
  return usd;
}
