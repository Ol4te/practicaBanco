package org.example;

import java.sql.*;

public class DAOCuenta {

    //Ponemos los metodos necesarios de CRUD


    //1. Crear Clientes y Cuenta, al crear una cuenta se asocia siempre un titular

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

}
