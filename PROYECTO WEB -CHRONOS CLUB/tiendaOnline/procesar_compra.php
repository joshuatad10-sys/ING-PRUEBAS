<?php
require 'config/database.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $nombre = $_POST['nombre'] ?? '';
    $apellido = $_POST['apellido'] ?? '';
    $correo = $_POST['correo'] ?? '';
    $telefono = $_POST['telefono'] ?? '';
    $direccion = $_POST['direccion'] ?? '';
    $ciudad = $_POST['ciudad'] ?? '';
    $codigo_postal = $_POST['codigo_postal'] ?? '';

    if ($nombre && $apellido && $correo && $telefono && $direccion && $ciudad && $codigo_postal) {
        $db = new Database();
        $con = $db->conectar();

        $sql = $con->prepare("INSERT INTO clientes (nombre, apellido, correo, telefono, direccion, ciudad, codigo_postal, fecha_registro)
                              VALUES (?, ?, ?, ?, ?, ?, ?, NOW())");
        $sql->execute([$nombre, $apellido, $correo, $telefono, $direccion, $ciudad, $codigo_postal]);
    }
}
