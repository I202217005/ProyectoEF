package pe.edu.i202217005.proyecto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.i202217005.proyecto.dto.ProductoDTO;
import pe.edu.i202217005.proyecto.entity.Producto;
import pe.edu.i202217005.proyecto.service.ProductoService;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/productos")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/lista")
    public String listarProductos(Model model,
                                  @RequestParam(required = false) String buscar,
                                  @RequestParam(required = false) String filtroStock) {
        List<Producto> productos;

        // Aplicar búsqueda si existe
        if (buscar != null && !buscar.isEmpty()) {
            productos = productoService.buscarProductosPorNombre(buscar);
        } else {
            productos = productoService.listarProductos();
        }

        // Aplicar filtro de stock
        if (filtroStock != null) {
            switch (filtroStock) {
                case "low":
                    productos = productos.stream()
                            .filter(p -> p.getStock() > 0 && p.getStock() <= 5)
                            .collect(Collectors.toList());
                    break;
                case "out":
                    productos = productos.stream()
                            .filter(p -> p.getStock() == 0)
                            .collect(Collectors.toList());
                    break;
                case "available":
                    productos = productos.stream()
                            .filter(p -> p.getStock() > 5)
                            .collect(Collectors.toList());
                    break;
            }
        }

        model.addAttribute("productos", productos);
        model.addAttribute("buscar", buscar);
        model.addAttribute("filtroStock", filtroStock);

        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new ProductoDTO());
        return "productos/form";
    }
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        try {
            Producto producto = productoService.obtenerProducto(id);
            ProductoDTO productoDTO = convertirADTO(producto);
            model.addAttribute("producto", productoDTO);
            return "productos/form";
        } catch (Exception e) {
            return "redirect:/productos/lista";
        }
    }

    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute ProductoDTO productoDTO,
                                  @RequestParam(required = false) MultipartFile imagenFile,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Manejar la imagen nueva si se proporciona
            if (imagenFile != null && !imagenFile.isEmpty()) {
                // Validar tamaño de la imagen (por ejemplo, máximo 2MB)
                if (imagenFile.getSize() > 2 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("error", "La imagen no debe superar los 2MB");
                    return "redirect:/productos/form";
                }

                // Convertir a Base64
                byte[] bytes = imagenFile.getBytes();
                String base64Image = Base64.getEncoder().encodeToString(bytes);
                productoDTO.setImagen(base64Image);
            }

            // Guardar o actualizar producto
            if (productoDTO.getId() != null) {
                productoService.actualizarProducto(productoDTO.getId(), productoDTO);
                redirectAttributes.addFlashAttribute("success", "Producto actualizado correctamente");
            } else {
                productoService.crearProducto(productoDTO);
                redirectAttributes.addFlashAttribute("success", "Producto creado correctamente");
            }

            return "redirect:/productos/lista";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar el producto: " + e.getMessage());
            return "redirect:/productos/form";
        }
    }
    private ProductoDTO convertirADTO(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());

        // Manejar la imagen con cuidado para evitar problemas de memoria
        if (producto.getImagen() != null && producto.getImagen().length() < 100000) {
            dto.setImagen(producto.getImagen());
        }

        return dto;
    }
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("success", "Producto eliminado correctamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos/lista";
    }
}