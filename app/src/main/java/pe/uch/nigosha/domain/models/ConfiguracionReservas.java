package pe.uch.nigosha.domain.models;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Horario del prototipo; pendiente de validación por la dueña. Agenda con capacidad de una
 * atención.
 */
public final class ConfiguracionReservas {
  public static final int APERTURA = 9;
  public static final int CIERRE = 18;
  public static final int PASO_MINUTOS = 30;
  public static final long ANTICIPACION = 30L * 60L * 1000L;
  public static final String ZONA = "America/Lima";

  private ConfiguracionReservas() {}

  public static boolean seCruzan(long inicio, long fin, long otroInicio, long otroFin) {
    return inicio < otroFin && fin > otroInicio;
  }

  public static boolean jornadaValida(long inicio, long fin, long ahora) {
    if (fin <= inicio || inicio < ahora + ANTICIPACION) return false;
    Calendar start = Calendar.getInstance(TimeZone.getTimeZone(ZONA));
    start.setTimeInMillis(inicio);
    if (start.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
        || start.get(Calendar.MINUTE) % PASO_MINUTOS != 0
        || start.get(Calendar.SECOND) != 0
        || start.get(Calendar.MILLISECOND) != 0) return false;
    Calendar open = (Calendar) start.clone();
    open.set(Calendar.HOUR_OF_DAY, APERTURA);
    open.set(Calendar.MINUTE, 0);
    Calendar close = (Calendar) open.clone();
    close.set(Calendar.HOUR_OF_DAY, CIERRE);
    return inicio >= open.getTimeInMillis() && fin <= close.getTimeInMillis();
  }
}
