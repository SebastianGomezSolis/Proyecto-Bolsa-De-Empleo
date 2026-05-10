// Definiciones de tipos para toda la aplicación frontend.
// Estos tipos describen la estructura de datos que se intercambian con el backend
// y se usan para tipado estático en toda la aplicación.

// Tipo que representa la información de una sesión de usuario activa.
// Contiene los datos esenciales del usuario autenticado y se almacena en localStorage.
export interface Sesion {
  // ID único del usuario en la base de datos
  id: number;

  // Correo electrónico del usuario
  correo: string;

  // Rol del usuario (ADMIN, EMPRESA u OFERENTE)
  rol: 'ADMIN' | 'EMPRESA' | 'OFERENTE';

  // ID de referencia que vincula al usuario con su entidad específica
  // (Administrador.id, Empresa.id u Oferente.id)
  referenciaId: number;

  // Token JWT generado para autenticar requests subsecuentes
  token: string;
}

// Tipo que representa un mensaje global (toast/notificación) que se muestra al usuario.
// Se usa para comunicar éxito, error, información o advertencias de forma no intrusiva.
export interface MensajeGlobal {
  // Tipo de mensaje que determina el estilo visual (color, ícono)
  tipo: 'success' | 'error' | 'info' | 'warning' | 'danger';

  // Texto del mensaje que se mostrará al usuario
  texto: string;
}

// Interfaz que define la estructura de un puesto de trabajo.
// Representa los datos completos de un puesto tal como los devuelve el API backend.
export interface Puesto {
  // ID único del puesto en la base de datos
  id: number;

  // Descripción detallada del puesto (requisitos, responsabilidades, etc.)
  descripcion: string;

  // Salario ofrecido para el puesto (en colones)
  salario: number;

  // Tipo de publicación del puesto: "publico" (visible para todos) o "privado" (solo para oferentes)
  tipoPublicacion: string;

  // Información de la empresa que publica el puesto
  empresa: {
    // ID de la empresa
    id: number;
    // Nombre de la empresa
    nombre: string;
    // Correo del usuario asociado a la empresa
    usuarioCorreo: string;
  };

  // Estado del puesto: true si está activo y visible, false si está desactivado
  activo: boolean;

  // Fecha y hora en que el puesto fue registrado
  fechaRegistro: string;

  // Lista de caracteristicas (habilidades) requeridas para este puesto
  // Cada elemento incluye la característica y el nivel requerido
  caracteristicas: {
    // ID de la característica/habilidad requerida
    id: number;
    // Nombre de la característica/habilidad
    nombre: string;
    // Nivel mínimo requerido (1-5) para que un candidato sea considerado
    nivelRequerido: number;
  }[];

  // Tipo de cambio al momento de la consulta (se agrega desde la respuesta del API)
  tipoCambio?: TipoCambio;
}

// Interfaz que define la estructura de un oferente (candidato).
// Representa los datos completos de un oferente tal como los devuelve el API backend.
export interface Oferente {
  // ID único del oferente en la base de datos
  id: number;

  // Identificación del oferente (cédula, pasaporte, etc.)
  identificacion: string;

  // Nombre(s) completo(s) del oferente
  nombre: string;

  // Primer apellido del oferente
  primerApellido: string;

  // Información de la nacionalidad del oferente
  nacionalidad: {
    // Código ISO de 2 letras de la nacionalidad (ej: "CR", "US")
    iso: string;
    // Nombre completo de la nacionalidad (ej: "Costa Rica", "Estados Unidos")
    nombre: string;
  };

  // Número de teléfono de contacto del oferente
  telefono: string;

  // Ciudad o lugar donde reside actualmente el oferente
  lugarResidencia: string;

  // Estado del oferente: true si está autorizado y puede iniciar sesión, false si está pendiente
  autorizado: boolean;

  // Ruta o path donde se almacena el curriculum (CV) en formato PDF del oferente
  // Relativo al directorio public del frontend (ej: "uploads/curriculos/12345.pdf")
  curriculum: string;

  // Lista de habilidades registradas por el oferente
  // Cada elemento incluye la característica y el nivel del oferente
  habilidades: {
    // ID de la característica/habilidad
    id: number;
    // Nombre de la característica/habilidad
    nombre: string;
    // Nivel de dominio del oferente en esta habilidad (1-5)
    nivel: number;
  }[];
}

// Interfaz que define la estructura de una empresa.
// Representa los datos completos de una empresa tal como los devuelve el API backend.
export interface Empresa {
  // ID único de la empresa en la base de datos
  id: number;

  // Nombre comercial o razón social de la empresa
  nombre: string;

  // Ubicación física o dirección de la empresa
  localizacion: string;

