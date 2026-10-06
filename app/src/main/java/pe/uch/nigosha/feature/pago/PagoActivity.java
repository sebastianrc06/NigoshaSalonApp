package pe.uch.nigosha.feature.pago;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.*;
import androidx.core.view.*;
import pe.uch.nigosha.R;

/** Entrada compatible con pantallas antiguas; la lógica y la vista residen en PagoFragment. */
public final class PagoActivity extends AppCompatActivity {
  public static final String EXTRA_CITA_ID = "citaId";

  @Override
  protected void onCreate(Bundle saved) {
    super.onCreate(saved);
    WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    setContentView(R.layout.activity_pago);
    View root = findViewById(R.id.pagoActivityRoot);
    ViewCompat.setOnApplyWindowInsetsListener(
        root,
        (view, insets) -> {
          Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
          return WindowInsetsCompat.CONSUMED;
        });
    boolean light =
        ColorUtils.calculateLuminance(ContextCompat.getColor(this, R.color.nigosha_bg)) > 0.5;
    WindowCompat.getInsetsController(getWindow(), root).setAppearanceLightStatusBars(light);
    WindowCompat.getInsetsController(getWindow(), root).setAppearanceLightNavigationBars(light);
    ViewCompat.requestApplyInsets(root);
    if (saved == null) {
      PagoFragment fragment = new PagoFragment();
      Bundle args = new Bundle();
      args.putString("citaId", getIntent().getStringExtra(EXTRA_CITA_ID));
      fragment.setArguments(args);
      getSupportFragmentManager().beginTransaction().replace(R.id.pagoHost, fragment).commit();
    }
  }
}
