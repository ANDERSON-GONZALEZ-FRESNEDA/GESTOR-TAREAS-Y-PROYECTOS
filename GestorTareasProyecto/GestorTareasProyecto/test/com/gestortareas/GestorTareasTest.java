package com.gestortareas;

import com.gestortareas.domain.CompositeTask;
import com.gestortareas.domain.Prioridad;
import com.gestortareas.domain.SimpleTask;
import com.gestortareas.domain.TaskComponent;
import com.gestortareas.persistence.ArchivoRepositorio;
import com.gestortareas.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class GestorTareasTest {

    private TaskService service;

    @BeforeEach
    public void setUp() {
        File temp = new File("test_tareas.dat");
        if (temp.exists()) {
            temp.delete();
        }
        service = new TaskService(new ArchivoRepositorio("test_tareas.dat"), false);
    }

    @Test
    public void testTransicionesValidasEInvalidasState() {
        SimpleTask tarea = service.crearTarea("Hacer informe", "Entrega final", Prioridad.ALTA, LocalDate.now().plusDays(3));
        assertEquals("Pendiente", tarea.getNombreEstado());

        // No puede saltar de Pendiente a Terminada (RF6)
        assertThrows(IllegalStateException.class, () -> service.cambiarEstadoTarea(tarea.getId(), "Terminada"));

        // Flujo válido: Pendiente -> En curso -> En revisión -> Terminada
        service.cambiarEstadoTarea(tarea.getId(), "En curso");
        assertEquals("En curso", tarea.getNombreEstado());

        service.cambiarEstadoTarea(tarea.getId(), "En revisión");
        assertEquals("En revisión", tarea.getNombreEstado());

        // En revisión no permite editar contenido (RF7)
        assertFalse(tarea.puedeModificar());
        assertThrows(IllegalStateException.class, () ->
            service.editarTarea(tarea.getId(), "Nuevo título", "Desc", Prioridad.BAJA, LocalDate.now().plusDays(5))
        );

        service.cambiarEstadoTarea(tarea.getId(), "Terminada");
        assertEquals("Terminada", tarea.getNombreEstado());
        assertEquals(100.0, tarea.getProgreso());
    }

    @Test
    public void testProgresoDinamicoCompositeYCambiosDeEstado() {
        CompositeTask proyecto = service.crearTareaCompuesta("Proyecto Java", "App completa", Prioridad.ALTA, LocalDate.now().plusDays(10));
        SimpleTask t1 = new SimpleTask("Backend", "Clases", Prioridad.ALTA, LocalDate.now().plusDays(4));
        SimpleTask t2 = new SimpleTask("Frontend", "Swing", Prioridad.MEDIA, LocalDate.now().plusDays(8));

        service.agregarSubtarea(proyecto.getId(), t1);
        service.agregarSubtarea(proyecto.getId(), t2);

        assertEquals(0.0, proyecto.getProgreso());

        t1.cambiarEstado("En curso");
        t1.cambiarEstado("En revisión");
        t1.cambiarEstado("Terminada");

        assertEquals(50.0, proyecto.getProgreso());
        assertEquals("En curso", proyecto.getNombreEstado());

        // Pasar subtarea restante a En curso y luego toda la compuesta a En revisión y Terminada
        t2.cambiarEstado("En curso");
        service.cambiarEstadoTarea(proyecto.getId(), "En revisión");
        assertEquals("En revisión", proyecto.getNombreEstado());

        service.cambiarEstadoTarea(proyecto.getId(), "Terminada");
        assertEquals(100.0, proyecto.getProgreso());
        assertEquals("Terminada", proyecto.getNombreEstado());
    }

    @Test
    public void testClonacionProfundaPrototype() {
        CompositeTask base = service.crearTareaCompuesta("Sprint", "Trabajo semanal", Prioridad.MEDIA, LocalDate.now().plusDays(7));
        SimpleTask sub = new SimpleTask("Reunión", "Daily", Prioridad.BAJA, LocalDate.now().plusDays(1));
        sub.cambiarEstado("En curso");
        service.agregarSubtarea(base.getId(), sub);

        TaskComponent plantilla = service.guardarComoPlantilla(base.getId(), "Plantilla Sprint");
        TaskComponent clon = service.crearDesdePlantilla(plantilla.getId());

        // Nuevo ID y estado inicial Pendiente (RF9)
        assertNotEquals(plantilla.getId(), clon.getId());
        assertEquals("Pendiente", clon.getNombreEstado());
        assertEquals("Pendiente", clon.getSubtareas().get(0).getNombreEstado());

        // Modificar el clon no afecta la plantilla original
        clon.setTitulo("Sprint Semana 2");
        assertEquals("Plantilla Sprint", plantilla.getTitulo());
    }

    @Test
    public void testEliminarTareasYSubtareas() {
        CompositeTask proyecto = service.crearTareaCompuesta("Proyecto", "Desc", Prioridad.ALTA, LocalDate.now().plusDays(5));
        SimpleTask sub = new SimpleTask("Subtarea 1", "Desc", Prioridad.MEDIA, LocalDate.now().plusDays(2));
        service.agregarSubtarea(proyecto.getId(), sub);
        assertEquals(1, proyecto.getSubtareas().size());

        service.eliminarSubtarea(proyecto.getId(), sub.getId());
        assertEquals(0, proyecto.getSubtareas().size());

        service.eliminarTarea(proyecto.getId());
        assertEquals(0, service.getTareas().size());
    }
}
