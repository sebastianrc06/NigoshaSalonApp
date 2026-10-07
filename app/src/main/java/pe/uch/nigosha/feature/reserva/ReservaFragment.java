package pe.uch.nigosha.feature.reserva;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import pe.uch.nigosha.R;
import pe.uch.nigosha.core.ui.UiState;
import pe.uch.nigosha.core.util.InputValidator;
import pe.uch.nigosha.core.util.MoneyFormatter;
import pe.uch.nigosha.domain.models.Servicio;

public final class ReservaFragment extends Fragment {

    private static final TimeZone LIMA =
            TimeZone.getTimeZone("America/Lima");

    private ReservaViewModel model;
    private ReservaSubmitViewModel submit;

    private TextInputEditText nombre;
    private TextInputEditText telefono;

    private TextWatcher nombreWatcher;
    private TextWatcher telefonoWatcher;

    private RecyclerView horarios;
    private HorarioAdapter adapter;

    private ScrollView scrollReserva;

    private View pantalla;

    private final Runnable refrescar =
            new Runnable() {

                @Override
                public void run() {

                    if (pantalla != null) {

                        model.refreshSlots();

                        pantalla.postDelayed(
                                this,
                                30000
                        );
                    }
                }
            };

    public ReservaFragment() {
        super(R.layout.fragment_reserva);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {

        super.onViewCreated(
                view,
                savedInstanceState
        );

        pantalla = view;

        /*
         * ---------------------------------------------------------
         * PROTECCIÓN DEL BOTÓN INFERIOR
         * ---------------------------------------------------------
         *
         * El BottomNavigationView normal está oculto durante
         * Reserva.
         *
         * Por eso esta pantalla necesita proteger su propio botón
         * de la barra de navegación física/gestual del teléfono.
         *
         * Ejemplo Samsung:
         *
         *      |||       O       <
         *
         * El padding se aplica únicamente a este contenedor.
         * No modifica Inicio, Servicios, Mis citas ni El salón.
         */
        View contenedorBoton =
                view.findViewById(
                        R.id.contenedorBotonReserva
                );

        ViewCompat.setOnApplyWindowInsetsListener(
                contenedorBoton,
                (v, windowInsets) -> {

                    Insets navigationBars =
                            windowInsets.getInsets(
                                    WindowInsetsCompat.Type.navigationBars()
                            );

                    v.setPadding(
                            v.getPaddingLeft(),
                            v.getPaddingTop(),
                            v.getPaddingRight(),
                            navigationBars.bottom
                    );

                    return windowInsets;
                }
        );

        ViewCompat.requestApplyInsets(
                contenedorBoton
        );

        model =
                ReservaFlow
                        .provider(this)
                        .get(
                                ReservaViewModel.class
                        );

        submit =
                ReservaFlow
                        .provider(this)
                        .get(
                                ReservaSubmitViewModel.class
                        );

        if (submit.isSaved()) {
            submit.nuevaSolicitud();
        }

        /*
         * Scroll principal de la pantalla.
         */
        scrollReserva =
                view.findViewById(
                        R.id.scrollReserva
                );

        configurarDatosPersonales(view);

        configurarHorarios(view);

        configurarBotones(view);

        observarServicio(view);

        observarAgenda(view);

        observarHorarios(view);

        model.load(
                requireArguments()
                        .getString("servicioId")
        );
    }

    /*
     * ------------------------------------------------------------
     * DATOS PERSONALES
     * ------------------------------------------------------------
     */

    private void configurarDatosPersonales(
            View view
    ) {

        nombre =
                view.findViewById(
                        R.id.etNombreReserva
                );

        telefono =
                view.findViewById(
                        R.id.etTelefonoReserva
                );

        nombre.setText(
                model.getNombre()
        );

        telefono.setText(
                model.getTelefono()
        );

        nombreWatcher =
                watcher(true);

        telefonoWatcher =
                watcher(false);

        nombre.addTextChangedListener(
                nombreWatcher
        );

        telefono.addTextChangedListener(
                telefonoWatcher
        );

        /*
         * Al abrirse el teclado movemos el formulario
         * para mantener visible el campo Nombre.
         */
        nombre.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

                        desplazarAlCampo(
                                v
                        );
                    }
                }
        );

        /*
         * Lo mismo para el campo Celular.
         */
        telefono.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

                        desplazarAlCampo(
                                v
                        );
                    }
                }
        );

        /*
         * El botón "Siguiente" del teclado pasa
         * automáticamente de Nombre a Celular.
         */
        nombre.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId
                            == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) {

                        telefono.requestFocus();

                        desplazarAlCampo(
                                telefono
                        );

                        return true;
                    }

                    return false;
                }
        );

        /*
         * "Realizado" cierra el teclado numérico.
         */
        telefono.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId
                            == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {

                        android.view.inputmethod.InputMethodManager imm =
                                (android.view.inputmethod.InputMethodManager)
                                        requireContext()
                                                .getSystemService(
                                                        android.content.Context.INPUT_METHOD_SERVICE
                                                );

                        imm.hideSoftInputFromWindow(
                                telefono.getWindowToken(),
                                0
                        );

                        telefono.clearFocus();

                        return true;
                    }

                    return false;
                }
        );
    }

    /*
     * ------------------------------------------------------------
     * HORARIOS
     * ------------------------------------------------------------
     */

    private void configurarHorarios(
            View view
    ) {

        horarios =
                view.findViewById(
                        R.id.rvHorariosReserva
                );

        horarios.setLayoutManager(
                new GridLayoutManager(
                        requireContext(),
                        3
                )
        );

        horarios.setNestedScrollingEnabled(
                false
        );

        adapter =
                new HorarioAdapter(
                        start -> {

                            model.selectStart(
                                    start
                            );

                            model.refreshSlots();
                        }
                );

        horarios.setAdapter(
                adapter
        );
    }

    /*
     * ------------------------------------------------------------
     * BOTONES
     * ------------------------------------------------------------
     */

    private void configurarBotones(
            View view
    ) {

        view.findViewById(
                        R.id.btnVolverReserva
                )
                .setOnClickListener(
                        v ->
                                NavHostFragment
                                        .findNavController(this)
                                        .popBackStack()
                );

        view.findViewById(
                        R.id.btnElegirFechaReserva
                )
                .setOnClickListener(
                        v -> elegirFecha()
                );

        view.findViewById(
                        R.id.btnReintentarReserva
                )
                .setOnClickListener(
                        v -> {

                            model.reload();

                            model.retryAgenda();
                        }
                );

        view.findViewById(
                        R.id.btnRevisarReserva
                )
                .setOnClickListener(
                        v -> revisar()
                );
    }

    /*
     * ------------------------------------------------------------
     * OBSERVAR SERVICIO
     * ------------------------------------------------------------
     */

    private void observarServicio(
            View view
    ) {

        model.getServiceState()
                .observe(
                        getViewLifecycleOwner(),
                        state -> {

                            if (state == null) {
                                return;
                            }

                            boolean ok =
                                    state.status
                                            == UiState.Status.SUCCESS;

                            view.findViewById(
                                            R.id.progressReserva
                                    )
                                    .setVisibility(
                                            state.status
                                                    == UiState.Status.LOADING
                                                    ? View.VISIBLE
                                                    : View.GONE
                                    );

                            view.findViewById(
                                            R.id.contenidoReserva
                                    )
                                    .setVisibility(
                                            ok
                                                    ? View.VISIBLE
                                                    : View.GONE
                                    );

                            TextView error =
                                    view.findViewById(
                                            R.id.tvErrorReserva
                                    );

                            error.setVisibility(
                                    state.status
                                            == UiState.Status.ERROR
                                            ? View.VISIBLE
                                            : View.GONE
                            );

                            error.setText(
                                    state.message
                            );

                            if (ok) {

                                Servicio servicio =
                                        state.data;

                                TextView nombreServicio =
                                        view.findViewById(
                                                R.id.tvReservaServicio
                                        );

                                nombreServicio.setText(
                                        servicio.nombre
                                );

                                TextView importes =
                                        view.findViewById(
                                                R.id.tvReservaImportes
                                        );

                                importes.setText(
                                        servicio.duracionMinutos
                                                + " minutos · "
                                                + MoneyFormatter.format(
                                                servicio.precio
                                        )
                                                + "\nAdelanto requerido (50%): "
                                                + MoneyFormatter.format(
                                                MoneyFormatter.adelanto(
                                                        servicio.precio
                                                )
                                        )
                                );
                            }

                            controles();
                        }
                );
    }

    /*
     * ------------------------------------------------------------
     * OBSERVAR AGENDA
     * ------------------------------------------------------------
     */

    private void observarAgenda(
            View view
    ) {

        model.getAgendaMessage()
                .observe(
                        getViewLifecycleOwner(),
                        message -> {

                            TextView mensaje =
                                    view.findViewById(
                                            R.id.tvMensajeHorarios
                                    );

                            mensaje.setText(
                                    message
                            );

                            controles();
                        }
                );
    }

    /*
     * ------------------------------------------------------------
     * OBSERVAR HORARIOS
     * ------------------------------------------------------------
     */

    private void observarHorarios(
            View view
    ) {

        model.getSlots()
                .observe(
                        getViewLifecycleOwner(),
                        values -> {

                            List<HorarioAdapter.Horario> items =
                                    new ArrayList<>();

                            if (values != null) {

                                for (Long start : values) {

                                    items.add(
                                            new HorarioAdapter.Horario(
                                                    start,

                                                    model.isAvailable(
                                                            start
                                                    ),

                                                    start.equals(
                                                            model.getSelectedStart()
                                                    )
                                            )
                                    );
                                }
                            }

                            if (adapter != null) {

                                adapter.submit(
                                        items
                                );
                            }

                            MaterialButton fecha =
                                    view.findViewById(
                                            R.id.btnElegirFechaReserva
                                    );

                            fecha.setText(
                                    model.getDateLabel()
                            );

                            controles();
                        }
                );
    }

    /*
     * ------------------------------------------------------------
     * ESTADO DE CONTROLES
     * ------------------------------------------------------------
     */

    private void controles() {

        if (pantalla == null) {
            return;
        }

        pantalla.findViewById(
                        R.id.btnRevisarReserva
                )
                .setEnabled(
                        model.canSubmit()
                                && !submit.isBusy()
                );

        UiState<Servicio> state =
                model.getServiceState()
                        .getValue();

        boolean error =
                state != null
                        && state.status
                        == UiState.Status.ERROR;

        pantalla.findViewById(
                        R.id.btnReintentarReserva
                )
                .setVisibility(
                        error
                                || model.isAgendaFailed()
                                ? View.VISIBLE
                                : View.GONE
                );
    }

    /*
     * ------------------------------------------------------------
     * TEXT WATCHER
     * ------------------------------------------------------------
     */

    private TextWatcher watcher(
            boolean esNombre
    ) {

        return new TextWatcher() {

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

                if (esNombre) {

                    model.setNombre(
                            s.toString()
                    );

                } else {

                    model.setTelefono(
                            s.toString()
                    );
                }
            }

            @Override
            public void afterTextChanged(
                    Editable editable
            ) {
            }
        };
    }

    /*
     * ------------------------------------------------------------
     * CALENDARIO MATERIAL
     * ------------------------------------------------------------
     */

    private void elegirFecha() {

        Calendar hoyLima =
                Calendar.getInstance(
                        LIMA
                );

        hoyLima.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        hoyLima.set(
                Calendar.MINUTE,
                0
        );

        hoyLima.set(
                Calendar.SECOND,
                0
        );

        hoyLima.set(
                Calendar.MILLISECOND,
                0
        );

        Calendar inicioUtc =
                Calendar.getInstance(
                        TimeZone.getTimeZone(
                                "UTC"
                        )
                );

        inicioUtc.clear();

        inicioUtc.set(
                hoyLima.get(
                        Calendar.YEAR
                ),

                hoyLima.get(
                        Calendar.MONTH
                ),

                hoyLima.get(
                        Calendar.DAY_OF_MONTH
                )
        );

        long fechaMinima =
                inicioUtc.getTimeInMillis();

        CalendarConstraints.Builder restricciones =
                new CalendarConstraints.Builder()
                        .setStart(
                                fechaMinima
                        )
                        .setValidator(
                                DateValidatorPointForward.from(
                                        fechaMinima
                                )
                        );

        MaterialDatePicker.Builder<Long> builder =
                MaterialDatePicker
                        .Builder
                        .datePicker();

        builder.setTitleText(
                "Elige el día de tu visita"
        );

        builder.setPositiveButtonText(
                "Seleccionar"
        );

        builder.setNegativeButtonText(
                "Cancelar"
        );

        builder.setCalendarConstraints(
                restricciones.build()
        );

        /*
         * Si ya existe una fecha seleccionada,
         * abrimos el calendario sobre esa fecha.
         */
        Calendar diaActual =
                model.getPickerDay();

        if (diaActual != null) {

            Calendar seleccionUtc =
                    Calendar.getInstance(
                            TimeZone.getTimeZone(
                                    "UTC"
                            )
                    );

            seleccionUtc.clear();

            seleccionUtc.set(
                    diaActual.get(
                            Calendar.YEAR
                    ),

                    diaActual.get(
                            Calendar.MONTH
                    ),

                    diaActual.get(
                            Calendar.DAY_OF_MONTH
                    )
            );

            if (seleccionUtc.getTimeInMillis()
                    >= fechaMinima) {

                builder.setSelection(
                        seleccionUtc
                                .getTimeInMillis()
                );
            }
        }

        MaterialDatePicker<Long> picker =
                builder.build();

        picker.addOnPositiveButtonClickListener(
                selection -> {

                    if (selection == null) {
                        return;
                    }

                    Calendar seleccionado =
                            Calendar.getInstance(
                                    TimeZone.getTimeZone(
                                            "UTC"
                                    )
                            );

                    seleccionado.setTimeInMillis(
                            selection
                    );

                    int year =
                            seleccionado.get(
                                    Calendar.YEAR
                            );

                    int month =
                            seleccionado.get(
                                    Calendar.MONTH
                            );

                    int day =
                            seleccionado.get(
                                    Calendar.DAY_OF_MONTH
                            );

                    /*
                     * Nigosha no atiende domingos.
                     */
                    if (seleccionado.get(
                            Calendar.DAY_OF_WEEK
                    ) == Calendar.SUNDAY) {

                        if (pantalla != null) {

                            TextView mensaje =
                                    pantalla.findViewById(
                                            R.id.tvMensajeHorarios
                                    );

                            mensaje.setText(
                                    "Los domingos no atendemos. "
                                            + "Selecciona otro día."
                            );
                        }

                        return;
                    }

                    model.selectDay(
                            year,
                            month,
                            day
                    );
                }
        );

        picker.show(
                getParentFragmentManager(),
                "NIGOSHA_DATE_PICKER"
        );
    }

    /*
     * ------------------------------------------------------------
     * DESPLAZAMIENTO CUANDO APARECE EL TECLADO
     * ------------------------------------------------------------
     */

    private void desplazarAlCampo(
            View campo
    ) {

        if (pantalla == null
                || campo == null
                || scrollReserva == null) {

            return;
        }

        /*
         * Samsung y otros fabricantes tardan unos milisegundos
         * en terminar la animación del teclado.
         *
         * Esperamos para calcular la posición con la pantalla
         * ya redimensionada.
         */
        scrollReserva.postDelayed(
                () -> {

                    if (pantalla == null
                            || scrollReserva == null
                            || campo == null) {

                        return;
                    }

                    int[] posicionCampo =
                            new int[2];

                    campo.getLocationOnScreen(
                            posicionCampo
                    );

                    int[] posicionScroll =
                            new int[2];

                    scrollReserva.getLocationOnScreen(
                            posicionScroll
                    );

                    /*
                     * Dejamos espacio encima del campo para
                     * mantener visible también "Tus datos".
                     */
                    int destino =
                            scrollReserva.getScrollY()
                                    + posicionCampo[1]
                                    - posicionScroll[1]
                                    - 120;

                    scrollReserva.smoothScrollTo(
                            0,
                            Math.max(
                                    0,
                                    destino
                            )
                    );

                },
                350
        );
    }

    /*
     * ------------------------------------------------------------
     * REVISAR RESERVA
     * ------------------------------------------------------------
     */

    private void revisar() {

        if (pantalla == null) {
            return;
        }

        TextInputLayout inputNombre =
                pantalla.findViewById(
                        R.id.inputNombreReserva
                );

        TextInputLayout inputTelefono =
                pantalla.findViewById(
                        R.id.inputTelefonoReserva
                );

        boolean nameOk =
                InputValidator.nombreValido(
                        model.getNombre()
                );

        boolean phoneOk =
                InputValidator.celularValido(
                        model.getTelefono()
                );

        inputNombre.setError(
                nameOk
                        ? null
                        : "Escribe tu nombre y apellido "
                        + "(3 a 100 caracteres)."
        );

        inputTelefono.setError(
                phoneOk
                        ? null
                        : "Escribe un celular peruano "
                        + "de 9 dígitos."
        );

        if (!nameOk
                || !phoneOk) {

            if (!nameOk) {

                nombre.requestFocus();

                desplazarAlCampo(
                        nombre
                );

            } else {

                telefono.requestFocus();

                desplazarAlCampo(
                        telefono
                );
            }

            return;
        }

        model.refreshSlots();

        if (!model.canSubmit()) {
            return;
        }

        Servicio service =
                model.getServicio();

        Bundle args =
                new Bundle();

        args.putString(
                "servicioId",
                service.id
        );

        args.putString(
                "nombreServicio",
                service.nombre
        );

        args.putDouble(
                "precio",
                service.precio
        );

        args.putInt(
                "duracion",
                service.duracionMinutos
        );

        args.putLong(
                "inicio",
                model.getSelectedStart()
        );

        args.putString(
                "cliente",
                model.getNombre()
                        .trim()
        );

        args.putString(
                "telefono",
                InputValidator.celular(
                        model.getTelefono()
                )
        );

        NavHostFragment
                .findNavController(this)
                .navigate(
                        R.id.action_reserva_resumen,
                        args
                );
    }

    /*
     * ------------------------------------------------------------
     * CICLO DE VIDA
     * ------------------------------------------------------------
     */

    @Override
    public void onResume() {

        super.onResume();

        if (pantalla != null) {

            pantalla.removeCallbacks(
                    refrescar
            );

            pantalla.post(
                    refrescar
            );
        }
    }

    @Override
    public void onPause() {

        if (pantalla != null) {

            pantalla.removeCallbacks(
                    refrescar
            );
        }

        super.onPause();
    }

    @Override
    public void onDestroyView() {

        if (pantalla != null) {

            pantalla.removeCallbacks(
                    refrescar
            );
        }

        if (nombre != null
                && nombreWatcher != null) {

            nombre.removeTextChangedListener(
                    nombreWatcher
            );

            nombre.setOnFocusChangeListener(
                    null
            );

            nombre.setOnEditorActionListener(
                    null
            );
        }

        if (telefono != null
                && telefonoWatcher != null) {

            telefono.removeTextChangedListener(
                    telefonoWatcher
            );

            telefono.setOnFocusChangeListener(
                    null
            );

            telefono.setOnEditorActionListener(
                    null
            );
        }

        if (horarios != null) {

            horarios.setAdapter(
                    null
            );
        }

        pantalla = null;

        scrollReserva = null;

        horarios = null;
        adapter = null;

        nombre = null;
        telefono = null;

        nombreWatcher = null;
        telefonoWatcher = null;

        super.onDestroyView();
    }
}