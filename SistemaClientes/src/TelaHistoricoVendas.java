import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TelaHistoricoVendas extends JFrame {

    private JTable tabela;
    private DefaultTableModel modelo;

    public TelaHistoricoVendas() {
        setTitle("Histórico de Vendas");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        modelo = new DefaultTableModel();
        modelo.addColumn("ID");
        modelo.addColumn("Cliente");
        modelo.addColumn("Data");
        modelo.addColumn("Valor Total");

        tabela = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tabela);
        add(scroll, BorderLayout.CENTER);

        JPanel painel = new JPanel();
        JButton btnDetalhes = new JButton("Ver Detalhes");
        JButton btnAtualizar = new JButton("Atualizar");

        btnDetalhes.addActionListener(e -> verDetalhes());
        btnAtualizar.addActionListener(e -> carregarVendas());

        painel.add(btnDetalhes);
        painel.add(btnAtualizar);
        add(painel, BorderLayout.SOUTH);

        carregarVendas();
        setVisible(true);
    }

    private void carregarVendas() {
        try {
            modelo.setRowCount(0);
            List<String[]> lista = new VendaDAO().listar();
            for (String[] venda : lista) {
                modelo.addRow(venda);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar vendas: " + e.getMessage());
        }
    }

    private void verDetalhes() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma venda!");
            return;
        }
        try {
            int vendaId = Integer.parseInt((String) modelo.getValueAt(linha, 0));
            List<String[]> itens = new VendaDAO().listarItens(vendaId);

            DefaultTableModel modeloDetalhes = new DefaultTableModel();
            modeloDetalhes.addColumn("Produto");
            modeloDetalhes.addColumn("Quantidade");
            modeloDetalhes.addColumn("Preço Unitário");

            for (String[] item : itens) {
                modeloDetalhes.addRow(item);
            }

            JTable tabelaDetalhes = new JTable(modeloDetalhes);
            JOptionPane.showMessageDialog(this, new JScrollPane(tabelaDetalhes),
                "Detalhes da Venda #" + vendaId, JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }
}