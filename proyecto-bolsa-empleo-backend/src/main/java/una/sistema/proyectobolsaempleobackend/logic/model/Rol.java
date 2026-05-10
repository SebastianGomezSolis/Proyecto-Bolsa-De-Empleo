package una.sistema.proyectobolsaempleobackend.logic.model;

// Enum que define los roles disponibles en el sistema.
// Cada rol determina las acciones y endpoints a los que un usuario puede acceder.
public enum Rol {
    // Rol de administrador: gestiona empresas y oferentes pendientes,
    // administra caracteristicas y genera reportes.
    ADMIN,

    // Rol de empresa: puede crear, editar y desactivar puestos de trabajo,
    // ademas de buscar y ver candidatos.
    EMPRESA,

    // Rol de oferente (candidato): puede registrar habilidades,
    // subir curriculum y buscar puestos de trabajo.
    OFERENTE
}
