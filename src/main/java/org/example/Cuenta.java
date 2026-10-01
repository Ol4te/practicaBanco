package org.example;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class Cuenta {
    private Long id;
    private String iban;
    private double saldo;

    @ToString.Exclude
    @Builder.Default
    private List<Cliente> titulares = new ArrayList<>();

    public void añadirCliente(Cliente cliente){
        this.titulares.add(cliente);
    }
}
