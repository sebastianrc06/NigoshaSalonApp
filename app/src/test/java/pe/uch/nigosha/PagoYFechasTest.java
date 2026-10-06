package pe.uch.nigosha;

import static org.junit.Assert.*;

import org.junit.Test;
import pe.uch.nigosha.core.util.*;
import pe.uch.nigosha.domain.models.*;

public class PagoYFechasTest {
  @Test
  public void adelantoRequeridoNoEquivaleAPagado() {
    Cita cita = new Cita();
    cita.precio = 100;
    cita.adelanto = 50;
    ResumenPago resumen = new ResumenPago(cita);
    assertEquals("S/ 50.00", resumen.requerido);
    assertEquals("Por verificar", resumen.abonado);
    assertEquals("Por verificar", resumen.saldo);
    cita.adelantoPagado = 0.0;
    resumen = new ResumenPago(cita);
    assertEquals("S/ 100.00", resumen.saldo);
  }

  @Test
  public void importeInvalidoNoSePresentaComoPagado() {
    Cita cita = new Cita();
    cita.precio = 100;
    cita.adelantoPagado = Double.NaN;
    assertEquals("Por verificar", new ResumenPago(cita).abonado);
    cita.adelantoPagado = 150.0;
    assertEquals("Por verificar", new ResumenPago(cita).saldo);
  }

  @Test
  public void redondeaAdelantoACentimos() {
    assertEquals(22.51, MoneyFormatter.adelanto(45.01), 0.00001);
  }

  @Test
  public void interpretaFechasEnLimaSinAceptarFechasImposibles() {
    assertNull(DateTimeFormatter.parse("31/02/2030", "dd/MM/yyyy"));
    Long value = DateTimeFormatter.parse("07/01/2030 10:00", "dd/MM/yyyy HH:mm");
    assertNotNull(value);
    assertEquals("07/01/2030 10:00", DateTimeFormatter.format(value, "dd/MM/yyyy HH:mm"));
  }

  @Test
  public void citaCanceladaNoEsProximaYCitaAntiguaNoSeCancelaSinDatos() {
    Cita cita = new Cita();
    cita.inicioMillis = Long.MAX_VALUE - 60000;
    cita.finMillis = Long.MAX_VALUE;
    cita.estado = "CANCELADA";
    assertFalse(cita.proxima(0));
    cita.estado = "PENDIENTE";
    cita.estadoPago = "PENDIENTE";
    cita.adelantoPagado = 0.0;
    assertTrue(cita.cancelable(0));
    cita.inicioMillis = null;
    assertFalse(cita.cancelable(0));
  }

  @Test
  public void validaTelefonoEIdentificadoresFirebase() {
    assertTrue(InputValidator.celularValido("990 938 752"));
    assertFalse(InputValidator.celularValido("123456789"));
    assertFalse(InputValidator.idValido("citas/otra"));
    assertTrue(InputValidator.idValido("solicitud-123"));
  }
}
