package org.example;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {
    private Long id;
    private String dni;
    private String nombre;
    private String apellidos;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Builder.Default
    private List<Cuenta> cuentas = new ArrayList<>();

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


    //TODO quitar una cuenta requiere un procedimiento mas complejo por ejemplo conmprobar que la Cuenta haya sido cancelada para que no haya cuentas sin clientes

}
