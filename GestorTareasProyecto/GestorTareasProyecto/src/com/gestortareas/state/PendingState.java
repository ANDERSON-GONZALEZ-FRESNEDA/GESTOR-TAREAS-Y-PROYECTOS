package com.gestortareas.state;

import com.gestortareas.domain.TaskComponent;

public class PendingState implements TaskState {
    private static final long serialVersionUID = 1L;

    @Override
    public void cambiarEstado(TaskComponent tarea) {
        tarea.setEstado(new InProgressState());
    }

    @Override
    public void cambiarEstado(TaskComponent tarea, String nuevoEstado) {
        if (nuevoEstado.equalsIgnoreCase("Pendiente")) {
            return;
        }
        if (nuevoEstado.equalsIgnoreCase("En curso") || nuevoEstado.equalsIgnoreCase("EnCurso")) {
            tarea.setEstado(new InProgressState());
        } else {
            throw new IllegalStateException(
                "Transición inválida: De 'Pendiente' solo se puede pasar a 'En curso'."
            );
        }
    }

    @Override
    public boolean puedeModificar() {
        return true;
    }

    @Override
    public boolean puedeCompletarse() {
        return false;
    }

    @Override
    public String getNombreEstado() {
        return "Pendiente";
    }

    @Override
    public double getProgreso() {
        return 0.0;
    }
}
