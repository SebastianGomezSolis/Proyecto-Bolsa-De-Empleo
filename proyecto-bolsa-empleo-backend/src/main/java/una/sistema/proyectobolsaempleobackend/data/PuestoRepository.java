package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import una.sistema.proyectobolsaempleobackend.logic.model.Puesto;

import java.time.LocalDateTime;
import java.util.List;

// Repositorio para operaciones CRUD con la entidad Puesto.
// Proporciona consultas especificas para filtrar puestos por diferentes criterios.
public interface PuestoRepository extends CrudRepository<Puesto, Integer> {
    // Busca todos los puestos pertencientes a una empresa especifica
    List<Puesto> findByEmpresaId(Integer empresaId);

    // Busca todos los puestos activos (independiente del tipo de publicacion)
    List<Puesto> findByActivoTrue();

    // Busca puestos activos de un tipo de publicacion especifico
    List<Puesto> findByActivoTrueAndTipoPublicacion(String tipoPublicacion);

    // Busca puestos por tipo y estado de actividad
    List<Puesto> findByTipoPublicacionAndActivo(String tipoPublicacion, Boolean activo);

    // Busca los ultimos 5 puestos publicos ordenados por fecha de registro descendente
    List<Puesto> findTop5ByTipoPublicacionAndActivoOrderByFechaRegistroDesc(String tipoPublicacion, Boolean activo);

    // Busca puestos registrados dentro de un rango de fechas
    List<Puesto> findByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin);

    // Consulta JPQL para buscar puestos por mes y ano de registro
    @Query("SELECT p FROM Puesto p WHERE MONTH(p.fechaRegistro) = :mes AND YEAR(p.fechaRegistro) = :anio")
    List<Puesto> findByMesYAnio(@Param("mes") int mes, @Param("anio") int anio);
}
