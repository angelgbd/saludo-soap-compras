# Compras por SOAP

Ejemplo de compra distribuida con un cliente Java Swing, un servidor SOAP y un inventario compartido. La interfaz se mantiene básica para facilitar su uso y revisión.

## Requisitos

- Java 17 o superior
- Maven 3.8 o superior

## Ejecutar el servidor

En una terminal, desde `servidor`:

```powershell
mvn clean package
mvn exec:java
```

La ventana del servidor muestra las existencias y el servicio SOAP publica el WSDL en `http://localhost:8080/SaludoSOAPServer/SaludoService?wsdl`.

## Ejecutar el cliente

Con el servidor activo, abre otra terminal en `cliente`:

```powershell
mvn clean package
mvn exec:java
```

Se pueden abrir varios clientes; todos comparten el inventario del servidor. El cliente presenta el catálogo, permite formar un carrito y muestra mensajes gráficos de éxito y error.

Cada pedido se valida completo antes de descontar existencias. Si falta inventario para cualquier producto, no se descuenta ninguno. El inventario de ejemplo vuelve a sus cantidades iniciales cuando se reinicia el servidor.

Consulta los README de `cliente` y `servidor` para configuración adicional.
