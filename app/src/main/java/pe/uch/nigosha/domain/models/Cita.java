package pe.uch.nigosha.domain.models;

import com.google.firebase.database.IgnoreExtraProperties;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

@IgnoreExtraProperties
public class Cita {

  public String id;
  public String clienteId;
  public String clienteNombre;
  public String servicioNombre;

  public double precio;
  public double adelanto;

  public String fecha;
  public String hora;
  public String estado;

  public String servicioId;
  public String telefono;
  public String zonaHoraria;
  public String estadoPago;

  public Long inicioMillis;
  public Long finMillis;
  public Long creadaEn;
  public Long canceladaEn;

  public int duracionMinutos;

  public Double adelantoRequerido;
  public Double adelantoPagado;
  public Double saldoPendiente;

  public Cita() {}

  public Cita(
      String id,
      String clienteId,
      String clienteNombre,
      String servicioNombre,
      double precio,
      double adelanto,
      String fecha,
      String hora,
      String estado) {
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

  public long inicioOrden() {
    if (inicioMillis != null && inicioMillis > 0) {
      return inicioMillis;
    }

    if (fecha == null || hora == null) {
      return 0;
    }

    String value = fecha + " " + hora;

    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ROOT);

    formatter.setTimeZone(TimeZone.getTimeZone("America/Lima"));
    formatter.setLenient(false);

    try {
      java.util.Date date = formatter.parse(value);

      return date != null && formatter.format(date).equals(value) ? date.getTime() : 0;

    } catch (java.text.ParseException exception) {
      return 0;
    }
  }

  public boolean proxima(long now) {
    boolean active =
        "PENDIENTE".equalsIgnoreCase(estado)
            || "CONFIRMADA".equalsIgnoreCase(estado)
            || "EN_ATENCION".equalsIgnoreCase(estado);

    long until = finMillis != null && finMillis > inicioOrden() ? finMillis : inicioOrden();

    return active && until > now;
  }

  public boolean cancelable(long now) {
    return inicioMillis != null
        && inicioMillis > now
        && "PENDIENTE".equalsIgnoreCase(estado)
        && "PENDIENTE".equalsIgnoreCase(estadoPago)
        && adelantoPagado != null
        && adelantoPagado == 0.0;
  }

  public double requerido() {
    return adelantoRequerido != null ? adelantoRequerido : adelanto;
  }
}
