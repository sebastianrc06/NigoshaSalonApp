package pe.uch.nigosha.models;

public class Cita {
    public String id;
    public String clienteId;
    public String clienteNombre;
    public String servicioNombre;
    public double precio;
    public double adelanto;
    public String fecha;
    public String hora;
    public String estado; // "PENDIENTE", "CONFIRMADA", "ATENDIDA"

    public Cita() { }

    public Cita(String id, String clienteId, String clienteNombre, String servicioNombre, double precio, double adelanto, String fecha, String hora, String estado) {
        this.id = id;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.servicioNombre = servicioNombre;
        this.precio = precio;
        this.adelanto = adelanto;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
    }
}