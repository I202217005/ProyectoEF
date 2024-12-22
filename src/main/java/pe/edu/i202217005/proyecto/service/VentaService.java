package pe.edu.i202217005.proyecto.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pe.edu.i202217005.proyecto.dto.*;
import pe.edu.i202217005.proyecto.entity.DetalleVenta;
import pe.edu.i202217005.proyecto.entity.Producto;
import pe.edu.i202217005.proyecto.entity.Usuario;
import pe.edu.i202217005.proyecto.entity.Venta;
import pe.edu.i202217005.proyecto.repository.DetalleVentaRepository;
import pe.edu.i202217005.proyecto.repository.VentaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class VentaService {
    private final VentaRepository ventaRepository;
    private final ProductoService productoService;
    private final DetalleVentaRepository detalleVentaRepository;

    public VentaService(VentaRepository ventaRepository,
                        ProductoService productoService,
                        DetalleVentaRepository detalleVentaRepository) {
        this.ventaRepository = ventaRepository;
        this.productoService = productoService;
        this.detalleVentaRepository = detalleVentaRepository;
    }

    // Listar todas las ventas
    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    // Obtener una venta específica
    public Venta obtenerVenta(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
    }

    // Crear nueva venta
    public Venta crearVenta(VentaDTO ventaDTO) {
        // Validar que haya detalles
        if (ventaDTO.getDetalles() == null || ventaDTO.getDetalles().isEmpty()) {
            throw new RuntimeException("La venta debe tener al menos un producto");
        }

        Venta venta = new Venta();
        venta.setClienteId(ventaDTO.getClienteId());
        venta.setFechaVenta(LocalDateTime.now());
        venta.setEstado("PENDIENTE");

        BigDecimal total = BigDecimal.ZERO;

        // Guardar la venta primero para tener el ID
        venta = ventaRepository.save(venta);

        try {
            for (DetalleVentaDTO detalleDTO : ventaDTO.getDetalles()) {
                // Validar cantidad
                if (detalleDTO.getCantidad() <= 0) {
                    throw new RuntimeException("La cantidad debe ser mayor a 0");
                }

                Producto producto = productoService.obtenerProducto(detalleDTO.getProductoId());

                // Validar stock
                if (producto.getStock() < detalleDTO.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
                }

                DetalleVenta detalle = new DetalleVenta();
                detalle.setVenta(venta);
                detalle.setProducto(producto);
                detalle.setCantidad(detalleDTO.getCantidad());
                detalle.setPrecioUnitario(producto.getPrecio());
                detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(detalleDTO.getCantidad())));

                venta.getDetalles().add(detalle);
                total = total.add(detalle.getSubtotal());

                // Actualizar stock
                productoService.actualizarStock(producto.getId(), detalleDTO.getCantidad());
            }

            venta.setTotal(total);
            venta.setEstado("COMPLETADA");
            return ventaRepository.save(venta);
        } catch (Exception e) {
            // Si algo falla, marcamos la venta como CANCELADA
            venta.setEstado("CANCELADA");
            ventaRepository.save(venta);
            throw new RuntimeException("Error al procesar la venta: " + e.getMessage());
        }
    }

    // Cancelar venta
    public Venta cancelarVenta(Long id) {
        Venta venta = obtenerVenta(id);

        if ("COMPLETADA".equals(venta.getEstado())) {
            // Devolver stock
            for (DetalleVenta detalle : venta.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoService.actualizarProducto(producto.getId(), convertToDTO(producto));
            }
        }

        venta.setEstado("CANCELADA");
        return ventaRepository.save(venta);
    }

    // Buscar ventas por cliente
    public List<Venta> buscarVentasPorCliente(Long clienteId) {
        return ventaRepository.findByClienteId(clienteId);
    }

    // Buscar ventas por fecha
    public List<Venta> buscarVentasPorFecha(LocalDateTime fecha) {
        return ventaRepository.findByFechaVentaBetween(
                fecha.withHour(0).withMinute(0),
                fecha.withHour(23).withMinute(59)
        );
    }

    // Obtener reporte de ventas
    public Map<String, Object> obtenerReporteVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<Venta> ventas = ventaRepository.findByFechaVentaBetween(fechaInicio, fechaFin);

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("totalVentas", ventas.size());
        reporte.put("montoTotal", ventas.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        reporte.put("ventasPorEstado", ventas.stream()
                .collect(Collectors.groupingBy(Venta::getEstado, Collectors.counting())));

        return reporte;
    }

    // Método auxiliar para convertir Producto a ProductoDTO
    private ProductoDTO convertToDTO(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        return dto;
    }

    @Transactional
    public Venta procesarCompraPublica(CompraDTO compraDTO) {


        // Obtener el producto
        Producto producto = productoService.obtenerProducto(compraDTO.getProductoId());

        // Validar stock
        if (producto.getStock() < 1) {
            throw new RuntimeException("Producto sin stock disponible");
        }

        // Crear la venta
        Venta venta = new Venta();
        venta.setClienteId(Long.valueOf(1)); // ID por defecto para ventas públicas
        venta.setFechaVenta(LocalDateTime.now());
        venta.setEstado("COMPLETADA");

        // Crear el detalle de venta
        DetalleVenta detalle = new DetalleVenta();
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        detalle.setCantidad(1); // Por defecto 1 unidad
        detalle.setPrecioUnitario(producto.getPrecio());
        detalle.setSubtotal(producto.getPrecio()); // Precio * cantidad (1)

        // Guardar la venta y su detalle
        venta.setTotal(detalle.getSubtotal());
        venta.getDetalles().add(detalle);
        venta = ventaRepository.save(venta);

        // Actualizar el stock
        productoService.actualizarStock(producto.getId(), 1);

        return venta;
    }

    public List<ReporteProductoDTO> obtenerReporteVentas() {
        List<Producto> productos = productoService.listarProductos();
        List<ReporteProductoDTO> reportes = new ArrayList<>();

        for (Producto producto : productos) {
            ReporteProductoDTO reporte = new ReporteProductoDTO();
            reporte.setProducto(producto);

            // Obtener ventas del producto
            List<DetalleVenta> ventas = detalleVentaRepository.findByProductoId(producto.getId());

            // Calcular estadísticas
            reporte.setTotalVentas(ventas.size());
            reporte.setCantidadVendida(ventas.stream().mapToLong(v -> v.getCantidad()).sum());
            reporte.setIngresoTotal(ventas.stream()
                    .map(v -> v.getPrecioUnitario().multiply(new BigDecimal(v.getCantidad())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));

            // Obtener últimas ventas
            List<VentaUsuarioDTO> ultimasVentas = ventas.stream()
                    .map(v -> {
                        VentaUsuarioDTO ventaUsuario = new VentaUsuarioDTO();

                        ventaUsuario.setFecha(v.getVenta().getFechaVenta().toLocalDate());
                        ventaUsuario.setCantidad(v.getCantidad());
                        ventaUsuario.setTotal(v.getSubtotal());
                        return ventaUsuario;
                    })
                    .sorted(Comparator.comparing(VentaUsuarioDTO::getFecha).reversed())
                    .limit(5)  // Solo las últimas 5 ventas
                    .collect(Collectors.toList());

            reporte.setUltimasVentas(ultimasVentas);
            reportes.add(reporte);
        }

        return reportes;
    }

    public long contarUsuariosUnicos() {
        return ventaRepository.countDistinctUsuarios();
    }
}