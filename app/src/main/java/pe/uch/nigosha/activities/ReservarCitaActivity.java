package pe.uch.nigosha.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;
import pe.uch.nigosha.R;
import pe.uch.nigosha.models.Cita;
import pe.uch.nigosha.models.Servicio;

public class ReservarCitaActivity extends AppCompatActivity {

    private ImageButton btnVolver;
    private TextView tvServicioSeleccionado, tvPrecioTotal, tvAdelantoMinimo;
    private TextInputEditText etFecha, etHora, etNombreCliente;
    private MaterialButton btnConfirmar;

    private Servicio servicio;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservar_cita);

        btnVolver = findViewById(R.id.btnVolver);
        tvServicioSeleccionado = findViewById(R.id.tvServicioSeleccionado);
        tvPrecioTotal = findViewById(R.id.tvPrecioTotal);
        tvAdelantoMinimo = findViewById(R.id.tvAdelantoMinimo);
        etFecha = findViewById(R.id.etFecha);
        etHora = findViewById(R.id.etHora);
        etNombreCliente = findViewById(R.id.etNombreCliente);
        btnConfirmar = findViewById(R.id.btnConfirmar);

        servicio = (Servicio) getIntent().getSerializableExtra("servicio");
        dbRef = FirebaseDatabase.getInstance("https://nigoshasalonapp-default-rtdb.firebaseio.com").getReference();

        btnVolver.setOnClickListener(v -> finish());

        if (servicio != null) {
            tvServicioSeleccionado.setText(servicio.nombre);
            tvPrecioTotal.setText(String.format("Precio Total: S/ %.2f", servicio.precio));
            tvAdelantoMinimo.setText(String.format("Adelanto requerido: S/ %.2f (50%%)", servicio.precio * 0.5));
        }

        configurarPickers();

        btnConfirmar.setOnClickListener(v -> guardarCitaEnFirebase());
    }

    private void configurarPickers() {
        etFecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                etFecha.setText(String.format("%02d/%02d/%d", dayOfMonth, month + 1, year));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dp.show();
        });

        etHora.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            TimePickerDialog tp = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                etHora.setText(String.format("%02d:%02d", hourOfDay, minute));
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
            tp.show();
        });
    }

    private void guardarCitaEnFirebase() {
        String fecha = etFecha.getText() != null ? etFecha.getText().toString().trim() : "";
        String hora = etHora.getText() != null ? etHora.getText().toString().trim() : "";
        String cliente = etNombreCliente.getText() != null ? etNombreCliente.getText().toString().trim() : "";

        if (fecha.isEmpty() || hora.isEmpty() || cliente.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmar.setEnabled(false);

        String citaId = dbRef.child("citas").push().getKey();
        double precioTotal = servicio != null ? servicio.precio : 0.0;
        double adelanto = servicio != null ? servicio.precio * 0.5 : 0.0;
        String srvNombre = servicio != null ? servicio.nombre : "Servicio Nigosha";

        Cita nuevaCita = new Cita(citaId, "CLI_DEMO", cliente, srvNombre, precioTotal, adelanto, fecha, hora, "PENDIENTE");

        if (citaId != null) {
            dbRef.child("citas").child(citaId).setValue(nuevaCita).addOnCompleteListener(task -> {
                btnConfirmar.setEnabled(true);
                if (task.isSuccessful()) {
                    Toast.makeText(this, "¡Cita reservada!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "Error al guardar: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}