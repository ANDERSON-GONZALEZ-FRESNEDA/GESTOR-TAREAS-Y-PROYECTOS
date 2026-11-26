package com.gestortareas.state;

import com.gestortareas.domain.TaskComponent;

public class CompletedState implements TaskState {
    private static final long serialVersionUID = 1L;

    @Override
    public void cambiarEstado(TaskComponent tarea) {
        throw new IllegalStateException("La tarea ya está 'Terminada' y no puede cambiar de estado.");
    }

    @Override
    public void cambiarEstado(TaskComponent tarea, String nuevoEstado) {
        if (nuevoEstado.equalsIgnoreCase("Terminada")) {
            return;
        }
        throw new IllegalStateException("La tarea ya está 'Terminada' y no puede cambiar de estado.");
    }

    @Override
    public boolean puedeModificar() {
        return false;
    }

    @Override
    public boolean puedeCompletarse() {
        return false;
    }

    @Override
    public String getNombreEstado() {
        return "Terminada";
    }

    @Override
    public double getProgreso() {
        return 100.0;
    }
}
