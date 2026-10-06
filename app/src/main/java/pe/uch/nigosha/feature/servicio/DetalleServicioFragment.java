package pe.uch.nigosha.feature.servicio;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import pe.uch.nigosha.R;
import pe.uch.nigosha.core.ui.UiState;
import pe.uch.nigosha.core.util.MoneyFormatter;
import pe.uch.nigosha.domain.models.Servicio;

public class DetalleServicioFragment extends Fragment {

    private Servicio selected;

    public DetalleServicioFragment() {
        super(R.layout.fragment_detalle_servicio);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        DetalleServicioViewModel viewModel =
                new ViewModelProvider(this)
                        .get(DetalleServicioViewModel.class);

        view.findViewById(R.id.btnVolverServicio)
                .setOnClickListener(
                        v -> NavHostFragment.findNavController(this)
                                .popBackStack()
                );

        view.findViewById(R.id.btnReintentarDetalle)
                .setOnClickListener(v -> viewModel.retry());

        view.findViewById(R.id.btnReservarDetalle)
                .setOnClickListener(v -> {
                    if (selected == null) {
                        return;
                    }

                    if (!selected.sePuedeReservar()) {
                        String text =
                                "Hola, quisiera consultar "
                                        + selected.nombre
                                        + ". ¿Podrían confirmar precio, "
                                        + "duración y disponibilidad?";

                        Intent intent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        "https://wa.me/51990938752?text="
                                                + Uri.encode(text)
                                )
                        );

                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException exception) {
                            Toast.makeText(
                                    requireContext(),
                                    "No hay una aplicación disponible "
                                            + "para abrir el enlace",
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                        return;
                    }

                    Bundle arguments = new Bundle();
                    arguments.putString("servicioId", selected.id);

                    NavHostFragment.findNavController(this)
                            .navigate(
                                    R.id.action_detalle_reserva,
                                    arguments
                            );
                });

        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) {
                return;
            }

            selected = null;

            boolean success = state.status == UiState.Status.SUCCESS;
            boolean error = state.status == UiState.Status.ERROR;

            view.findViewById(R.id.progressDetalleServicio)
                    .setVisibility(
                            state.status == UiState.Status.LOADING
                                    ? View.VISIBLE
                                    : View.GONE
                    );

            view.findViewById(R.id.contenidoDetalleServicio)
                    .setVisibility(
                            success ? View.VISIBLE : View.GONE
                    );

            view.findViewById(R.id.btnReintentarDetalle)
                    .setVisibility(
                            error ? View.VISIBLE : View.GONE
                    );

            TextView errorText =
                    view.findViewById(R.id.tvErrorDetalleServicio);

            errorText.setVisibility(
                    error ? View.VISIBLE : View.GONE
            );

            if (error) {
                errorText.setText(state.message);
            }

            if (success) {
                selected = state.data;
                bind(view, selected);
            }
        });

        Bundle arguments = getArguments();

        viewModel.load(
                arguments == null
                        ? null
                        : arguments.getString("servicioId")
        );
    }

    private void bind(View view, Servicio servicio) {
        setText(view, R.id.tvDetalleNombre, servicio.nombre);
        setText(view, R.id.tvDetalleCategoria, servicio.categoria);

        String description = servicio.descripcion;

        setText(
                view,
                R.id.tvDetalleDescripcion,
                description == null || description.trim().isEmpty()
                        ? "Consulta con el salón los detalles de este servicio."
                        : description.trim()
        );

        setText(
                view,
                R.id.tvDetalleDuracion,
                servicio.duracionMinutos > 0
                        ? "Duración estimada: "
                        + servicio.duracionMinutos + " minutos"
                        : "Duración por confirmar con el salón"
        );

        setText(
                view,
                R.id.btnReservarDetalle,
                servicio.sePuedeReservar()
                        ? "Elegir fecha y hora"
                        : "Consultar disponibilidad"
        );

        double deposit = MoneyFormatter.adelanto(servicio.precio);

        setText(
                view,
                R.id.tvDetallePrecio,
                servicio.precioPorConfirmar
                        ? "Precio por confirmar"
                        : (servicio.precioDesde
                        ? "Precio desde: "
                        : "Precio total: ")
                        + MoneyFormatter.format(servicio.precio)
        );

        setText(
                view,
                R.id.tvDetalleAdelanto,
                servicio.precioPorConfirmar
                        ? "Adelanto: se calculará al confirmar el precio"
                        : "Adelanto requerido (50%): "
                        + MoneyFormatter.format(deposit)
        );

        setText(
                view,
                R.id.tvDetalleSaldo,
                servicio.precioPorConfirmar
                        ? "Consulta el presupuesto con el salón"
                        : "Saldo restante: "
                        + MoneyFormatter.format(servicio.precio - deposit)
        );
    }

    private void setText(View view, int id, String text) {
        ((TextView) view.findViewById(id)).setText(text);
    }

    @Override
    public void onDestroyView() {
        selected = null;
        super.onDestroyView();
    }
}