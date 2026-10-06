package pe.uch.nigosha.feature.citas;

import android.view.View;
import android.widget.TextView;
import pe.uch.nigosha.R;
import pe.uch.nigosha.core.util.DateTimeFormatter;
import pe.uch.nigosha.domain.models.*;

public final class CitaViews {
  private CitaViews() {}

  public static void bind(View view, Cita cita) {
    ResumenPago pago = new ResumenPago(cita);
    text(
        view,
        R.id.tvDetalleServicio,
        DateTimeFormatter.texto(cita.servicioNombre, "Servicio por verificar"));
    text(
        view,
        R.id.tvDetalleFecha,
        cita.inicioOrden() > 0
            ? DateTimeFormatter.format(cita.inicioOrden(), "EEEE d 'de' MMMM · HH:mm")
            : "Fecha y hora por verificar");
    text(view, R.id.tvDetalleEstado, "Reserva: " + DateTimeFormatter.estado(cita.estado));
    text(view, R.id.tvDetalleCliente, DateTimeFormatter.texto(cita.clienteNombre, "Por verificar"));
    text(view, R.id.tvDetalleTelefono, DateTimeFormatter.texto(cita.telefono, "Por verificar"));
    text(
        view,
        R.id.tvDetalleFin,
        cita.finMillis != null && cita.finMillis > cita.inicioOrden()
            ? DateTimeFormatter.format(cita.finMillis, "dd/MM/yyyy · HH:mm")
            : "Por verificar");
    text(view, R.id.tvDetalleCodigo, cita.id);
    text(view, R.id.tvDetalleTotal, pago.total);
    text(view, R.id.tvDetalleRequerido, pago.requerido);
    text(view, R.id.tvDetalleAbonado, pago.abonado);
    text(view, R.id.tvDetalleSaldo, pago.saldo);
    text(view, R.id.tvDetallePago, DateTimeFormatter.estado(cita.estadoPago));
  }

  private static void text(View view, int id, String value) {
    ((TextView) view.findViewById(id)).setText(value);
  }
}
