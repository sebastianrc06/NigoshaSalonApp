package pe.uch.nigosha.core.util;

public final class InputValidator {
  private InputValidator() {}

  public static String celular(String value) {
    return value == null ? "" : value.replaceAll("[\\s()-]", "");
  }

  public static boolean nombreValido(String value) {
    return value != null && value.trim().length() >= 3 && value.trim().length() <= 100;
  }

  public static boolean celularValido(String value) {
    return celular(value).matches("9\\d{8}");
  }

  public static boolean idValido(String value) {
    return value != null
        && !value.trim().isEmpty()
        && !value.matches(".*[.#$\\[\\]/\\x00-\\x1F\\x7F].*");
  }

  public static boolean importeValido(double value) {
    return Double.isFinite(value) && value >= 0;
  }
}
