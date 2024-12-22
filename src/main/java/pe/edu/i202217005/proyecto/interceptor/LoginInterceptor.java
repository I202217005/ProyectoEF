package pe.edu.i202217005.proyecto.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String requestURI = request.getRequestURI();

        // Permitir acceso público
        if (requestURI.startsWith("/login") ||
                requestURI.startsWith("/css") ||
                requestURI.startsWith("/js") ||
                requestURI.startsWith("/public")) {
            return true;
        }

        // Verificar sesión
        HttpSession session = request.getSession();
        if (session.getAttribute("usuario") == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Verificar permisos según rol
        String rol = (String) session.getAttribute("rol");
        if ("ROLE_ADMIN".equals(rol)) {
            // Administrador tiene acceso a todo
            return true;
        } else if ("ROLE_USER".equals(rol)) {
            // Usuario solo tiene acceso a rutas públicas y específicas
            if (requestURI.startsWith("/productos/") ||
                    requestURI.startsWith("/ventas/")) {
                response.sendRedirect("/public/catalogo");
                return false;
            }
        }

        return true;
    }
}