  // Número de teléfono de contacto de la empresa
  telefono: string;

  // Descripción o información adicional sobre la empresa
  descripcion: string;

  // Estado de la empresa: true si está autorizada y puede usar el sistema, false si está pendiente
  autorizado: boolean;
}

// Interfaz que define la estructura de una característica.
// Representa los datos de una categoría/habilidad en el árbol jerarquico.
export interface Caracteristica {
  // ID único de la característica en la base de datos
  id: number;

  // Nombre de la característica (ej: "Java", "Base de Datos", "Trabajo en equipo")
  nombre: string;

  // ID de la característica padre (null si es una raíz del árbol)
  // Las características forman un árbol donde las hojas son habilidades específicas
  // y las nodos intermedios son categorías
  padreId: number | null;

  // Lista de subcaracterísticas (hijos) de esta característica
  // Vacía si la característica es una hoja (no tiene hijos)
  hijos: Caracteristica[];
}

// Interfaz que define la estructura de una nacionalidad.
// Representa los datos de un país para el registro de oferentes.
export interface Nacionalidad {
  // Código ISO de 2 letras como identificador único (ej: "CR", "US", "MX")
  iso: string;

  // Nombre completo del país (ej: "Costa Rica", "Estados Unidos")
  nombre: string;

  // Descripción o información adicional sobre el país
  descripcion: string | null;

  // Código ISO de 3 letras del país (ej: "CRI", "USA")
  iso3: string | null;

  // Código numérico asignado al país (ej: 506 para Costa Rica)
  codigoNumero: number | null;

  // Código telefónico internacional del país (ej: 506)
  codigoTelefono: number | null;
}

// Interfaz que define la estructura de un resultado de matching (emparejamiento).
// Representa la compatibilidad entre un puesto y un oferente basada en habilidades.
export interface MatchResult {
  // El oferente que corresponde a este resultado
  oferente: Oferente;

  // Similitud calculada usando coseno (valor entre 0.0 y 1.0)
  similitud: number;

  // Porcentaje de compatibilidad (similitud * 100), valor entre 0 y 100
  porcentaje: number;

  // Cantidad de requisitos (caracteristicas del puesto) que el oferente cumple
  requisitosCumplidos: number;

  // Total de requisitos (caracteristicas) que tiene el puesto
  totalRequisitos: number;
}

// Interfaz que define los parámetros de consulta para buscar puestos por caracteristicas.
// Se usa en los endpoints de búsqueda tanto para ofertaes como para público.
export interface BusquedaPuestosParams {
  // Lista opcional de IDs de caracteristicas por las que filtrar los puestos
  // Si está vacío o no se proporciona, retorna todos los puestos (o ninguno según la lógica)
  caracteristicaIds?: number[];
}

// Interfaz que define la estructura de un reporte de puestos por mes y año.
// Se usa para generar y mostrar reportes PDF en el panel de administración.
export interface ReportePuestos {
  // Mes del reporte (1-12)
  mes: number;

  // Año del reporte (ej: 2026)
  anio: number;

  // Lista de puestos incluidos en el reporte
  puestos: Puesto[];

  // Nombre del mes en español (ej: "Enero", "Febrero")
  nombreMes: string;

  // Total de puestos encontrados en el período
  totalPuestos: number;
}

// Interfaz que define la estructura de credenciales para inicio de sesión.
// Se usa en el formulario de login para enviar datos al backend.
export interface LoginCredentials {
  // Correo electrónico del usuario registrado en el sistema
  correo: string;

  // Contrasena del usuario (se verifica contra el hash almacenado en la BD)
  clave: string;
}

// Interfaz que define la estructura de una respuesta de inicio de sesión exitosa.
// Contiene los datos del usuario autenticado y el token JWT generado.
export interface LoginResponse {
  // ID único del usuario en la base de datos
  id: number;

  // Correo electrónico del usuario autenticado
  correo: string;

  // Nombre del rol del usuario (ADMIN, EMPRESA u OFERENTE)
  rol: string;

  // ID de referencia que vincula al usuario con su entidad específica
  referenciaId: number;

  // Token JWT generado para autenticar requests subsecuentes
  token: string;
}

// Interfaz que define la estructura de datos para registrar una nueva empresa.
// Contiene todos los datos necesarios para crear una empresa y su usuario asociado.
export interface RegistroEmpresaData {
  // Correo electrónico único para la cuenta de la empresa
  correo: string;

  // Contrasena para la cuenta (se almacenará hasheada)
  clave: string;

  // Nombre comercial o razón social de la empresa
  nombre: string;

  // Ubicación o dirección de la empresa
  localizacion: string;

