package pe.edu.i202217005.proyecto.dto;

import pe.edu.i202217005.proyecto.entity.Usuario;

import java.math.BigDecimal;
import java.time.LocalDate;

public class VentaUsuarioDTO {
    private Usuario usuario;
    private LocalDate fecha;
    private int cantidad;
    private BigDecimal total;



    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

}