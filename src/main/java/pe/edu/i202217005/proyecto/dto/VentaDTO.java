package pe.edu.i202217005.proyecto.dto;

import java.util.List;

public class VentaDTO {
    private Long clienteId;
    private List<DetalleVentaDTO> detalles;

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<DetalleVentaDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentaDTO> detalles) {
        this.detalles = detalles;
    }
}