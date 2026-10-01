package org.example;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {
    private String id;
    private String dni;
    private String nombre;
    private String apellidos;

    @ToString.Exclude
    @Builder.Default
    private List<Cuenta> cuentas = new ArrayList<>();

    public void añadirCuenta(Cuenta cuentaNueva){
        if(this.cuentas == null) this.cuentas = new ArrayList<>();
        this.cuentas.add(cuentaNueva);
    }

    //TODO quitar una cuenta requiere un procedimiento mas complejo por ejemplo conmprobar que la Cuenta haya sido cancelada para que no haya cuentas sin clientes

}
