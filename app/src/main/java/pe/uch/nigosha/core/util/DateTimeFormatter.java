package pe.uch.nigosha.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public final class DateTimeFormatter {
  private DateTimeFormatter() {}

  public static TimeZone zona() {
    return TimeZone.getTimeZone("America/Lima");
  }

  public static String format(long value, String pattern) {
    SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.forLanguageTag("es-PE"));
    formatter.setTimeZone(zona());
    return formatter.format(value);
  }

  public static Long parse(String value, String pattern) {
    if (value == null) return null;
    SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.ROOT);
    formatter.setTimeZone(zona());
    formatter.setLenient(false);
    try {
      java.util.Date date = formatter.parse(value);
      return date != null && formatter.format(date).equals(value) ? date.getTime() : null;
    } catch (ParseException exception) {
      return null;
    }
  }

  public static String texto(String value, String fallback) {
    return value == null || value.trim().isEmpty() ? fallback : value.trim();
  }

  public static String estado(String value) {
    return texto(value, "Por verificar").replace('_', ' ');
  }
}
