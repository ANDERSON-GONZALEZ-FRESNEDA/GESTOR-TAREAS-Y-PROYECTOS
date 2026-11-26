package com.gestortareas.domain;

import com.gestortareas.state.CompletedState;
import com.gestortareas.state.InProgressState;
import com.gestortareas.state.InReviewState;
import com.gestortareas.state.PendingState;
import com.gestortareas.state.TaskState;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CompositeTask implements TaskComponent {
    private static final long serialVersionUID = 1L;
    private static int contador = 1;

    private final String id;
    private String titulo;
    private String descripcion;
    private Prioridad prioridad;
    private LocalDate fechaLimite;
    private TaskState estadoActual;
    private final List<TaskComponent> subtareas;

    public CompositeTask(String titulo, String descripcion, Prioridad prioridad, LocalDate fechaLimite) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío.");
        }
        this.id = "C-" + (contador++);
        this.titulo = titulo.trim();
        this.descripcion = (descripcion == null) ? "" : descripcion.trim();
        this.prioridad = (prioridad == null) ? Prioridad.MEDIA : prioridad;
        this.fechaLimite = (fechaLimite == null) ? LocalDate.now().plusDays(7) : fechaLimite;
        this.estadoActual = new PendingState();
        this.subtareas = new ArrayList<>();
    }

    public static void actualizarContador(int siguiente) {
        if (siguiente > contador) {
            contador = siguiente;
        }
    }

    private void verificarSiEsModificable() {
        if (!puedeModificar()) {
            throw new IllegalStateException(
                "No se puede modificar la tarea compuesta '" + titulo + "' porque está en estado '" + getNombreEstado() + "'."
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
    public void agregar(TaskComponent c) {
        verificarSiEsModificable();
        if (c == null || c.getId().equals(this.id)) {
            throw new IllegalArgumentException("Subtarea inválida.");
        }
        subtareas.add(c);
    }

    @Override
    public void eliminar(TaskComponent c) {
        verificarSiEsModificable();
        if (!subtareas.remove(c)) {
            throw new IllegalArgumentException("La subtarea seleccionada no pertenece a esta tarea compuesta.");
        }
    }

    @Override
    public List<TaskComponent> getSubtareas() {
        return subtareas;
    }

    @Override
    public boolean esCompuesta() {
        return true;
    }

    // RF3: Calcula el progreso dinámicamente según sus subtareas
    @Override
    public double getProgreso() {
        if (subtareas.isEmpty()) {
            return estadoActual.getProgreso();
        }
        double suma = 0;
        for (TaskComponent sub : subtareas) {
            suma += sub.getProgreso();
        }
        return suma / subtareas.size();
    }

    // RF3: Calcula el estado dinámicamente según el estado de sus subtareas
    @Override
    public TaskState getEstadoActual() {
        if (subtareas.isEmpty()) {
            return estadoActual;
        }
        boolean todasTerminadas = true;
        boolean todasPendientes = true;
        boolean algunaEnCursoOPendiente = false;

        for (TaskComponent sub : subtareas) {
            String est = sub.getNombreEstado();
            if (!est.equals("Terminada")) {
                todasTerminadas = false;
            }
            if (!est.equals("Pendiente")) {
                todasPendientes = false;
            }
            if (est.equals("Pendiente") || est.equals("En curso")) {
                algunaEnCursoOPendiente = true;
            }
        }

        if (todasTerminadas) {
            estadoActual = new CompletedState();
        } else if (todasPendientes) {
            estadoActual = new PendingState();
        } else if (!algunaEnCursoOPendiente) {
            estadoActual = new InReviewState();
        } else {
            estadoActual = new InProgressState();
        }
        return estadoActual;
    }

    @Override
    public void setEstado(TaskState e) {
        this.estadoActual = e;
    }

    @Override
    public void cambiarEstado() {
        if (subtareas.isEmpty()) {
            estadoActual.cambiarEstado(this);
            return;
        }
        TaskState actual = getEstadoActual();
        boolean avanzoAlguna = false;
        for (TaskComponent sub : subtareas) {
            if (!sub.getNombreEstado().equals("Terminada")) {
                sub.cambiarEstado();
                avanzoAlguna = true;
            }
        }
        if (!avanzoAlguna) {
            actual.cambiarEstado(this);
        }
    }

    @Override
    public void cambiarEstado(String nuevoEstado) {
        if (subtareas.isEmpty()) {
            estadoActual.cambiarEstado(this, nuevoEstado);
            return;
        }

        String estadoActualNombre = getNombreEstado();
        if (estadoActualNombre.equals("Terminada")) {
            throw new IllegalStateException("La tarea compuesta ya está 'Terminada' y no puede cambiar de estado.");
        }

        if (nuevoEstado.equalsIgnoreCase("En curso") || nuevoEstado.equalsIgnoreCase("EnCurso")) {
            boolean cambio = false;
            for (TaskComponent sub : subtareas) {
                String est = sub.getNombreEstado();
                if (est.equals("Pendiente") || est.equals("En revisión")) {
                    sub.cambiarEstado("En curso");
                    cambio = true;
                }
            }
            if (!cambio && !estadoActualNombre.equals("En curso")) {
                throw new IllegalStateException("No hay subtareas disponibles para pasar a 'En curso'.");
            }
        } else if (nuevoEstado.equalsIgnoreCase("En revisión") || nuevoEstado.equalsIgnoreCase("En revision")
                || nuevoEstado.equalsIgnoreCase("EnRevision")) {
            for (TaskComponent sub : subtareas) {
                if (sub.getNombreEstado().equals("Pendiente")) {
                    throw new IllegalStateException(
                        "No se puede pasar a 'En revisión' porque la subtarea '" + sub.getTitulo() + "' sigue en 'Pendiente'."
                    );
                }
            }
            for (TaskComponent sub : subtareas) {
                if (sub.getNombreEstado().equals("En curso")) {
                    sub.cambiarEstado("En revisión");
                }
            }
        } else if (nuevoEstado.equalsIgnoreCase("Terminada")) {
            for (TaskComponent sub : subtareas) {
                String est = sub.getNombreEstado();
                if (!est.equals("En revisión") && !est.equals("Terminada")) {
                    throw new IllegalStateException(
                        "No se puede pasar a 'Terminada' porque la subtarea '" + sub.getTitulo() + "' está en '" + est + "' (debe pasar primero por 'En revisión')."
                    );
                }
            }
            for (TaskComponent sub : subtareas) {
                if (sub.getNombreEstado().equals("En revisión")) {
                    sub.cambiarEstado("Terminada");
                }
            }
        } else if (nuevoEstado.equalsIgnoreCase("Pendiente")) {
            for (TaskComponent sub : subtareas) {
                String est = sub.getNombreEstado();
                if (est.equals("En revisión") || est.equals("Terminada")) {
                    throw new IllegalStateException(
                        "No se puede volver a 'Pendiente' porque la subtarea '" + sub.getTitulo() + "' ya está en '" + est + "'."
                    );
                }
            }
            for (TaskComponent sub : subtareas) {
                if (sub.getNombreEstado().equals("En curso")) {
                    sub.cambiarEstado("Pendiente");
                }
            }
        } else {
            throw new IllegalStateException("Estado no reconocido: " + nuevoEstado);
        }
    }

    @Override
    public String getNombreEstado() {
        return getEstadoActual().getNombreEstado();
    }

    @Override
    public boolean puedeModificar() {
        return getEstadoActual().puedeModificar();
    }

    @Override
    public boolean puedeCompletarse() {
        return getEstadoActual().puedeCompletarse();
    }

    // RF8 y RF9: Clonación profunda de la tarea compuesta y sus subtareas
    @Override
    public TaskComponent clonar() {
        CompositeTask copia = new CompositeTask(this.titulo, this.descripcion, this.prioridad, this.fechaLimite);
        copia.setEstado(new PendingState());
        for (TaskComponent sub : this.subtareas) {
            copia.agregar(sub.clonar());
        }
        return copia;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + titulo + " (" + getNombreEstado() + " - " + (int) getProgreso() + "%)";
    }
}
