package pe.edu.i202217005.proyecto.dto;

import pe.edu.i202217005.proyecto.entity.Producto;

import java.math.BigDecimal;
import java.util.List;

public class ReporteProductoDTO {
    private Producto producto;
    private long totalVentas;
    private long cantidadVendida;
    private BigDecimal ingresoTotal;
    private List<VentaUsuarioDTO> ultimasVentas;

    // Getters y setters...

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public long getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(long totalVentas) {
        this.totalVentas = totalVentas;
    }

    public long getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(long cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public BigDecimal getIngresoTotal() {
        return ingresoTotal;
    }

    public void setIngresoTotal(BigDecimal ingresoTotal) {
        this.ingresoTotal = ingresoTotal;
    }

    public List<VentaUsuarioDTO> getUltimasVentas() {
        return ultimasVentas;
    }

    public void setUltimasVentas(List<VentaUsuarioDTO> ultimasVentas) {
        this.ultimasVentas = ultimasVentas;
    }
}