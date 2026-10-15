package org.example.proyectocrudg3.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.proyectocrudg3.database.ConexionBD;
import org.example.proyectocrudg3.model.Categoria;
import org.example.proyectocrudg3.model.Marca;
import org.example.proyectocrudg3.model.Producto;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductosController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private ComboBox<Marca> cmbMarca;
    @FXML private DatePicker dpFechaCreacion;
    @FXML private TextField txtDescripcion;

    @FXML private Button btnNuevo;
    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;

    @FXML private TextField txtFiltroTexto;
    @FXML private ComboBox<Categoria> cmbFiltroCategoria;
    @FXML private DatePicker dpFiltroDesde;
    @FXML private DatePicker dpFiltroHasta;
    @FXML private TextField txtPrecioMin;
    @FXML private TextField txtPrecioMax;
    @FXML private CheckBox chkConStock;
    @FXML private ComboBox<String> cmbOrden;

    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarFiltros;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colMarca;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, LocalDate> colFecha;

    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private final ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList();
    private final ObservableList<Marca> listaMarcas = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configurarTableView();
        configurarSeleccionTableView();
        cargarCategorias();
        cargarMarcas();
        cargarOpcionesOrden();
        buscarProductos();
    }

    private void configurarTableView() {
        colCodigo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCodigo()));
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colCategoria.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoria()));
        colMarca.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMarca()));
        colPrecio.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrecio()).asObject());
        colStock.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStock()).asObject());
        colFecha.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getFechaCreacion()));
        tblProductos.setItems(listaProductos);
    }

    private void configurarSeleccionTableView() {
        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarProductoEnFormulario(newVal);
            }
        });
    }

    private void cargarProductoEnFormulario(Producto p) {
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtStock.setText(String.valueOf(p.getStock()));
        dpFechaCreacion.setValue(p.getFechaCreacion());

        for (Categoria c : listaCategorias) {
            if (c.getIdCategoria() == p.getIdCategoria()) {
                cmbCategoria.setValue(c);
                break;
            }
        }

        for (Marca m : listaMarcas) {
            if (m.getIdMarca() == p.getIdMarca()) {
                cmbMarca.setValue(m);
                break;
            }
        }
    }

    private void cargarCategorias() {
        listaCategorias.clear();
        String sql = "SELECT id_categoria, nombre FROM catalogos.categorias WHERE estado = 1 ORDER BY nombre";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaCategorias.add(new Categoria(rs.getInt("id_categoria"), rs.getString("nombre")));
            }

            cmbCategoria.setItems(FXCollections.observableArrayList(listaCategorias));
            cmbFiltroCategoria.setItems(FXCollections.observableArrayList(listaCategorias));
        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudieron cargar las categorías.", e);
        }
    }

    private void cargarMarcas() {
        listaMarcas.clear();
        String sql = "SELECT id_marca, nombre FROM catalogos.marcas WHERE estado = 1 ORDER BY nombre";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaMarcas.add(new Marca(rs.getInt("id_marca"), rs.getString("nombre")));
            }

            cmbMarca.setItems(FXCollections.observableArrayList(listaMarcas));
        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudieron cargar las marcas.", e);
        }
    }

    private void cargarOpcionesOrden() {
        cmbOrden.setItems(FXCollections.observableArrayList(
                "Nombre",
                "Precio menor a mayor",
                "Precio mayor a menor",
                "Fecha más reciente",
                "Fecha más antigua",
                "Stock mayor a menor",
                "Stock menor a mayor"
        ));
        cmbOrden.setValue("Nombre");
    }

    private Producto crearProductoDesdeResultSet(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("id_producto"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getDouble("precio"),
                rs.getInt("stock"),
                rs.getDate("fecha_creacion").toLocalDate(),
                rs.getInt("id_categoria"),
                rs.getString("categoria"),
                rs.getInt("id_marca"),
                rs.getString("marca"),
                rs.getInt("estado")
        );
    }

    // ==========================================
    // OPERACIÓN CRUD: GUARDAR PRODUCTO
    // ==========================================
    @FXML
    private void guardarProducto() {
        // Valida que los campos requeridos no estén vacíos y tengan el formato numérico correcto
        if (!validarFormulario()) {
            return; // Detiene la ejecución si alguna validación del formulario falla
        }

        // Obtiene el texto del código eliminando espacios en blanco al inicio y al final
        String codigo = txtCodigo.getText().trim();

        // Verifica preventivamente en la base de datos si ya existe un registro con ese código
        if (existeCodigo(codigo, null)) {
            // Muestra una ventana de advertencia notificando que el código ya está en uso
            mostrarAlerta(Alert.AlertType.WARNING, "Código duplicado", "El código " + codigo + " ya pertenece a otro producto.");
            // Posiciona el cursor en el campo del código para que el usuario lo corrija
            txtCodigo.requestFocus();
            // Interrumpe la operación para no intentar la inserción duplicada
            return;
        }

        // Define la sentencia SQL parametrizada para insertar un nuevo registro en la tabla de productos
        String sql = """
                INSERT INTO catalogos.productos
                (codigo, nombre, descripcion, precio, stock, fecha_creacion, id_categoria, id_marca, estado)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)
                """;

        // Abre la conexión y prepara la sentencia SQL dentro de un bloque try-with-resources para su cierre automático
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asigna el código en el primer parámetro
            ps.setString(1, codigo);
            // Asigna el nombre ingresado en el segundo parámetro
            ps.setString(2, txtNombre.getText().trim());
            // Asigna la descripción ingresada en el tercer parámetro
            ps.setString(3, txtDescripcion.getText().trim());
            // Asigna el precio convertido a BigDecimal para mantener precisión monetaria en el cuarto parámetro
            ps.setBigDecimal(4, new BigDecimal(txtPrecio.getText().trim()));
            // Convierte y asigna el stock a tipo entero en el quinto parámetro
            ps.setInt(5, Integer.parseInt(txtStock.getText().trim()));
            // Convierte la fecha seleccionada en el DatePicker a java.sql.Date y la asigna al sexto parámetro
            ps.setDate(6, Date.valueOf(dpFechaCreacion.getValue()));
            // Obtiene el identificador numérico de la categoría seleccionada en el ComboBox para el séptimo parámetro
            ps.setInt(7, cmbCategoria.getValue().getIdCategoria());
            // Obtiene el identificador numérico de la marca seleccionada en el ComboBox para el octavo parámetro
            ps.setInt(8, cmbMarca.getValue().getIdMarca());

            // Ejecuta la inserción en la base de datos y retorna la cantidad de filas afectadas
            int filasAfectadas = ps.executeUpdate();

            // Si al menos una fila fue insertada exitosamente
            if (filasAfectadas > 0) {
                // Notifica al usuario que el producto se registró satisfactoriamente
                mostrarAlerta(Alert.AlertType.INFORMATION, "Registro guardado", "El producto fue registrado correctamente.");
                // Limpia las cajas de texto y controles de entrada
                limpiarFormulario();
                // Vuelve a consultar la base de datos para refrescar la tabla visual
                buscarProductos();
            }
        } catch (SQLException e) {
            // Evalúa si el código de error SQL corresponde a una violación de restricción UNIQUE (23505 en PostgreSQL)
            if ("23505".equals(e.getSQLState())) {
                // Notifica duplicidad detectada por la base de datos
                mostrarAlerta(Alert.AlertType.WARNING, "Registro duplicado", "El código ingresado ya existe.");
            } else {
                // Muestra cualquier otro error técnico proveniente de PostgreSQL
                mostrarErrorBaseDatos("No se pudo guardar el producto.", e);
            }
        }
    }

    // ==========================================
    // OPERACIÓN CRUD: ACTUALIZAR PRODUCTO
    // ==========================================
    @FXML
    private void actualizarProducto() {
        // Obtiene el producto seleccionado actualmente en la tabla visual
        Producto productoSeleccionado = tblProductos.getSelectionModel().getSelectedItem();

        // Valida que el usuario haya seleccionado una fila antes de intentar actualizar
        if (productoSeleccionado == null) {
            // Muestra mensaje indicando que debe seleccionar un registro
            mostrarAlerta(Alert.AlertType.WARNING, "Producto no seleccionado", "Seleccione un producto del TableView antes de actualizar.");
            // Termina la ejecución si no hay ningún elemento seleccionado
            return;
        }

        // Valida las reglas de formato de los campos del formulario
        if (!validarFormulario()) {
            // Termina si los datos no son válidos
            return;
        }

        // Obtiene el código escrito en el campo de texto
        String codigo = txtCodigo.getText().trim();

        // Comprueba si el código ya pertenece a otro registro que no sea el que estamos editando
        if (existeCodigo(codigo, productoSeleccionado.getIdProducto())) {
            // Notifica que el código ya existe en otro registro
            mostrarAlerta(Alert.AlertType.WARNING, "Código duplicado", "El código ingresado pertenece a otro producto.");
            // Enfoca el campo para corregir el valor
            txtCodigo.requestFocus();
            // Cancela la actualización
            return;
        }

        // Sentencia SQL parametrizada para actualizar los datos del producto según su id primario
        String sql = """
                UPDATE catalogos.productos
                SET codigo = ?, nombre = ?, descripcion = ?, precio = ?, stock = ?, fecha_creacion = ?, id_categoria = ?, id_marca = ?
                WHERE id_producto = ?
                """;

        // Establece la conexión y prepara la sentencia SQL de actualización
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asigna el código en el primer parámetro
            ps.setString(1, codigo);
            // Asigna el nombre en el segundo parámetro
            ps.setString(2, txtNombre.getText().trim());
            // Asigna la descripción en el tercer parámetro
            ps.setString(3, txtDescripcion.getText().trim());
            // Asigna el precio en el cuarto parámetro
            ps.setBigDecimal(4, new BigDecimal(txtPrecio.getText().trim()));
            // Asigna el stock en el quinto parámetro
            ps.setInt(5, Integer.parseInt(txtStock.getText().trim()));
            // Asigna la fecha de creación en el sexto parámetro
            ps.setDate(6, Date.valueOf(dpFechaCreacion.getValue()));
            // Asigna el id de categoría en el séptimo parámetro
            ps.setInt(7, cmbCategoria.getValue().getIdCategoria());
            // Asigna el id de marca en el octavo parámetro
            ps.setInt(8, cmbMarca.getValue().getIdMarca());
            // Condición WHERE: asigna el id_producto original del elemento en edición
            ps.setInt(9, productoSeleccionado.getIdProducto());

            // Ejecuta la actualización y devuelve la cantidad de registros modificados
            int filasAfectadas = ps.executeUpdate();

            // Si se actualizó el registro en la base de datos
            if (filasAfectadas > 0) {
                // Muestra alerta informando el éxito de la actualización
                mostrarAlerta(Alert.AlertType.INFORMATION, "Registro actualizado", "El producto fue actualizado correctamente.");
                // Vacía los controles del formulario
                limpiarFormulario();
                // Recarga la tabla con los datos actualizados
                buscarProductos();
            }
        } catch (SQLException e) {
            // Captura errores de clave duplicada a nivel de PostgreSQL
            if ("23505".equals(e.getSQLState())) {
                // Informa sobre código duplicado en BD
                mostrarAlerta(Alert.AlertType.WARNING, "Código duplicado", "El código ingresado ya está siendo utilizado.");
            } else {
                // Muestra error técnico de BD
                mostrarErrorBaseDatos("No se pudo actualizar el producto.", e);
            }
        }
    }

    // ==========================================
    // OPERACIÓN CRUD: ELIMINAR PRODUCTO (LÓGICO)
    // ==========================================
    @FXML
    private void eliminarProducto() {
        // Obtiene la fila seleccionada por el usuario en la tabla
        Producto productoSeleccionado = tblProductos.getSelectionModel().getSelectedItem();

        // Verifica que exista un producto seleccionado
        if (productoSeleccionado == null) {
            // Advierte al usuario que seleccione un registro
            mostrarAlerta(Alert.AlertType.WARNING, "Producto no seleccionado", "Seleccione un producto antes de eliminarlo.");
            // Cancela el flujo si no hay selección
            return;
        }

        // Construye un cuadro de diálogo para confirmar la eliminación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        // Título de la ventana emergente
        confirmacion.setTitle("Confirmar eliminación");
        // Encabezado de la advertencia
        confirmacion.setHeaderText("Eliminar producto");
        // Mensaje con el nombre del producto
        confirmacion.setContentText("¿Desea desactivar el producto " + productoSeleccionado.getNombre() + "?");

        // Muestra la ventana modal y espera la decisión del usuario
        Optional<ButtonType> respuesta = confirmacion.showAndWait();

        // Si el usuario cierra el diálogo o presiona Cancelar, aborta la operación
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        // Sentencia SQL de eliminación lógica: cambia el estado a 0 sin borrar físicamente el registro
        String sql = "UPDATE catalogos.productos SET estado = 0 WHERE id_producto = ?";

        // Prepara y ejecuta la actualización de estado
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Pasa el ID del producto seleccionado como condición del WHERE
            ps.setInt(1, productoSeleccionado.getIdProducto());

            // Ejecuta el cambio en la base de datos
            int filasAfectadas = ps.executeUpdate();

            // Si la desactivación fue exitosa
            if (filasAfectadas > 0) {
                // Notifica al usuario que el producto fue desactivado
                mostrarAlerta(Alert.AlertType.INFORMATION, "Producto eliminado", "El producto fue desactivado correctamente.");
                // Limpia los controles
                limpiarFormulario();
                // Actualiza el TableView (el producto ya no aparecerá porque estado = 0)
                buscarProductos();
            }
        } catch (SQLException e) {
            // Muestra mensaje de fallo
            mostrarErrorBaseDatos("No se pudo eliminar el producto.", e);
        }
    }

    // ==========================================
    // BÚSQUEDA PARAMETRIZADA Y FILTROS DINÁMICOS
    // ==========================================
    @FXML
    private void buscarProductos() {
        // Limpia la lista observable para vaciar los resultados anteriores
        listaProductos.clear();

        // Construye la base de la consulta con INNER JOIN para traer nombres de catálogo y filtrando solo activos
        StringBuilder sql = new StringBuilder("""
                SELECT p.id_producto, p.codigo, p.nombre, p.descripcion, p.precio, p.stock, p.fecha_creacion,
                       c.id_categoria, c.nombre AS categoria, m.id_marca, m.nombre AS marca, p.estado
                FROM catalogos.productos p
                INNER JOIN catalogos.categorias c ON p.id_categoria = c.id_categoria
                INNER JOIN catalogos.marcas m ON p.id_marca = m.id_marca
                WHERE p.estado = 1
                """);

        // Lista dinámica donde se guardan los valores de los parámetros en el mismo orden que se agregan los '?'
        List<Object> parametros = new ArrayList<>();
        // Obtiene el texto ingresado en el buscador
        String texto = txtFiltroTexto.getText().trim();

        // Filtro por coincidencia en Código o Nombre
        if (!texto.isBlank()) {
            // Agrega la condición OR utilizando ILIKE para ignorar mayúsculas y minúsculas
            sql.append(" AND (p.codigo ILIKE ? OR p.nombre ILIKE ?) ");
            // Agrega los comodines '%' para buscar coincidencias parciales
            parametros.add("%" + texto + "%");
            // Agrega el segundo comodín para la comparación de nombre
            parametros.add("%" + texto + "%");
        }

        // Filtro por Categoría
        Categoria categoria = cmbFiltroCategoria.getValue();
        // Si hay una categoría seleccionada en el filtro
        if (categoria != null) {
            // Concatena la condición para comparar por la clave foránea
            sql.append(" AND p.id_categoria = ? ");
            // Agrega el id seleccionado a los parámetros
            parametros.add(categoria.getIdCategoria());
        }

        // Filtro por Fecha Desde
        LocalDate fechaDesde = dpFiltroDesde.getValue();
        // Si se especificó una fecha inicial
        if (fechaDesde != null) {
            // Filtra registros cuya fecha de creación sea mayor o igual a la indicada
            sql.append(" AND p.fecha_creacion >= ? ");
            // Añade la fecha a los parámetros
            parametros.add(fechaDesde);
        }

        // Filtro por Fecha Hasta
        LocalDate fechaHasta = dpFiltroHasta.getValue();
        // Si se especificó una fecha final
        if (fechaHasta != null) {
            // Filtra registros cuya fecha de creación sea menor o igual a la indicada
            sql.append(" AND p.fecha_creacion <= ? ");
            // Añade la fecha a los parámetros
            parametros.add(fechaHasta);
        }

        // Valida que el rango cronológico tenga coherencia
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            // Alerta si la fecha inicial es mayor a la final
            mostrarAlerta(Alert.AlertType.WARNING, "Rango de fechas incorrecto", "La fecha inicial no puede ser posterior a la fecha final.");
            // Cancela la búsqueda si el rango está invertido
            return;
        }

        // Filtro por Precio Mínimo
        BigDecimal precioMinimo = null;
        // Si el campo de precio mínimo tiene contenido
        if (!txtPrecioMin.getText().isBlank()) {
            try {
                // Parsea el valor ingresado a BigDecimal
                precioMinimo = new BigDecimal(txtPrecioMin.getText().trim());

                // Valida que el precio mínimo no sea negativo
                if (precioMinimo.compareTo(BigDecimal.ZERO) < 0) {
                    // Advierte que el precio no puede ser negativo
                    mostrarAlerta(Alert.AlertType.WARNING, "Precio incorrecto", "El precio mínimo no puede ser negativo.");
                    // Detiene la búsqueda
                    return;
                }

                // Agrega la condición de límite inferior
                sql.append(" AND p.precio >= ? ");
                // Añade el precio a los parámetros
                parametros.add(precioMinimo);
            } catch (NumberFormatException e) {
                // Advierte formato numérico inválido
                mostrarAlerta(Alert.AlertType.WARNING, "Precio incorrecto", "El precio mínimo debe ser numérico.");
                // Enfoca la caja de texto
                txtPrecioMin.requestFocus();
                // Detiene la búsqueda
                return;
            }
        }

        // Filtro por Precio Máximo
        BigDecimal precioMaximo = null;
        // Si el campo de precio máximo tiene contenido
        if (!txtPrecioMax.getText().isBlank()) {
            try {
                // Parsea el valor ingresado a BigDecimal
                precioMaximo = new BigDecimal(txtPrecioMax.getText().trim());

                // Valida que el precio máximo no sea negativo
                if (precioMaximo.compareTo(BigDecimal.ZERO) < 0) {
                    // Advierte que el precio no puede ser negativo
                    mostrarAlerta(Alert.AlertType.WARNING, "Precio incorrecto", "El precio máximo no puede ser negativo.");
                    // Detiene la búsqueda
                    return;
                }

                // Agrega la condición de límite superior
                sql.append(" AND p.precio <= ? ");
                // Añade el precio a los parámetros
                parametros.add(precioMaximo);
            } catch (NumberFormatException e) {
                // Advierte formato numérico inválido
                mostrarAlerta(Alert.AlertType.WARNING, "Precio incorrecto", "El precio máximo debe ser numérico.");
                // Enfoca la caja de texto
                txtPrecioMax.requestFocus();
                // Detiene la búsqueda
                return;
            }
        }

        // Valida que el precio mínimo no supere al máximo
        if (precioMinimo != null && precioMaximo != null && precioMinimo.compareTo(precioMaximo) > 0) {
            // Muestra mensaje de advertencia
            mostrarAlerta(Alert.AlertType.WARNING, "Rango de precios incorrecto", "El precio mínimo no puede ser mayor que el precio máximo.");
            // Detiene la búsqueda
            return;
        }

        // Filtro para productos con existencias (stock > 0)
        if (chkConStock.isSelected()) {
            // Agrega condición para stock mayor a cero
            sql.append(" AND p.stock > 0 ");
        }

        // Whitelist de ordenamiento para evitar inyecciones SQL
        String orden = cmbOrden.getValue();
        // Si no se ha seleccionado ninguno, se define por defecto "Nombre"
        if (orden == null) {
            orden = "Nombre";
        }

        // Evalúa la opción de ordenamiento seleccionada y concatena la cláusula ORDER BY correspondiente
        switch (orden) {
            case "Precio menor a mayor" -> sql.append(" ORDER BY p.precio ASC ");
            case "Precio mayor a menor" -> sql.append(" ORDER BY p.precio DESC ");
            case "Fecha más reciente" -> sql.append(" ORDER BY p.fecha_creacion DESC ");
            case "Fecha más antigua" -> sql.append(" ORDER BY p.fecha_creacion ASC ");
            case "Stock mayor a menor" -> sql.append(" ORDER BY p.stock DESC ");
            case "Stock menor a mayor" -> sql.append(" ORDER BY p.stock ASC ");
            default -> sql.append(" ORDER BY p.nombre ASC ");
        }

        // Prepara la ejecución de la consulta dinámica
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            // Itera la lista de parámetros para asignarlos a cada '?' según su tipo de dato correspondiente
            for (int i = 0; i < parametros.size(); i++) {
                // Obtiene el valor del parámetro en la posición actual
                Object valor = parametros.get(i);

                // Evalúa si el parámetro es una fecha
                if (valor instanceof LocalDate fecha) {
                    // Convierte a java.sql.Date y lo asigna al índice correspondiente
                    ps.setDate(i + 1, Date.valueOf(fecha));
                } else if (valor instanceof BigDecimal numero) {
                    // Asigna el valor monetario como BigDecimal
                    ps.setBigDecimal(i + 1, numero);
                } else {
                    // Para texto u enteros, se asigna como objeto general
                    ps.setObject(i + 1, valor);
                }
            }

            // Ejecuta la consulta SELECT y obtiene el cursor con los resultados
            try (ResultSet rs = ps.executeQuery()) {
                // Itera sobre cada registro devuelto por PostgreSQL
                while (rs.next()) {
                    // Convierte la fila actual en una instancia de Producto y la añade a la lista observable
                    listaProductos.add(crearProductoDesdeResultSet(rs));
                }
            }

            // Vincula la lista observable con el TableView para renderizar los resultados en pantalla
            tblProductos.setItems(listaProductos);

            // Si ningún producto cumplió con los filtros, notifica al usuario
            if (listaProductos.isEmpty()) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sin resultados", "No existen productos que coincidan con los filtros aplicados.");
            }
        } catch (SQLException e) {
            // Muestra mensaje de error si ocurre un fallo al ejecutar la consulta
            mostrarErrorBaseDatos("No se pudo realizar la búsqueda.", e);
        }
    }

    @FXML
    private void limpiarFormulario() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();
        cmbCategoria.setValue(null);
        cmbMarca.setValue(null);
        dpFechaCreacion.setValue(LocalDate.now());
        txtDescripcion.clear();
        tblProductos.getSelectionModel().clearSelection();
    }

    @FXML
    private void limpiarFiltros() {
        txtFiltroTexto.clear();
        cmbFiltroCategoria.setValue(null);
        dpFiltroDesde.setValue(null);
        dpFiltroHasta.setValue(null);
        txtPrecioMin.clear();
        txtPrecioMax.clear();
        chkConStock.setSelected(false);
        cmbOrden.setValue("Nombre");
        buscarProductos();
    }

    private boolean existeCodigo(String codigo, Integer idProductoExcluir) {
        String sql = idProductoExcluir == null
                ? "SELECT COUNT(*) FROM catalogos.productos WHERE codigo = ?"
                : "SELECT COUNT(*) FROM catalogos.productos WHERE codigo = ? AND id_producto <> ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            if (idProductoExcluir != null) {
                ps.setInt(2, idProductoExcluir);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            mostrarErrorBaseDatos("Error validando duplicidad de código.", e);
        }
        return false;
    }

    private boolean validarFormulario() {
        if (txtCodigo.getText().trim().isBlank() || txtNombre.getText().trim().isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos", "El código y el nombre son obligatorios.");
            return false;
        }

        if (cmbCategoria.getValue() == null || cmbMarca.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", "Debe seleccionar una categoría y una marca.");
            return false;
        }

        if (dpFechaCreacion.getValue() == null) {
            dpFechaCreacion.setValue(LocalDate.now());
        }

        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Precio Inválido", "El precio debe ser mayor a 0.");
                return false;
            }
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Precio Inválido", "Ingrese un precio numérico válido.");
            txtPrecio.requestFocus();
            return false;
        }

        try {
            int stock = Integer.parseInt(txtStock.getText().trim());
            if (stock < 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Stock Inválido", "El stock no puede ser negativo.");
                return false;
            }
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Stock Inválido", "Ingrese un stock entero válido.");
            txtStock.requestFocus();
            return false;
        }

        return true;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarErrorBaseDatos(String mensaje, SQLException e) {
        mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", mensaje + "\n" + e.getMessage());
        e.printStackTrace();
    }
}