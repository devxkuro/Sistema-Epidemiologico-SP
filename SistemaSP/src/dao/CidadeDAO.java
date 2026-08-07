package dao;

import database.Conexao;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import model.Cidade;
import model.Coleta;
import model.Doenca;

public class CidadeDAO {

    // Centraliza a referência temporal para evitar inconsistências nos filtros
    private int getMes() { return LocalDate.now().getMonthValue(); }
    private int getAno() { return LocalDate.now().getYear(); }

    // Lista cidades carregando coletas e alertas do mês atual em uma única consulta.
    //O uso de LinkedHashMap mantém a ordenação alfabética vinda do SQL.
    //Overwrite bacaninha tbm do LinkedHashMap, mas acho q te maneiras melhores 
     
    public List<Cidade> listarTodas() {
        Map<String, Cidade> mapa = new LinkedHashMap<>();
        String sql = "SELECT c.id, c.nome, c.populacao, co.casos, co.mes, co.ano, " +
                     "co.populacao_no_mes, d.nome as d_nome, d.cor_hex " +
                     "FROM cidades c " +
                     "LEFT JOIN coletas co ON c.id = co.cidade_id AND co.ano = ? " +
                     "LEFT JOIN doencas d ON co.doenca_id = d.id " +
                     "ORDER BY c.nome ASC, co.mes ASC";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, getAno());
            ResultSet rs = stmt.executeQuery();
            int mesAtual = getMes();

            while (rs.next()) {
                String id = rs.getString("id");
                // computeIfAbsent evita duplicar objetos Cidade durante o processamento do JOIN
                Cidade cidade = mapa.computeIfAbsent(id, k -> {
                    try {
                        return new Cidade(id, rs.getString("nome"), rs.getLong("populacao"));
                    } catch (SQLException e) { return null; }
                });

                String nomeD = rs.getString("d_nome");
                if (nomeD != null && cidade != null) {
                    int casos = rs.getInt("casos");
                    int mes = rs.getInt("mes");
                    
                    // Alimenta o histórico para os gráficos de tendência
                    cidade.getColetas().add(new Coleta(mes, rs.getInt("ano"), casos, nomeD, rs.getInt("populacao_no_mes")));
                    
                    // Alimenta os alertas instantâneos (usados no CityChip)
                    if (mes == mesAtual) {
                        Doenca d = new Doenca();
                        d.setNome(nomeD);
                        d.setCasos(casos);
                        String hex = rs.getString("cor_hex");
                        d.setCorFromHex(hex != null ? hex.trim() : "#7A7A7A");
                        cidade.getDoencas().add(d);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("// Erro na listagem: " + e.getMessage());
        }
        return new ArrayList<>(mapa.values());
    }

    //Busca doenças e casos específicos para alimentar o PiePanel.
    
    public List<Doenca> buscarDoencasPorMes(String cidadeId, int mes) {
        List<Doenca> lista = new ArrayList<>();
        String sql = "SELECT d.nome, d.cor_hex, IFNULL(co.casos, 0) as casos " +
                     "FROM doencas d " +
                     "LEFT JOIN coletas co ON d.id = co.doenca_id AND co.cidade_id = ? " +
                     "AND co.mes = ? AND co.ano = ? ORDER BY d.id ASC";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cidadeId);
            stmt.setInt(2, mes);
            stmt.setInt(3, getAno());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Doenca d = new Doenca();
                d.setNome(rs.getString("nome"));
                d.setCasos(rs.getInt("casos"));
                String hex = rs.getString("cor_hex");
                d.setCorFromHex(hex != null ? hex.trim() : "#7A7A7A");
                lista.add(d);
            }
        } catch (Exception e) {
            System.err.println("// Erro ao buscar doenças do mês: " + e.getMessage());
        }
        return lista;
    }

    //Garante a existência de registros para os 12 meses do ano.
    //Utiliza um procesamento em lotes pra alta performance.
    //Eu acho que o ideal era ser uma em stream/realtime, mas acho que teria que implementar api(???)
     
    public void garantirDoencasBase(String cidadeId) {
        String sqlInsert = "INSERT IGNORE INTO coletas (cidade_id, doenca_id, ano, mes, casos, populacao_no_mes) " +
                           "VALUES (?, ?, ?, ?, 0, (SELECT populacao FROM cidades WHERE id = ?))";
        
        try (Connection conn = Conexao.conectar()) {
            conn.setAutoCommit(false); // Melhora performance de escrita em lote
            
            List<Integer> ids = new ArrayList<>();
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT id FROM doencas")) {
                while (rs.next()) ids.add(rs.getInt("id"));
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                int ano = getAno();
                for (int idD : ids) {
                    for (int m = 1; m <= 12; m++) {
                        stmt.setString(1, cidadeId);
                        stmt.setInt(2, idD);
                        stmt.setInt(3, ano);
                        stmt.setInt(4, m);
                        stmt.setString(5, cidadeId);
                        stmt.addBatch();
                    }
                }
                stmt.executeBatch();
                conn.commit();
            }
        } catch (Exception e) {
            System.err.println("// Erro ao gerar base anual: " + e.getMessage());
        }
    }

    //salva os casos e atualiza a população da cidade em uma transação.

    public void salvarDados(String cidadeId, Doenca doenca, int mes, int popMes) {
        String sqlColeta = "INSERT INTO coletas (cidade_id, doenca_id, ano, mes, casos, populacao_no_mes) " +
                           "VALUES (?, (SELECT id FROM doencas WHERE nome = ? LIMIT 1), ?, ?, ?, ?) " +
                           "ON DUPLICATE KEY UPDATE casos = VALUES(casos), populacao_no_mes = VALUES(populacao_no_mes)";
        
        try (Connection conn = Conexao.conectar()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement st = conn.prepareStatement(sqlColeta)) {
                st.setString(1, cidadeId); st.setString(2, doenca.getNome());
                st.setInt(3, getAno()); st.setInt(4, mes);      
                st.setInt(5, doenca.getCasos()); st.setInt(6, popMes); 
                st.executeUpdate();
            }

            try (PreparedStatement st = conn.prepareStatement("UPDATE cidades SET populacao = ? WHERE id = ?")) {
                st.setInt(1, popMes); st.setString(2, cidadeId);
                st.executeUpdate();
            }
            
            conn.commit(); 
        } catch (Exception e) {
            System.err.println("// Erro no salvamento: " + e.getMessage());
        }
    }

    //remove fisicamente o registro de uma coleta específica.
    
    public boolean removerCasos(String cidadeId, String nomeDoenca, int mes) {
        String sql = "DELETE FROM coletas WHERE cidade_id = ? " +
                     "AND doenca_id = (SELECT id FROM doencas WHERE nome = ? LIMIT 1) " +
                     "AND mes = ? AND ano = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cidadeId);
            stmt.setString(2, nomeDoenca);
            stmt.setInt(3, mes);
            stmt.setInt(4, getAno());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("// Erro ao remover registro: " + e.getMessage());
            return false;
        }
    }
}