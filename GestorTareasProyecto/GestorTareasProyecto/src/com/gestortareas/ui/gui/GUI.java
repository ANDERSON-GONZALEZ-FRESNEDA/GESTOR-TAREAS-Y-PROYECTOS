package com.gestortareas.ui.gui;

import com.gestortareas.domain.CompositeTask;
import com.gestortareas.domain.Prioridad;
import com.gestortareas.domain.SimpleTask;
import com.gestortareas.domain.TaskComponent;
import com.gestortareas.service.TaskService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class GUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private final transient TaskService service;

    // Árbol principal de tareas (Composite)
    private JTree arbolTareas;
    private DefaultTreeModel modeloArbol;
    private DefaultMutableTreeNode nodoRaiz;

    // Panel de detalle de la tarea seleccionada
    private JLabel lblDetalleTitulo;
    private JLabel lblDetalleTipo;
    private JLabel lblDetalleEstado;
    private JLabel lblDetallePrioridad;
    private JLabel lblDetalleFecha;
    private JLabel lblDetalleEditable;
    private JTextArea txtDetalleDescripcion;
    private JProgressBar barraProgreso;

    // Panel de plantillas (Prototype)
    private DefaultListModel<TaskComponent> modeloPlantillas;
    private JList<TaskComponent> listaPlantillas;

    public GUI(TaskService service) {
        this.service = service;
        aplicarEstiloSistema();

        setTitle("Gestor de Tareas y Proyectos");
        setSize(1020, 620);
        setMinimumSize(new Dimension(880, 520));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. Encabezado superior limpio
        JPanel panelEncabezado = new JPanel(new BorderLayout());
        panelEncabezado.setBackground(new Color(33, 47, 61));
        panelEncabezado.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel panelTitulos = new JPanel(new GridLayout(2, 1));
        panelTitulos.setOpaque(false);
        JLabel lblTituloApp = new JLabel("Gestor de Tareas y Proyectos");
        lblTituloApp.setForeground(Color.WHITE);
        lblTituloApp.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel lblSubtitulo = new JLabel("Administra tareas simples, proyectos compuestos y plantillas");
        lblSubtitulo.setForeground(new Color(200, 210, 220));
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));

        panelTitulos.add(lblTituloApp);
        panelTitulos.add(lblSubtitulo);

        JButton btnGuardar = new JButton("Guardar Datos");
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> {
            try {
                service.guardarDatos();
                JOptionPane.showMessageDialog(this, "Datos guardados correctamente en 'datos_tareas.dat'.", "Guardado", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        });

        panelEncabezado.add(panelTitulos, BorderLayout.WEST);
        panelEncabezado.add(btnGuardar, BorderLayout.EAST);
        add(panelEncabezado, BorderLayout.NORTH);

        // 2. Panel izquierdo/central: Árbol de tareas + Botones de acción
        JPanel panelCentral = new JPanel(new BorderLayout(8, 8));
        panelCentral.setBorder(new EmptyBorder(0, 12, 12, 0));

        nodoRaiz = new DefaultMutableTreeNode("Proyectos y Tareas");
        modeloArbol = new DefaultTreeModel(nodoRaiz);
        arbolTareas = new JTree(modeloArbol);
        arbolTareas.setRowHeight(26);
        arbolTareas.setFont(new Font("SansSerif", Font.PLAIN, 13));
        arbolTareas.setCellRenderer(new RenderizadorTareas());
        arbolTareas.addTreeSelectionListener(e -> actualizarPanelDetalle());

        JScrollPane scrollArbol = new JScrollPane(arbolTareas);
        scrollArbol.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 205, 210)),
            "Lista de Tareas y Subtareas",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 13)
        ));
        panelCentral.add(scrollArbol, BorderLayout.CENTER);

        // Barra inferior de acciones de tareas
        JPanel panelAcciones = new JPanel(new GridLayout(2, 3, 8, 8));
        panelAcciones.setBorder(new EmptyBorder(6, 0, 0, 0));

        JButton btnSimple = crearBoton("+ Tarea Simple");
        JButton btnCompuesta = crearBoton("+ Tarea Compuesta");
        JButton btnSubtarea = crearBoton("+ Agregar Subtarea");
        JButton btnEstado = crearBoton("Cambiar Estado");
        JButton btnEditar = crearBoton("Editar Tarea");
        JButton btnEliminar = crearBoton("Eliminar Seleccionada");

        btnSimple.addActionListener(e -> abrirDialogoNuevaTarea(false));
        btnCompuesta.addActionListener(e -> abrirDialogoNuevaTarea(true));
        btnSubtarea.addActionListener(e -> abrirDialogoAgregarSubtarea());
        btnEstado.addActionListener(e -> cambiarEstadoSeleccionada());
        btnEditar.addActionListener(e -> editarTareaSeleccionada());
        btnEliminar.addActionListener(e -> eliminarSeleccionada());

        panelAcciones.add(btnSimple);
        panelAcciones.add(btnCompuesta);
        panelAcciones.add(btnSubtarea);
        panelAcciones.add(btnEstado);
        panelAcciones.add(btnEditar);
        panelAcciones.add(btnEliminar);

        panelCentral.add(panelAcciones, BorderLayout.SOUTH);
        add(panelCentral, BorderLayout.CENTER);

        // 3. Panel derecho: Detalle de tarea + Plantillas (Prototype)
        JPanel panelDerecho = new JPanel(new BorderLayout(8, 10));
        panelDerecho.setPreferredSize(new Dimension(350, 500));
        panelDerecho.setBorder(new EmptyBorder(0, 0, 12, 12));

        // 3A. Tarjeta de detalle
        JPanel panelDetalle = new JPanel(new BorderLayout(6, 6));
        panelDetalle.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 205, 210)),
            "Detalle de la Tarea",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 13)
        ));

        JPanel gridInfo = new JPanel(new GridLayout(6, 1, 3, 3));
        gridInfo.setBorder(new EmptyBorder(4, 8, 4, 8));
        lblDetalleTitulo = new JLabel("Selecciona una tarea del árbol");
        lblDetalleTitulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblDetalleTipo = new JLabel("Tipo: -");
        lblDetalleEstado = new JLabel("Estado: -");
        lblDetallePrioridad = new JLabel("Prioridad: -");
        lblDetalleFecha = new JLabel("Fecha límite: -");
        lblDetalleEditable = new JLabel("Edición: -");

        gridInfo.add(lblDetalleTitulo);
        gridInfo.add(lblDetalleTipo);
        gridInfo.add(lblDetalleEstado);
        gridInfo.add(lblDetallePrioridad);
        gridInfo.add(lblDetalleFecha);
        gridInfo.add(lblDetalleEditable);

        txtDetalleDescripcion = new JTextArea(3, 20);
        txtDetalleDescripcion.setEditable(false);
        txtDetalleDescripcion.setLineWrap(true);
        txtDetalleDescripcion.setWrapStyleWord(true);
        txtDetalleDescripcion.setBackground(new Color(248, 249, 250));
        txtDetalleDescripcion.setBorder(BorderFactory.createTitledBorder("Descripción"));

        barraProgreso = new JProgressBar(0, 100);
        barraProgreso.setStringPainted(true);
        barraProgreso.setPreferredSize(new Dimension(200, 22));

        panelDetalle.add(gridInfo, BorderLayout.NORTH);
        panelDetalle.add(new JScrollPane(txtDetalleDescripcion), BorderLayout.CENTER);
        panelDetalle.add(barraProgreso, BorderLayout.SOUTH);

        panelDerecho.add(panelDetalle, BorderLayout.NORTH);

        // 3B. Panel de plantillas
        JPanel panelPlantillas = new JPanel(new BorderLayout(6, 6));
        panelPlantillas.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 205, 210)),
            "Plantillas Reutilizables (Prototype)",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 13)
        ));

        modeloPlantillas = new DefaultListModel<>();
        listaPlantillas = new JList<>(modeloPlantillas);
        listaPlantillas.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panelPlantillas.add(new JScrollPane(listaPlantillas), BorderLayout.CENTER);

        JPanel panelBotonesPlantilla = new JPanel(new GridLayout(3, 1, 5, 5));
        JButton btnGuardarPlantilla = crearBoton("Guardar Tarea como Plantilla");
        JButton btnUsarPlantilla = crearBoton("Crear Tarea desde Plantilla");
        JButton btnEliminarPlantilla = crearBoton("Eliminar Plantilla");

        btnGuardarPlantilla.addActionListener(e -> guardarComoPlantilla());
        btnUsarPlantilla.addActionListener(e -> instanciarPlantilla());
        btnEliminarPlantilla.addActionListener(e -> eliminarPlantillaSeleccionada());

        panelBotonesPlantilla.add(btnGuardarPlantilla);
        panelBotonesPlantilla.add(btnUsarPlantilla);
        panelBotonesPlantilla.add(btnEliminarPlantilla);
        panelPlantillas.add(panelBotonesPlantilla, BorderLayout.SOUTH);

        panelDerecho.add(panelPlantillas, BorderLayout.CENTER);
        add(panelDerecho, BorderLayout.EAST);

        refrescarVista(null);
    }

    private void aplicarEstiloSistema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    private JButton crearBoton(String texto) {
        JButton btn = new JButton(texto);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return btn;
    }

    private TaskComponent getSeleccionada() {
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) arbolTareas.getLastSelectedPathComponent();
        if (nodo != null && nodo.getUserObject() instanceof TaskComponent) {
            return (TaskComponent) nodo.getUserObject();
        }
        return null;
    }

    private void actualizarPanelDetalle() {
        TaskComponent t = getSeleccionada();
        if (t == null) {
            lblDetalleTitulo.setText("Selecciona una tarea del árbol");
            lblDetalleTipo.setText("Tipo: -");
            lblDetalleEstado.setText("Estado: -");
            lblDetallePrioridad.setText("Prioridad: -");
            lblDetalleFecha.setText("Fecha límite: -");
            lblDetalleEditable.setText("Edición: -");
            txtDetalleDescripcion.setText("");
            barraProgreso.setValue(0);
            barraProgreso.setString("0%");
            return;
        }

        lblDetalleTitulo.setText("[" + t.getId() + "] " + t.getTitulo());
        lblDetalleTipo.setText("Tipo: " + (t.esCompuesta() ? "Tarea Compuesta (" + t.getSubtareas().size() + " subtareas)" : "Tarea Simple"));
        lblDetalleEstado.setText("Estado: " + t.getNombreEstado());
        lblDetallePrioridad.setText("Prioridad: " + t.getPrioridad());
        lblDetalleFecha.setText("Fecha límite: " + t.getFechaLimite());
        lblDetalleEditable.setText("Edición: " + (t.puedeModificar() ? "Permitida" : "Bloqueada (" + t.getNombreEstado() + ")"));
        txtDetalleDescripcion.setText(t.getDescripcion().isEmpty() ? "(Sin descripción)" : t.getDescripcion());

        int porcentaje = (int) Math.round(t.getProgreso());
        barraProgreso.setValue(porcentaje);
        barraProgreso.setString("Progreso: " + porcentaje + "%");
    }

    private void abrirDialogoNuevaTarea(boolean esCompuesta) {
        JTextField txtTitulo = new JTextField();
        JTextField txtDesc = new JTextField();
        JComboBox<Prioridad> cmbPrio = new JComboBox<>(Prioridad.values());
        JTextField txtFecha = new JTextField(LocalDate.now().plusDays(7).toString());

        Object[] campos = {
            "Título:", txtTitulo,
            "Descripción:", txtDesc,
            "Prioridad:", cmbPrio,
            "Fecha límite (AAAA-MM-DD):", txtFecha
        };

        String tituloVentana = esCompuesta ? "Nueva Tarea Compuesta" : "Nueva Tarea Simple";
        int res = JOptionPane.showConfirmDialog(this, campos, tituloVentana, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String titulo = txtTitulo.getText();
                String desc = txtDesc.getText();
                Prioridad prio = (Prioridad) cmbPrio.getSelectedItem();
                LocalDate fecha = parsearFecha(txtFecha.getText());

                TaskComponent creada = esCompuesta
                    ? service.crearTareaCompuesta(titulo, desc, prio, fecha)
                    : service.crearTarea(titulo, desc, prio, fecha);

                refrescarVista(creada.getId());
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        }
    }

    private void abrirDialogoAgregarSubtarea() {
        List<CompositeTask> compuestas = service.getTareasCompuestas();
        if (compuestas.isEmpty()) {
            mostrarError("Primero debes crear al menos una Tarea Compuesta para poder agregarle subtareas.");
            return;
        }

        JComboBox<CompositeTask> cmbPadre = new JComboBox<>(compuestas.toArray(new CompositeTask[0]));
        TaskComponent seleccionada = getSeleccionada();
        if (seleccionada != null && seleccionada.esCompuesta()) {
            cmbPadre.setSelectedItem(seleccionada);
        }

        String[] modos = {"Crear una subtarea nueva", "Mover una tarea principal existente"};
        JComboBox<String> cmbModo = new JComboBox<>(modos);

        // Campos para nueva subtarea
        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Simple", "Compuesta"});
        JTextField txtTitulo = new JTextField();
        JTextField txtDesc = new JTextField();
        JComboBox<Prioridad> cmbPrio = new JComboBox<>(Prioridad.values());
        JTextField txtFecha = new JTextField(LocalDate.now().plusDays(7).toString());

        // Combo para tarea existente
        JComboBox<TaskComponent> cmbExistente = new JComboBox<>(service.getTareas().toArray(new TaskComponent[0]));
        cmbExistente.setEnabled(false);

        cmbModo.addActionListener(e -> {
            boolean crearNueva = cmbModo.getSelectedIndex() == 0;
            cmbTipo.setEnabled(crearNueva);
            txtTitulo.setEnabled(crearNueva);
            txtDesc.setEnabled(crearNueva);
            cmbPrio.setEnabled(crearNueva);
            txtFecha.setEnabled(crearNueva);
            cmbExistente.setEnabled(!crearNueva);
        });

        Object[] campos = {
            "Tarea Compuesta Padre:", cmbPadre,
            "¿Qué deseas hacer?:", cmbModo,
            "--- Si creas una nueva ---",
            "Tipo de subtarea:", cmbTipo,
            "Título:", txtTitulo,
            "Descripción:", txtDesc,
            "Prioridad:", cmbPrio,
            "Fecha límite (AAAA-MM-DD):", txtFecha,
            "--- Si mueves una existente ---",
            "Tarea existente a mover:", cmbExistente
        };

        int res = JOptionPane.showConfirmDialog(this, campos, "Agregar Subtarea", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                CompositeTask padre = (CompositeTask) cmbPadre.getSelectedItem();
                if (padre == null) {
                    return;
                }

                if (cmbModo.getSelectedIndex() == 0) {
                    String titulo = txtTitulo.getText();
                    String desc = txtDesc.getText();
                    Prioridad prio = (Prioridad) cmbPrio.getSelectedItem();
                    LocalDate fecha = parsearFecha(txtFecha.getText());
                    boolean compuesta = cmbTipo.getSelectedIndex() == 1;

                    TaskComponent sub = compuesta
                        ? new CompositeTask(titulo, desc, prio, fecha)
                        : new SimpleTask(titulo, desc, prio, fecha);
                    service.agregarSubtarea(padre.getId(), sub);
                    refrescarVista(sub.getId());
                } else {
                    TaskComponent hija = (TaskComponent) cmbExistente.getSelectedItem();
                    if (hija == null) {
                        mostrarError("No hay tareas disponibles para mover.");
                        return;
                    }
                    service.moverComoSubtarea(padre.getId(), hija.getId());
                    refrescarVista(hija.getId());
                }
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        }
    }

    private void cambiarEstadoSeleccionada() {
        TaskComponent t = getSeleccionada();
        if (t == null) {
            mostrarError("Selecciona primero una tarea o subtarea en el árbol.");
            return;
        }

        String[] estados = {"Pendiente", "En curso", "En revisión", "Terminada"};
        JComboBox<String> cmbEstados = new JComboBox<>(estados);
        cmbEstados.setSelectedItem(t.getNombreEstado());

        Object[] mensaje = {
            "Tarea: " + t.getTitulo(),
            "Estado actual: " + t.getNombreEstado(),
            "Flujo permitido: Pendiente → En curso → En revisión → Terminada",
            "Nuevo estado:", cmbEstados
        };

        int res = JOptionPane.showConfirmDialog(this, mensaje, "Cambiar Estado", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String elegido = (String) cmbEstados.getSelectedItem();
            try {
                service.cambiarEstadoTarea(t.getId(), elegido);
                refrescarVista(t.getId());
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        }
    }

    private void editarTareaSeleccionada() {
        TaskComponent t = getSeleccionada();
        if (t == null) {
            mostrarError("Selecciona primero la tarea que deseas editar.");
            return;
        }
        if (!t.puedeModificar()) {
            mostrarError("No se puede editar '" + t.getTitulo() + "' porque está en estado '" + t.getNombreEstado() + "'.");
            return;
        }

        JTextField txtTitulo = new JTextField(t.getTitulo());
        JTextField txtDesc = new JTextField(t.getDescripcion());
        JComboBox<Prioridad> cmbPrio = new JComboBox<>(Prioridad.values());
        cmbPrio.setSelectedItem(t.getPrioridad());
        JTextField txtFecha = new JTextField(t.getFechaLimite().toString());

        Object[] campos = {
            "Título:", txtTitulo,
            "Descripción:", txtDesc,
            "Prioridad:", cmbPrio,
            "Fecha límite (AAAA-MM-DD):", txtFecha
        };

        int res = JOptionPane.showConfirmDialog(this, campos, "Editar Tarea", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                service.editarTarea(
                    t.getId(),
                    txtTitulo.getText(),
                    txtDesc.getText(),
                    (Prioridad) cmbPrio.getSelectedItem(),
                    parsearFecha(txtFecha.getText())
                );
                refrescarVista(t.getId());
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        }
    }

    private void eliminarSeleccionada() {
        DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) arbolTareas.getLastSelectedPathComponent();
        if (nodoSeleccionado == null || !(nodoSeleccionado.getUserObject() instanceof TaskComponent)) {
            mostrarError("Selecciona una tarea o subtarea del árbol para eliminarla.");
            return;
        }

        TaskComponent tarea = (TaskComponent) nodoSeleccionado.getUserObject();
        DefaultMutableTreeNode nodoPadre = (DefaultMutableTreeNode) nodoSeleccionado.getParent();
        boolean esSubtarea = nodoPadre != null && (nodoPadre.getUserObject() instanceof TaskComponent);

        String pregunta = esSubtarea
            ? "¿Deseas eliminar la subtarea '" + tarea.getTitulo() + "' de '" + ((TaskComponent) nodoPadre.getUserObject()).getTitulo() + "'?"
            : "¿Deseas eliminar la tarea '" + tarea.getTitulo() + "'?";

        int confirmar = JOptionPane.showConfirmDialog(this, pregunta, "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (esSubtarea) {
                TaskComponent padre = (TaskComponent) nodoPadre.getUserObject();
                service.eliminarSubtarea(padre.getId(), tarea.getId());
                refrescarVista(padre.getId());
            } else {
                service.eliminarTarea(tarea.getId());
                refrescarVista(null);
            }
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void guardarComoPlantilla() {
        TaskComponent t = getSeleccionada();
        if (t == null) {
            mostrarError("Selecciona primero una tarea en el árbol para guardarla como plantilla.");
            return;
        }

        String nombre = (String) JOptionPane.showInputDialog(
            this,
            "Nombre de la plantilla:",
            "Guardar como Plantilla",
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            "Plantilla - " + t.getTitulo()
        );

        if (nombre != null) {
            try {
                service.guardarComoPlantilla(t.getId(), nombre);
                refrescarVista(t.getId());
                JOptionPane.showMessageDialog(this, "Plantilla guardada correctamente.", "Plantilla", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        }
    }

    private void instanciarPlantilla() {
        TaskComponent plantilla = listaPlantillas.getSelectedValue();
        if (plantilla == null) {
            mostrarError("Selecciona una plantilla de la lista de la derecha.");
            return;
        }
        try {
            TaskComponent nueva = service.crearDesdePlantilla(plantilla.getId());
            refrescarVista(nueva.getId());
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void eliminarPlantillaSeleccionada() {
        TaskComponent plantilla = listaPlantillas.getSelectedValue();
        if (plantilla == null) {
            mostrarError("Selecciona una plantilla de la lista para eliminarla.");
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(
            this,
            "¿Eliminar la plantilla '" + plantilla.getTitulo() + "'?",
            "Eliminar Plantilla",
            JOptionPane.YES_NO_OPTION
        );
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                service.eliminarPlantilla(plantilla.getId());
                refrescarVista(null);
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        }
    }

    private LocalDate parsearFecha(String texto) {
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido. Usa AAAA-MM-DD (ejemplo: " + LocalDate.now().plusDays(5) + ").");
        }
    }

    private void refrescarVista(String idASeleccionar) {
        nodoRaiz.removeAllChildren();
        for (TaskComponent t : service.getTareas()) {
            nodoRaiz.add(crearNodo(t));
        }
        modeloArbol.reload();

        for (int i = 0; i < arbolTareas.getRowCount(); i++) {
            arbolTareas.expandRow(i);
        }

        if (idASeleccionar != null) {
            seleccionarNodoPorId(nodoRaiz, idASeleccionar);
        }

        modeloPlantillas.clear();
        for (TaskComponent p : service.getPlantillas()) {
            modeloPlantillas.addElement(p);
        }

        actualizarPanelDetalle();
    }

    private boolean seleccionarNodoPorId(DefaultMutableTreeNode actual, String idBuscado) {
        if (actual.getUserObject() instanceof TaskComponent) {
            TaskComponent t = (TaskComponent) actual.getUserObject();
            if (t.getId().equalsIgnoreCase(idBuscado)) {
                TreePath path = new TreePath(actual.getPath());
                arbolTareas.setSelectionPath(path);
                arbolTareas.scrollPathToVisible(path);
                return true;
            }
        }
        for (int i = 0; i < actual.getChildCount(); i++) {
            if (seleccionarNodoPorId((DefaultMutableTreeNode) actual.getChildAt(i), idBuscado)) {
                return true;
            }
        }
        return false;
    }

    private DefaultMutableTreeNode crearNodo(TaskComponent t) {
        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(t);
        if (t.esCompuesta()) {
            for (TaskComponent sub : t.getSubtareas()) {
                nodo.add(crearNodo(sub));
            }
        }
        return nodo;
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    // Renderizador sencillo para mostrar colores según el estado de cada tarea
    private static class RenderizadorTareas extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (value instanceof DefaultMutableTreeNode) {
                Object obj = ((DefaultMutableTreeNode) value).getUserObject();
                if (obj instanceof TaskComponent) {
                    TaskComponent t = (TaskComponent) obj;
                    String tipo = t.esCompuesta() ? "[Compuesta] " : "";
                    setText(tipo + "[" + t.getId() + "] " + t.getTitulo() + "  —  " + t.getNombreEstado() + " (" + (int) t.getProgreso() + "%)");

                    if (!sel) {
                        switch (t.getNombreEstado()) {
                            case "Terminada":
                                setForeground(new Color(25, 135, 84));
                                break;
                            case "En revisión":
                                setForeground(new Color(180, 95, 6));
                                break;
                            case "En curso":
                                setForeground(new Color(13, 110, 253));
                                break;
                            default:
                                setForeground(new Color(70, 70, 70));
                                break;
                        }
                    }
                }
            }
            return this;
        }
    }
}
