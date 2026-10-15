package org.example;

import java.sql.SQLException;
import java.util.Scanner;

public class AppBanco {

    private static DAOCliente daoCliente = new DAOCliente();
    private static DAOCuenta DAOCuenta = new DAOCuenta();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int opcion = -1;

        while (opcion != 0) {
            System.out.println("\n--- MENU DEL BANCO ---");
            System.out.println("1. Registrar nuevo cliente");
            System.out.println("2. Abrir nueva cuenta");
            System.out.println("3. Ingresar dinero");
            System.out.println("4. Retirar dinero");
            System.out.println("5. Realizar transferencia");
            System.out.println("6. Consultar cuenta por IBAN");
            System.out.println("0. Salir");
            System.out.print("Elige una opcion: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());

                switch (opcion) {
                    case 1: registrarCliente(); break;
                    case 2: abrirCuenta(); break;
                    case 3: ingresarDinero(); break;
                    case 4: retirarDinero(); break;
                    case 5: transferir(); break;
                    case 6: consultarCuenta(); break;
                    case 0: System.out.println("Saliendo del sistema..."); break;
                    default: System.out.println("Opcion incorrecta.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Debes introducir un numero valido para el menu.");
            } catch (SQLException e) {
                System.out.println("Error en la base de datos: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void registrarCliente() throws SQLException {
        String dni = leerDniValido("Introduce DNI: ");
        String nombre = leerTextoValido("Introduce Nombre: ");
        String apellidos = leerTextoValido("Introduce Apellidos: ");

        Cliente nuevoCliente = Cliente.builder()
                .dni(dni)
                .nombre(nombre)
                .apellidos(apellidos)
                .build();

        daoCliente.crearCliente(nuevoCliente);
        System.out.println("Cliente registrado con exito. ID asignado: " + nuevoCliente.getId());
    }

    private static void abrirCuenta() throws SQLException {
        String dni = leerDniValido("Introduce el DNI del titular principal: ");

        Cliente titular = daoCliente.buscarClientePorDni(dni);
        if (titular == null) {
            System.out.println("No existe ningun cliente con ese DNI.");
            return;
        }

        String iban = leerIbanValido("Introduce el nuevo IBAN (Empieza por ES + 22 numeros): ");

        Cuenta nuevaCuenta = Cuenta.builder()
                .iban(iban)
                .saldo(0.0)
                .titularPrincipal(titular)
                .build();

        DAOCuenta.crearCuenta(nuevaCuenta, titular.getId());
        System.out.println("Cuenta creada con exito. ID: " + nuevaCuenta.getId());
    }

    private static void ingresarDinero() throws SQLException {
        String iban = leerIbanValido("Introduce el IBAN de la cuenta: ");

        Cuenta cuenta = DAOCuenta.buscarCuentaPorIban(iban);
        if (cuenta == null) {
            System.out.println("Cuenta no encontrada.");
            return;
        }

        double cantidad = leerCantidadValida("Cantidad a ingresar: ");
        double nuevoSaldo = cuenta.getSaldo() + cantidad;
        DAOCuenta.actualizarSaldo(cuenta.getId(), nuevoSaldo);
        System.out.println("Ingreso realizado. Nuevo saldo: " + nuevoSaldo + " euros");
    }

    private static void retirarDinero() throws SQLException {
        String iban = leerIbanValido("Introduce el IBAN de la cuenta: ");

        Cuenta cuenta = DAOCuenta.buscarCuentaPorIban(iban);
        if (cuenta == null) {
            System.out.println("Cuenta no encontrada.");
            return;
        }

        double cantidad = leerCantidadValida("Cantidad a retirar: ");

        if (cuenta.getSaldo() < cantidad) {
            System.out.println("Saldo insuficiente. Saldo actual: " + cuenta.getSaldo() + " euros");
            return;
        }

        double nuevoSaldo = cuenta.getSaldo() - cantidad;
        DAOCuenta.actualizarSaldo(cuenta.getId(), nuevoSaldo);
        System.out.println("Retirada realizada. Nuevo saldo: " + nuevoSaldo + " euros");
    }

    private static void transferir() throws SQLException {
        String ibanOrigen = leerIbanValido("Introduce el IBAN de origen: ");
        Cuenta origen = DAOCuenta.buscarCuentaPorIban(ibanOrigen);
        if (origen == null) {
            System.out.println("Cuenta de origen no encontrada.");
            return;
        }

        String ibanDestino = leerIbanValido("Introduce el IBAN de destino: ");
        Cuenta destino = DAOCuenta.buscarCuentaPorIban(ibanDestino);
        if (destino == null) {
            System.out.println("Cuenta de destino no encontrada.");
            return;
        }

        double cantidad = leerCantidadValida("Cantidad a transferir: ");

        if (origen.getSaldo() < cantidad) {
            System.out.println("Saldo insuficiente en la cuenta de origen.");
            return;
        }

        DAOCuenta.actualizarSaldo(origen.getId(), origen.getSaldo() - cantidad);
        DAOCuenta.actualizarSaldo(destino.getId(), destino.getSaldo() + cantidad);

        System.out.println("Transferencia de " + cantidad + " euros completada con exito.");
    }

    private static void consultarCuenta() throws SQLException {
        String iban = leerIbanValido("Introduce el IBAN: ");
        Cuenta cuenta = DAOCuenta.buscarCuentaPorIban(iban);

        if (cuenta == null) {
            System.out.println("Cuenta no encontrada.");
            return;
        }

        System.out.println("\n--- DATOS DE LA CUENTA ---");
        System.out.println("ID: " + cuenta.getId());
        System.out.println("IBAN: " + cuenta.getIban());
        System.out.println("SALDO: " + cuenta.getSaldo() + " euros");
        System.out.println("TITULARES:");
        for (Cliente c : cuenta.getTitulares()) {
            System.out.println(" - " + c.getNombre() + " " + c.getApellidos() + " (DNI: " + c.getDni() + ")");
        }
    }


    // =================================================================================
    // METODOS DE VALIDACION ROBUSTA
    // =================================================================================

    private static String leerTextoValido(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String input = scanner.nextLine().trim();
            if (input.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$") && !input.isBlank()) {
                return input;
            }
            System.out.println("Entrada invalida. Usa solo letras y espacios.");
        }
    }

    private static String leerDniValido(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String dni = scanner.nextLine().trim().toUpperCase();

            if (dni.matches("^\\d{8}[A-Z]$")) {
                String letrasValidas = "TRWAGMYFPDXBNJZSQVHLCKE";
                int numeros = Integer.parseInt(dni.substring(0, 8));
                char letraCalculada = letrasValidas.charAt(numeros % 23);

                if (letraCalculada == dni.charAt(8)) {
                    return dni;
                } else {
                    System.out.println("La letra del DNI es incorrecta. Deberia ser: " + letraCalculada);
                }
            } else {
                System.out.println("Formato incorrecto. Deben ser 8 numeros seguidos de 1 letra.");
            }
        }
    }

    private static String leerIbanValido(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String iban = scanner.nextLine().replaceAll("\\s", "").toUpperCase();
            if (iban.matches("^ES\\d{22}$")) {
                return iban;
            }
            System.out.println("Formato de IBAN invalido. Debe empezar por 'ES' seguido de 22 numeros.");
        }
    }

    private static double leerCantidadValida(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                double cantidad = Double.parseDouble(scanner.nextLine().trim());
                if (cantidad > 0) {
                    return cantidad;
                } else {
                    System.out.println("La cantidad debe ser mayor que cero.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Por favor, introduce un numero valido (usa punto para decimales).");
            }
        }
    }
}