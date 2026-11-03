package com.ejemplo.soap;

import jakarta.xml.ws.Endpoint;
import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

/** Inicia el servicio SOAP y la ventana sencilla para consultar existencias. */
public class Servidor_SOAP {
    public static void main(String[] args) {
        String direccion = System.getProperty("soap.address",
                "http://0.0.0.0:8080/SaludoSOAPServer/SaludoService");
        Endpoint endpoint = Endpoint.publish(direccion, new SaludoServiceImpl());

        System.out.println("Servicio SOAP listo: http://localhost:8080/SaludoSOAPServer/SaludoService?wsdl");
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("No hay escritorio gráfico disponible; el servicio sigue activo.");
        } else {
            SwingUtilities.invokeLater(() -> VentanaInventario.mostrar(endpoint));
        }
    }
}
