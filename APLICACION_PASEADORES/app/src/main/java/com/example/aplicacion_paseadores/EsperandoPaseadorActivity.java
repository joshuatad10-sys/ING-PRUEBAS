package com.example.aplicacion_paseadores;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EsperandoPaseadorActivity extends AppCompatActivity {

    private TextView txtEstado, txtPaseador, txtTiempoLlegada, txtDistancia;
    private Button btnCancelar;
    private Handler handler;
    private int tiempoLlegada = 8; // minutos
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esperando_paseador);

        prefs = getSharedPreferences("DogsTravels", MODE_PRIVATE);

        txtEstado = findViewById(R.id.txt_estado);
        txtPaseador = findViewById(R.id.txt_paseador);
        txtTiempoLlegada = findViewById(R.id.txt_tiempo_llegada);
        txtDistancia = findViewById(R.id.txt_distancia);
        btnCancelar = findViewById(R.id.btn_cancelar);

        // Obtener datos del paseo
        String paseador = prefs.getString("paseador_seleccionado", "Paseador");
        float distancia = prefs.getFloat("distancia_km", 0.7f);
        float tarifa = prefs.getFloat("tarifa", 30.0f);

        txtPaseador.setText("👤 " + paseador);
        txtDistancia.setText("📍 Distancia: " + String.format("%.1f", distancia) + " km");
        txtEstado.setText("⏳ Esperando al paseador...");
        txtTiempoLlegada.setText("⏱️ Tiempo de llegada estimado: " + tiempoLlegada + " min");

        btnCancelar.setOnClickListener(v -> {
            Toast.makeText(this, "Paseo cancelado", Toast.LENGTH_SHORT).show();
            finish();
        });

        handler = new Handler(Looper.getMainLooper());
        
        // Simular llegada del paseador después de 15 segundos
        handler.postDelayed(() -> {
            // Cuando llegue el paseador, iniciar el paseo
            Intent intent = new Intent(EsperandoPaseadorActivity.this, PaseoEnCursoActivity.class);
            startActivity(intent);
            finish();
        }, 15000); // 15 segundos

        // Actualizar tiempo cada minuto
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (tiempoLlegada > 0) {
                    tiempoLlegada--;
                    txtTiempoLlegada.setText("⏱️ Tiempo de llegada estimado: " + tiempoLlegada + " min");
                    handler.postDelayed(this, 60000); // Cada minuto
                }
            }
        }, 60000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}


