package pe.edu.i202217005.proyecto.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {

        // Validación de credenciales
        if (validarCredenciales(username, password)) {
            session.setAttribute("usuario", username);

            // Determinar el rol y redirigir según corresponda
            String rol = obtenerRol(username);
            session.setAttribute("rol", rol);

            if ("ROLE_ADMIN".equals(rol)) {
                return "redirect:/productos/lista";
            } else if ("ROLE_USER".equals(rol)) {
                return "redirect:/public/catalogo";
            }
        }

        redirectAttributes.addFlashAttribute("error", "Usuario o contraseña incorrectos");
        return "redirect:/login?error";
    }

    private boolean validarCredenciales(String username, String password) {
        // Validación simple (en producción esto debería consultar la base de datos)
        return ("admin".equals(username) && "admin".equals(password)) ||
                ("user".equals(username) && "user".equals(password));
    }

    private String obtenerRol(String username) {
        // Asignación simple de roles (en producción esto vendría de la base de datos)
        if ("admin".equals(username)) {
            return "ROLE_ADMIN";
        } else {
            return "ROLE_USER";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}