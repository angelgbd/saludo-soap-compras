package com.ejemplo.soap;

import jakarta.xml.ws.Endpoint;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

/** Pantalla de solo lectura que refleja los descuentos hechos por todos los clientes. */
public final class VentanaInventario {
    private VentanaInventario() { }

    public static void mostrar(Endpoint endpoint) {
        if (GraphicsEnvironment.isHeadless()) return;

        JFrame ventana = new JFrame("Inventario del servidor SOAP");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(560, 330);
        ventana.setLocationByPlatform(true);

        JLabel titulo = new JLabel("  Inventario compartido — se actualiza automáticamente");
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Código", "Producto", "Precio", "Existencias"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(25);

        JButton actualizar = new JButton("Actualizar ahora");
        JLabel estado = new JLabel("Servicio SOAP activo");
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pie.add(actualizar);
        pie.add(estado);

        ventana.add(titulo, BorderLayout.NORTH);
        ventana.add(new JScrollPane(tabla), BorderLayout.CENTER);
        ventana.add(pie, BorderLayout.SOUTH);

        Runnable refrescar = () -> {
            modelo.setRowCount(0);
            for (String[] fila : SaludoServiceImpl.consultarInventario()) modelo.addRow(fila);
        };
        actualizar.addActionListener(evento -> refrescar.run());
        new Timer(750, evento -> refrescar.run()).start();
        refrescar.run();

        ventana.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                endpoint.stop();
            }
        });
        ventana.setVisible(true);
    }
}
