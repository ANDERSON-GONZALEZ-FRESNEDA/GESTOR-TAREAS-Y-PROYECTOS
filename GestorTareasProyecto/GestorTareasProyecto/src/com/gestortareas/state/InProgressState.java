package com.gestortareas.state;

import com.gestortareas.domain.TaskComponent;

public class InProgressState implements TaskState {
    private static final long serialVersionUID = 1L;

    @Override
    public void cambiarEstado(TaskComponent tarea) {
        tarea.setEstado(new InReviewState());
    }

    @Override
    public void cambiarEstado(TaskComponent tarea, String nuevoEstado) {
        if (nuevoEstado.equalsIgnoreCase("En curso") || nuevoEstado.equalsIgnoreCase("EnCurso")) {
            return;
        }
        if (nuevoEstado.equalsIgnoreCase("En revisión") || nuevoEstado.equalsIgnoreCase("En revision")
                || nuevoEstado.equalsIgnoreCase("EnRevision")) {
            tarea.setEstado(new InReviewState());
        } else if (nuevoEstado.equalsIgnoreCase("Pendiente")) {
            tarea.setEstado(new PendingState());
        } else {
            throw new IllegalStateException(
                "Transición inválida: De 'En curso' debe pasar a 'En revisión' antes de 'Terminada'."
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
        return "En curso";
    }

    @Override
    public double getProgreso() {
        return 50.0;
    }
}
