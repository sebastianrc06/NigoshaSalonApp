package pe.uch.nigosha.feature.cliente.reserva;

import android.os.Bundle;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import pe.uch.nigosha.R;
import pe.uch.nigosha.core.ui.*;
import pe.uch.nigosha.feature.cliente.citas.CitaViews;
import pe.uch.nigosha.feature.cliente.citas.DetalleCitaViewModel;

public final class ResultadoReservaFragment extends Fragment {
  public ResultadoReservaFragment() {
    super(R.layout.fragment_resultado_reserva);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
    super.onViewCreated(view, saved);
    DetalleCitaViewModel model = new ViewModelProvider(this).get(DetalleCitaViewModel.class);
    view.findViewById(R.id.btnReintentarEstado).setOnClickListener(v -> model.reload());
    view.findViewById(R.id.btnCitasResultado).setOnClickListener(v -> salir(true));
    view.findViewById(R.id.btnServiciosResultado).setOnClickListener(v -> salir(false));
    view.findViewById(R.id.btnAdelantoResultado)
        .setOnClickListener(
            v -> {
              Bundle args = new Bundle();
              args.putString("citaId", requireArguments().getString("citaId"));
              NavHostFragment.findNavController(this).navigate(R.id.pagoFragment, args);
            });
    requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(
            getViewLifecycleOwner(),
            new OnBackPressedCallback(true) {
              @Override
              public void handleOnBackPressed() {
                salir(true);
              }
            });
    model
        .getState()
        .observe(
            getViewLifecycleOwner(),
            state -> {
              StateViews.render(view, state, R.id.contenidoResultado);
              if (state != null && state.status == UiState.Status.SUCCESS)
                CitaViews.bind(view, state.data);
            });
    model.load(requireArguments().getString("citaId"));
  }

  private void salir(boolean citas) {
    NavHostFragment.findNavController(this).popBackStack(R.id.catalogoListaFragment, false);
    if (citas)
      ((BottomNavigationView) requireActivity().findViewById(R.id.clienteBottomNavigation))
          .setSelectedItemId(R.id.misCitasFragment);
  }
}
