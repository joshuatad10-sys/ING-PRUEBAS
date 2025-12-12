# Configuración de Google Maps

Para que el mapa funcione correctamente, necesitas obtener una API key de Google Maps.

## Pasos para obtener la API key:

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Habilita la API de "Maps SDK for Android"
4. Ve a "Credenciales" y crea una nueva API key
5. Copia la API key
6. Abre el archivo `app/src/main/AndroidManifest.xml`
7. Reemplaza `YOUR_API_KEY_HERE` con tu API key real en la línea:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="TU_API_KEY_AQUI" />
   ```

## Nota importante:
- Sin la API key, el mapa no se mostrará correctamente
- Asegúrate de restringir la API key por aplicación Android para mayor seguridad


