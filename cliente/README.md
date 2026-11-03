# Cliente SOAP de compras

Cliente Java 17 con una interfaz gráfica básica para consultar el catálogo, armar un carrito y confirmar la compra.

## Iniciar

Primero inicia el proyecto `SaludoSOAPServer`. Después abre una terminal en esta carpeta y ejecuta:

```powershell
mvn clean package
mvn exec:java
```

Puedes iniciar varias instancias del cliente; todas consultan y descuentan el mismo inventario del servidor.

El cliente usa por defecto `http://localhost:8080/SaludoSOAPServer/SaludoService`. Para conectarlo a otro equipo, indica la dirección al iniciarlo:

```powershell
mvn exec:java "-Dsoap.url=http://DIRECCION-DEL-SERVIDOR:8080/SaludoSOAPServer/SaludoService" "-Dsoap.wsdl=http://DIRECCION-DEL-SERVIDOR:8080/SaludoSOAPServer/SaludoService?wsdl"
```
