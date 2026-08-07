import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    public void salvar(Produto produto) throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "INSERT INTO produtos (codigo, nome, preco, fornecedor_id) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, produto.codigo);
        stmt.setString(2, produto.nome);
        stmt.setDouble(3, produto.preco);
        stmt.setInt(4, produto.fornecedorId);
        stmt.executeUpdate();
        conn.close();
    }

    public List<Produto> listar() throws Exception {
        Connection conn = Conexao.conectar();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM produtos");
        List<Produto> lista = new ArrayList<>();
        while (rs.next()) {
            lista.add(new Produto(
                rs.getInt("id"),
                rs.getInt("codigo"),
                rs.getString("nome"),
                rs.getDouble("preco"),
                rs.getInt("fornecedor_id")
            ));
        }
        conn.close();
        return lista;
    }

    public void atualizar(Produto produto) throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "UPDATE produtos SET nome=?, preco=?, fornecedor_id=? WHERE codigo=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, produto.nome);
        stmt.setDouble(2, produto.preco);
        stmt.setInt(3, produto.fornecedorId);
        stmt.setInt(4, produto.codigo);
        stmt.executeUpdate();
        conn.close();
    }

    public void remover(int codigo) throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "DELETE FROM produtos WHERE codigo=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, codigo);
        stmt.executeUpdate();
        conn.close();
    }
}