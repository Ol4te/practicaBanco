# Proyecto Banco de Consola

Practica de consola basica que simula la gestion de un sistema bancario relacional. El proyecto esta enfocado en aplicar buenas practicas de programacion, especificamente la separacion de responsabilidades siguiendo una estructura basada en el modelo MVC (Modelo-Vista-Controlador) y el patron DAO.

## 1. Tecnologias Empleadas

- **Lenguaje:** Java 21.
- **Librerias:** Lombok (configurado y compatible con Java 21 para la reduccion de codigo repetitivo).
- **Base de Datos:** MySQL.
- **Conectividad:** Conector nativo JDBC de MySQL.
- **Entorno de BD:** MySQL Workbench (requerido para el despliegue de las pruebas reales).

## 2. Estructura y Separacion de Responsabilidades (MVC)

El codigo se divide en capas logicas para aislar la persistencia de la interfaz:

- **Modelo (Dominio):** Clases Cliente y Cuenta. Se encargan de representar los datos y auto-validar su integridad (verificacion de DNI, control de caracteres invalidos, formatos correctos) antes de cualquier transaccion.
- **Controlador (DAOs y Conexion):** Clases DAOCliente, DAOCuenta y ConexionBD. Aislan toda la logica de persistencia, ejecutando las consultas SQL (PreparedStatement) y asegurando las transacciones (commit/rollback) sin que la interfaz se comunique directamente con la base de datos.
- **Vista (Interfaz):** Clase AppBanco. Interactua por consola con el usuario, solicitando datos y mostrando resultados de forma aislada.

## 3. Despliegue y Pruebas

1. Abrir MySQL Workbench.
2. Localizar y ejecutar el script SQL adjunto en la carpeta `resources` para generar la base de datos de prueba (`banco`) y crear la estructura relacional.
3. Verificar que las credenciales de acceso (usuario y contraseña) coinciden en la clase `ConexionBD`.
4. Ejecutar la clase principal `AppBanco` para iniciar el menu de consola.
