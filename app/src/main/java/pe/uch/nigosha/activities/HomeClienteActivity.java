package pe.uch.nigosha.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import pe.uch.nigosha.R;
import pe.uch.nigosha.adapters.CitaAdapter;
import pe.uch.nigosha.adapters.ServicioAdapter;
import pe.uch.nigosha.models.Cita;
import pe.uch.nigosha.models.Servicio;

public class HomeClienteActivity extends AppCompatActivity {
    private RecyclerView rvPrincipal;
    private MaterialButtonToggleGroup toggleGroup;
    private DatabaseReference dbRef;

    private List<Servicio> listaServicios = new ArrayList<>();
    private List<Cita> listaCitas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_cliente);

        rvPrincipal = findViewById(R.id.rvPrincipal);
        toggleGroup = findViewById(R.id.toggleGroup);
        rvPrincipal.setLayoutManager(new LinearLayoutManager(this));

        // Conexión explícita con la URL de tu base de datos Firebase
        dbRef = FirebaseDatabase.getInstance("https://nigoshasalonapp-default-rtdb.firebaseio.com").getReference();

        cargarCatalogoServicios();
        verificarCargaInicialServicios();

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnVerServicios) {
                    cargarCatalogoServicios();
                } else if (checkedId == R.id.btnVerMisCitas) {
                    cargarMisCitas();
                }
            }
        });
    }

    private void cargarCatalogoServicios() {
        dbRef.child("servicios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaServicios.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Servicio s = ds.getValue(Servicio.class);
                    if (s != null) {
                        listaServicios.add(s);
                    }
                }
                rvPrincipal.setAdapter(new ServicioAdapter(HomeClienteActivity.this, listaServicios));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeClienteActivity.this, "Error al cargar: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarMisCitas() {
        dbRef.child("citas").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaCitas.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Cita c = ds.getValue(Cita.class);
                    if (c != null) {
                        listaCitas.add(c);
                    }
                }
                rvPrincipal.setAdapter(new CitaAdapter(HomeClienteActivity.this, listaCitas));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeClienteActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verificarCargaInicialServicios() {
        dbRef.child("servicios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    guardarServicioBase("srv_01", "Balayage Orgánico & Matizado", "Cabello", 180.0, 120);
                    guardarServicioBase("srv_02", "Corte + Cepillado & Estilizado", "Cabello", 45.0, 45);
                    guardarServicioBase("srv_03", "Limpieza Facial Profunda", "Facial", 70.0, 60);
                    guardarServicioBase("srv_04", "Micropigmentación de Cejas", "Permanente", 250.0, 90);
                    guardarServicioBase("srv_05", "Lifting de Pestañas + Tinte", "Ojos", 65.0, 45);
                    guardarServicioBase("srv_06", "Manicure Rusa & Esmaltado en Gel", "Uñas", 55.0, 60);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void guardarServicioBase(String id, String nom, String cat, double pre, int min) {
        Servicio s = new Servicio(id, nom, cat, pre, min);
        dbRef.child("servicios").child(id).setValue(s);
    }
}