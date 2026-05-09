package una.sistema.proyectobolsaempleobackend.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import una.sistema.proyectobolsaempleobackend.logic.model.Puesto;

import java.time.LocalDateTime;
import java.util.List;

public interface PuestoRepository extends CrudRepository<Puesto, Integer> {
    List<Puesto> findByEmpresaId(Integer empresaId);
    List<Puesto> findByActivoTrue();
    List<Puesto> findByActivoTrueAndTipoPublicacion(String tipoPublicacion);
    List<Puesto> findByTipoPublicacionAndActivo(String tipoPublicacion, Boolean activo);
    List<Puesto> findTop5ByTipoPublicacionAndActivoOrderByFechaRegistroDesc(String tipoPublicacion, Boolean activo);
    List<Puesto> findByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT p FROM Puesto p WHERE MONTH(p.fechaRegistro) = :mes AND YEAR(p.fechaRegistro) = :anio")
    List<Puesto> findByMesYAnio(@Param("mes") int mes, @Param("anio") int anio);
}
