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
##  Cómo ejecutarlo

Requisitos: **Java 21**, **Maven** (o IntelliJ IDEA) y **Docker Desktop**.

### Opción A: con Docker (recomendada, no hace falta instalar MySQL)

```bash
# 1. Levantar MySQL con la base de datos y los datos de prueba ya cargados
docker compose up -d --wait

# 2. Ejecutar la clase AppBanco (desde IntelliJ)

# 3. Al terminar, apagar la base de datos
docker compose down
```
Cada vez que se levanta, la base de datos parte de los mismos datos de prueba. Los cambios que hagas no se conservan tras `docker compose down`.

> Si ya tienes un MySQL instalado en tu ordenador usando el puerto 3306, páralo antes de ejecutar Docker.

### Opción B: con MySQL instalado (MySQL Workbench)

1. Abrir MySQL Workbench y ejecutar el script `src/main/resources/banco_practica1.sql`.
2. Comprobar que el usuario y la contraseña de la clase `ConexionBD` coinciden con los de tu MySQL.
3. Ejecutar la clase `AppBanco`.

