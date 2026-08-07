import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TelaPrincipal extends JFrame {

    private DefaultTableModel modelo;

    public TelaPrincipal() {
        setTitle("Sistema Distribuidora");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        criarMenu();
        criarPainelPrincipal();
        carregarUltimasVendas();
        setVisible(true);
    }

    private void criarMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuCadastros = new JMenu("Cadastros");
        JMenuItem itemClientes = new JMenuItem("Clientes");
        JMenuItem itemProdutos = new JMenuItem("Produtos");
        JMenuItem itemFornecedores = new JMenuItem("Fornecedores");
        itemClientes.addActionListener(e -> new TelaClientes());
        itemProdutos.addActionListener(e -> new TelaProdutos());
        itemFornecedores.addActionListener(e -> new TelaFornecedores());
        menuCadastros.add(itemClientes);
        menuCadastros.add(itemProdutos);
        menuCadastros.add(itemFornecedores);

        JMenu menuEstoque = new JMenu("Estoque");
        JMenuItem itemEstoque = new JMenuItem("Controle de Estoque");
        itemEstoque.addActionListener(e -> new TelaEstoque());
        menuEstoque.add(itemEstoque);

        JMenu menuVendas = new JMenu("Vendas");
        JMenuItem itemVendas = new JMenuItem("Nova Venda");
        JMenuItem itemHistorico = new JMenuItem("Histórico de Vendas");
        itemVendas.addActionListener(e -> new TelaVendas());
        itemHistorico.addActionListener(e -> new TelaHistoricoVendas());
        menuVendas.add(itemVendas);
        menuVendas.add(itemHistorico);

        menuBar.add(menuCadastros);
        menuBar.add(menuEstoque);
        menuBar.add(menuVendas);
        setJMenuBar(menuBar);
    }

    private void criarPainelPrincipal() {
        JPanel painel = new JPanel(new BorderLayout());

        JLabel titulo = new JLabel("Últimas 10 Vendas", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        modelo = new DefaultTableModel();
        modelo.addColumn("ID");
        modelo.addColumn("Cliente");
        modelo.addColumn("Data");
        modelo.addColumn("Valor Total");

        JTable tabela = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tabela);

        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.addActionListener(e -> carregarUltimasVendas());

        painel.add(titulo, BorderLayout.NORTH);
        painel.add(scroll, BorderLayout.CENTER);
        painel.add(btnAtualizar, BorderLayout.SOUTH);

        add(painel);
    }

    private void carregarUltimasVendas() {
        try {
            modelo.setRowCount(0);
            VendaDAO dao = new VendaDAO();
            List<String[]> lista = dao.listarUltimas(10);
            for (String[] venda : lista) {
                modelo.addRow(venda);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar vendas: " + e.getMessage());
        }
    }
}