package com.example.aplicacion_paseadores;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CalificarPaseadorActivity extends AppCompatActivity {

    private TextView txtPaseador, txtTarifa;
    private RatingBar ratingBar;
    private EditText editPropina;
    private Button btnEnviar;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calificar_paseador);

        prefs = getSharedPreferences("DogsTravels", MODE_PRIVATE);

        txtPaseador = findViewById(R.id.txt_paseador);
        txtTarifa = findViewById(R.id.txt_tarifa);
        ratingBar = findViewById(R.id.rating_bar);
        editPropina = findViewById(R.id.edit_propina);
        btnEnviar = findViewById(R.id.btn_enviar_calificacion);

        // Obtener datos del paseo
        String paseador = prefs.getString("paseador_seleccionado", "Paseador");
        float tarifa = prefs.getFloat("tarifa", 30.0f);

        txtPaseador.setText("👤 " + paseador);
        txtTarifa.setText("💰 Total pagado: $" + String.format("%.2f", tarifa) + " MXN");

        btnEnviar.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            if (rating > 0) {
                // Obtener propina si se ingresó
                float propina = 0.0f;
                if (editPropina != null && !editPropina.getText().toString().trim().isEmpty()) {
                    try {
                        propina = Float.parseFloat(editPropina.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        propina = 0.0f;
                    }
                }
                
                String mensaje = "¡Gracias por tu calificación de " + rating + " estrellas!";
                if (propina > 0) {
                    mensaje += "\nPropina: $" + String.format("%.2f", propina) + " MXN";
                }
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
                
                // Guardar calificación y propina
                SharedPreferences.Editor editor = prefs.edit();
                editor.putFloat("ultima_calificacion", rating);
                editor.putFloat("ultima_propina", propina);
                editor.apply();
                
                // Regresar a MainActivity
                finish();
            } else {
                Toast.makeText(this, "Por favor califica al paseador", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

