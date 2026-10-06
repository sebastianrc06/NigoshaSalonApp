package pe.uch.nigosha;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Test;
import pe.uch.nigosha.domain.models.ConfiguracionReservas;

public class ReservaRulesTest {
  private long time(int day, int hour, int minute) {
    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/Lima"));
    c.clear();
    c.set(2030, 0, day, hour, minute);
    return c.getTimeInMillis();
  }

  @Test
  public void aceptaServicioQueTerminaExactamenteAlCerrar() {
    long start = time(7, 17, 0);
    assertTrue(ConfiguracionReservas.jornadaValida(start, time(7, 18, 0), start - 3600000));
  }

  @Test
  public void rechazaServicioQueSuperaElCierre() {
    long start = time(7, 17, 30);
    assertFalse(ConfiguracionReservas.jornadaValida(start, time(7, 18, 30), start - 3600000));
  }

  @Test
  public void rechazaDomingoYHoraFueraDeLaGrilla() {
    long domingo = time(6, 10, 0), fuera = time(7, 10, 15);
    assertFalse(ConfiguracionReservas.jornadaValida(domingo, domingo + 3600000, domingo - 3600000));
    assertFalse(ConfiguracionReservas.jornadaValida(fuera, fuera + 3600000, fuera - 3600000));
  }

  @Test
  public void respetaAnticipacionMinima() {
    long start = time(7, 10, 0);
    assertTrue(ConfiguracionReservas.jornadaValida(start, start + 3600000, start - 1800000));
    assertFalse(ConfiguracionReservas.jornadaValida(start, start + 3600000, start - 1799999));
  }

  @Test
  public void consecutivasNoSeCruzanPeroSolapadasSi() {
    assertFalse(ConfiguracionReservas.seCruzan(100, 200, 200, 300));
    assertTrue(ConfiguracionReservas.seCruzan(100, 201, 200, 300));
    assertTrue(ConfiguracionReservas.seCruzan(100, 400, 200, 300));
  }

  @Test
  public void rechazaAntesDeAbrirYDuracionInvalida() {
    long start = time(7, 8, 30);
    assertFalse(ConfiguracionReservas.jornadaValida(start, start + 3600000, start - 3600000));
    start = time(7, 10, 0);
    assertFalse(ConfiguracionReservas.jornadaValida(start, start, start - 3600000));
  }
}
