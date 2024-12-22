package pe.edu.i202217005.proyecto.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.i202217005.proyecto.dto.VentaDTO;
import pe.edu.i202217005.proyecto.entity.Venta;
import pe.edu.i202217005.proyecto.service.ProductoService;
import pe.edu.i202217005.proyecto.service.VentaService;

@Controller
@RequestMapping("/ventas")
public class VentaController {
    private final VentaService ventaService;
    private final ProductoService productoService;

    public VentaController(VentaService ventaService, ProductoService productoService) {
        this.ventaService = ventaService;
        this.productoService = productoService;
    }

    @GetMapping("/lista")
    public String listarVentas(Model model) {
        model.addAttribute("ventas", ventaService.listarVentas());
        return "ventas/lista";
    }

    @GetMapping("/form")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("venta", new VentaDTO());
        model.addAttribute("productos", productoService.listarProductosDisponibles());
        return "ventas/form";
    }

    @PostMapping("/guardar")
    public String guardarVenta(@ModelAttribute("venta") VentaDTO ventaDTO) {
        ventaService.crearVenta(ventaDTO);
        return "redirect:/ventas/lista";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Venta venta = ventaService.obtenerVenta(id);
        model.addAttribute("venta", venta);
        return "ventas/detalle";
    }

    @GetMapping("/cancelar/{id}")
    public String cancelarVenta(@PathVariable Long id) {
        ventaService.cancelarVenta(id);
        return "redire  ct:/ventas/lista";
    }
}