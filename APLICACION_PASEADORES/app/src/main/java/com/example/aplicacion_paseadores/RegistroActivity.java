package com.example.aplicacion_paseadores;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistroActivity extends AppCompatActivity {

    private EditText editNombre, editEmail, editTelefono;
    private Button btnRegistrar, btnIniciarSesion;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        prefs = getSharedPreferences("DogsTravels", MODE_PRIVATE);

        editNombre = findViewById(R.id.edit_nombre);
        editEmail = findViewById(R.id.edit_email);
        editTelefono = findViewById(R.id.edit_telefono);
        btnRegistrar = findViewById(R.id.btn_registrar);
        btnIniciarSesion = findViewById(R.id.btn_iniciar_sesion);

        // Verificar si ya hay sesión iniciada
        if (prefs.getBoolean("sesion_iniciada", false)) {
            editNombre.setText(prefs.getString("nombre", ""));
            editEmail.setText(prefs.getString("email", ""));
            editTelefono.setText(prefs.getString("telefono", ""));
        }

        btnRegistrar.setOnClickListener(v -> {
            String nombre = editNombre.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String telefono = editTelefono.getText().toString().trim();

            if (nombre.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Guardar datos del usuario
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("nombre", nombre);
            editor.putString("email", email);
            editor.putString("telefono", telefono);
            editor.putBoolean("sesion_iniciada", true);
            editor.apply();

            Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnIniciarSesion.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String nombre = prefs.getString("nombre", "");

            if (email.isEmpty()) {
                Toast.makeText(this, "Ingresa tu email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (prefs.getBoolean("sesion_iniciada", false) && 
                email.equals(prefs.getString("email", ""))) {
                Toast.makeText(this, "¡Bienvenido de nuevo, " + nombre + "!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Usuario no encontrado. Regístrate primero.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


