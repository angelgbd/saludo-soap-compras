package com.ejemplo.cliente;

import com.ejemplo.stub.ISaludoService;
import com.ejemplo.stub.ItemCarrito;
import com.ejemplo.stub.Producto;
import com.ejemplo.stub.ResultadoPedido;
import com.ejemplo.stub.SaludoService;
import jakarta.xml.ws.BindingProvider;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/** Ventana sencilla para consultar el catálogo y enviar un carrito por SOAP. */
public class Main {
    private static final String DIRECCION = System.getProperty("soap.url",
            "http://localhost:8080/SaludoSOAPServer/SaludoService");
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    private final JFrame ventana = new JFrame("Tienda SOAP");
    private final DefaultTableModel catalogoModelo = modelo("Código", "Producto", "Precio");
    private final DefaultTableModel carritoModelo = modelo("Código", "Producto", "Cantidad", "Precio", "Importe");
    private final JTable catalogoTabla = new JTable(catalogoModelo);
    private final JTable carritoTabla = new JTable(carritoModelo);
    private final JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final JButton agregar = new JButton("Agregar al carrito");
    private final JButton quitar = new JButton("Quitar producto");
    private final JButton vaciar = new JButton("Vaciar carrito");
    private final JButton comprar = new JButton("Confirmar compra");
    private final JLabel estado = new JLabel("Conectando con el servidor...");
    private final List<Producto> productos = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().mostrar());
    }

    private void mostrar() {
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(760, 600);
        ventana.setLocationByPlatform(true);

        catalogoTabla.setRowHeight(24);
        carritoTabla.setRowHeight(24);
        catalogoTabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        carritoTabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        JPanel superior = new JPanel(new BorderLayout(6, 6));
        superior.setBorder(BorderFactory.createTitledBorder("Productos del catálogo"));
        superior.add(new JScrollPane(catalogoTabla), BorderLayout.CENTER);
        JPanel agregarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        agregarPanel.add(new JLabel("Cantidad:"));
        agregarPanel.add(cantidad);
        agregarPanel.add(agregar);
        superior.add(agregarPanel, BorderLayout.SOUTH);

        JPanel inferior = new JPanel(new BorderLayout(6, 6));
        inferior.setBorder(BorderFactory.createTitledBorder("Mi carrito"));
        inferior.add(new JScrollPane(carritoTabla), BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(quitar);
        acciones.add(vaciar);
        acciones.add(comprar);
        inferior.add(acciones, BorderLayout.SOUTH);

        JPanel contenido = new JPanel(new GridLayout(2, 1, 8, 8));
        contenido.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        contenido.add(superior);
        contenido.add(inferior);
        ventana.add(contenido, BorderLayout.CENTER);
        estado.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
        ventana.add(estado, BorderLayout.SOUTH);

        agregar.addActionListener(evento -> agregarProducto());
        quitar.addActionListener(evento -> quitarProducto());
        vaciar.addActionListener(evento -> {
            carritoModelo.setRowCount(0);
            actualizarBotones(true);
        });
        comprar.addActionListener(evento -> confirmarCompra());
        actualizarBotones(false);
        ventana.setVisible(true);
        cargarCatalogo();
    }

    private void cargarCatalogo() {
        estado.setText("Cargando catálogo...");
        new SwingWorker<List<Producto>, Void>() {
            @Override protected List<Producto> doInBackground() throws Exception {
                return crearPuerto().obtenerCatalogo();
            }

            @Override protected void done() {
                try {
                    productos.clear();
                    productos.addAll(get());
                    catalogoModelo.setRowCount(0);
                    for (Producto producto : productos) {
                        catalogoModelo.addRow(new Object[]{producto.getId(), producto.getNombre(),
                            MONEDA.format(producto.getPrecio())});
                    }
                    estado.setText("Catálogo listo. El inventario se confirma al realizar la compra.");
                    actualizarBotones(true);
                } catch (Exception ex) {
                    estado.setText("No se pudo conectar con el servidor SOAP.");
                    JOptionPane.showMessageDialog(ventana,
                            "No se pudo cargar el catálogo. Verifica que el servidor esté iniciado.\n\n" + mensaje(ex),
                            "Error de conexión", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void agregarProducto() {
        int fila = catalogoTabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(ventana, "Selecciona un producto del catálogo.",
                    "Falta seleccionar", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String id = String.valueOf(catalogoModelo.getValueAt(fila, 0));
        Producto producto = productos.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
        if (producto == null) return;

        int unidades = (Integer) cantidad.getValue();
        for (int i = 0; i < carritoModelo.getRowCount(); i++) {
            if (id.equals(carritoModelo.getValueAt(i, 0))) {
                int nuevaCantidad = (Integer) carritoModelo.getValueAt(i, 2) + unidades;
                carritoModelo.setValueAt(nuevaCantidad, i, 2);
                carritoModelo.setValueAt(MONEDA.format(producto.getPrecio() * nuevaCantidad), i, 4);
                actualizarBotones(true);
                return;
            }
        }
        carritoModelo.addRow(new Object[]{producto.getId(), producto.getNombre(), unidades,
            MONEDA.format(producto.getPrecio()), MONEDA.format(producto.getPrecio() * unidades)});
        carritoTabla.setRowSelectionInterval(carritoModelo.getRowCount() - 1, carritoModelo.getRowCount() - 1);
        actualizarBotones(true);
    }

    private void quitarProducto() {
        int fila = carritoTabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(ventana, "Selecciona un producto del carrito.",
                    "Falta seleccionar", JOptionPane.ERROR_MESSAGE);
            return;
        }
        carritoModelo.removeRow(fila);
        actualizarBotones(true);
    }

    private void confirmarCompra() {
        if (carritoModelo.getRowCount() == 0) {
            JOptionPane.showMessageDialog(ventana, "Agrega al menos un producto antes de confirmar.",
                    "Carrito vacío", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<ItemCarrito> items = new ArrayList<>();
        for (int i = 0; i < carritoModelo.getRowCount(); i++) {
            String id = String.valueOf(carritoModelo.getValueAt(i, 0));
            int unidades = (Integer) carritoModelo.getValueAt(i, 2);
            Producto producto = productos.stream().filter(p -> p.getId().equals(id)).findFirst().orElseThrow();
            ItemCarrito item = new ItemCarrito();
            item.setProductoId(id);
            item.setCantidad(unidades);
            item.setPrecio(producto.getPrecio());
            items.add(item);
        }

        actualizarBotones(false);
        estado.setText("Enviando carrito al servidor...");
        new SwingWorker<ResultadoPedido, Void>() {
            @Override protected ResultadoPedido doInBackground() throws Exception {
                return crearPuerto().procesarCarrito(items);
            }

            @Override protected void done() {
                actualizarBotones(true);
                try {
                    ResultadoPedido resultado = get();
                    if (resultado.isExitoso()) {
                        carritoModelo.setRowCount(0);
                        actualizarBotones(true);
                    }
                    estado.setText(resultado.getMensaje());
                    JOptionPane.showMessageDialog(ventana, resultado.getMensaje(),
                            resultado.isExitoso() ? "Compra realizada" : "No se pudo completar",
                            resultado.isExitoso() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    estado.setText("No se pudo completar la compra.");
                    JOptionPane.showMessageDialog(ventana,
                            "No se pudo enviar el carrito. Verifica la conexión con el servidor.\n\n" + mensaje(ex),
                            "Error de conexión", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private ISaludoService crearPuerto() throws Exception {
        String wsdl = System.getProperty("soap.wsdl", DIRECCION + "?wsdl");
        SaludoService servicio = new SaludoService(new URL(wsdl));
        ISaludoService puerto = servicio.getSaludoServicePort();
        ((BindingProvider) puerto).getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY, DIRECCION);
        ((BindingProvider) puerto).getRequestContext().put("com.sun.xml.ws.connect.timeout", 5000);
        ((BindingProvider) puerto).getRequestContext().put("com.sun.xml.ws.request.timeout", 10000);
        return puerto;
    }

    private void actualizarBotones(boolean habilitado) {
        boolean conCatalogo = habilitado && !productos.isEmpty();
        agregar.setEnabled(conCatalogo);
        quitar.setEnabled(habilitado && carritoModelo.getRowCount() > 0);
        vaciar.setEnabled(habilitado && carritoModelo.getRowCount() > 0);
        comprar.setEnabled(habilitado && carritoModelo.getRowCount() > 0);
    }

    private static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private static String mensaje(Exception ex) {
        Throwable causa = ex;
        while (causa.getCause() != null) causa = causa.getCause();
        return causa.getMessage() == null ? causa.getClass().getSimpleName() : causa.getMessage();
    }
}
