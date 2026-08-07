import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TelaFornecedores extends JFrame {

    private JTable tabela;
    private DefaultTableModel modelo;

    public TelaFornecedores() {
        setTitle("Fornecedores");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        modelo = new DefaultTableModel();
        modelo.addColumn("Nome");
        modelo.addColumn("Telefone");
        modelo.addColumn("CNPJ");

        tabela = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tabela);
        add(scroll, BorderLayout.CENTER);

        JPanel painel = new JPanel();
        JButton add = new JButton("Adicionar");
        JButton edit = new JButton("Editar");
        JButton del = new JButton("Remover");

        add.addActionListener(e -> adicionarFornecedor());
        edit.addActionListener(e -> editarFornecedor());
        del.addActionListener(e -> removerFornecedor());

        painel.add(add);
        painel.add(edit);
        painel.add(del);
        add(painel, BorderLayout.SOUTH);

        carregarFornecedores();
        setVisible(true);
    }

    private void carregarFornecedores() {
        try {
            modelo.setRowCount(0);
            FornecedorDAO dao = new FornecedorDAO();
            List<Fornecedor> lista = dao.listar();
            for (Fornecedor f : lista) {
                modelo.addRow(new Object[]{f.nome, f.telefone, f.cnpj});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar fornecedores: " + e.getMessage());
        }
    }

    private void adicionarFornecedor() {
        String nome = JOptionPane.showInputDialog(this, "Nome:");
        if (nome == null || nome.trim().isEmpty()) return;
        String telefone = JOptionPane.showInputDialog(this, "Telefone:");
        if (telefone == null || telefone.trim().isEmpty()) return;
        String cnpj = JOptionPane.showInputDialog(this, "CNPJ:");
        if (cnpj == null || cnpj.trim().isEmpty()) return;

        try {
            new FornecedorDAO().salvar(new Fornecedor(0, nome, telefone, cnpj));
            carregarFornecedores();
            JOptionPane.showMessageDialog(this, "Fornecedor adicionado com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void editarFornecedor() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um fornecedor para editar!");
            return;
        }
        String cnpj = (String) modelo.getValueAt(linha, 2);
        String nome = JOptionPane.showInputDialog(this, "Novo nome:", modelo.getValueAt(linha, 0));
        if (nome == null || nome.trim().isEmpty()) return;
        String telefone = JOptionPane.showInputDialog(this, "Novo telefone:", modelo.getValueAt(linha, 1));
        if (telefone == null || telefone.trim().isEmpty()) return;

        try {
            new FornecedorDAO().atualizar(new Fornecedor(0, nome, telefone, cnpj));
            carregarFornecedores();
            JOptionPane.showMessageDialog(this, "Fornecedor atualizado com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void removerFornecedor() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um fornecedor para remover!");
            return;
        }
        int confirmacao = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja remover o fornecedor?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacao != JOptionPane.YES_OPTION) return;

        try {
            String cnpj = (String) modelo.getValueAt(linha, 2);
            new FornecedorDAO().remover(cnpj);
            carregarFornecedores();
            JOptionPane.showMessageDialog(this, "Fornecedor removido com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }
}