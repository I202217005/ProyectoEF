package pe.edu.i202217005.proyecto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.i202217005.proyecto.entity.Producto;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByPrecioBetween(BigDecimal precioMin, BigDecimal precioMax);
    List<Producto> findByStockLessThan(Integer stockMinimo);
    List<Producto> findByStockGreaterThan(Integer stockMinimo);
}