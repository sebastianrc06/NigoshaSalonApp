package pe.uch.nigosha.feature.auth.registro;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import pe.uch.nigosha.BuildConfig;
import pe.uch.nigosha.R;
import pe.uch.nigosha.data.api.dni.DniApiClient;
import pe.uch.nigosha.data.api.dni.DniResponse;
import pe.uch.nigosha.domain.models.Usuario;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroActivity extends AppCompatActivity {

    // =========================================================
    // INPUT LAYOUTS
    // =========================================================

    private TextInputLayout inputDni;
    private TextInputLayout inputNombres;
    private TextInputLayout inputApellidoPaterno;
    private TextInputLayout inputApellidoMaterno;
    private TextInputLayout inputTelefono;
    private TextInputLayout inputCorreo;
    private TextInputLayout inputPassword;
    private TextInputLayout inputConfirmarPassword;


    // =========================================================
    // EDIT TEXT
    // =========================================================

    private TextInputEditText etDni;
    private TextInputEditText etNombres;
    private TextInputEditText etApellidoPaterno;
    private TextInputEditText etApellidoMaterno;
    private TextInputEditText etTelefono;
    private TextInputEditText etCorreo;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmarPassword;


    // =========================================================
    // BOTONES
    // =========================================================

    private MaterialButton btnVerificarDni;
    private MaterialButton btnCrearCuenta;
    private MaterialButton btnVolverLogin;


    // =========================================================
    // ESTADO DNI
    // =========================================================

    private ProgressBar progressDni;
    private TextView tvEstadoDni;

    private boolean dniVerificado = false;
    private String ultimoDniVerificado = "";

    private Call<DniResponse> llamadaDni;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usuariosRef;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registro);

        inicializarFirebase();

        enlazarVistas();

        configurarEventos();

        bloquearDatosReniec();
    }


    // =========================================================
    // FIREBASE
    // =========================================================

    private void inicializarFirebase() {

        firebaseAuth = FirebaseAuth.getInstance();

        usuariosRef = FirebaseDatabase
                .getInstance()
                .getReference("usuarios");
    }


    // =========================================================
    // ENLAZAR VISTAS
    // =========================================================

    private void enlazarVistas() {

        inputDni =
                findViewById(R.id.inputDniRegistro);

        inputNombres =
                findViewById(R.id.inputNombresRegistro);

        inputApellidoPaterno =
                findViewById(R.id.inputApellidoPaternoRegistro);

        inputApellidoMaterno =
                findViewById(R.id.inputApellidoMaternoRegistro);

        inputTelefono =
                findViewById(R.id.inputTelefonoRegistro);

        inputCorreo =
                findViewById(R.id.inputCorreoRegistro);

        inputPassword =
                findViewById(R.id.inputPasswordRegistro);

        inputConfirmarPassword =
                findViewById(R.id.inputConfirmarPasswordRegistro);


        etDni =
                findViewById(R.id.etDniRegistro);

        etNombres =
                findViewById(R.id.etNombresRegistro);

        etApellidoPaterno =
                findViewById(R.id.etApellidoPaternoRegistro);

        etApellidoMaterno =
                findViewById(R.id.etApellidoMaternoRegistro);

        etTelefono =
                findViewById(R.id.etTelefonoRegistro);

        etCorreo =
                findViewById(R.id.etCorreoRegistro);

        etPassword =
                findViewById(R.id.etPasswordRegistro);

        etConfirmarPassword =
                findViewById(R.id.etConfirmarPasswordRegistro);


        btnVerificarDni =
                findViewById(R.id.btnVerificarDni);

        btnCrearCuenta =
                findViewById(R.id.btnCrearCuenta);

        btnVolverLogin =
                findViewById(R.id.btnVolverLogin);


        progressDni =
                findViewById(R.id.progressDni);

        tvEstadoDni =
                findViewById(R.id.tvEstadoDni);
    }


    // =========================================================
    // EVENTOS
    // =========================================================

    private void configurarEventos() {

        btnVolverLogin.setOnClickListener(
                v -> finish()
        );


        btnVerificarDni.setOnClickListener(
                v -> consultarDni()
        );


        btnCrearCuenta.setOnClickListener(
                v -> validarRegistro()
        );


        etDni.setOnFocusChangeListener(
                (v, tieneFoco) -> {

                    if (tieneFoco) {
                        invalidarDniSiCambio();
                    }
                }
        );


        etDni.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId == EditorInfo.IME_ACTION_DONE) {

                        consultarDni();

                        return true;
                    }

                    return false;
                }
        );
    }


    // =========================================================
    // BLOQUEAR DATOS OBTENIDOS DE API
    // =========================================================

    private void bloquearDatosReniec() {

        etNombres.setEnabled(false);

        etApellidoPaterno.setEnabled(false);

        etApellidoMaterno.setEnabled(false);
    }


    // =========================================================
    // CONSULTAR DNI
    // =========================================================

    private void consultarDni() {

        inputDni.setError(null);

        String dni = texto(etDni);


        // -----------------------------------------------------
        // VALIDAR DNI
        // -----------------------------------------------------

        if (!dni.matches("\\d{8}")) {

            inputDni.setError(
                    "El DNI debe tener exactamente 8 dígitos"
            );

            return;
        }


        // -----------------------------------------------------
        // VALIDAR TOKEN
        // -----------------------------------------------------

        if (BuildConfig.APISPERU_TOKEN == null
                || BuildConfig.APISPERU_TOKEN
                .trim()
                .isEmpty()) {

            Toast.makeText(
                    this,
                    "No se encontró la configuración de APIs Perú",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        limpiarIdentidad();

        mostrarCargandoDni(true);

        tvEstadoDni.setVisibility(View.VISIBLE);

        tvEstadoDni.setText(
                "Consultando DNI..."
        );


        // -----------------------------------------------------
        // TOKEN
        // -----------------------------------------------------

        String authorization =
                "Bearer "
                        + BuildConfig.APISPERU_TOKEN.trim();


        // -----------------------------------------------------
        // LLAMADA API
        // -----------------------------------------------------

        llamadaDni = DniApiClient
                .getService()
                .consultarDni(
                        authorization,
                        dni
                );


        llamadaDni.enqueue(

                new Callback<DniResponse>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<DniResponse> call,
                            @NonNull Response<DniResponse> response
                    ) {

                        if (isFinishing()
                                || isDestroyed()) {

                            return;
                        }


                        mostrarCargandoDni(false);


                        // -------------------------------------
                        // ERROR HTTP
                        // -------------------------------------

                        if (!response.isSuccessful()) {

                            mostrarErrorDni(
                                    "No se pudo verificar el DNI. Código "
                                            + response.code()
                            );

                            return;
                        }


                        // -------------------------------------
                        // RESPUESTA
                        // -------------------------------------

                        DniResponse persona =
                                response.body();


                        if (persona == null
                                || TextUtils.isEmpty(
                                persona.getNombres()
                        )) {

                            mostrarErrorDni(
                                    "No se encontraron datos para este DNI"
                            );

                            return;
                        }


                        // -------------------------------------
                        // MOSTRAR DATOS
                        // -------------------------------------

                        etNombres.setText(
                                persona.getNombres()
                        );

                        etApellidoPaterno.setText(
                                persona.getApellidoPaterno()
                        );

                        etApellidoMaterno.setText(
                                persona.getApellidoMaterno()
                        );


                        // -------------------------------------
                        // DNI VERIFICADO
                        // -------------------------------------

                        dniVerificado = true;

                        ultimoDniVerificado = dni;


                        tvEstadoDni.setVisibility(
                                View.VISIBLE
                        );

                        tvEstadoDni.setText(
                                "✓ DNI verificado correctamente"
                        );


                        // -------------------------------------
                        // SIGUIENTE CAMPO
                        // -------------------------------------

                        etTelefono.requestFocus();
                    }


                    @Override
                    public void onFailure(
                            @NonNull Call<DniResponse> call,
                            @NonNull Throwable throwable
                    ) {

                        if (call.isCanceled()
                                || isFinishing()
                                || isDestroyed()) {

                            return;
                        }


                        mostrarCargandoDni(false);


                        mostrarErrorDni(
                                "No pudimos conectar con el servicio de DNI"
                        );
                    }
                }
        );
    }


    // =========================================================
    // VALIDAR FORMULARIO
    // =========================================================

    private void validarRegistro() {

        limpiarErrores();

        invalidarDniSiCambio();


        String dni =
                texto(etDni);

        String telefono =
                texto(etTelefono);

        String correo =
                texto(etCorreo);

        String password =
                texto(etPassword);

        String confirmar =
                texto(etConfirmarPassword);


        boolean valido = true;


        // -----------------------------------------------------
        // DNI
        // -----------------------------------------------------

        if (!dni.matches("\\d{8}")) {

            inputDni.setError(
                    "Ingresa un DNI válido"
            );

            valido = false;

        } else if (!dniVerificado) {

            inputDni.setError(
                    "Primero verifica el DNI"
            );

            valido = false;
        }


        // -----------------------------------------------------
        // TELÉFONO
        // -----------------------------------------------------

        if (!telefono.matches("\\d{9}")) {

            inputTelefono.setError(
                    "Ingresa un celular de 9 dígitos"
            );

            valido = false;
        }


        // -----------------------------------------------------
        // CORREO
        // -----------------------------------------------------

        if (TextUtils.isEmpty(correo)
                || !android.util.Patterns.EMAIL_ADDRESS
                .matcher(correo)
                .matches()) {

            inputCorreo.setError(
                    "Ingresa un correo válido"
            );

            valido = false;
        }


        // -----------------------------------------------------
        // CONTRASEÑA
        // -----------------------------------------------------

        if (password.length() < 6) {

            inputPassword.setError(
                    "Usa al menos 6 caracteres"
            );

            valido = false;
        }


        // -----------------------------------------------------
        // CONFIRMAR CONTRASEÑA
        // -----------------------------------------------------

        if (!password.equals(confirmar)) {

            inputConfirmarPassword.setError(
                    "Las contraseñas no coinciden"
            );

            valido = false;
        }


        if (!valido) {
            return;
        }


        // -----------------------------------------------------
        // CREAR CUENTA
        // -----------------------------------------------------

        crearCuentaFirebase(
                dni,
                telefono,
                correo,
                password
        );
    }


    // =========================================================
    // CREAR CUENTA FIREBASE AUTH
    // =========================================================

    private void crearCuentaFirebase(
            String dni,
            String telefono,
            String correo,
            String password
    ) {

        String nombres =
                texto(etNombres);

        String apellidoPaterno =
                texto(etApellidoPaterno);

        String apellidoMaterno =
                texto(etApellidoMaterno);


        /*
         * Firebase Authentication requiere un email cuando
         * usamos Email/Password.
         *
         * El cliente NO utilizará este correo técnico.
         *
         * LOGIN:
         *
         * DNI
         * +
         * contraseña
         *
         * Internamente:
         *
         * DNI@nigosha.local
         */

        String emailAuth =
                dni + "@nigosha.local";


        mostrarCreandoCuenta(true);


        firebaseAuth
                .createUserWithEmailAndPassword(
                        emailAuth,
                        password
                )
                .addOnCompleteListener(
                        this,
                        task -> {

                            if (isFinishing()
                                    || isDestroyed()) {

                                return;
                            }


                            // =================================
                            // ERROR CREANDO AUTH
                            // =================================

                            if (!task.isSuccessful()) {

                                mostrarCreandoCuenta(false);


                                if (task.getException()
                                        instanceof FirebaseAuthUserCollisionException) {

                                    inputDni.setError(
                                            "Este DNI ya tiene una cuenta registrada"
                                    );

                                    etDni.requestFocus();

                                    return;
                                }


                                String mensaje =
                                        "No se pudo crear la cuenta";


                                if (task.getException() != null
                                        && task.getException()
                                        .getMessage() != null) {

                                    mensaje =
                                            task
                                                    .getException()
                                                    .getMessage();
                                }


                                Toast.makeText(
                                        this,
                                        mensaje,
                                        Toast.LENGTH_LONG
                                ).show();


                                return;
                            }


                            // =================================
                            // OBTENER USUARIO FIREBASE
                            // =================================

                            FirebaseUser firebaseUser =
                                    firebaseAuth
                                            .getCurrentUser();


                            if (firebaseUser == null) {

                                mostrarCreandoCuenta(false);


                                Toast.makeText(
                                        this,
                                        "No se pudo obtener el usuario creado",
                                        Toast.LENGTH_LONG
                                ).show();


                                return;
                            }


                            String uid =
                                    firebaseUser.getUid();


                            // =================================
                            // CREAR PERFIL
                            // =================================

                            Usuario usuario =
                                    new Usuario(
                                            uid,
                                            dni,
                                            nombres,
                                            apellidoPaterno,
                                            apellidoMaterno,
                                            correo,
                                            telefono,
                                            "CLIENTE",
                                            true,
                                            System.currentTimeMillis()
                                    );


                            // =================================
                            // GUARDAR PERFIL
                            // =================================

                            guardarUsuarioFirebase(
                                    firebaseUser,
                                    usuario
                            );
                        }
                );
    }


    // =========================================================
    // GUARDAR USUARIO EN REALTIME DATABASE
    // =========================================================

    private void guardarUsuarioFirebase(
            FirebaseUser firebaseUser,
            Usuario usuario
    ) {

        String uid =
                firebaseUser.getUid();


        usuariosRef
                .child(uid)
                .setValue(usuario)
                .addOnCompleteListener(
                        task -> {

                            if (isFinishing()
                                    || isDestroyed()) {

                                return;
                            }


                            // =================================
                            // PERFIL GUARDADO
                            // =================================

                            if (task.isSuccessful()) {

                                mostrarCreandoCuenta(false);


                                /*
                                 * IMPORTANTE:
                                 *
                                 * createUserWithEmailAndPassword()
                                 * deja al usuario autenticado.
                                 *
                                 * Nosotros queremos que después
                                 * del registro vuelva al Login.
                                 */

                                firebaseAuth.signOut();


                                Toast.makeText(
                                        this,
                                        "Cuenta creada correctamente. Ya puedes iniciar sesión.",
                                        Toast.LENGTH_LONG
                                ).show();


                                finish();

                                return;
                            }


                            // =================================
                            // ERROR GUARDANDO PERFIL
                            // =================================

                            /*
                             * No queremos dejar una cuenta huérfana:
                             *
                             * Authentication = creada
                             * Database = NO creada
                             *
                             * Por eso intentamos eliminar el usuario
                             * de Authentication.
                             */

                            firebaseUser
                                    .delete()
                                    .addOnCompleteListener(
                                            deleteTask -> {

                                                firebaseAuth.signOut();

                                                mostrarCreandoCuenta(false);


                                                Toast.makeText(
                                                        this,
                                                        "No se pudo guardar el perfil. La cuenta no fue completada. Inténtalo nuevamente.",
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                );
    }


    // =========================================================
    // ESTADO CREANDO CUENTA
    // =========================================================

    private void mostrarCreandoCuenta(
            boolean creando
    ) {

        btnCrearCuenta.setEnabled(
                !creando
        );

        btnVerificarDni.setEnabled(
                !creando
        );

        btnVolverLogin.setEnabled(
                !creando
        );


        etDni.setEnabled(
                !creando
        );

        etTelefono.setEnabled(
                !creando
        );

        etCorreo.setEnabled(
                !creando
        );

        etPassword.setEnabled(
                !creando
        );

        etConfirmarPassword.setEnabled(
                !creando
        );


        if (creando) {

            btnCrearCuenta.setText(
                    "Creando cuenta..."
            );

        } else {

            btnCrearCuenta.setText(
                    "Crear cuenta"
            );
        }
    }


    // =========================================================
    // INVALIDAR DNI SI CAMBIÓ
    // =========================================================

    private void invalidarDniSiCambio() {

        String dniActual =
                texto(etDni);


        if (dniVerificado
                && !dniActual.equals(
                ultimoDniVerificado
        )) {

            limpiarIdentidad();
        }
    }


    // =========================================================
    // LIMPIAR IDENTIDAD
    // =========================================================

    private void limpiarIdentidad() {

        dniVerificado = false;

        ultimoDniVerificado = "";


        etNombres.setText("");

        etApellidoPaterno.setText("");

        etApellidoMaterno.setText("");


        tvEstadoDni.setVisibility(
                View.GONE
        );
    }


    // =========================================================
    // ERROR DNI
    // =========================================================

    private void mostrarErrorDni(
            String mensaje
    ) {

        limpiarIdentidad();


        tvEstadoDni.setVisibility(
                View.VISIBLE
        );


        tvEstadoDni.setText(
                mensaje
        );
    }


    // =========================================================
    // CARGANDO DNI
    // =========================================================

    private void mostrarCargandoDni(
            boolean cargando
    ) {

        progressDni.setVisibility(
                cargando
                        ? View.VISIBLE
                        : View.GONE
        );


        btnVerificarDni.setEnabled(
                !cargando
        );


        etDni.setEnabled(
                !cargando
        );
    }


    // =========================================================
    // LIMPIAR ERRORES
    // =========================================================

    private void limpiarErrores() {

        inputDni.setError(null);

        inputTelefono.setError(null);

        inputCorreo.setError(null);

        inputPassword.setError(null);

        inputConfirmarPassword.setError(null);
    }


    // =========================================================
    // OBTENER TEXTO
    // =========================================================

    private String texto(
            TextInputEditText editText
    ) {

        if (editText.getText() == null) {

            return "";
        }


        return editText
                .getText()
                .toString()
                .trim();
    }


    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        if (llamadaDni != null) {

            llamadaDni.cancel();
        }


        super.onDestroy();
    }
}