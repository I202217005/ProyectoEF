package pe.edu.i202217005.proyecto.service;

import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pe.edu.i202217005.proyecto.dto.ProductoDTO;
import pe.edu.i202217005.proyecto.entity.Producto;
import pe.edu.i202217005.proyecto.repository.ProductoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
@Transactional
public class ProductoService {
    private final ProductoRepository productoRepository;
    private static final String PRODUCTO_NO_ENCONTRADO = "Producto no encontrado con ID: ";

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }
    private static final int MAX_IMAGE_LENGTH = 100000;
    // Métodos de Listado
    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public List<Producto> listarProductosDisponibles() {
        return productoRepository.findAll().stream()
                .filter(producto -> producto.getStock() > 0)
                .collect(Collectors.toList());
    }

    public List<Producto> buscarProductosPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public List<Producto> listarProductosPorPrecio(BigDecimal precioMin, BigDecimal precioMax) {
        return productoRepository.findByPrecioBetween(precioMin, precioMax);
    }

    // Métodos de Obtención
    public Producto obtenerProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCTO_NO_ENCONTRADO + id));
    }

    public boolean existeProducto(Long id) {
        return productoRepository.existsById(id);
    }

    // Métodos de Creación y Actualización
    public Producto crearProducto(ProductoDTO productoDTO) {
        validarProductoDTO(productoDTO);

        Producto producto = new Producto();
        copiarDatosDeDTO(producto, productoDTO);
        producto.setCreatedAt(LocalDateTime.now());
        producto.setUpdatedAt(LocalDateTime.now());

        return productoRepository.save(producto);
    }

    public Producto actualizarProducto(Long id, ProductoDTO productoDTO) {
        Producto producto = obtenerProducto(id);
        producto.setNombre(productoDTO.getNombre());
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setPrecio(productoDTO.getPrecio());
        producto.setStock(productoDTO.getStock());

        // Validar y establecer la imagen
        if (productoDTO.getImagen() != null) {
            if (productoDTO.getImagen().length() > MAX_IMAGE_LENGTH) {
                throw new RuntimeException("La imagen es demasiado grande. Máximo permitido: 100KB");
            }
            producto.setImagen(productoDTO.getImagen());
        }

        producto.setUpdatedAt(LocalDateTime.now());
        return productoRepository.save(producto);
    }
    // Métodos de Gestión de Stock
    public void actualizarStock(Long id, Integer cantidad) {
        Producto producto = obtenerProducto(id);
        if (producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
        }
        producto.setStock(producto.getStock() - cantidad);
        producto.setUpdatedAt(LocalDateTime.now());
        productoRepository.save(producto);
    }

    public void agregarStock(Long id, Integer cantidad) {
        Producto producto = obtenerProducto(id);
        producto.setStock(producto.getStock() + cantidad);
        producto.setUpdatedAt(LocalDateTime.now());
        productoRepository.save(producto);
    }

    // Método de Eliminación
    public void eliminarProducto(Long id) {
        if (!existeProducto(id)) {
            throw new RuntimeException(PRODUCTO_NO_ENCONTRADO + id);
        }
        try {
            Producto producto = obtenerProducto(id);
            if (producto.getStock() == 0) {
                throw new RuntimeException("No se puede eliminar un producto sin stock");
            }
            productoRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("No se puede eliminar el producto porque tiene ventas asociadas");
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el producto: " + e.getMessage());
        }
    }

    // Métodos auxiliares privados
    private void validarProductoDTO(ProductoDTO dto) {
        if (dto == null) {
            throw new RuntimeException("Los datos del producto no pueden ser nulos");
        }
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre del producto es requerido");
        }
        if (dto.getPrecio() == null || dto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El precio debe ser mayor a 0");
        }
        if (dto.getStock() == null || dto.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }
    }

    private void copiarDatosDeDTO(Producto producto, ProductoDTO dto) {
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        if (dto.getImagen() != null && !dto.getImagen().trim().isEmpty()) {
            producto.setImagen(dto.getImagen());
        }
    }

    // Métodos estadísticos
    public long contarProductos() {
        return productoRepository.count();
    }

    public BigDecimal calcularValorTotalInventario() {
        return productoRepository.findAll().stream()
                .map(p -> p.getPrecio().multiply(new BigDecimal(p.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Producto> obtenerProductosBajoStock(Integer stockMinimo) {
        return productoRepository.findByStockLessThan(stockMinimo);
    }
}