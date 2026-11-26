package com.gestortareas.service;

import com.gestortareas.domain.CompositeTask;
import com.gestortareas.domain.Prioridad;
import com.gestortareas.domain.Prototype;
import com.gestortareas.domain.SimpleTask;
import com.gestortareas.domain.TaskComponent;
import com.gestortareas.persistence.ArchivoRepositorio;
import com.gestortareas.persistence.DatosSistema;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskService {
    private final List<TaskComponent> tareas;
    private final List<TaskComponent> plantillas;
    private final ArchivoRepositorio repositorio;

    public TaskService() {
        this(new ArchivoRepositorio(), true);
    }

    public TaskService(ArchivoRepositorio repositorio, boolean cargarAlIniciar) {
        this.repositorio = repositorio;
        this.tareas = new ArrayList<>();
        this.plantillas = new ArrayList<>();
        if (cargarAlIniciar) {
            try {
                cargarDatos();
            } catch (Exception e) {
                // Si no hay archivo todavía, empieza vacío
            }
        }
    }

    // RF1: Crear tarea simple
    public SimpleTask crearTarea(String titulo, String descripcion, Prioridad prioridad, LocalDate fechaLimite) {
        SimpleTask nueva = new SimpleTask(titulo, descripcion, prioridad, fechaLimite);
        tareas.add(nueva);
        guardarAutomatico();
        return nueva;
    }

    // RF2: Crear tarea compuesta
    public CompositeTask crearTareaCompuesta(String titulo, String descripcion, Prioridad prioridad, LocalDate fechaLimite) {
        CompositeTask nueva = new CompositeTask(titulo, descripcion, prioridad, fechaLimite);
        tareas.add(nueva);
        guardarAutomatico();
        return nueva;
    }

    // RF4: Agregar subtarea nueva a una tarea compuesta
    public void agregarSubtarea(String idPadre, TaskComponent subtarea) {
        TaskComponent padre = buscarTareaPorId(idPadre);
        padre.agregar(subtarea);
        guardarAutomatico();
    }

    // RF4: Mover una tarea existente como subtarea de una compuesta
    public void moverComoSubtarea(String idPadre, String idHija) {
        TaskComponent padre = buscarTareaPorId(idPadre);
        TaskComponent hija = buscarTareaPorId(idHija);
        if (padre.getId().equals(hija.getId()) || contieneDescendiente(hija, padre.getId())) {
            throw new IllegalArgumentException("No se puede mover una tarea dentro de sí misma o de sus propias subtareas.");
        }
        padre.agregar(hija);
        tareas.remove(hija);
        guardarAutomatico();
    }

    private boolean contieneDescendiente(TaskComponent raiz, String idBuscado) {
        if (!raiz.esCompuesta()) {
            return false;
        }
        for (TaskComponent sub : raiz.getSubtareas()) {
            if (sub.getId().equalsIgnoreCase(idBuscado) || contieneDescendiente(sub, idBuscado)) {
                return true;
            }
        }
        return false;
    }

    // RF4: Eliminar subtarea de una compuesta
    public void eliminarSubtarea(String idPadre, String idSubtarea) {
        TaskComponent padre = buscarTareaPorId(idPadre);
        TaskComponent hija = buscarTareaPorId(idSubtarea);
        padre.eliminar(hija);
        guardarAutomatico();
    }

    // Eliminar una tarea principal (raíz)
    public void eliminarTarea(String idTarea) {
        TaskComponent tarea = buscarTareaPorId(idTarea);
        if (!tareas.remove(tarea)) {
            throw new IllegalArgumentException("La tarea es una subtarea; elimínala desde su tarea compuesta padre.");
        }
        guardarAutomatico();
    }

    // RF5 y RF6: Cambiar estado validando transiciones
    public void cambiarEstadoTarea(String idTarea) {
        TaskComponent tarea = buscarTareaPorId(idTarea);
        tarea.cambiarEstado();
        guardarAutomatico();
    }

    public void cambiarEstadoTarea(String idTarea, String nuevoEstado) {
        TaskComponent tarea = buscarTareaPorId(idTarea);
        tarea.cambiarEstado(nuevoEstado);
        guardarAutomatico();
    }

    // RF7: Editar tarea respetando su estado actual
    public void editarTarea(String idTarea, String titulo, String descripcion, Prioridad prioridad, LocalDate fechaLimite) {
        TaskComponent tarea = buscarTareaPorId(idTarea);
        tarea.setTitulo(titulo);
        tarea.setDescripcion(descripcion);
        tarea.setPrioridad(prioridad);
        tarea.setFechaLimite(fechaLimite);
        guardarAutomatico();
    }

    // RF8: Guardar tarea existente como plantilla
    public TaskComponent guardarComoPlantilla(String idTarea, String nombrePlantilla) {
        TaskComponent original = buscarTareaPorId(idTarea);
        TaskComponent plantilla = original.clonar();
        if (nombrePlantilla != null && !nombrePlantilla.trim().isEmpty()) {
            plantilla.setTitulo(nombrePlantilla.trim());
        }
        plantillas.add(plantilla);
        guardarAutomatico();
        return plantilla;
    }

    // RF8: Crear plantilla desde cero
    public TaskComponent crearPlantillaDesdeCero(TaskComponent base) {
        TaskComponent plantilla = base.clonar();
        plantillas.add(plantilla);
        guardarAutomatico();
        return plantilla;
    }

    // Eliminar plantilla guardada
    public void eliminarPlantilla(String idPlantilla) {
        TaskComponent plantilla = buscarPlantillaPorId(idPlantilla);
        plantillas.remove(plantilla);
        guardarAutomatico();
    }

    // RF9: Instanciar nueva tarea desde plantilla (Patrón Prototype)
    public TaskComponent crearDesdePlantilla(Prototype plantilla) {
        TaskComponent nuevaTarea = (TaskComponent) plantilla.clonar();
        tareas.add(nuevaTarea);
        guardarAutomatico();
        return nuevaTarea;
    }

    public TaskComponent crearDesdePlantilla(String idPlantilla) {
        TaskComponent plantilla = buscarPlantillaPorId(idPlantilla);
        return crearDesdePlantilla((Prototype) plantilla);
    }

    public List<TaskComponent> getTareas() {
        return tareas;
    }

    public List<TaskComponent> getPlantillas() {
        return plantillas;
    }

    public List<CompositeTask> getTareasCompuestas() {
        List<CompositeTask> compuestas = new ArrayList<>();
        recolectarCompuestas(tareas, compuestas);
        return compuestas;
    }

    private void recolectarCompuestas(List<TaskComponent> lista, List<CompositeTask> destino) {
        for (TaskComponent t : lista) {
            if (t.esCompuesta()) {
                destino.add((CompositeTask) t);
                recolectarCompuestas(t.getSubtareas(), destino);
            }
        }
    }

    public TaskComponent buscarTareaPorId(String id) {
        TaskComponent encontrada = buscarEnLista(tareas, id);
        if (encontrada == null) {
            throw new IllegalArgumentException("No existe una tarea con ID: " + id);
        }
        return encontrada;
    }

    private TaskComponent buscarEnLista(List<TaskComponent> lista, String id) {
        for (TaskComponent t : lista) {
            if (t.getId().equalsIgnoreCase(id)) {
                return t;
            }
            if (t.esCompuesta()) {
                TaskComponent enHija = buscarEnLista(t.getSubtareas(), id);
                if (enHija != null) {
                    return enHija;
                }
            }
        }
        return null;
    }

    public TaskComponent buscarPlantillaPorId(String id) {
        for (TaskComponent p : plantillas) {
            if (p.getId().equalsIgnoreCase(id)) {
                return p;
            }
        }
        throw new IllegalArgumentException("No existe una plantilla con ID: " + id);
    }

    // RF13: Guardar y cargar datos en archivo local
    public void guardarDatos() throws IOException {
        repositorio.guardar(new DatosSistema(tareas, plantillas));
    }

    public void cargarDatos() throws IOException, ClassNotFoundException {
        DatosSistema datos = repositorio.cargar();
        tareas.clear();
        plantillas.clear();
        tareas.addAll(datos.getTareas());
        plantillas.addAll(datos.getPlantillas());
        sincronizarContadores(tareas);
        sincronizarContadores(plantillas);
    }

    private void sincronizarContadores(List<TaskComponent> lista) {
        for (TaskComponent t : lista) {
            String[] partes = t.getId().split("-");
            if (partes.length == 2) {
                int num = Integer.parseInt(partes[1]);
                if (t.esCompuesta()) {
                    CompositeTask.actualizarContador(num + 1);
                    sincronizarContadores(t.getSubtareas());
                } else {
                    SimpleTask.actualizarContador(num + 1);
                }
            }
        }
    }

    private void guardarAutomatico() {
        try {
            guardarDatos();
        } catch (IOException e) {
            System.out.println("Error al guardar datos: " + e.getMessage());
        }
    }
}
