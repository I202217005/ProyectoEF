package pe.edu.i202217005.proyecto.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.i202217005.proyecto.dto.CompraDTO;
import pe.edu.i202217005.proyecto.entity.Producto;
import pe.edu.i202217005.proyecto.service.ProductoService;
import pe.edu.i202217005.proyecto.service.VentaService;

import java.util.Map;

@Controller
@RequestMapping("/public")
public class PublicController {

    private final ProductoService productoService;
    private final VentaService ventaService;

    public PublicController(ProductoService productoService, VentaService ventaService) {
        this.productoService = productoService;
        this.ventaService = ventaService;
    }

    @GetMapping("/catalogo")
    public String mostrarCatalogo(Model model) {
        model.addAttribute("productos", productoService.listarProductosDisponibles());
        return "public/catalogo";
    }

    @GetMapping("/comprar/{id}")
    public String mostrarFormularioCompra(@PathVariable Long id, Model model) {
        Producto producto = productoService.obtenerProducto(id);
        // Verificar stock antes de mostrar el formulario de compra
        if (producto.getStock() <= 0) {
            return "redirect:/public/catalogo";
        }
        model.addAttribute("producto", producto);
        return "public/compra";
    }

    @PostMapping("/procesar-compra")
    @ResponseBody  // Para responder al Ajax
    public ResponseEntity<?> procesarCompra(@ModelAttribute CompraDTO compraDTO, HttpServletRequest request) {
        try {
            ventaService.procesarCompraPublica(compraDTO);
            return ResponseEntity.ok().body(Map.of("message", "Compra exitosa"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    }