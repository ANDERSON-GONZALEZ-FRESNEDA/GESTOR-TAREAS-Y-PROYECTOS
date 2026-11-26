package com.gestortareas;

import com.gestortareas.service.TaskService;
import com.gestortareas.ui.console.ConsolaUI;
import com.gestortareas.ui.gui.GUI;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        TaskService service = new TaskService();

        String[] opciones = {"Interfaz Gráfica (Swing)", "Interfaz de Consola"};
        int seleccion = JOptionPane.showOptionDialog(
            null,
            "¿Cómo deseas iniciar el Gestor de Tareas y Proyectos?",
            "Seleccionar Interfaz",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            opciones,
            opciones[0]
        );

        if (seleccion == 1) {
            new ConsolaUI(service).iniciar();
        } else if (seleccion == 0) {
            SwingUtilities.invokeLater(() -> new GUI(service).setVisible(true));
        }
    }
}
