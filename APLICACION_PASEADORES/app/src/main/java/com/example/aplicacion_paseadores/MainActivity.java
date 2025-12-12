package com.example.aplicacion_paseadores;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Button btnRegistro, btnPerfilPerro, btnConfirmar;
    private TextView txtUsuarioNombre;
    private android.widget.EditText txtOrigen, txtDestino;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private SharedPreferences prefs;
    
    private Marker markerOrigen, markerDestino, markerPuntoRecogida;
    private LatLng origenLatLng, destinoLatLng, puntoRecogidaLatLng;
    private Polyline rutaPolyline;
    private String paseadorSeleccionado = null;
    private boolean seleccionandoOrigen = false;
    private boolean seleccionandoDestino = false;
    private boolean seleccionandoPuntoRecogida = false;
    private android.widget.CheckBox checkIdaVuelta, checkRecogerOtroPunto;
    private android.widget.EditText txtPuntoRecogida;
    private android.view.View layoutPuntoRecogida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences("DogsTravels", MODE_PRIVATE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicializar vistas
        txtUsuarioNombre = findViewById(R.id.txt_usuario_nombre);
        txtOrigen = findViewById(R.id.input_origen);
        txtDestino = findViewById(R.id.input_destino);
        btnRegistro = findViewById(R.id.btn_registro);
        btnPerfilPerro = findViewById(R.id.btn_perfil_perro);
        btnConfirmar = findViewById(R.id.btn_confirmar);
        
        // Inicializar TextInputEditText para hacerlos clickeables
        if (txtOrigen != null) {
            txtOrigen.setFocusable(false);
            txtOrigen.setClickable(true);
        }
        if (txtDestino != null) {
            txtDestino.setFocusable(false);
            txtDestino.setClickable(true);
        }

        // Verificar si hay usuario logueado
        verificarUsuarioLogueado();

        // Inicializar mapa programáticamente
        initializeMap();

        // Botón de registro
        btnRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
            startActivityForResult(intent, 100);
        });

        // Botón de perfil de perro
        btnPerfilPerro.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PerfilPerroActivity.class);
            startActivityForResult(intent, 200);
        });
        
        // Cargar perfiles de perros guardados
        cargarPerfilesPerros();

        // Botón de ubicación actual
        Button btnUbicacionActual = findViewById(R.id.btn_ubicacion_actual);
        if (btnUbicacionActual != null) {
            btnUbicacionActual.setOnClickListener(v -> usarUbicacionActual());
        }

        // Botones para seleccionar origen y destino desde el mapa
        txtOrigen.setOnClickListener(v -> {
            seleccionandoOrigen = true;
            seleccionandoDestino = false;
            Toast.makeText(this, "Toca el mapa para seleccionar el punto de recogida", Toast.LENGTH_SHORT).show();
        });

        txtDestino.setOnClickListener(v -> {
            seleccionandoDestino = true;
            seleccionandoOrigen = false;
            Toast.makeText(this, "Toca el mapa para seleccionar el destino", Toast.LENGTH_SHORT).show();
        });

        // Botones de selección de paseadores
        setupPaseadoresButtons();

        // Botón confirmar paseo
        btnConfirmar.setOnClickListener(v -> {
            if (origenLatLng == null) {
                Toast.makeText(this, "Por favor selecciona el punto de partida", Toast.LENGTH_SHORT).show();
                return;
            }
            
            boolean esIdaVuelta = checkIdaVuelta != null && checkIdaVuelta.isChecked();
            
            // Si es ida y vuelta, el destino es el mismo que el origen
            if (esIdaVuelta) {
                destinoLatLng = origenLatLng;
            } else if (destinoLatLng == null) {
                // Solo requiere destino si NO es ida y vuelta
                Toast.makeText(this, "Por favor selecciona el destino", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (paseadorSeleccionado == null) {
                Toast.makeText(this, "Por favor selecciona un paseador", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Calcular distancia y tarifa
            float distancia;
            if (esIdaVuelta) {
                // Para ida y vuelta, usar una distancia estimada (ej: 2 km de paseo)
                // O calcular basándose en un radio desde el origen
                distancia = 2.0f; // Distancia estimada para un paseo ida y vuelta
            } else {
                // Para viaje normal, calcular distancia real
                distancia = calcularDistancia(origenLatLng, destinoLatLng);
            }
            
            // La tarifa ya incluye el concepto de ida y vuelta en la distancia
            float tarifa = calcularTarifa(distancia);
            
            // Guardar datos del paseo
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("paseador_seleccionado", paseadorSeleccionado);
            editor.putFloat("origen_lat", (float) origenLatLng.latitude);
            editor.putFloat("origen_lng", (float) origenLatLng.longitude);
            editor.putFloat("destino_lat", (float) destinoLatLng.latitude);
            editor.putFloat("destino_lng", (float) destinoLatLng.longitude);
            editor.putBoolean("ida_vuelta", checkIdaVuelta != null && checkIdaVuelta.isChecked());
            if (puntoRecogidaLatLng != null) {
                editor.putFloat("punto_recogida_lat", (float) puntoRecogidaLatLng.latitude);
                editor.putFloat("punto_recogida_lng", (float) puntoRecogidaLatLng.longitude);
            }
            editor.putFloat("distancia_km", distancia);
            editor.putFloat("tarifa", tarifa);
            editor.apply();

            Intent intent = new Intent(MainActivity.this, EsperandoPaseadorActivity.class);
            startActivity(intent);
        });

        // Solicitar permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void verificarUsuarioLogueado() {
        if (prefs.getBoolean("sesion_iniciada", false)) {
            String nombre = prefs.getString("nombre", "");
            if (!nombre.isEmpty() && txtUsuarioNombre != null) {
                txtUsuarioNombre.setText("👤 " + nombre);
                txtUsuarioNombre.setVisibility(android.view.View.VISIBLE);
                if (btnRegistro != null) {
                    btnRegistro.setText("Cambiar usuario");
                }
            }
        }
    }

    private void setupPaseadoresButtons() {
        Button btnPaseador1 = findViewById(R.id.btn_seleccionar_paseador1);
        Button btnPaseador2 = findViewById(R.id.btn_seleccionar_paseador2);
        
        if (btnPaseador1 != null) {
            btnPaseador1.setOnClickListener(v -> {
                paseadorSeleccionado = "María Gómez";
                actualizarSeleccionPaseadores();
                Toast.makeText(this, "Paseador seleccionado: " + paseadorSeleccionado, Toast.LENGTH_SHORT).show();
            });
        }
        
        if (btnPaseador2 != null) {
            btnPaseador2.setOnClickListener(v -> {
                paseadorSeleccionado = "Carlos Díaz";
                actualizarSeleccionPaseadores();
                Toast.makeText(this, "Paseador seleccionado: " + paseadorSeleccionado, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void actualizarSeleccionPaseadores() {
        Button btnPaseador1 = findViewById(R.id.btn_seleccionar_paseador1);
        Button btnPaseador2 = findViewById(R.id.btn_seleccionar_paseador2);
        
        if (btnPaseador1 != null) {
            if ("María Gómez".equals(paseadorSeleccionado)) {
                btnPaseador1.setText("✓ Seleccionado");
                btnPaseador1.setEnabled(false);
            } else {
                btnPaseador1.setText("Seleccionar");
                btnPaseador1.setEnabled(true);
            }
        }
        
        if (btnPaseador2 != null) {
            if ("Carlos Díaz".equals(paseadorSeleccionado)) {
                btnPaseador2.setText("✓ Seleccionado");
                btnPaseador2.setEnabled(false);
            } else {
                btnPaseador2.setText("Seleccionar");
                btnPaseador2.setEnabled(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            // Usuario regresó del registro
            verificarUsuarioLogueado();
        } else if (requestCode == 200) {
            // Perfil de perro guardado
            cargarPerfilesPerros();
        }
    }

    private void cargarPerfilesPerros() {
        android.widget.LinearLayout layoutPerros = findViewById(R.id.layout_perros);
        if (layoutPerros == null) return;
        
        layoutPerros.removeAllViews();
        
        try {
            String perfilesJson = prefs.getString("perfiles_perros", "[]");
            org.json.JSONArray perfilesArray = new org.json.JSONArray(perfilesJson);
            
            if (perfilesArray.length() == 0) {
                android.widget.TextView txtVacio = new android.widget.TextView(this);
                txtVacio.setText("No hay perfiles guardados. Agrega uno para comenzar.");
                txtVacio.setTextAppearance(android.R.style.TextAppearance_Material_Body2);
                txtVacio.setPadding(0, 16, 0, 16);
                layoutPerros.addView(txtVacio);
                return;
            }
            
            for (int i = 0; i < perfilesArray.length(); i++) {
                org.json.JSONObject perfil = perfilesArray.getJSONObject(i);
                String nombre = perfil.getString("nombre");
                String raza = perfil.getString("raza");
                String tamanio = perfil.getString("tamanio");
                
                com.google.android.material.button.MaterialButton btnPerro = new com.google.android.material.button.MaterialButton(this);
                btnPerro.setText("🐕 " + nombre + " - " + raza + " (" + tamanio + ")");
                btnPerro.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
                btnPerro.setPadding(16, 16, 16, 16);
                if (i > 0) {
                    android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) btnPerro.getLayoutParams();
                    params.topMargin = 8;
                    btnPerro.setLayoutParams(params);
                }
                btnPerro.setOnClickListener(v -> {
                    Toast.makeText(this, "Perro seleccionado: " + nombre, Toast.LENGTH_SHORT).show();
                    // Guardar perro seleccionado
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("perro_seleccionado", nombre);
                    editor.apply();
                });
                layoutPerros.addView(btnPerro);
            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }

    private void initializeMap() {
        try {
            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commit();
            mapFragment.getMapAsync(this);
        } catch (Exception e) {
            // Si hay error con Google Maps, mostrar mensaje pero continuar
            android.util.Log.e("MainActivity", "Error inicializando mapa: " + e.getMessage());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);

        // Habilitar controles del mapa
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                android.util.Log.e("MainActivity", "Error habilitando ubicación: " + e.getMessage());
            }
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);

            // Obtener ubicación actual
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                        } else {
                            // Ubicación por defecto (Ciudad de México)
                            LatLng defaultLocation = new LatLng(19.4326, -99.1332);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 13f));
                        }
                    });
        } else {
            // Ubicación por defecto si no hay permisos
            LatLng defaultLocation = new LatLng(19.4326, -99.1332);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 13f));
        }

        // Agregar algunos marcadores de ejemplo (paseadores)
        LatLng paseador1 = new LatLng(19.4326, -99.1332);
        mMap.addMarker(new MarkerOptions()
                .position(paseador1)
                .title("María Gómez - Paseador disponible"));

        LatLng paseador2 = new LatLng(19.4400, -99.1400);
        mMap.addMarker(new MarkerOptions()
                .position(paseador2)
                .title("Carlos Díaz - Paseador disponible"));
    }

    @Override
    public void onMapClick(LatLng point) {
        if (seleccionandoOrigen) {
            // Eliminar marcador anterior si existe
            if (markerOrigen != null) {
                markerOrigen.remove();
            }
            
            // Crear nuevo marcador de origen
            markerOrigen = mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("Punto de recogida")
                    .snippet("Origen"));
            origenLatLng = point;
            txtOrigen.setText("📍 " + point.latitude + ", " + point.longitude);
            seleccionandoOrigen = false;
            dibujarRuta();
            Toast.makeText(this, "Punto de recogida seleccionado", Toast.LENGTH_SHORT).show();
            
        } else if (seleccionandoDestino) {
            // Eliminar marcador anterior si existe
            if (markerDestino != null) {
                markerDestino.remove();
            }
            
            // Crear nuevo marcador de destino
            markerDestino = mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("Destino")
                    .snippet("Punto de llegada"));
            destinoLatLng = point;
            txtDestino.setText("📍 " + point.latitude + ", " + point.longitude);
            seleccionandoDestino = false;
            dibujarRuta();
            Toast.makeText(this, "Destino seleccionado", Toast.LENGTH_SHORT).show();
        }
    }

    private void dibujarRuta() {
        if (origenLatLng == null) {
            return;
        }
        
        boolean esIdaVuelta = checkIdaVuelta != null && checkIdaVuelta.isChecked();
        
        // Si es ida y vuelta, el destino es el origen
        if (esIdaVuelta) {
            destinoLatLng = origenLatLng;
        }
        
        if (destinoLatLng == null) {
            return; // No hay destino aún (solo si no es ida y vuelta)
        }
        
        // Eliminar ruta anterior si existe
        if (rutaPolyline != null) {
            rutaPolyline.remove();
        }
        
        // Dibujar línea entre origen y destino
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(origenLatLng, destinoLatLng)
                .width(8)
                .color(0xFF6B35); // Color primario
        
        // Si es ida y vuelta, dibujar un círculo alrededor del punto
        if (esIdaVuelta) {
            // Crear un círculo aproximado para mostrar que es ida y vuelta
            // Agregar puntos alrededor del origen para simular un círculo
            for (int i = 0; i <= 360; i += 30) {
                double angle = Math.toRadians(i);
                double radius = 0.001; // Radio pequeño en grados
                LatLng point = new LatLng(
                        origenLatLng.latitude + radius * Math.cos(angle),
                        origenLatLng.longitude + radius * Math.sin(angle)
                );
                polylineOptions.add(point);
            }
            polylineOptions.add(origenLatLng); // Cerrar el círculo
        }
        
        rutaPolyline = mMap.addPolyline(polylineOptions);
        
        // Calcular y mostrar tarifa
        float distancia;
        if (esIdaVuelta) {
            distancia = 2.0f; // Distancia estimada para paseo ida y vuelta
        } else {
            distancia = calcularDistancia(origenLatLng, destinoLatLng);
        }
        float tarifa = calcularTarifa(distancia);
        TextView txtTarifa = findViewById(R.id.txt_tarifa);
        if (txtTarifa != null) {
            txtTarifa.setText("$" + String.format("%.2f", tarifa) + " MXN");
        }
        
        // Ajustar cámara para mostrar ambos puntos
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(origenLatLng);
        builder.include(destinoLatLng);
        if (puntoRecogidaLatLng != null) {
            builder.include(puntoRecogidaLatLng);
        }
        try {
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
        } catch (Exception e) {
            // Si falla, usar zoom simple
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origenLatLng, 13f));
        }
    }

    private float calcularDistancia(LatLng origen, LatLng destino) {
        // Fórmula de Haversine para calcular distancia entre dos puntos
        double radioTierra = 6371; // Radio de la Tierra en km
        double lat1 = Math.toRadians(origen.latitude);
        double lat2 = Math.toRadians(destino.latitude);
        double deltaLat = Math.toRadians(destino.latitude - origen.latitude);
        double deltaLng = Math.toRadians(destino.longitude - origen.longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (float) (radioTierra * c);
    }

    private float calcularTarifa(float distanciaKm) {
        // Tarifa base: $30 MXN
        // Por cada km adicional: $15 MXN
        // Mínimo: $30 MXN
        float tarifaBase = 30.0f;
        float tarifaPorKm = 15.0f;
        float tarifaTotal = tarifaBase + (distanciaKm * tarifaPorKm);
        return Math.max(tarifaTotal, tarifaBase); // Mínimo la tarifa base
    }

    public void usarUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            
                            // Eliminar marcador anterior si existe
                            if (markerOrigen != null) {
                                markerOrigen.remove();
                            }
                            
                            // Crear nuevo marcador de origen
                            markerOrigen = mMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("Punto de recogida")
                                    .snippet("Ubicación actual"));
                            origenLatLng = currentLocation;
                            txtOrigen.setText("📍 Ubicación actual");
                            
                            // Mover cámara a la ubicación
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                            
                            // Si ya hay destino, dibujar ruta
                            if (destinoLatLng != null) {
                                dibujarRuta();
                            }
                            
                            Toast.makeText(this, "Ubicación actual establecida como punto de recogida", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Se necesitan permisos de ubicación", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        try {
                            mMap.setMyLocationEnabled(true);
                        } catch (SecurityException e) {
                            android.util.Log.e("MainActivity", "Error habilitando ubicación: " + e.getMessage());
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}