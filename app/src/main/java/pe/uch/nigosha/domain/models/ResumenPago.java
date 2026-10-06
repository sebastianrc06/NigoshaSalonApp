package pe.uch.nigosha.domain.models;

import pe.uch.nigosha.core.util.InputValidator;
import pe.uch.nigosha.core.util.MoneyFormatter;

public final class ResumenPago {
  public final Cita cita;
  public final String total, requerido, abonado, saldo;

  public ResumenPago(Cita cita) {
    this.cita = cita;
    total = dinero(cita.precio);
    requerido = dinero(cita.requerido());
    abonado = cita.adelantoPagado == null ? "Por verificar" : dinero(cita.adelantoPagado);
    saldo =
        cita.adelantoPagado != null
                && InputValidator.importeValido(cita.precio)
                && InputValidator.importeValido(cita.adelantoPagado)
                && cita.adelantoPagado <= cita.precio
            ? dinero(cita.precio - cita.adelantoPagado)
            : "Por verificar";
  }

  private static String dinero(double value) {
    return InputValidator.importeValido(value) ? MoneyFormatter.format(value) : "Por verificar";
  }
}
