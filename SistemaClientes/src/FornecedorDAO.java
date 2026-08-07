import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FornecedorDAO {

    public void salvar(Fornecedor fornecedor) throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "INSERT INTO fornecedores (nome, telefone, cnpj) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, fornecedor.nome);
        stmt.setString(2, fornecedor.telefone);
        stmt.setString(3, fornecedor.cnpj);
        stmt.executeUpdate();
        conn.close();
    }

    public List<Fornecedor> listar() throws Exception {
        Connection conn = Conexao.conectar();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM fornecedores");
        List<Fornecedor> lista = new ArrayList<>();
        while (rs.next()) {
            lista.add(new Fornecedor(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("telefone"),
                rs.getString("cnpj")
            ));
        }
        conn.close();
        return lista;
    }

    public void atualizar(Fornecedor fornecedor) throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "UPDATE fornecedores SET nome=?, telefone=? WHERE cnpj=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, fornecedor.nome);
        stmt.setString(2, fornecedor.telefone);
        stmt.setString(3, fornecedor.cnpj);
        stmt.executeUpdate();
        conn.close();
    }

    public void remover(String cnpj) throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "DELETE FROM fornecedores WHERE cnpj=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, cnpj);
        stmt.executeUpdate();
        conn.close();
    }
}