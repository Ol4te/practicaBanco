package org.example;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
public class Cliente {
    private Long id;
    private String dni;
    private String nombre;
    private String apellidos;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<Cuenta> cuentas = new ArrayList<>();

    // Constructor blindado: ningún Cliente puede nacer sin DNI o sin nombre
    @Builder
    public Cliente(Long id, String dni, String nombre, String apellidos) {
        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("El DNI del cliente es obligatorio.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
        }
        this.id = id;
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.cuentas = new ArrayList<>();
    }

    public void añadirCuenta(Cuenta cuentaNueva){
        if(cuentaNueva == null){
            throw new IllegalArgumentException("La cuentan nueva no puede ser nula");
        }
        if(this.cuentas == null){
            this.cuentas = new ArrayList<>();
        }

        if(this.cuentas.contains(cuentaNueva)) return;

        this.cuentas.add(cuentaNueva);
        //Sincronizamos en el otro extremo
        if(cuentaNueva.getTitulares() != null && !cuentaNueva.getTitulares().contains(this)){
            cuentaNueva.getTitulares().add(this);
        }
    }

}
