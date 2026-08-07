import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TelaEstoque extends JFrame {

    private JTable tabela;
    private DefaultTableModel modelo;

    public TelaEstoque() {
        setTitle("Controle de Estoque");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        modelo = new DefaultTableModel();
        modelo.addColumn("Código");
        modelo.addColumn("Produto");
        modelo.addColumn("Quantidade");
        modelo.addColumn("Quantidade Mínima");
        modelo.addColumn("Status");

        tabela = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tabela);
        add(scroll, BorderLayout.CENTER);

        JPanel painel = new JPanel();
        JButton btnEntrada = new JButton("Entrada");
        JButton btnSaida = new JButton("Saída");
        JButton btnAtualizar = new JButton("Atualizar");

        btnEntrada.addActionListener(e -> movimentarEstoque(true));
        btnSaida.addActionListener(e -> movimentarEstoque(false));
        btnAtualizar.addActionListener(e -> carregarEstoque());

        painel.add(btnEntrada);
        painel.add(btnSaida);
        painel.add(btnAtualizar);
        add(painel, BorderLayout.SOUTH);

        carregarEstoque();
        setVisible(true);
    }

    private void carregarEstoque() {
        try {
            modelo.setRowCount(0);
            List<String[]> lista = new EstoqueDAO().listar();
            for (String[] item : lista) {
                int quantidade = Integer.parseInt(item[2]);
                int quantidadeMinima = Integer.parseInt(item[3]);
                String status = quantidade <= quantidadeMinima ? "⚠ Estoque Baixo" : "OK";
                modelo.addRow(new Object[]{item[0], item[1], item[2], item[3], status});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar estoque: " + e.getMessage());
        }
    }

    private void movimentarEstoque(boolean entrada) {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um produto!");
            return;
        }

        String tipo = entrada ? "entrada" : "saída";
        String quantidadeStr = JOptionPane.showInputDialog(this, "Quantidade de " + tipo + ":");
        if (quantidadeStr == null || quantidadeStr.trim().isEmpty()) return;

        try {
            int produtoId = Integer.parseInt((String) modelo.getValueAt(linha, 0));
            int quantidade = Integer.parseInt(quantidadeStr);
            EstoqueDAO dao = new EstoqueDAO();
            if (entrada) {
                dao.entrada(produtoId, quantidade);
            } else {
                dao.saida(produtoId, quantidade);
            }
            carregarEstoque();
            JOptionPane.showMessageDialog(this, "Estoque atualizado com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }
}