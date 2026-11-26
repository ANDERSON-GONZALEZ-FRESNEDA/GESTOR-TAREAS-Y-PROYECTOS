package com.gestortareas.state;

import com.gestortareas.domain.TaskComponent;

public class InReviewState implements TaskState {
    private static final long serialVersionUID = 1L;

    @Override
    public void cambiarEstado(TaskComponent tarea) {
        tarea.setEstado(new CompletedState());
    }

    @Override
    public void cambiarEstado(TaskComponent tarea, String nuevoEstado) {
        if (nuevoEstado.equalsIgnoreCase("En revisión") || nuevoEstado.equalsIgnoreCase("En revision")
                || nuevoEstado.equalsIgnoreCase("EnRevision")) {
            return;
        }
        if (nuevoEstado.equalsIgnoreCase("Terminada")) {
            tarea.setEstado(new CompletedState());
        } else if (nuevoEstado.equalsIgnoreCase("En curso") || nuevoEstado.equalsIgnoreCase("EnCurso")) {
            tarea.setEstado(new InProgressState());
        } else {
            throw new IllegalStateException(
                "Transición inválida: Desde 'En revisión' solo puede pasar a 'Terminada' o volver a 'En curso'."
            );
        }
    }

    @Override
    public boolean puedeModificar() {
        return false; // En revisión no se puede editar el contenido (RF7)
    }

    @Override
    public boolean puedeCompletarse() {
        return true;
    }

    @Override
    public String getNombreEstado() {
        return "En revisión";
    }

    @Override
    public double getProgreso() {
        return 80.0;
    }
}
