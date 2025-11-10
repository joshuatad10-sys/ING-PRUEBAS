<?php
    define("CLIENT_ID", "AUSqjaUVIRUlhGIWZvvQRa9FM-PrDTCORqkruL6BTnPNwDO-HsJ9bGx_jTDsTQFPwm50tG4ZHhTkocf9");
    define("CURRENCY", "MXN");
    define("KEY_TOKEN", "ABC.drp-87");
    define("MONEDA", "$");
    

    session_start();

    $num_cart = 0;
    if(isset($_SESSION['carrito']['productos'])){
        $num_cart = count($_SESSION['carrito']['productos']);
    } 
?>  