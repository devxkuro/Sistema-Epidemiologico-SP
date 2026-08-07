import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public void salvar(Cliente cliente) throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "INSERT INTO clientes (nome, endereco, telefone, cpf) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, cliente.nome);
        stmt.setString(2, cliente.endereco);
        stmt.setString(3, cliente.telefone);
        stmt.setString(4, cliente.cpf);
        stmt.executeUpdate();
        conn.close();
    }

    public List<Cliente> listar() throws Exception {
        Connection conn = Conexao.conectar();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM clientes");
        List<Cliente> lista = new ArrayList<>();
        while (rs.next()) {
            lista.add(new Cliente(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("endereco"),
                rs.getString("telefone"),
                rs.getString("cpf")
            ));
        }
        conn.close();
        return lista;
    }

    public void atualizar(Cliente cliente) throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "UPDATE clientes SET nome=?, endereco=?, telefone=? WHERE cpf=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, cliente.nome);
        stmt.setString(2, cliente.endereco);
        stmt.setString(3, cliente.telefone);
        stmt.setString(4, cliente.cpf);
        stmt.executeUpdate();
        conn.close();
    }

    public void remover(String cpf) throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "DELETE FROM clientes WHERE cpf=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, cpf);
        stmt.executeUpdate();
        conn.close();
    }
}