package com.ejemplo.soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.WebParam;

@WebService
public interface ISaludoService {

    @WebMethod
    String saludar(String nombre);

    @WebMethod
    int sumar(int a, int b);

    @WebMethod
    Producto[] obtenerCatalogo();

    @WebMethod
    ResultadoPedido procesarCarrito(@WebParam(name = "carrito") ItemCarrito[] carrito);
}
