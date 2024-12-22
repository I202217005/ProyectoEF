package pe.edu.i202217005.proyecto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.i202217005.proyecto.entity.Venta;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByClienteId(Long clienteId);
    List<Venta> findByFechaVentaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    List<Venta> findByEstado(String estado);

    @Query("SELECT COUNT(DISTINCT v.usuario) FROM Venta v WHERE v.usuario.rol = 'ROLE_USER'")
    long countDistinctUsuarios();
}