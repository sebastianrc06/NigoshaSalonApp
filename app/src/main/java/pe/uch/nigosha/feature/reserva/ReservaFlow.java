package pe.uch.nigosha.feature.reserva;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.fragment.NavHostFragment;
import pe.uch.nigosha.R;

/** Borrador y envío compartidos solamente durante el flujo del catálogo. */
public final class ReservaFlow {
  private ReservaFlow() {}

  public static ViewModelProvider provider(Fragment fragment) {
    NavBackStackEntry entry =
        NavHostFragment.findNavController(fragment).getBackStackEntry(R.id.catalogoFragment);
    return new ViewModelProvider(entry);
  }
}
