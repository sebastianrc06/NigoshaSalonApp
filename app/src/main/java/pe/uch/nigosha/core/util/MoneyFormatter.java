package pe.uch.nigosha.core.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class MoneyFormatter {

  private MoneyFormatter() {}

  public static String format(double amount) {
    return String.format(Locale.forLanguageTag("es-PE"), "S/ %.2f", amount);
  }

  public static double adelanto(double precio) {
    return BigDecimal.valueOf(precio)
        .multiply(new BigDecimal("0.50"))
        .setScale(2, RoundingMode.HALF_UP)
        .doubleValue();
  }
}
