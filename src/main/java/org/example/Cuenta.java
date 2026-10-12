package org.example;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
// Aqui no podemos usar  @AllArgsConstructor ya es que obligatorio que la cuenta tenga siempre un cliente
public class Cuenta {
    private Long id;
    private String iban;
    private double saldo;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude //evita bucle infinito StackOVerFLow
    private List<Cliente> titulares = new ArrayList<>();

    @Builder
    public Cuenta(String iban, double saldo, Cliente titularPrincipal){
        if (titularPrincipal == null){
            throw new IllegalArgumentException("Se debe asignar un cliente");
        }
        this.iban = iban;
        this.saldo = saldo;
        this.titulares = new ArrayList<>();
        this.añadirCliente(titularPrincipal);
    }

    //Estas operaciones pueden resultar redundantes puesto que se elaboran de manera permanente en el DAO
    //pero aportan consistencia de forma que no se puede registar en la BD un cliente sin datos o una cuenta sin datos/titulares
     public void añadirCliente(Cliente cliente){
        this.titulares.add(cliente);
    }

    public void removeCliente(Cliente cliente){
        //1. Comprobación que el cliente no sea nulo
        if(cliente == null){
            return;
        }

        //2. Comprobacion que la lista tenga mas de un cliente para no quedar huerfana

        if(this.titulares.size() <= 1){
            throw new IllegalArgumentException("No se puede eliminar el titular principal");
        }

        boolean eliminado = this.titulares.removeIf(c -> c.equals(cliente) || (c.getId() != null && Objects.equals(c.getId(), cliente.getId())));

        //3. Ahora borramos en la lista de clientes la cuenta
        if (eliminado && cliente.getCuentas() != null){
            cliente.getCuentas().remove(this);
        }
    }


}
