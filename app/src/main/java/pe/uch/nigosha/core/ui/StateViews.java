package pe.uch.nigosha.core.ui;

import android.view.View;
import android.widget.TextView;
import pe.uch.nigosha.R;

public final class StateViews {
  private StateViews() {}

  public static void render(View root, UiState<?> state, int contenido) {
    boolean ok = state != null && state.status == UiState.Status.SUCCESS;
    boolean error = state != null && state.status == UiState.Status.ERROR;
    root.findViewById(R.id.estadoCarga)
        .setVisibility(
            state == null || state.status == UiState.Status.LOADING ? View.VISIBLE : View.GONE);
    root.findViewById(R.id.estadoError).setVisibility(error ? View.VISIBLE : View.GONE);
    root.findViewById(contenido).setVisibility(ok ? View.VISIBLE : View.GONE);
    ((TextView) root.findViewById(R.id.tvErrorEstado)).setText(error ? state.message : "");
  }
}
