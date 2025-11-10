<?php
require 'config/config.php';
require 'config/database.php';
$db = new Database();
$con = $db->conectar();

$sql = $con->prepare("SELECT id, nombre, precio FROM productos WHERE activo=1");
$sql->execute();
$resultado = $sql->fetchAll(PDO::FETCH_ASSOC);

//session_destroy();
//print_r($_SESSION);  
?>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tienda online</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
    <link href="css/estilos.css" rel="stylesheet">
</head>

<body>

    <header>
        <div class="navbar navbar-expand-lg navbar-dark bg-dark">
            <div class="container">
                <a href="#" class="navbar-brand">
                    <strong>Chronos Club</strong>
                </a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarHeader" aria-controls="navbarHeader" aria-expanded="false" aria-label="Toggle navigation">
                    <span class="navbar-toggler-icon"></span>
                </button>

                <div class="collapse navbar-collapse" id="navbarHeader">
                    <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                        <li class="nav-item">
                            <a href="#" class="nav-link active">Catálogo</a>
                        </li>
                        <li class="nav-item">
                            <a href="#" class="nav-link">Contacto</a>
                        </li>
                    </ul>
                    <a href="checkout.php" class="btn btn-primary">
                        Carrito <span id="num_cart" class="badge bg-secondary"><?php echo $num_cart; ?></span>
                    </a>
                </div>
            </div>
        </div>
    </header>

    <!---Contenido--->
    <main>
        <div class="container my-5">
            <div class="row row-cols-1 row-cols-sm-2 row-cols-md-3 g-3">
                <?php foreach ($resultado as $row) { ?>
                    <div class="col">
                        <div class="card shadow-sm">
                            <?php
                            $id = $row['id'];
                            $imagen = "img/productos/" . $id . "/principal.jpg";
                            if (!file_exists($imagen)) {
                                $imagen = "img/no-photo.jpg";
                            }
                            ?>
                            <img src="<?php echo $imagen; ?>" class="card-img-top" alt="<?php echo $row['nombre']; ?>">
                            <div class="card-body text-center">
                                <h5 class="card-title"><?php echo $row['nombre']; ?></h5>
                                <p class="card-text">$<?php echo number_format($row['precio'], 2, '.', ','); ?></p>
                                <div class="d-flex justify-content-center gap-2">
                                    <a href="details.php?id=<?php echo $row['id']; ?>&token=<?php echo hash_hmac('sha1', $row['id'], KEY_TOKEN); ?>" class="btn btn-primary">Detalles</a>
                                    <button class="btn btn-outline-success" type="button" onclick="addProducto(<?php echo $row['id']; ?>, '<?php echo hash_hmac('sha1', $row['id'], KEY_TOKEN); ?>')">Agregar al carrito</button>
                                </div>
                            </div>
                        </div>   
                    </div>
                <?php } ?>
            </div>
        </div>
    </main>

    <!-- PIE DE PÁGINA -->
    <footer class="bg-dark text-white pt-4 mt-5">
        <div class="container text-center text-md-start">
            <div class="row">
                <!-- Sección: Información -->
                <div class="col-md-4 mb-4">
                    <h5 class="text-uppercase fw-bold">Chronos Club</h5>
                    <p>Tu tienda exclusiva de relojes de lujo y estilo. Encuentra el reloj perfecto para cada ocasión.</p>
                </div>

                <!-- Sección: Contacto -->
                <div class="col-md-4 mb-4">
                    <h6 class="text-uppercase fw-bold">Contacto</h6>
                    <p class="mb-1"><i class="bi bi-envelope-fill"></i> contacto@chronosclub.com</p>
                    <p class="mb-1"><i class="bi bi-telephone-fill"></i> +52 55 6431 6401</p>
                    <p><i class="bi bi-geo-alt-fill"></i> Ciudad de México, México</p>
                </div>

                <!-- Sección: Redes Sociales -->
                <div class="col-md-4 mb-4 text-center">
                    <h6 class="text-uppercase fw-bold">Síguenos</h6>
                    <a href="#" class="text-white me-3 fs-4"><i class="bi bi-facebook"></i></a>
                    <a href="#" class="text-white me-3 fs-4"><i class="bi bi-instagram"></i></a>
                    <a href="#" class="text-white me-3 fs-4"><i class="bi bi-twitter"></i></a>
                    <a href="#" class="text-white fs-4"><i class="bi bi-whatsapp"></i></a>
                </div>
            </div>
        </div>
        <div class="text-center py-3 bg-secondary">
            © <?php echo date('Y'); ?> Chronos Club — Todos los derechos reservados.
        </div>
    </footer>

    <!-- Bootstrap JS + Iconos -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">

    <script>
        function addProducto(id, token) {
            let url = 'clases/carrito.php'
            let formData = new FormData()
            formData.append('id', id)
            formData.append('token', token)

            fetch(url, {
                    method: 'POST',
                    body: formData,
                    mode: 'cors'
                }).then(response => response.json())
                .then(data => {
                    if (data.ok) {
                        let elemento = document.getElementById("num_cart")
                        elemento.innerHTML = data.numero
                    }
                })
        }
    </script>

</body>

</html>