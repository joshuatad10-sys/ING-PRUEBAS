<?php
require 'config/config.php';
require 'config/database.php';
$db = new Database();
$con = $db->conectar();

$productos = isset($_SESSION['carrito']['productos']) ? $_SESSION['carrito']['productos'] : null;
$lista_carrito = array();

if ($productos != null) {
    foreach ($productos as $clave => $cantidad) {
        $sql = $con->prepare("SELECT id, nombre, precio, descuento, $cantidad AS cantidad FROM productos WHERE id=? AND activo=1");
        $sql->execute([$clave]);
        $lista_carrito[] = $sql->fetch(PDO::FETCH_ASSOC);
    }
} else {
    header("Location: index.php");
    exit;
}
?>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pago | Chronos Club</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/estilos.css" rel="stylesheet">
</head>

<body>
    <header>
        <div class="navbar navbar-expand-lg navbar-dark bg-dark">
            <div class="container">
                <a href="#" class="navbar-brand"><strong>Chronos Club</strong></a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarHeader">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navbarHeader">
                    <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                        <li class="nav-item"><a href="#" class="nav-link active">Catálogo</a></li>
                        <li class="nav-item"><a href="#" class="nav-link">Contacto</a></li>
                    </ul>
                    <a href="carrito.php" class="btn btn-primary">
                        Carrito <span id="num_cart" class="badge bg-secondary"><?php echo $num_cart; ?></span>
                    </a>
                </div>
            </div>
        </div>
    </header>

    <main>
        <div class="container py-4">
            <h3 class="mb-4 text-center">Resumen de tu pedido</h3>

            <!-- TABLA DE PRODUCTOS -->
            <div class="table-responsive mb-5">
                <table class="table table-bordered">
                    <thead class="table-dark">
                        <tr>
                            <th>Producto</th>
                            <th>Cantidad</th>
                            <th>Subtotal</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php
                        if ($lista_carrito == null) {
                            echo '<tr><td colspan="3" class="text-center"><b>Lista vacía</b></td></tr>';
                        } else {
                            $total = 0;
                            foreach ($lista_carrito as $producto) {
                                $_id = $producto['id'];
                                $nombre = $producto['nombre'];
                                $precio = $producto['precio'];
                                $descuento = $producto['descuento'];
                                $cantidad = $producto['cantidad'];
                                $precio_desc = $precio - (($precio * $descuento) / 100);
                                $subtotal = $cantidad * $precio_desc;
                                $total += $subtotal;
                        ?>
                                <tr>
                                    <td><?php echo $nombre; ?></td>
                                    <td><?php echo $cantidad; ?></td>
                                    <td><?php echo MONEDA . number_format($subtotal, 2, '.', ','); ?></td>
                                </tr>
                            <?php } ?>
                            <tr>
                                <td colspan="2" class="text-end"><b>Total:</b></td>
                                <td><b><?php echo MONEDA . number_format($total, 2, '.', ','); ?></b></td>
                            </tr>
                        <?php } ?>
                    </tbody>
                </table>
            </div>

            <!-- FORMULARIO DE DATOS DE ENVÍO -->
            <h4 class="mb-3">Datos del comprador</h4>
            <form id="formCompra" class="row g-3 needs-validation" novalidate>
                <div class="col-md-6">
                    <label for="nombre" class="form-label">Nombre</label>
                    <input type="text" class="form-control" id="nombre" name="nombre" required>
                    <div class="invalid-feedback">Por favor ingresa tu nombre.</div>
                </div>

                <div class="col-md-6">
                    <label for="apellido" class="form-label">Apellido</label>
                    <input type="text" class="form-control" id="apellido" name="apellido" required>
                    <div class="invalid-feedback">Por favor ingresa tu apellido.</div>
                </div>

                <div class="col-md-6">
                    <label for="correo" class="form-label">Correo electrónico</label>
                    <input type="email" class="form-control" id="correo" name="correo" required>
                    <div class="invalid-feedback">Por favor ingresa un correo válido.</div>
                </div>

                <div class="col-md-6">
                    <label for="telefono" class="form-label">Teléfono</label>
                    <input type="tel" class="form-control" id="telefono" name="telefono" pattern="[0-9]{10}" required>
                    <div class="invalid-feedback">Ingresa un número válido de 10 dígitos.</div>
                </div>

                <div class="col-md-12">
                    <label for="direccion" class="form-label">Dirección completa</label>
                    <textarea class="form-control" id="direccion" name="direccion" rows="3" required></textarea>
                    <div class="invalid-feedback">Por favor ingresa tu dirección.</div>
                </div>

                <div class="col-md-6">
                    <label for="ciudad" class="form-label">Ciudad</label>
                    <input type="text" class="form-control" id="ciudad" name="ciudad" required>
                    <div class="invalid-feedback">Ingresa tu ciudad.</div>
                </div>

                <div class="col-md-6">
                    <label for="codigo_postal" class="form-label">Código postal</label>
                    <input type="text" class="form-control" id="codigo_postal" name="codigo_postal" pattern="[0-9]{5}" required>
                    <div class="invalid-feedback">Ingresa un código postal válido de 5 dígitos.</div>
                </div>
            </form>

            <!-- BOTÓN DE PAGO PAYPAL -->
            <div class="mt-5">
                <h4 class="text-center mb-3">Pago con PayPal</h4>
                <div id="paypal-button-container" class="text-center"></div>
            </div>
        </div>
    </main>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://www.paypal.com/sdk/js?client-id=<?php echo CLIENT_ID; ?>&currency=<?php echo CURRENCY; ?>"></script>

    <script>
        // Validación de formulario antes del pago
        function validarFormulario() {
            const form = document.getElementById('formCompra');
            if (!form.checkValidity()) {
                form.classList.add('was-validated');
                return false;
            }
            return true;
        }

        paypal.Buttons({
            style: {
                color: 'blue',
                shape: 'pill',
                label: 'pay'
            },
            createOrder: function(data, actions) {
                if (!validarFormulario()) {
                    alert("Por favor completa todos los datos antes de continuar con el pago.");
                    return;
                }
                return actions.order.create({
                    purchase_units: [{
                        amount: {
                            value: <?php echo $total; ?>
                        }
                    }]
                });
            },
            onApprove: function(data, actions) {
                return actions.order.capture().then(function(detalles) {
                    // Obtener datos del formulario
                    const formData = new FormData(document.getElementById('formCompra'));

                    // Enviar datos al servidor para guardar en la base de datos
                    fetch('procesar_compra.php', {
                        method: 'POST',
                        body: formData
                    }).then(response => {
                        if (!response.ok) {
                            console.error("Error al guardar los datos del cliente");
                        }
                    });

                    // Después, registrar el pago en PayPal
                    return fetch('clases/captura.php', {
                        method: 'POST',
                        headers: {
                            'content-type': 'application/json'
                        },
                        body: JSON.stringify({
                            detalles: detalles
                        })
                    }).then(() => {
                        window.location.href = "completado.php?key=" + detalles['id'];
                    });
                });
            },
            onCancel: function(data) {
                alert("Pago cancelado");
            }
        }).render('#paypal-button-container');
    </script>
</body>

</html>