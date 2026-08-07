package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    
    private static final String DB_NAME = "sistema";
    private static final String USUARIO = "root";
    private static final String SENHA = "mysql";

    private static final String URL_MARIADB = "jdbc:mariadb://localhost:3306/" + DB_NAME;
    private static final String URL_MYSQL = "jdbc:mysql://localhost:3306/" + DB_NAME + "?serverTimezone=UTC";

    public static Connection conectar() throws Exception {
        try {
            // Conecta silenciosamente
            return DriverManager.getConnection(URL_MARIADB, USUARIO, SENHA);
        } catch (SQLException e1) {
            // Só avisa no console se o MariaDB falhar e precisar do reserva
            System.err.println("[SISTEPID] Fallback: MariaDB offline, tentando MySQL...");
            
            try {
                return DriverManager.getConnection(URL_MYSQL, USUARIO, SENHA);
            } catch (SQLException e2) {
                // Erro crítico: nenhum dos dois funcionou, ferrou de vez
                throw new Exception("""
                                    Falha total na conex\u00e3o com o banco de dados.
                                    Verifique se o servi\u00e7o mariadb est\u00e1 rodando (systemctl status mariadb).""");
            }
        }
    }

    public static void fechar(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Erro ao fechar ainda é importante logar para evitar memory leaks
                System.err.println("[SISTEPID] Erro ao encerrar conexão: " + e.getMessage());
            }
        }
    }
}