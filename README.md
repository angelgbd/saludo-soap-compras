Compras por SOAP

Ejemplo de compra distribuida con un cliente Java Swing, un servidor SOAP y un inventario.

Requisitos

- Java 17 o superior
- Maven 3.8 o superior

 Ejecutar el servidor

En una terminal, desde servidor:

mvn clean package
mvn exec:java


La ventana del servidor muestra las existencias y el servicio SOAP.

 Ejecutar el cliente

Con el servidor activo, abre otra terminal en cliente:

mvn clean package
mvn exec:java

Se pueden abrir varios clientes; todos comparten el inventario del servidor. El cliente presenta el catálogo, permite formar un carrito y muestra mensajes gráficos de éxito y error.


