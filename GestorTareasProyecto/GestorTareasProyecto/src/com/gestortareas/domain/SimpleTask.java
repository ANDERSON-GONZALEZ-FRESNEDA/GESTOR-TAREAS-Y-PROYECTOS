package com.gestortareas.domain;

import com.gestortareas.state.PendingState;
import com.gestortareas.state.TaskState;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class SimpleTask implements TaskComponent {
    private static final long serialVersionUID = 1L;
    private static int contador = 1;

    private final String id;
    private String titulo;
    private String descripcion;
    private Prioridad prioridad;
    private LocalDate fechaLimite;
    private TaskState estadoActual;

    public SimpleTask(String titulo, String descripcion, Prioridad prioridad, LocalDate fechaLimite) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío.");
        }
        this.id = "T-" + (contador++);
        this.titulo = titulo.trim();
        this.descripcion = (descripcion == null) ? "" : descripcion.trim();
        this.prioridad = (prioridad == null) ? Prioridad.MEDIA : prioridad;
        this.fechaLimite = (fechaLimite == null) ? LocalDate.now().plusDays(7) : fechaLimite;
        this.estadoActual = new PendingState();
    }

    public static void actualizarContador(int siguiente) {
        if (siguiente > contador) {
            contador = siguiente;
        }
    }

    private void verificarSiEsModificable() {
        if (!puedeModificar()) {
            throw new IllegalStateException(
                "No se puede modificar la tarea '" + titulo + "' porque está en estado '" + getNombreEstado() + "'."
            );
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitulo() {
        return titulo;
    }

    @Override
    public void setTitulo(String titulo) {
        verificarSiEsModificable();
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío.");
        }
        this.titulo = titulo.trim();
    }

    @Override
    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public void setDescripcion(String descripcion) {
        verificarSiEsModificable();
        this.descripcion = (descripcion == null) ? "" : descripcion.trim();
    }

    @Override
    public Prioridad getPrioridad() {
        return prioridad;
    }

    @Override
    public void setPrioridad(Prioridad prioridad) {
        verificarSiEsModificable();
        this.prioridad = prioridad;
    }

    @Override
    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    @Override
    public void setFechaLimite(LocalDate fechaLimite) {
        verificarSiEsModificable();
        this.fechaLimite = fechaLimite;
    }

    @Override
    public double getProgreso() {
        return estadoActual.getProgreso();
    }

    @Override
    public void agregar(TaskComponent c) {
        throw new UnsupportedOperationException("Una tarea simple no puede tener subtareas.");
    }

    @Override
    public void eliminar(TaskComponent c) {
        throw new UnsupportedOperationException("Una tarea simple no tiene subtareas.");
    }

    @Override
    public List<TaskComponent> getSubtareas() {
        return Collections.emptyList();
    }

    @Override
    public boolean esCompuesta() {
        return false;
    }

    @Override
    public TaskState getEstadoActual() {
        return estadoActual;
    }

    @Override
    public void setEstado(TaskState e) {
        this.estadoActual = e;
    }

    @Override
    public void cambiarEstado() {
        estadoActual.cambiarEstado(this);
    }

    @Override
    public void cambiarEstado(String nuevoEstado) {
        estadoActual.cambiarEstado(this, nuevoEstado);
    }

    @Override
    public String getNombreEstado() {
        return estadoActual.getNombreEstado();
    }

    @Override
    public boolean puedeModificar() {
        return estadoActual.puedeModificar();
    }

    @Override
    public boolean puedeCompletarse() {
        return estadoActual.puedeCompletarse();
    }

    @Override
    public TaskComponent clonar() {
        SimpleTask copia = new SimpleTask(this.titulo, this.descripcion, this.prioridad, this.fechaLimite);
        copia.setEstado(new PendingState());
        return copia;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + titulo + " (" + getNombreEstado() + " - " + (int) getProgreso() + "%)";
    }
}
