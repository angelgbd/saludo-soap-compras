# Servidor SOAP con inventario

Aplicación Java 17 con un servicio SOAP y una ventana sencilla para ver el inventario compartido.

## Iniciar

Abre una terminal en esta carpeta y ejecuta:

```powershell
mvn clean package
mvn exec:java
```

La ventana muestra las existencias y las actualiza automáticamente. El servicio publica el WSDL en:

```text
http://localhost:8080/SaludoSOAPServer/SaludoService?wsdl
```

Deja esta ventana y el servicio abiertos mientras usas uno o más clientes. El inventario de ejemplo vuelve a sus cantidades iniciales al reiniciar el servidor.

## Productos iniciales

| Código | Producto | Precio | Existencias |
|---|---|---:|---:|
| P001 | Café | 45.00 | 12 |
| P002 | Té | 32.50 | 10 |
| P003 | Galletas | 28.00 | 8 |
| P004 | Chocolate | 39.90 | 6 |

El catálogo SOAP devuelve todos los productos aunque alguno llegue a cero existencias. Cada carrito se valida completo antes de descontar el inventario.
