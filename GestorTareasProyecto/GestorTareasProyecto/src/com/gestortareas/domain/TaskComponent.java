package com.gestortareas.domain;

import com.gestortareas.state.TaskState;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public interface TaskComponent extends Prototype, Serializable {
    String getId();
    String getTitulo();
    void setTitulo(String titulo);

    String getDescripcion();
    void setDescripcion(String descripcion);

    Prioridad getPrioridad();
    void setPrioridad(Prioridad prioridad);

    LocalDate getFechaLimite();
    void setFechaLimite(LocalDate fechaLimite);

    // Patrón Composite
    double getProgreso();
    void agregar(TaskComponent c);
    void eliminar(TaskComponent c);
    List<TaskComponent> getSubtareas();
    boolean esCompuesta();

    // Patrón State
    TaskState getEstadoActual();
    void setEstado(TaskState e);
    void cambiarEstado();
    void cambiarEstado(String nuevoEstado);
    String getNombreEstado();
    boolean puedeModificar();
    boolean puedeCompletarse();

    // Patrón Prototype
    @Override
    TaskComponent clonar();
}
