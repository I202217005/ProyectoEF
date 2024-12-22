package pe.edu.i202217005.proyecto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pe.edu.i202217005.proyecto.dto.ReporteProductoDTO;
import pe.edu.i202217005.proyecto.entity.Producto;
import pe.edu.i202217005.proyecto.service.ProductoService;
import pe.edu.i202217005.proyecto.service.VentaService;

import java.math.BigDecimal;
import java.util.List;
@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final VentaService ventaService;

    public ReporteController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping("/productos")
    public String mostrarReporteProductos(Model model) {
        List<ReporteProductoDTO> reportes = ventaService.obtenerReporteVentas();
        long usuariosUnicos = ventaService.contarUsuariosUnicos();

        model.addAttribute("reportes", reportes);
        model.addAttribute("usuariosUnicos", usuariosUnicos);

        return "reportes/productos";
    }
}