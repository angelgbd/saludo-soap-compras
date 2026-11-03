package com.ejemplo.soap;

import jakarta.jws.WebService;
import java.util.LinkedHashMap;
import java.util.Map;

@WebService(
        endpointInterface = "com.ejemplo.soap.ISaludoService",
        serviceName = "SaludoService",
        portName = "SaludoServicePort"
)
public class SaludoServiceImpl implements ISaludoService {
    private static final Map<String, ProductoInventario> INVENTARIO = crearInventario();

    @Override
    public String saludar(String nombre) {
        return "Hola, " + nombre + ". Bienvenido al servicio web SOAP.";
    }

    @Override
    public int sumar(int a, int b) {
        return a + b;
    }

    /** Devuelve todo el catálogo, aunque un producto tenga inventario en cero. */
    @Override
    public Producto[] obtenerCatalogo() {
        synchronized (INVENTARIO) {
            return INVENTARIO.values().stream()
                    .map(item -> new Producto(item.id, item.nombre, item.precio))
                    .toArray(Producto[]::new);
        }
    }

    /** Comprueba todos los renglones antes de descontar; el pedido es atómico. */
    @Override
    public ResultadoPedido procesarCarrito(ItemCarrito[] carrito) {
        synchronized (INVENTARIO) {
            if (carrito == null || carrito.length == 0) {
                return new ResultadoPedido(false, "El carrito está vacío.");
            }

            Map<String, Long> cantidades = new LinkedHashMap<>();
            for (ItemCarrito item : carrito) {
                if (item == null || item.getProductoId() == null || item.getProductoId().isBlank()) {
                    return new ResultadoPedido(false, "El carrito contiene un producto inválido.");
                }
                if (item.getCantidad() <= 0 || !Double.isFinite(item.getPrecio()) || item.getPrecio() <= 0) {
                    return new ResultadoPedido(false, "La cantidad y el precio deben ser mayores que cero.");
                }

                ProductoInventario producto = INVENTARIO.get(item.getProductoId());
                if (producto == null) {
                    return new ResultadoPedido(false, "El producto " + item.getProductoId() + " no existe en el catálogo.");
                }
                if (Double.compare(item.getPrecio(), producto.precio) != 0) {
                    return new ResultadoPedido(false, "El precio de " + producto.nombre + " cambió. Actualiza el catálogo.");
                }

                long acumulado = cantidades.getOrDefault(item.getProductoId(), 0L) + item.getCantidad();
                if (acumulado > Integer.MAX_VALUE) {
                    return new ResultadoPedido(false, "La cantidad solicitada es demasiado grande.");
                }
                cantidades.put(item.getProductoId(), acumulado);
            }

            for (Map.Entry<String, Long> entrada : cantidades.entrySet()) {
                ProductoInventario producto = INVENTARIO.get(entrada.getKey());
                if (entrada.getValue() > producto.existencias) {
                    return new ResultadoPedido(false, "Inventario insuficiente para " + producto.nombre
                            + ". Disponibles: " + producto.existencias + ". No se descontó ningún producto.");
                }
            }

            cantidades.forEach((id, cantidad) -> INVENTARIO.get(id).existencias -= cantidad.intValue());
            return new ResultadoPedido(true, "Compra realizada. Inventario actualizado.");
        }
    }

    /** Copia solo para la pantalla administrativa; el inventario queda compartido entre clientes. */
    public static String[][] consultarInventario() {
        synchronized (INVENTARIO) {
            return INVENTARIO.values().stream()
                    .map(item -> new String[]{item.id, item.nombre,
                        String.format(java.util.Locale.ROOT, "%.2f", item.precio),
                        Integer.toString(item.existencias)})
                    .toArray(String[][]::new);
        }
    }

    private static Map<String, ProductoInventario> crearInventario() {
        Map<String, ProductoInventario> productos = new LinkedHashMap<>();
        productos.put("P001", new ProductoInventario("P001", "Café", 45.00, 12));
        productos.put("P002", new ProductoInventario("P002", "Té", 32.50, 10));
        productos.put("P003", new ProductoInventario("P003", "Galletas", 28.00, 8));
        productos.put("P004", new ProductoInventario("P004", "Chocolate", 39.90, 6));
        return productos;
    }

    private static final class ProductoInventario {
        private final String id;
        private final String nombre;
        private final double precio;
        private int existencias;

        private ProductoInventario(String id, String nombre, double precio, int existencias) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
            this.existencias = existencias;
        }
    }
}
