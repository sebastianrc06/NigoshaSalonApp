package pe.uch.nigosha.feature.cliente.catalogo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import pe.uch.nigosha.R;
import pe.uch.nigosha.core.ui.UiState;

public class CatalogoFragment extends Fragment {

    private CatalogoViewModel viewModel;
    private ServicioAdapter adapter;

    private ChipGroup chips;
    private TextInputEditText search;
    private TextWatcher watcher;
    private RecyclerView recycler;

    private TextView message;
    private TextView results;

    private View loading;
    private View retry;

    private List<String> renderedCategories = new ArrayList<>();
    private String renderedSelection;
    private boolean rebuilding;

    public CatalogoFragment() {
        super(R.layout.fragment_catalogo);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this)
                .get(CatalogoViewModel.class);

        MaterialButtonToggleGroup tipos =
                view.findViewById(R.id.toggleTiposCatalogo);

        tipos.check(
                viewModel.getPaquetes()
                        ? R.id.btnTipoPaquetes
                        : R.id.btnTipoServicios
        );

        tipos.addOnButtonCheckedListener((group, id, checked) -> {
            if (checked) {
                viewModel.setPaquetes(id == R.id.btnTipoPaquetes);
            }
        });

        chips = view.findViewById(R.id.chipsCategorias);
        search = view.findViewById(R.id.etBuscarServicio);
        recycler = view.findViewById(R.id.rvCatalogo);
        message = view.findViewById(R.id.tvMensajeCatalogo);
        results = view.findViewById(R.id.tvResultadosCatalogo);
        loading = view.findViewById(R.id.progressCatalogo);
        retry = view.findViewById(R.id.btnReintentarCatalogo);

        adapter = new ServicioAdapter(servicio -> {
            Bundle arguments = new Bundle();
            arguments.putString("servicioId", servicio.id);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_catalogo_detalle, arguments);
        });

        recycler.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        recycler.setAdapter(adapter);

        search.setText(viewModel.getSearch());

        watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {
                viewModel.setSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        search.addTextChangedListener(watcher);
        retry.setOnClickListener(v -> viewModel.reload());

        chips.setOnCheckedStateChangeListener((group, ids) -> {
            if (rebuilding || ids.isEmpty()) {
                return;
            }

            Chip selected = group.findViewById(ids.get(0));

            if (selected != null) {
                viewModel.setCategory((String) selected.getTag());
            }
        });

        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) {
                return;
            }

            boolean success = state.status == UiState.Status.SUCCESS;
            boolean error = state.status == UiState.Status.ERROR;

            loading.setVisibility(
                    state.status == UiState.Status.LOADING
                            ? View.VISIBLE
                            : View.GONE
            );

            recycler.setVisibility(success ? View.VISIBLE : View.GONE);
            retry.setVisibility(error ? View.VISIBLE : View.GONE);
            message.setVisibility(View.GONE);
            results.setText("");

            if (error) {
                message.setText(state.message);
                message.setVisibility(View.VISIBLE);
            }

            if (success) {
                renderCategories();
                adapter.submit(state.data);

                int count = state.data.size();

                results.setText(
                        count + (
                                viewModel.getPaquetes()
                                        ? (count == 1 ? " paquete" : " paquetes")
                                        : (count == 1 ? " servicio" : " servicios")
                        )
                );

                if (count == 0) {
                    message.setText(
                            viewModel.getCategories().isEmpty()
                                    ? "Todavía no hay servicios publicados."
                                    : "No encontramos servicios. "
                                    + "Prueba otra búsqueda o categoría."
                    );

                    message.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void renderCategories() {
        List<String> categories = viewModel.getCategories();
        String selection = viewModel.getCategory();

        if (renderedCategories.equals(categories)
                && selection.equals(renderedSelection)) {
            return;
        }

        rebuilding = true;
        chips.removeAllViews();

        addCategory("Todos", "", selection);

        for (String category : categories) {
            addCategory(category, category, selection);
        }

        renderedCategories = new ArrayList<>(categories);
        renderedSelection = selection;
        rebuilding = false;
    }

    private void addCategory(
            String label,
            String value,
            String selectedValue
    ) {
        Chip chip = new Chip(requireContext());

        chip.setId(View.generateViewId());
        chip.setText(label);
        chip.setTag(value);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(false);
        chip.setEnsureMinTouchTargetSize(true);

        chips.addView(chip);

        if (value.equals(selectedValue)) {
            chip.setChecked(true);
        }
    }

    @Override
    public void onDestroyView() {
        search.removeTextChangedListener(watcher);
        recycler.setAdapter(null);

        adapter = null;
        chips = null;
        search = null;
        watcher = null;
        recycler = null;
        message = null;
        results = null;
        loading = null;
        retry = null;

        renderedCategories = new ArrayList<>();
        renderedSelection = null;

        super.onDestroyView();
    }
}