import java.sql.Connection;
import java.sql.DriverManager;

public class Conexao {
    
    private static final String URL = "jdbc:mariadb://localhost:3306/sistema";
    private static final String USUARIO = "admin";
    private static final String SENHA = "1234";
    
    public static Connection conectar() throws Exception {

        Class.forName("org.mariadb.jdbc.Driver");

        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}