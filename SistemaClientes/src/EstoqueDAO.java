import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstoqueDAO {

    public void salvar(int produtoId, int quantidade, int quantidadeMinima) throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "INSERT INTO estoque (produto_id, quantidade, quantidade_minima) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, produtoId);
        stmt.setInt(2, quantidade);
        stmt.setInt(3, quantidadeMinima);
        stmt.executeUpdate();
        conn.close();
    }

    public void entrada(int produtoId, int quantidade) throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "UPDATE estoque SET quantidade = quantidade + ? WHERE produto_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, quantidade);
        stmt.setInt(2, produtoId);
        stmt.executeUpdate();
        conn.close();
    }

    public void saida(int produtoId, int quantidade) throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "UPDATE estoque SET quantidade = quantidade - ? WHERE produto_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, quantidade);
        stmt.setInt(2, produtoId);
        stmt.executeUpdate();
        conn.close();
    }

    public List<String[]> listar() throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "SELECT p.codigo, p.nome, e.quantidade, e.quantidade_minima " +
                     "FROM estoque e JOIN produtos p ON e.produto_id = p.id";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        List<String[]> lista = new ArrayList<>();
        while (rs.next()) {
            lista.add(new String[]{
                rs.getString("codigo"),
                rs.getString("nome"),
                rs.getString("quantidade"),
                rs.getString("quantidade_minima")
            });
        }
        conn.close();
        return lista;
    }

    public boolean estoqueBaixo(int produtoId) throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "SELECT quantidade, quantidade_minima FROM estoque WHERE produto_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, produtoId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("quantidade") <= rs.getInt("quantidade_minima");
        }
        conn.close();
        return false;
    }
}