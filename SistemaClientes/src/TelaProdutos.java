import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TelaProdutos extends JFrame {

    private JTable tabela;
    private DefaultTableModel modelo;

    public TelaProdutos() {
        setTitle("Produtos");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        modelo = new DefaultTableModel();
        modelo.addColumn("Código");
        modelo.addColumn("Nome");
        modelo.addColumn("Preço");
        modelo.addColumn("Fornecedor");

        tabela = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tabela);
        add(scroll, BorderLayout.CENTER);

        JPanel painel = new JPanel();
        JButton add = new JButton("Adicionar");
        JButton edit = new JButton("Editar");
        JButton del = new JButton("Remover");

        add.addActionListener(e -> adicionarProduto());
        edit.addActionListener(e -> editarProduto());
        del.addActionListener(e -> removerProduto());

        painel.add(add);
        painel.add(edit);
        painel.add(del);
        add(painel, BorderLayout.SOUTH);

        carregarProdutos();
        setVisible(true);
    }

    private void carregarProdutos() {
        try {
            modelo.setRowCount(0);
            List<Produto> lista = new ProdutoDAO().listar();
            List<Fornecedor> fornecedores = new FornecedorDAO().listar();
            for (Produto p : lista) {
                String nomeFornecedor = fornecedores.stream()
                    .filter(f -> f.id == p.fornecedorId)
                    .map(f -> f.nome)
                    .findFirst()
                    .orElse("N/A");
                modelo.addRow(new Object[]{p.codigo, p.nome, p.preco, nomeFornecedor});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar produtos: " + e.getMessage());
        }
    }

    private void adicionarProduto() {
        try {
            List<Fornecedor> fornecedores = new FornecedorDAO().listar();
            if (fornecedores.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cadastre um fornecedor primeiro!");
                return;
            }

            String codigoStr = JOptionPane.showInputDialog(this, "Código:");
            if (codigoStr == null || codigoStr.trim().isEmpty()) return;
            String nome = JOptionPane.showInputDialog(this, "Nome:");
            if (nome == null || nome.trim().isEmpty()) return;
            String precoStr = JOptionPane.showInputDialog(this, "Preço:");
            if (precoStr == null || precoStr.trim().isEmpty()) return;

            String[] nomes = fornecedores.stream().map(f -> f.nome).toArray(String[]::new);
            String escolha = (String) JOptionPane.showInputDialog(this, "Fornecedor:", "Fornecedor",
                JOptionPane.QUESTION_MESSAGE, null, nomes, nomes[0]);
            if (escolha == null) return;

            int fornecedorId = fornecedores.stream()
                .filter(f -> f.nome.equals(escolha))
                .findFirst().get().id;

            new ProdutoDAO().salvar(new Produto(0, Integer.parseInt(codigoStr), nome, Double.parseDouble(precoStr), fornecedorId));
            carregarProdutos();
            JOptionPane.showMessageDialog(this, "Produto adicionado com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void editarProduto() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para editar!");
            return;
        }
        try {
            int codigo = (int) modelo.getValueAt(linha, 0);
            String nome = JOptionPane.showInputDialog(this, "Novo nome:", modelo.getValueAt(linha, 1));
            if (nome == null || nome.trim().isEmpty()) return;
            String precoStr = JOptionPane.showInputDialog(this, "Novo preço:", modelo.getValueAt(linha, 2));
            if (precoStr == null || precoStr.trim().isEmpty()) return;

            List<Fornecedor> fornecedores = new FornecedorDAO().listar();
            String[] nomes = fornecedores.stream().map(f -> f.nome).toArray(String[]::new);
            String escolha = (String) JOptionPane.showInputDialog(this, "Fornecedor:", "Fornecedor",
                JOptionPane.QUESTION_MESSAGE, null, nomes, modelo.getValueAt(linha, 3));
            if (escolha == null) return;

            int fornecedorId = fornecedores.stream()
                .filter(f -> f.nome.equals(escolha))
                .findFirst().get().id;

            new ProdutoDAO().atualizar(new Produto(0, codigo, nome, Double.parseDouble(precoStr), fornecedorId));
            carregarProdutos();
            JOptionPane.showMessageDialog(this, "Produto atualizado com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void removerProduto() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para remover!");
            return;
        }
        int confirmacao = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja remover o produto?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacao != JOptionPane.YES_OPTION) return;

        try {
            int codigo = (int) modelo.getValueAt(linha, 0);
            new ProdutoDAO().remover(codigo);
            carregarProdutos();
            JOptionPane.showMessageDialog(this, "Produto removido com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }
}