# Gestor de Tareas y Proyectos (Java)

Proyecto de Ingeniería de Software estructurado por paquetes (`com.gestortareas`) aplicando los patrones de diseño **Composite**, **State** y **Prototype**.

## Estructura de Paquetes (`RNF6`)

```text
GestorTareasProyecto/
├── README.md
├── src/
│   └── com/gestortareas/
│       ├── Main.java
│       ├── domain/
│       │   ├── Prototype.java
│       │   ├── Prioridad.java
│       │   ├── TaskComponent.java
│       │   ├── SimpleTask.java
│       │   └── CompositeTask.java
│       ├── state/
│       │   ├── TaskState.java
│       │   ├── PendingState.java
│       │   ├── InProgressState.java
│       │   ├── InReviewState.java
│       │   └── CompletedState.java
│       ├── persistence/
│       │   ├── DatosSistema.java
│       │   └── ArchivoRepositorio.java
│       ├── service/
│       │   └── TaskService.java
│       └── ui/
│           ├── console/
│           │   └── ConsolaUI.java
│           └── gui/
│               └── GUI.java
└── test/
    └── com/gestortareas/
        └── GestorTareasTest.java
```

## Patrones Implementados

- **Composite (`domain`)**: `TaskComponent`, `SimpleTask` y `CompositeTask` permiten gestionar tareas individuales y tareas compuestas jerárquicas con cálculo dinámico de progreso y estado.
- **State (`state`)**: `TaskState` junto con `PendingState`, `InProgressState`, `InReviewState` y `CompletedState` controlan el ciclo de vida de cada tarea y validan las transiciones permitidas (`Pendiente -> En curso -> En revisión -> Terminada`).
- **Prototype (`domain`)**: La interfaz `Prototype` y el método `clonar()` permiten guardar tareas simples o compuestas como plantillas e instanciar nuevas tareas reiniciando su estado a `Pendiente` y asignando un nuevo ID.

## Ejecución

1. Ejecuta la clase `com.gestortareas.Main`.
2. Elige **Interfaz Gráfica (Swing)** o **Interfaz de Consola**.
3. Los datos se guardan automáticamente en el archivo local `datos_tareas.dat`.
4. Para ejecutar las pruebas unitarias, corre `test/com/gestortareas/GestorTareasTest.java` con **JUnit 5**.
