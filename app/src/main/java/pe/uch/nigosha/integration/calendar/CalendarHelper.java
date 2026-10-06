package pe.uch.nigosha.integration.calendar;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.widget.Toast;
import pe.uch.nigosha.domain.models.Cita;
import pe.uch.nigosha.domain.models.ConfiguracionReservas;

/**
 * Abre el editor del calendario; el usuario elige si guarda. Sin sincronización ni permisos de
 * escritura.
 */
public final class CalendarHelper {
  private CalendarHelper() {}

  public static boolean disponible(Cita cita) {
    return cita != null
        && "CONFIRMADA".equalsIgnoreCase(cita.estado)
        && cita.inicioMillis != null
        && cita.inicioMillis > System.currentTimeMillis()
        && cita.finMillis != null
        && cita.finMillis > cita.inicioMillis;
  }

  public static void agregar(Context context, Cita cita, String direccion) {
    if (!disponible(cita)) {
      Toast.makeText(
              context,
              "El calendario está disponible para citas futuras confirmadas.",
              Toast.LENGTH_SHORT)
          .show();
      return;
    }
    Intent intent =
        new Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, "Nigosha · " + cita.servicioNombre)
            .putExtra(CalendarContract.Events.EVENT_LOCATION, direccion)
            .putExtra(
                CalendarContract.Events.DESCRIPTION,
                "Cita confirmada. Código: "
                    + cita.id
                    + ". Consulta cualquier cambio en Nigosha; este evento no se actualiza"
                    + " automáticamente.")
            .putExtra(CalendarContract.Events.EVENT_TIMEZONE, ConfiguracionReservas.ZONA)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cita.inicioMillis.longValue())
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cita.finMillis.longValue());
    try {
      context.startActivity(intent);
    } catch (ActivityNotFoundException e) {
      Toast.makeText(context, "No se encontró una aplicación de calendario.", Toast.LENGTH_SHORT)
          .show();
    }
  }
}
