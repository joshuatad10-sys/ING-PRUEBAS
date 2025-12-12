package com.example.aplicacion_paseadores;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PaseoEnCursoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView txtEstado, txtPaseador, txtTiempoEstimado, txtDistancia;
    private Button btnCancelar;
    private Handler handler;
    private int tiempoRestante = 20; // minutos (paseo breve)
    private SharedPreferences prefs;
    private boolean paseoIniciado = false;
    private boolean paseoTerminado = false;
    private GoogleMap mMap;
    private Marker markerPaseador;
    private LatLng ubicacionPaseador;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paseo_en_curso);

        prefs = getSharedPreferences("DogsTravels", MODE_PRIVATE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        txtEstado = findViewById(R.id.txt_estado);
        txtPaseador = findViewById(R.id.txt_paseador);
        txtTiempoEstimado = findViewById(R.id.txt_tiempo_estimado);
        txtDistancia = findViewById(R.id.txt_distancia);
        btnCancelar = findViewById(R.id.btn_cancelar);

        // Obtener datos del paseo
        String paseador = prefs.getString("paseador_seleccionado", "Paseador");
        float distancia = prefs.getFloat("distancia_km", 0.7f);
        txtPaseador.setText("👤 " + paseador);
        txtDistancia.setText("📍 Distancia: " + String.format("%.1f", distancia) + " km");

        // Inicializar mapa
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.map_container_paseo, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);

        // Iniciar el paseo inmediatamente (ya llegó el paseador)
        txtEstado.setText("🚶 Paseo en curso");
        txtTiempoEstimado.setText("⏱️ Tiempo restante: " + tiempoRestante + " min");
        paseoIniciado = true;

        btnCancelar.setOnClickListener(v -> {
            Toast.makeText(this, "Paseo cancelado", Toast.LENGTH_SHORT).show();
            finish();
        });

        handler = new Handler(Looper.getMainLooper());
        
        // Actualizar tiempo y ubicación del paseador cada 5 segundos (simulación rápida)
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!paseoTerminado) {
                    actualizarEstado();
                    actualizarUbicacionPaseador();
                    handler.postDelayed(this, 5000); // Cada 5 segundos
                }
            }
        }, 5000);

        // Terminar paseo después de 20 segundos (simulación breve)
        handler.postDelayed(() -> {
            if (!paseoTerminado) {
                terminarPaseo();
            }
        }, 20000);
    }

    private void actualizarEstado() {
        if (tiempoRestante > 0 && paseoIniciado) {
            tiempoRestante--;
            txtTiempoEstimado.setText("⏱️ Tiempo restante: " + tiempoRestante + " min");
            
            if (tiempoRestante <= 5) {
                txtEstado.setText("🏠 Regresando");
                txtDistancia.setText("📍 El paseador está regresando con tu perro");
            }
        }
    }

    private void terminarPaseo() {
        paseoTerminado = true;
        txtEstado.setText("✅ Paseo completado");
        txtTiempoEstimado.setText("⏱️ El paseador ha regresado");
        txtDistancia.setText("📍 Tu perro está de vuelta");
        
        // Después de 2 segundos, ir a la pantalla de calificación
        handler.postDelayed(() -> {
            Intent intent = new Intent(PaseoEnCursoActivity.this, CalificarPaseadorActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Obtener ubicación actual para simular que el paseador está cerca
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Simular ubicación del paseador cerca de la ubicación actual
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            // Agregar un pequeño offset para simular movimiento
                            ubicacionPaseador = new LatLng(
                                    currentLocation.latitude + 0.001,
                                    currentLocation.longitude + 0.001
                            );
                            
                            markerPaseador = mMap.addMarker(new MarkerOptions()
                                    .position(ubicacionPaseador)
                                    .title("Paseador")
                                    .snippet("En paseo"));
                            
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionPaseador, 15f));
                        } else {
                            // Ubicación por defecto
                            ubicacionPaseador = new LatLng(19.4326, -99.1332);
                            markerPaseador = mMap.addMarker(new MarkerOptions()
                                    .position(ubicacionPaseador)
                                    .title("Paseador")
                                    .snippet("En paseo"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionPaseador, 13f));
                        }
                    });
        } else {
            // Ubicación por defecto
            ubicacionPaseador = new LatLng(19.4326, -99.1332);
            markerPaseador = mMap.addMarker(new MarkerOptions()
                    .position(ubicacionPaseador)
                    .title("Paseador")
                    .snippet("En paseo"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionPaseador, 13f));
        }
    }

    private void actualizarUbicacionPaseador() {
        if (mMap != null && ubicacionPaseador != null && markerPaseador != null) {
            // Simular movimiento del paseador (pequeño desplazamiento)
            double offset = 0.0001; // Pequeño desplazamiento
            ubicacionPaseador = new LatLng(
                    ubicacionPaseador.latitude + (Math.random() - 0.5) * offset,
                    ubicacionPaseador.longitude + (Math.random() - 0.5) * offset
            );
            
            markerPaseador.setPosition(ubicacionPaseador);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicacionPaseador));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}

