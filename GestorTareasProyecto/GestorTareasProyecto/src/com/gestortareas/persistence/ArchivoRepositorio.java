package com.gestortareas.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ArchivoRepositorio {
    private final String rutaArchivo;

    public ArchivoRepositorio() {
        this("datos_tareas.dat");
    }

    public ArchivoRepositorio(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public void guardar(DatosSistema datos) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(rutaArchivo))) {
            oos.writeObject(datos);
        }
    }

    public DatosSistema cargar() throws IOException, ClassNotFoundException {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return new DatosSistema(new ArrayList<>(), new ArrayList<>());
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            return (DatosSistema) ois.readObject();
        }
    }
}