  // Número de teléfono de contacto
  telefono: string;

  // Descripción o información adicional sobre la empresa
  descripcion: string;
}

// Interfaz que define la estructura de datos para registrar un nuevo oferente (candidato).
// Contiene los datos personales del oferente, sus credenciales de acceso
// y la referencia a su nacionalidad.
export interface RegistroOferenteData {
  // Correo electrónico único para la cuenta del oferente
  correo: string;

  // Contrasena para la cuenta (se almacenará hasheada)
  clave: string;

  // Número de identificación único del oferente (cedula, pasaporte, etc.)
  identificacion: string;

  // Nombre(s) completo(s) del oferente
  nombre: string;

  // Primer apellido del oferente
  primerApellido: string;

  // Código ISO de la nacionalidad del oferente (ej: "CR", "US")
  isoNacionalidad: string;

  // Número de teléfono de contacto del oferente
  telefono: string;

  // Ciudad o lugar donde reside actualmente el oferente
  lugarResidencia: string;
}

// Interfaz que define la estructura de datos para crear un nuevo puesto de trabajo.
// Contiene la información del puesto y la lista de caracteristicas (con niveles)
// requeridas como requisitos para los candidatos.
export interface CrearPuestoData {
  // Descripción detallada del puesto (requisitos, responsabilidades, beneficios)
  descripcion: string;

  // Salario ofrecido para el puesto (en colones)
  salario: number;

  // Tipo de publicación: "publico" (visible para todos) o "privado" (solo para oferentes)
  tipoPublicacion: string;

  // Lista de IDs de las caracteristicas (hojas) que son requisitos para el puesto
  caracteristicaIds: number[];

  // Mapa que relaciona cada ID de caracteristica con su nivel requerido.
  // Formato: "nivel_ID" -> "valor" (ej: "nivel_5" -> "3")
  niveles: Record<string, string>;
}

// Interfaz que define la estructura de datos para registrar un nuevo administrador.
// Contiene los datos necesarios para crear un administrador y su usuario.
export interface RegistroAdminData {
  // Correo electrónico único para la cuenta del administrador
  correo: string;

  // Contrasena para la cuenta (se almacenará hasheada)
  clave: string;

  // Número de identificación único del administrador
  identificacion: string;

  // Nombre completo del administrador
  nombre: string;
}

// Tipo de dato que representa el tipo de cambio de compra y venta del dólar.
export interface TipoCambio {
  compra: number;
  venta: number;
  fecha: string;
}

// Interfaz que define la respuesta de endpoints de búsqueda de puestos.
export interface PuestosResponse {
  puestos: Puesto[];
  tipoCambio?: TipoCambio;
}

// Interfaz que define los datos de una empresa pendiente de aprobación.
export interface EmpresaPendiente {
  id: number;
  nombre: string;
  localizacion: string;
  telefono: string;
  descripcion: string;
  usuario: {
    id: number;
    correo: string;
  };
}

// Interfaz que define los datos de un oferente pendiente de aprobación.
export interface OferentePendiente {
  id: number;
  identificacion: string;
  nombre: string;
  primerApellido: string;
  telefono: string;
  lugarResidencia: string;
  usuario: {
    id: number;
    correo: string;
  };
}

// Interfaz que define la respuesta del endpoint de características para administrador.
export interface CaracteristicasAdminResponse {
  subcategorias: Caracteristica[];
  ruta: Caracteristica[];
  actual: Caracteristica | null;
  todas: Caracteristica[];
}

// Interfaz que define la respuesta del endpoint de características para oferente.
export interface CaracteristicasOferenteResponse {
  subcategorias: Caracteristica[];
  ruta: Caracteristica[];
  actual: Caracteristica | null;
}

// Interfaz que define la estructura de una habilidad registrada por un oferente.
export interface Habilidad {
  id: number;
  caracteristica: {
    id: number;
    nombre: string;
  };
  nivel: number;
}

// Interfaz que define el perfil de un oferente (extiende con datos de usuario).
export interface OferentePerfil {
  id: number;
  identificacion: string;
  nombre: string;
  primerApellido: string;
  telefono: string;
  lugarResidencia: string;
  curriculum: string;
  usuario: {
    id: number;
    correo: string;
  };
}

// Interfaz que define un candidato resultante del matching.
export interface Candidato {
  oferente: {
    id: number;
    nombre: string;
    primerApellido: string;
  };
  similitud: number;
  porcentaje: number;
  requisitosCumplidos: number;
  totalRequisitos: number;
}

// Tipo que define los roles de usuario disponibles en el sistema.
export type Rol = 'ADMIN' | 'EMPRESA' | 'OFERENTE';
