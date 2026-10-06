package pe.uch.nigosha.feature.citas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import pe.uch.nigosha.R;
import pe.uch.nigosha.core.ui.UiState;
import pe.uch.nigosha.domain.models.Cita;

public class MisCitasFragment extends Fragment {

  private MisCitasViewModel viewModel;
  private RecyclerView recycler;
  private CitaAdapter adapter;

  public MisCitasFragment() {
    super(R.layout.fragment_mis_citas);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    viewModel = new ViewModelProvider(this).get(MisCitasViewModel.class);

    recycler = view.findViewById(R.id.rvMisCitas);
    recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

    adapter = new CitaAdapter(this::mostrarDetalle);
    recycler.setAdapter(adapter);

    MaterialButtonToggleGroup toggle = view.findViewById(R.id.toggleMisCitas);

    toggle.check(viewModel.isHistorial() ? R.id.btnCitasHistorial : R.id.btnCitasProximas);

    toggle.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (isChecked) {
            viewModel.selectHistorial(checkedId == R.id.btnCitasHistorial);
          }
        });

    view.findViewById(R.id.btnReintentarMisCitas).setOnClickListener(v -> viewModel.reload());

    viewModel
        .getState()
        .observe(
            getViewLifecycleOwner(),
            state -> {
              if (state == null) {
                return;
              }

              boolean success = state.status == UiState.Status.SUCCESS;
              boolean error = state.status == UiState.Status.ERROR;

              view.findViewById(R.id.progressMisCitas)
                  .setVisibility(state.status == UiState.Status.LOADING ? View.VISIBLE : View.GONE);

              recycler.setVisibility(success ? View.VISIBLE : View.GONE);

              view.findViewById(R.id.btnReintentarMisCitas)
                  .setVisibility(error ? View.VISIBLE : View.GONE);

              TextView message = view.findViewById(R.id.tvMensajeMisCitas);

              message.setVisibility(View.GONE);

              if (error) {
                message.setText(state.message);
                message.setVisibility(View.VISIBLE);
              }

              if (success) {
                adapter.submit(state.data);

                if (state.data.isEmpty()) {
                  message.setText(
                      viewModel.isHistorial()
                          ? "Todavía no tienes citas " + "en el historial."
                          : "No tienes próximas citas. "
                              + "Explora Servicios para "
                              + "solicitar una.");

                  message.setVisibility(View.VISIBLE);
                }
              }
            });

    viewModel
        .getOperation()
        .observe(
            getViewLifecycleOwner(),
            operation -> {
              if (operation == null) {
                return;
              }

              TextView message = view.findViewById(R.id.tvOperacionMisCitas);

              message.setVisibility(View.VISIBLE);
              message.setText(
                  operation.status == UiState.Status.LOADING
                      ? "Cancelando solicitud..."
                      : operation.status == UiState.Status.ERROR
                          ? operation.message
                          : operation.data);
            });
  }

  @Override
  public void onResume() {
    super.onResume();

    if (viewModel != null) {
      viewModel.refresh();
    }
  }

  private void mostrarDetalle(Cita cita) {
    Bundle args = new Bundle();
    args.putString("citaId", cita.id);
    androidx.navigation.fragment.NavHostFragment.findNavController(this)
        .navigate(R.id.detalleCitaFragment, args);
  }

  @Override
  public void onDestroyView() {
    recycler.setAdapter(null);
    recycler = null;
    adapter = null;

    super.onDestroyView();
  }
}
