package com.gestortareas.persistence;

import com.gestortareas.domain.TaskComponent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DatosSistema implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<TaskComponent> tareas;
    private final List<TaskComponent> plantillas;

    public DatosSistema(List<TaskComponent> tareas, List<TaskComponent> plantillas) {
        this.tareas = new ArrayList<>(tareas);
        this.plantillas = new ArrayList<>(plantillas);
    }

    public List<TaskComponent> getTareas() {
        return tareas;
    }

    public List<TaskComponent> getPlantillas() {
        return plantillas;
    }
}
