import { Sesion } from '../types';

const CLAVE_SESION = 'bolsa.session';

export function obtenerSesionGuardada(): Sesion | null {
  const raw = sessionStorage.getItem(CLAVE_SESION);
  if (!raw) return null;
  try { return JSON.parse(raw) as Sesion; } catch { sessionStorage.removeItem(CLAVE_SESION); return null; }
}

export function guardarSesion(datos: Sesion): void {
  sessionStorage.setItem(CLAVE_SESION, JSON.stringify(datos));
}

export function limpiarSesion(): void {
  sessionStorage.removeItem(CLAVE_SESION);
}

export function obtenerToken(): string | null {
  return obtenerSesionGuardada()?.token ?? null;
}
