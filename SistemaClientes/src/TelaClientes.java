import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TelaClientes extends JFrame {

    JTable tabela;
    DefaultTableModel modelo;

    public TelaClientes() {

        setTitle("Clientes");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        modelo = new DefaultTableModel();
        modelo.addColumn("Nome");
        modelo.addColumn("Endereço");
        modelo.addColumn("Telefone");
        modelo.addColumn("CPF");

        tabela = new JTable(modelo);

        JScrollPane scroll = new JScrollPane(tabela);

        add(scroll, BorderLayout.CENTER);

        JPanel painel = new JPanel();

        JButton add = new JButton("Adicionar");
        JButton edit = new JButton("Editar");
        JButton del = new JButton("Remover");

        add.addActionListener(e -> adicionarCliente());
        edit.addActionListener(e -> editarCliente());
        del.addActionListener(e -> removerCliente());

        painel.add(add);
        painel.add(edit);
        painel.add(del);

        add(painel, BorderLayout.SOUTH);

        carregarClientes();

        setVisible(true);
    }

    private void carregarClientes() {

        try {

            modelo.setRowCount(0);

            ClienteDAO dao = new ClienteDAO();

            List<Cliente> lista = dao.listar();

            for (Cliente c : lista) {

                modelo.addRow(new Object[]{
                    c.nome, c.endereco, c.telefone, c.cpf
                });

            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, "Erro ao carregar clientes: " + e.getMessage());

        }
    }

    private void adicionarCliente() {

        String nome = JOptionPane.showInputDialog(this, "Nome:");
        if (nome == null || nome.trim().isEmpty()) return;

        String endereco = JOptionPane.showInputDialog(this, "Endereço:");
        if (endereco == null || endereco.trim().isEmpty()) return;

        String telefone = JOptionPane.showInputDialog(this, "Telefone:");
        if (telefone == null || telefone.trim().isEmpty()) return;

        String cpf = JOptionPane.showInputDialog(this, "CPF:");
        if (cpf == null || cpf.trim().isEmpty()) return;

        try {

            ClienteDAO dao = new ClienteDAO();
            dao.salvar(new Cliente(0, nome, endereco, telefone, cpf));
            carregarClientes();
            JOptionPane.showMessageDialog(this, "Cliente adicionado com sucesso!");

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, "Erro ao adicionar cliente: " + e.getMessage());

        }
    }

    private void editarCliente() {

        int linha = tabela.getSelectedRow();

        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente para editar!");
            return;
        }

        String cpf = (String) modelo.getValueAt(linha, 3);

        String nome = JOptionPane.showInputDialog(this, "Novo nome:", modelo.getValueAt(linha, 0));
        if (nome == null || nome.trim().isEmpty()) return;

        String endereco = JOptionPane.showInputDialog(this, "Novo endereço:", modelo.getValueAt(linha, 1));
        if (endereco == null || endereco.trim().isEmpty()) return;

        String telefone = JOptionPane.showInputDialog(this, "Novo telefone:", modelo.getValueAt(linha, 2));
        if (telefone == null || telefone.trim().isEmpty()) return;

        try {

            ClienteDAO dao = new ClienteDAO();
            dao.atualizar(new Cliente(0, nome, endereco, telefone, cpf));
            carregarClientes();
            JOptionPane.showMessageDialog(this, "Cliente atualizado com sucesso!");

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, "Erro ao editar cliente: " + e.getMessage());

        }
    }

    private void removerCliente() {

        int linha = tabela.getSelectedRow();

        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente para remover!");
            return;
        }

        String cpf = (String) modelo.getValueAt(linha, 3);

        int confirmacao = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja remover o cliente?", "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirmacao != JOptionPane.YES_OPTION) return;

        try {

            ClienteDAO dao = new ClienteDAO();
            dao.remover(cpf);
            carregarClientes();
            JOptionPane.showMessageDialog(this, "Cliente removido com sucesso!");

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, "Erro ao remover cliente: " + e.getMessage());

        }
    }
}