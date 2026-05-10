// Servicio para gestionar la sesión de usuario en el frontend.
// Este archivo maneja el almacenamiento y recuperación de datos de sesión
// en sessionStorage, incluyendo el token JWT necesario para autenticación
// con el backend.

import { Sesion } from '../types';

// Clave utilizada para almacenar los datos de sesión en sessionStorage
// Usamos sessionStorage (no localStorage) para que la sesión se elimine
// cuando se cierre la pestaña/navegador
const CLAVE_SESION = 'bolsa.session';

// Obtiene los datos de sesión guardados en sessionStorage.
// Si no existen o son inválidos, retorna null y limpia el almacenamiento.
export function obtenerSesionGuardada(): Sesion | null {
  // Obtener el valor almacenado como string JSON
  const raw = sessionStorage.getItem(CLAVE_SESION);
  if (!raw) return null;
  
  try { 
    // Intentar parsear el JSON y convertirlo a tipo Sesion
    return JSON.parse(raw) as Sesion; 
  } catch { 
    // Si falla el parsing, los datos están corruptos -> limpiar y retornar null
    sessionStorage.removeItem(CLAVE_SESION); 
    return null; 
  }
}

// Guarda los datos de sesión en sessionStorage.
// Se llama después de un inicio de sesión exitoso para mantener al usuario autenticado durante su navegación.
export function guardarSesion(datos: Sesion): void {
  // Convertir el objeto Sesion a JSON y almacenarlo en sessionStorage
  sessionStorage.setItem(CLAVE_SESION, JSON.stringify(datos));
}

// Elimina los datos de sesión de sessionStorage.
// Se llama al cerrar sesión para eliminar cualquier rastro de autenticación.
export function limpiarSesion(): void {
  // Remover el elemento de sessionStorage
  sessionStorage.removeItem(CLAVE_SESION);
}

// Obtiene el token JWT de la sesión actual.
// Este token se usa en el encabezado Authorization de las peticiones al backend.
export function obtenerToken(): string | null {
  // Obtener la sesión y retornar su token (o null si no hay sesión)
  return obtenerSesionGuardada()?.token ?? null;
}