package pe.uch.nigosha.core.session;

/**
 * Identidad compartida del prototipo. Reemplazar por una sesión autenticada antes de producción.
 */
public final class ClienteSession {
  private ClienteSession() {}

  public static String clienteId() {
    return "CLI_DEMO";
  }
}
