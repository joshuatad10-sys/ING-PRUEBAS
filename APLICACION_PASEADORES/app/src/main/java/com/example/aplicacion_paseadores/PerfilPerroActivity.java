package com.example.aplicacion_paseadores;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PerfilPerroActivity extends AppCompatActivity {

    private ImageView imgPerro;
    private EditText editNombrePerro, editRaza;
    private RadioGroup radioGroupTamanio;
    private Button btnSeleccionarFoto, btnGuardar;
    private Uri imageUri;
    private SharedPreferences prefs;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    if (imageUri != null) {
                        imgPerro.setImageURI(imageUri);
                        imgPerro.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_perro);

        prefs = getSharedPreferences("DogsTravels", MODE_PRIVATE);

        imgPerro = findViewById(R.id.img_perro);
        editNombrePerro = findViewById(R.id.edit_nombre_perro);
        editRaza = findViewById(R.id.edit_raza);
        radioGroupTamanio = findViewById(R.id.radio_group_tamanio);
        btnSeleccionarFoto = findViewById(R.id.btn_seleccionar_foto);
        btnGuardar = findViewById(R.id.btn_guardar_perfil);

        btnSeleccionarFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnGuardar.setOnClickListener(v -> {
            String nombre = editNombrePerro.getText().toString().trim();
            String raza = editRaza.getText().toString().trim();
            int selectedId = radioGroupTamanio.getCheckedRadioButtonId();

            if (nombre.isEmpty() || raza.isEmpty() || selectedId == -1) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton radioButton = findViewById(selectedId);
            String tamanio = radioButton.getText().toString();

            // Guardar perfil en SharedPreferences como JSON
            guardarPerfil(nombre, raza, tamanio, imageUri != null ? imageUri.toString() : "");

            Toast.makeText(this, "Perfil guardado: " + nombre + " (" + tamanio + ")", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void guardarPerfil(String nombre, String raza, String tamanio, String fotoUri) {
        try {
            // Obtener perfiles existentes
            String perfilesJson = prefs.getString("perfiles_perros", "[]");
            JSONArray perfilesArray = new JSONArray(perfilesJson);

            // Crear nuevo perfil
            JSONObject nuevoPerfil = new JSONObject();
            nuevoPerfil.put("nombre", nombre);
            nuevoPerfil.put("raza", raza);
            nuevoPerfil.put("tamanio", tamanio);
            nuevoPerfil.put("foto", fotoUri);
            nuevoPerfil.put("id", System.currentTimeMillis()); // ID único

            // Agregar nuevo perfil
            perfilesArray.put(nuevoPerfil);

            // Guardar
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("perfiles_perros", perfilesArray.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

