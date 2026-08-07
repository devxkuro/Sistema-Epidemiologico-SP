import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TelaVendas extends JFrame {

    private JTable tabelaItens;
    private DefaultTableModel modeloItens;
    private JLabel labelTotal;
    private List<int[]> itens = new ArrayList<>();
    private double valorTotal = 0;
    private int clienteId = -1;

    public TelaVendas() {
        setTitle("Nova Venda");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        modeloItens = new DefaultTableModel();
        modeloItens.addColumn("Produto");
        modeloItens.addColumn("Quantidade");
        modeloItens.addColumn("Preço Unitário");
        modeloItens.addColumn("Subtotal");

        tabelaItens = new JTable(modeloItens);
        JScrollPane scroll = new JScrollPane(tabelaItens);
        add(scroll, BorderLayout.CENTER);

        JPanel painelSul = new JPanel(new BorderLayout());
        JPanel painelBotoes = new JPanel();

        JButton btnCliente = new JButton("Selecionar Cliente");
        JButton btnProduto = new JButton("Adicionar Produto");
        JButton btnFinalizar = new JButton("Finalizar Venda");
        JButton btnCancelar = new JButton("Cancelar");

        btnCliente.addActionListener(e -> selecionarCliente());
        btnProduto.addActionListener(e -> adicionarProduto());
        btnFinalizar.addActionListener(e -> finalizarVenda());
        btnCancelar.addActionListener(e -> dispose());

        painelBotoes.add(btnCliente);
        painelBotoes.add(btnProduto);
        painelBotoes.add(btnFinalizar);
        painelBotoes.add(btnCancelar);

        labelTotal = new JLabel("Total: R$ 0,00", SwingConstants.RIGHT);
        labelTotal.setFont(new Font("Arial", Font.BOLD, 14));
        labelTotal.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        painelSul.add(labelTotal, BorderLayout.NORTH);
        painelSul.add(painelBotoes, BorderLayout.SOUTH);
        add(painelSul, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void selecionarCliente() {
        try {
            List<Cliente> clientes = new ClienteDAO().listar();
            if (clientes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cadastre um cliente primeiro!");
                return;
            }
            String[] nomes = clientes.stream().map(c -> c.nome).toArray(String[]::new);
            String escolha = (String) JOptionPane.showInputDialog(this, "Selecione o cliente:",
                "Cliente", JOptionPane.QUESTION_MESSAGE, null, nomes, nomes[0]);
            if (escolha == null) return;
            clienteId = clientes.stream()
                .filter(c -> c.nome.equals(escolha))
                .findFirst().get().id;
            setTitle("Nova Venda - Cliente: " + escolha);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void adicionarProduto() {
        if (clienteId == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente primeiro!");
            return;
        }
        try {
            List<Produto> produtos = new ProdutoDAO().listar();
            if (produtos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cadastre um produto primeiro!");
                return;
            }
            String[] nomes = produtos.stream().map(p -> p.nome).toArray(String[]::new);
            String escolha = (String) JOptionPane.showInputDialog(this, "Selecione o produto:",
                "Produto", JOptionPane.QUESTION_MESSAGE, null, nomes, nomes[0]);
            if (escolha == null) return;

            Produto produto = produtos.stream()
                .filter(p -> p.nome.equals(escolha))
                .findFirst().get();

            String quantidadeStr = JOptionPane.showInputDialog(this, "Quantidade:");
            if (quantidadeStr == null || quantidadeStr.trim().isEmpty()) return;
            int quantidade = Integer.parseInt(quantidadeStr);

            double subtotal = produto.preco * quantidade;
            valorTotal += subtotal;

            itens.add(new int[]{produto.id, quantidade, (int) produto.preco});
            modeloItens.addRow(new Object[]{produto.nome, quantidade, produto.preco, subtotal});
            labelTotal.setText(String.format("Total: R$ %.2f", valorTotal));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void finalizarVenda() {
        if (clienteId == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente!");
            return;
        }
        if (itens.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Adicione pelo menos um produto!");
            return;
        }
        try {
            VendaDAO vendaDAO = new VendaDAO();
            EstoqueDAO estoqueDAO = new EstoqueDAO();
            int vendaId = vendaDAO.salvarVenda(clienteId, valorTotal);
            for (int[] item : itens) {
                vendaDAO.salvarItem(vendaId, item[0], item[1], item[2]);
                estoqueDAO.saida(item[0], item[1]);
            }
            JOptionPane.showMessageDialog(this, "Venda finalizada com sucesso!");
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }
}