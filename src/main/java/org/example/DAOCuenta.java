package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAOCuenta {

    //Ponemos los metodos necesarios de CRUD


    //1. Crear Clientes y Cuenta, al crear una cuenta se asocia siempre un titular

    public Cuenta buscarCuentaPorIban(String iban) throws SQLException {
        String sql = "SELECT id FROM cuenta WHERE iban = ?";
        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement psql = conexion.prepareStatement(sql)) {
            psql.setString(1, iban);
            try (ResultSet rs = psql.executeQuery()) {
                if (rs.next()) {
                    // Reutilizamos el método que ya tienes
                    return buscarCuentaPorId(rs.getLong("id"));
                }
            }
        }
        return null;
    }

    public Cuenta crearCuenta(Cuenta cuenta, Long clienteId) throws SQLException{
        if(clienteId == null){
            throw new IllegalArgumentException("Se requiere un id de cliente");
        }

        //1. Preparamos statements con ? para insertar luego los valores
        //Hibernate si generara una tabla intermedia e inserta datos pero Java puro y MYsql no lo hacen
        //Por eso creamos la sqlRelacion
        String sqlCuenta = "insert into cuenta (iban, saldo) values (?,?)";
        String sqlRelacion = "insert into cliente_cuenta (cliente_id, cuenta_id) values (?,?)";

        try(Connection conexion = ConexionBD.getConnection()){
            conexion.setAutoCommit(false); //muy importante, ACTUALIZAMOS 2 TABLAS NO 1!

            try(PreparedStatement psql_cuenta = conexion.prepareStatement(sqlCuenta, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement psql_relacion = conexion.prepareStatement(sqlRelacion)){

                //1. Insertar cuenta y recuperamos el ID autoincremental
                    psql_cuenta.setString(1, cuenta.getIban());
                    psql_cuenta.setDouble(2, cuenta.getSaldo());
                    psql_cuenta.executeUpdate();

                    //ID autoincremental de la cuenta
                    try(ResultSet rs = psql_cuenta.getGeneratedKeys()){
                        if(rs.next()){
                            cuenta.setId(rs.getLong(1));
                        }else {
                            throw new SQLException("No se pudo obtener el ID asignado");
                        }
                    }

                    psql_relacion.setLong(1, clienteId);
                    psql_relacion.setLong(2, cuenta.getId());
                    psql_relacion.executeUpdate();

                    conexion.commit(); //Importante sin el commit no se registra nada en ninguna de las tablas
                    return cuenta;
            } catch (SQLException e){
                conexion.rollback(); //IMPORTANTE
                throw e;
            }

        }
    }

    public boolean añadirTitular(Long cuentaId, Long clienteId) throws SQLException{
        if(cuentaId == null || clienteId == null){
            throw new IllegalArgumentException("Cliente o cuenta no válidos");
        }

        String sql = "Insert into cliente_cuenta (cliente_id, cuenta_id) values (?,?)";

        try(Connection conexion = ConexionBD.getConnection();
        PreparedStatement ps = conexion.prepareStatement(sql)){
            ps.setLong(1, clienteId);
            ps.setLong(2, cuentaId);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == 1062) {
                throw new IllegalStateException("El cliente ya es titular de esta cuenta.");
            }
            throw new IllegalArgumentException("No se pudo asociar: la cuenta o el cliente indicados no existen.");
        }
    }

    public Cuenta buscarCuentaPorId(Long id_cuenta) throws SQLException{
       if(id_cuenta == null){
           throw new IllegalArgumentException("El Id debe ser válido");
       }

       String sql = "Select id, iban, saldo from cuenta where id = ?";
       String sqlTitulares = "Select c.id, c.dni, c.nombre, c.apellidos from cliente c "+ "inner join cliente_cuenta cc "+
               "on c.id = cc.cliente_id "+ "where cc.cuenta_id= ?";

       List<Cliente> lista_titulares = new ArrayList<>();

       try(Connection conexion = ConexionBD.getConnection();
       PreparedStatement obtenerCuenta = conexion.prepareStatement(sql);
       PreparedStatement obtenerClientes = conexion.prepareStatement(sqlTitulares))
       {
           obtenerCuenta.setLong(1, id_cuenta);
           ResultSet rs = obtenerCuenta.executeQuery();

            if(!rs.next()) {
                   return null; //Devolvemos cuenta nula
               }
            Long id_cuenta_encontrada= rs.getLong("id");
            String iban_encontrada = rs.getString("iban");
            Double saldo_encontrado = rs.getDouble("saldo");

            //Ahora los clientes
           obtenerClientes.setLong(1, id_cuenta);
           ResultSet titulares_resultset = obtenerClientes.executeQuery();

           while(titulares_resultset.next()){
               Cliente cliente = Cliente.builder().id(titulares_resultset.getLong("id")).
                       dni(titulares_resultset.getString("dni")).
                               nombre(titulares_resultset.getString("nombre"))
                       .apellidos(titulares_resultset.getString("apellidos"))
                       .build();
                lista_titulares.add(cliente);
           }

           if (lista_titulares.isEmpty()){
               throw new IllegalStateException("Error en la base de datos. Cuenta sin clientes.");
           }
            Cuenta encontrada = Cuenta.builder().iban(iban_encontrada).saldo(saldo_encontrado).titularPrincipal(lista_titulares.get(0)).build();
           encontrada.setId(id_cuenta_encontrada); //Se lo pasamos a Java no a la base de datos por lo que no cambiamos nada

           for (int i = 1; i< lista_titulares.size(); i++){
               encontrada.añadirCliente(lista_titulares.get(i));
           }
           encontrada.setTitulares(lista_titulares);
           return encontrada;
       }catch (SQLException e){
           throw e;
       }

    }

    public boolean actualizarSaldo(Long idCuenta, double saldoActulizado) throws SQLException{

        if(idCuenta == null){
            throw new IllegalArgumentException("ERROR: Se ha introducido un ID nulo.");
        }

        String sql = "Update cuenta set saldo = ? where id = ?";

        try(Connection conexion = ConexionBD.getConnection();
        PreparedStatement psql = conexion.prepareStatement(sql)){
            psql.setDouble(1, saldoActulizado);
            psql.setLong(2, idCuenta);
            if (psql.executeUpdate() > 0){
                return true;
            }
        }catch (SQLException e){
            throw e;
        }
        return false;
    }


    public boolean removeTitular(Long idCuenta, Long idCliente) throws SQLException {
        if (idCuenta == null || idCliente == null) {
            throw new IllegalArgumentException("ERROR: Se han introducido valores nulos");
        }
        String sqlClientes = "Select count(*) from cliente_cuenta where cuenta_id = ?";
        String sqlBorrarCliente = "Delete from cliente_cuenta where cuenta_id = ? and cliente_id= ?";

        try (Connection conexion = ConexionBD.getConnection()) {

            //Lo MAS Importante
            conexion.setAutoCommit(false);

            try (PreparedStatement psqlContarClientes = conexion.prepareStatement(sqlClientes);
                 PreparedStatement psqlBorrarCliente = conexion.prepareStatement(sqlBorrarCliente)) {

                psqlContarClientes.setLong(1, idCuenta);

                try (ResultSet numero_clientes = psqlContarClientes.executeQuery()) {
                    if (numero_clientes.next() && numero_clientes.getInt(1) <= 1) {
                        conexion.rollback();
                        throw new IllegalStateException("No se puede eliminar el único titular de la cuenta");
                    }
                }

                psqlBorrarCliente.setLong(1, idCuenta);
                psqlBorrarCliente.setLong(2, idCliente);
                int filasAfectadas = psqlBorrarCliente.executeUpdate();
                conexion.commit();
                if (filasAfectadas > 0) {
                    return true;
                }

            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }

            return false;
        }
    }

    public boolean borrarCuentaCompleta(Long idCuenta) throws SQLException{
        String sqlComprobarSaldo = "Select saldo from cuenta where id = ?";
        String sqlBorrarCuenta = "Delete from cuenta where id = ?";

        try(Connection conexion = ConexionBD.getConnection()){
            conexion.setAutoCommit(false);


            //1. Comprobamos que la cuenta no tenga saldo
            try(PreparedStatement psqlSaldo = conexion.prepareStatement(sqlComprobarSaldo);
                PreparedStatement psqlBorrarCuenta = conexion.prepareStatement(sqlBorrarCuenta)){
                psqlSaldo.setLong(1, idCuenta);
                try(ResultSet rsSaldo = psqlSaldo.executeQuery()){
                    if(rsSaldo.next()){
                        double saldo = rsSaldo.getDouble("saldo");
                        if(saldo != 0.0){
                            conexion.rollback();
                            throw new IllegalStateException("No se pueden borrar cuentas con saldo");
                        }
                        } else {
                        conexion.rollback();
                        return false;
                    }
                }

                //2. Preparamos el borrado de la cuenta
                psqlBorrarCuenta.setLong(1, idCuenta);

                int filas_afectadas = psqlBorrarCuenta.executeUpdate();
                conexion.commit();
                if(filas_afectadas>0){
                    return true;
                } else {
                    return false;
                }


            }catch(SQLException e){
                conexion.rollback();
                throw e;
            }
        }
    }

}
