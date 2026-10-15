package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAOCliente {

    //1. CREATE cliente
    public Cliente crearCliente(Cliente cliente) throws SQLException {
        String sqlCrearCliente = "Insert into cliente (dni, nombre, apellidos) values (?, ?, ?)";

        try(Connection conexion = ConexionBD.getConnection();
            PreparedStatement psqlCuenta = conexion.prepareStatement(sqlCrearCliente, Statement.RETURN_GENERATED_KEYS)){
            psqlCuenta.setString(1, cliente.getDni());
            psqlCuenta.setString(2, cliente.getNombre());
            psqlCuenta.setString(3, cliente.getApellidos());
            psqlCuenta.executeUpdate();


            try(ResultSet resultado = psqlCuenta.getGeneratedKeys()){
                if(resultado.next()){
                    cliente.setId(resultado.getLong(1));
                } else {
                    throw new SQLException("No se ha asignado ID al cliente");
                }
            } catch(SQLException e){
                throw e;
            }

        }
        return cliente;
    }


    //READ Cliente
    public Cliente buscarClientePorId(Long idCliente) throws SQLException{
        if(idCliente == null){
            throw new IllegalArgumentException("No se ha introducido un ID de cliente");
        }

        String sqlCliente = "Select id, dni, nombre, apellidos from cliente where id = ?";
        String sqlCuentas = "Select c.id, c.iban, c.saldo from cuenta c inner join cliente_cuenta cc on c.id = cc.cuenta_id where cc.cliente_id = ?";

        try(Connection conexion = ConexionBD.getConnection();
        PreparedStatement psql = conexion.prepareStatement(sqlCliente);
        PreparedStatement psqlCuentas = conexion.prepareStatement(sqlCuentas)){
            psql.setLong(1, idCliente);
            psqlCuentas.setLong(1, idCliente);
            try(ResultSet rs =psql.executeQuery()){
                if(!rs.next()){
                    return null; //no hay cliente con ese id
                }
                Cliente encontrado = Cliente.builder().id(rs.getLong("id")).nombre(rs.getString("nombre"))
                        .apellidos(rs.getString("apellidos")).dni(rs.getString("dni")).build();

                List<Cuenta> listaCuentas = new ArrayList<>();
                //Falta la lista de cuentas
                try(ResultSet rsCuentas = psqlCuentas.executeQuery()){
                    while(rsCuentas.next()){
                        Cuenta cuenta = Cuenta.builder().iban(rsCuentas.getString("iban"))
                                .saldo(rsCuentas.getDouble("saldo"))
                                .titularPrincipal(encontrado)
                                .build();
                        //Falta ponerle id
                        cuenta.setId(rsCuentas.getLong("id"));
                        listaCuentas.add(cuenta);
                    }
                }
                encontrado.setCuentas(listaCuentas);
                return encontrado;
            }
        }catch (SQLException e){
            throw e;
        }
    }

    public Cliente buscarClientePorDni(String dni) throws SQLException {
        String sql = "SELECT id FROM cliente WHERE dni = ?";
        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement psql = conexion.prepareStatement(sql)) {
            psql.setString(1, dni);
            try (ResultSet rs = psql.executeQuery()) {
                if (rs.next()) {
                    return buscarClientePorId(rs.getLong("id"));
                }
            }
        }
        return null;
    }


    //Listar todos los clientes
    //Solo es útil en bd pequeñas o medianas
    List<Cliente> listarClientes() throws SQLException{
        String sql = "Select * from cliente";
        List<Cliente> listaClientes = new ArrayList<>();

        try(Connection conexion = ConexionBD.getConnection();
        PreparedStatement psql = conexion.prepareStatement(sql)){

            ResultSet resultado = psql.executeQuery();

            while(resultado.next()){
                Cliente c = Cliente.builder().id(resultado.getLong("id"))
                        .dni(resultado.getString("dni"))
                        .nombre(resultado.getString("nombre"))
                        .apellidos(resultado.getString("apellidos"))
                        .build();

                listaClientes.add(c);
            }
        } catch (SQLException e) {
            throw e;
        }

        return listaClientes; //Si hay algun fallo devuelve una lista vacía.
    }

    //Update datos de cliente

    public boolean actualizarCliente(Cliente cliente) throws SQLException{
        if(cliente == null || cliente.getId() == null){
            throw new IllegalArgumentException();
        }

        String sql = "update cliente set dni=?, nombre=?, apellidos=? where id= ?";
        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement psql = conexion.prepareStatement(sql)) {

            psql.setString(1, cliente.getDni());
            psql.setString(2, cliente.getNombre());
            psql.setString(3, cliente.getApellidos());
            psql.setLong(4, cliente.getId());

            if (psql.executeUpdate() > 0){
                return true;
            }
        }
        return false;
    }

    public boolean eliminarCliente (Long idCliente) throws SQLException {
        if (idCliente == null) {
            throw new IllegalArgumentException("ID no válido");
        }
            String sqlCuentaVacia = "Select cuenta_id from cliente_cuenta where cuenta_id in (select cuenta_id from cliente_cuenta where cliente_id = ?)" +
                    " group by cuenta_id having count(cliente_id) = 1";

            String sqlBorrarCuenta = "Delete from cliente where id = ?";

            try (Connection conexion = ConexionBD.getConnection()) {
                conexion.setAutoCommit(false);

                try (PreparedStatement psqlCheck = conexion.prepareStatement(sqlCuentaVacia);
                     PreparedStatement psqlBorrar = conexion.prepareStatement(sqlBorrarCuenta)) {
                    psqlCheck.setLong(1, idCliente);

                    try (ResultSet rs = psqlCheck.executeQuery()) {
                        if (rs.next()) {
                            long idCuenta = rs.getLong("cuenta_id");
                            conexion.rollback();
                            throw new IllegalStateException("No se puede eliminar porque es el único titular de la cuenta.");
                        }
                    }
                    psqlBorrar.setLong(1, idCliente);
                    int filasAfectadas = psqlBorrar.executeUpdate();

                    conexion.commit();
                    if(filasAfectadas>0){
                        return true;
                    }
                } catch (SQLException e){
                    conexion.rollback();
                    throw e;
                }
            }
            return false;
        }




}
