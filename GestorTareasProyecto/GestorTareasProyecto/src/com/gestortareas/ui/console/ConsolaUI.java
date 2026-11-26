package com.gestortareas.ui.console;

import com.gestortareas.domain.CompositeTask;
import com.gestortareas.domain.Prioridad;
import com.gestortareas.domain.SimpleTask;
import com.gestortareas.domain.TaskComponent;
import com.gestortareas.service.TaskService;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class ConsolaUI {
    private final TaskService service;
    private final Scanner sc;

    public ConsolaUI(TaskService service) {
        this.service = service;
        this.sc = new Scanner(System.in);
    }

    public void iniciar() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n=== GESTOR DE TAREAS Y PROYECTOS (CONSOLA) ===");
            System.out.println("1. Listar tareas y progreso");
            System.out.println("2. Crear tarea simple");
            System.out.println("3. Crear tarea compuesta");
            System.out.println("4. Agregar subtarea a tarea compuesta");
            System.out.println("5. Eliminar subtarea de tarea compuesta");
            System.out.println("6. Eliminar tarea principal");
            System.out.println("7. Cambiar estado de tarea");
            System.out.println("8. Editar tarea");
            System.out.println("9. Guardar tarea como plantilla");
            System.out.println("10. Crear tarea desde plantilla");
            System.out.println("11. Guardar / Cargar datos manualmente");
            System.out.println("0. Salir");
            System.out.print("Opción: ");

            try {
                opcion = Integer.parseInt(sc.nextLine().trim());
                switch (opcion) {
                    case 1: listarTareas(); break;
                    case 2: crearSimple(); break;
                    case 3: crearCompuesta(); break;
                    case 4: agregarSubtarea(); break;
                    case 5: eliminarSubtarea(); break;
                    case 6: eliminarTarea(); break;
                    case 7: cambiarEstado(); break;
                    case 8: editarTarea(); break;
                    case 9: guardarPlantilla(); break;
                    case 10: crearDesdePlantilla(); break;
                    case 11: menuPersistencia(); break;
                    case 0: System.out.println("Saliendo..."); break;
                    default: System.out.println("Opción inválida.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void listarTareas() {
        List<TaskComponent> lista = service.getTareas();
        if (lista.isEmpty()) {
            System.out.println("No hay tareas registradas.");
            return;
        }
        for (TaskComponent t : lista) {
            imprimirTarea(t, "");
        }
    }

    private void imprimirTarea(TaskComponent t, String sangria) {
        System.out.printf("%s- [%s] %s | Prioridad: %s | Estado: %s | Progreso: %.1f%% | Límite: %s%n",
            sangria, t.getId(), t.getTitulo(), t.getPrioridad(),
            t.getNombreEstado(), t.getProgreso(), t.getFechaLimite());
        if (t.esCompuesta()) {
            for (TaskComponent sub : t.getSubtareas()) {
                imprimirTarea(sub, sangria + "   ");
            }
        }
    }

    private void crearSimple() {
        System.out.print("Título: ");
        String titulo = sc.nextLine();
        System.out.print("Descripción: ");
        String desc = sc.nextLine();
        Prioridad prio = leerPrioridad();
        LocalDate fecha = leerFecha();

        SimpleTask t = service.crearTarea(titulo, desc, prio, fecha);
        System.out.println("Tarea creada con ID: " + t.getId());
    }

    private void crearCompuesta() {
        System.out.print("Título de la tarea compuesta: ");
        String titulo = sc.nextLine();
        System.out.print("Descripción: ");
        String desc = sc.nextLine();
        Prioridad prio = leerPrioridad();
        LocalDate fecha = leerFecha();

        CompositeTask t = service.crearTareaCompuesta(titulo, desc, prio, fecha);
        System.out.println("Tarea compuesta creada con ID: " + t.getId());
    }

    private void agregarSubtarea() {
        listarTareas();
        System.out.print("ID de la tarea compuesta padre: ");
        String idPadre = sc.nextLine().trim();
        System.out.print("¿Crear nueva (1) o mover existente (2)?: ");
        String tipoOp = sc.nextLine().trim();

        if ("2".equals(tipoOp)) {
            System.out.print("ID de la tarea existente: ");
            String idHija = sc.nextLine().trim();
            service.moverComoSubtarea(idPadre, idHija);
            System.out.println("Subtarea agregada.");
        } else {
            System.out.print("Tipo de subtarea (1=Simple, 2=Compuesta): ");
            String tipo = sc.nextLine().trim();
            System.out.print("Título: ");
            String titulo = sc.nextLine();
            System.out.print("Descripción: ");
            String desc = sc.nextLine();
            Prioridad prio = leerPrioridad();
            LocalDate fecha = leerFecha();

            TaskComponent sub = "2".equals(tipo)
                ? new CompositeTask(titulo, desc, prio, fecha)
                : new SimpleTask(titulo, desc, prio, fecha);
            service.agregarSubtarea(idPadre, sub);
            System.out.println("Subtarea creada y agregada con ID: " + sub.getId());
        }
    }

    private void eliminarSubtarea() {
        listarTareas();
        System.out.print("ID de la tarea compuesta padre: ");
        String idPadre = sc.nextLine().trim();
        System.out.print("ID de la subtarea a eliminar: ");
        String idSub = sc.nextLine().trim();
        service.eliminarSubtarea(idPadre, idSub);
        System.out.println("Subtarea eliminada.");
    }

    private void eliminarTarea() {
        listarTareas();
        System.out.print("ID de la tarea principal a eliminar: ");
        String id = sc.nextLine().trim();
        service.eliminarTarea(id);
        System.out.println("Tarea eliminada.");
    }

    private void cambiarEstado() {
        listarTareas();
        System.out.print("ID de la tarea: ");
        String id = sc.nextLine().trim();
        System.out.print("Nuevo estado (Pendiente / En curso / En revisión / Terminada) o Enter para avanzar: ");
        String estado = sc.nextLine().trim();
        if (estado.isEmpty()) {
            service.cambiarEstadoTarea(id);
        } else {
            service.cambiarEstadoTarea(id, estado);
        }
        TaskComponent t = service.buscarTareaPorId(id);
        System.out.println("Estado actualizado a: " + t.getNombreEstado() + " (" + t.getProgreso() + "%)");
    }

    private void editarTarea() {
        listarTareas();
        System.out.print("ID de la tarea a editar: ");
        String id = sc.nextLine().trim();
        System.out.print("Nuevo título: ");
        String titulo = sc.nextLine();
        System.out.print("Nueva descripción: ");
        String desc = sc.nextLine();
        Prioridad prio = leerPrioridad();
        LocalDate fecha = leerFecha();

        service.editarTarea(id, titulo, desc, prio, fecha);
        System.out.println("Tarea actualizada.");
    }

    private void guardarPlantilla() {
        listarTareas();
        System.out.print("ID de la tarea a guardar como plantilla (o '0' para crear desde cero): ");
        String id = sc.nextLine().trim();
        if ("0".equals(id)) {
            System.out.print("Tipo (1=Simple, 2=Compuesta): ");
            String tipo = sc.nextLine().trim();
            System.out.print("Título plantilla: ");
            String titulo = sc.nextLine();
            System.out.print("Descripción: ");
            String desc = sc.nextLine();
            Prioridad prio = leerPrioridad();
            LocalDate fecha = leerFecha();
            TaskComponent base = "2".equals(tipo)
                ? new CompositeTask(titulo, desc, prio, fecha)
                : new SimpleTask(titulo, desc, prio, fecha);
            TaskComponent p = service.crearPlantillaDesdeCero(base);
            System.out.println("Plantilla creada con ID: " + p.getId());
        } else {
            System.out.print("Nombre para la plantilla (Enter para mantener título): ");
            String nombre = sc.nextLine();
            TaskComponent p = service.guardarComoPlantilla(id, nombre);
            System.out.println("Plantilla guardada con ID: " + p.getId());
        }
    }

    private void crearDesdePlantilla() {
        List<TaskComponent> plantillas = service.getPlantillas();
        if (plantillas.isEmpty()) {
            System.out.println("No hay plantillas guardadas.");
            return;
        }
        System.out.println("--- Plantillas disponibles ---");
        for (TaskComponent p : plantillas) {
            System.out.println("- [" + p.getId() + "] " + p.getTitulo());
        }
        System.out.print("ID de la plantilla: ");
        String idPlantilla = sc.nextLine().trim();
        TaskComponent nueva = service.crearDesdePlantilla(idPlantilla);
        System.out.println("Nueva tarea creada desde plantilla con ID: " + nueva.getId() + " (Estado: " + nueva.getNombreEstado() + ")");
    }

    private void menuPersistencia() throws Exception {
        System.out.print("1. Guardar ahora | 2. Cargar desde archivo: ");
        String op = sc.nextLine().trim();
        if ("1".equals(op)) {
            service.guardarDatos();
            System.out.println("Datos guardados en archivo.");
        } else if ("2".equals(op)) {
            service.cargarDatos();
            System.out.println("Datos cargados desde archivo.");
        }
    }

    private Prioridad leerPrioridad() {
        System.out.print("Prioridad (ALTA, MEDIA, BAJA): ");
        return Prioridad.valueOf(sc.nextLine().trim().toUpperCase());
    }

    private LocalDate leerFecha() {
        System.out.print("Fecha límite (YYYY-MM-DD): ");
        return LocalDate.parse(sc.nextLine().trim());
    }
}
