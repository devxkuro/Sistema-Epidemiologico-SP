import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VendaDAO {

    public int salvarVenda(int clienteId, double valorTotal) throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "INSERT INTO vendas (cliente_id, valor_total) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, clienteId);
        stmt.setDouble(2, valorTotal);
        stmt.executeUpdate();
        ResultSet rs = stmt.getGeneratedKeys();
        int idGerado = 0;
        if (rs.next()) {
            idGerado = rs.getInt(1);
        }
        conn.close();
        return idGerado;
    }

    public void salvarItem(int vendaId, int produtoId, int quantidade, double precoUnitario) throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "INSERT INTO venda_itens (venda_id, produto_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, vendaId);
        stmt.setInt(2, produtoId);
        stmt.setInt(3, quantidade);
        stmt.setDouble(4, precoUnitario);
        stmt.executeUpdate();
        conn.close();
    }

    public List<String[]> listar() throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "SELECT v.id, c.nome, v.data, v.valor_total " +
                     "FROM vendas v JOIN clientes c ON v.cliente_id = c.id";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        List<String[]> lista = new ArrayList<>();
        while (rs.next()) {
            lista.add(new String[]{
                rs.getString("id"),
                rs.getString("nome"),
                rs.getString("data"),
                rs.getString("valor_total")
            });
        }
        conn.close();
        return lista;
    }

    public List<String[]> listarItens(int vendaId) throws Exception {
        Connection conn = Conexao.conectar();
        String sql = "SELECT p.nome, vi.quantidade, vi.preco_unitario " +
                     "FROM venda_itens vi JOIN produtos p ON vi.produto_id = p.id " +
                     "WHERE vi.venda_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, vendaId);
        ResultSet rs = stmt.executeQuery();
        List<String[]> lista = new ArrayList<>();
        while (rs.next()) {
            lista.add(new String[]{
                rs.getString("nome"),
                rs.getString("quantidade"),
                rs.getString("preco_unitario")
            });
        }
        conn.close();
        return lista;
    }
    public List<String[]> listarUltimas(int limite) throws Exception {
    Connection conn = Conexao.conectar();
    String sql = "SELECT v.id, c.nome, v.data, v.valor_total " +
                 "FROM vendas v JOIN clientes c ON v.cliente_id = c.id " +
                 "ORDER BY v.data DESC LIMIT ?";
    PreparedStatement stmt = conn.prepareStatement(sql);
    stmt.setInt(1, limite);
    ResultSet rs = stmt.executeQuery();
    List<String[]> lista = new ArrayList<>();
    while (rs.next()) {
        lista.add(new String[]{
            rs.getString("id"),
            rs.getString("nome"),
            rs.getString("data"),
            rs.getString("valor_total")
        });
    }
    conn.close();
    return lista;
}
}
