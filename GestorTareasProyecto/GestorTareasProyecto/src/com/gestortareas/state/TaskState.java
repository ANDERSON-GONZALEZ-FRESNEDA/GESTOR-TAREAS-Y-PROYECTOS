package com.gestortareas.state;

import com.gestortareas.domain.TaskComponent;
import java.io.Serializable;

public interface TaskState extends Serializable {
    void cambiarEstado(TaskComponent tarea);
    void cambiarEstado(TaskComponent tarea, String nuevoEstado);
    boolean puedeModificar();
    boolean puedeCompletarse();
    String getNombreEstado();
    double getProgreso();
}
