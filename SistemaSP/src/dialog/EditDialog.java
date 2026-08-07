package dialog;

import dao.CidadeDAO;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import model.Cidade;
import model.Doenca;
import util.Cores;
import util.Texto;

public class EditDialog extends JDialog {
    private final Cidade cidade;
    private final Runnable onSave;
    private final List<JTextField> fieldsDoencas = new ArrayList<>();
    private final JTextField txtPopulacao = new JTextField();
    private final JComboBox<String> comboMeses;
    private List<Doenca> doencasRef;
    private final JPanel form; 

    private final String[] MESES = {
        "Janeiro", "Fevereiro", "Março", "Abril", 
        "Maio", "Junho", "Julho", "Agosto", 
        "Setembro", "Outubro", "Novembro", "Dezembro"
    };

    public EditDialog(Frame parent, Cidade cidade, Runnable onSave) {
        super(parent, "Editar Indicadores - " + cidade.getNome(), true);
        this.cidade = cidade;
        this.onSave = onSave;
        this.doencasRef = new ArrayList<>(cidade.getDoencas());

        setSize(420, 650);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        //  Estrutura Principal 
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(25, 30, 25, 30));

        //  Cabeçalho 
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        
        JLabel lblTitulo = new JLabel("ATUALIZAR DADOS MENSAIS");
        lblTitulo.setFont(Texto.getInter(16f).deriveFont(Font.BOLD));
        lblTitulo.setForeground(Cores.WINE);
        
        JLabel lblCidade = new JLabel(cidade.getNome().toUpperCase());
        lblCidade.setFont(Texto.getInter(12f));
        lblCidade.setForeground(Color.GRAY);
        
        header.add(lblTitulo);
        header.add(lblCidade);
        content.add(header, BorderLayout.NORTH);

        //  Corpo (Formulário) 
        form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        comboMeses = new JComboBox<>(MESES);
        comboMeses.setSelectedIndex(java.time.LocalDate.now().getMonthValue() - 1);
        comboMeses.addActionListener(e -> atualizarDadosPorMes());

        // Inicializa os campos com os dados do mês atual
        atualizarDadosPorMes();

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        content.add(scroll, BorderLayout.CENTER);

        //  Ação de Salvar 
        JButton btnSalvar = new JButton("CONFIRMAR ATUALIZAÇÃO");
        btnSalvar.setBackground(Cores.WINE);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(Texto.getInter(12f).deriveFont(Font.BOLD));
        btnSalvar.setPreferredSize(new Dimension(0, 45));
        btnSalvar.setBorderPainted(false);
        btnSalvar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalvar.setOpaque(true);
        
        btnSalvar.addActionListener(e -> executarSalvamento());
        
        content.add(btnSalvar, BorderLayout.SOUTH);
        add(content);
    }

    private void desenharCamposDoencas() {
        form.removeAll();
        fieldsDoencas.clear();

        // Seção de Configuração de Período e População
        adicionarLabelSecao("PERÍODO DE REFERÊNCIA");
        form.add(comboMeses);
        form.add(Box.createVerticalStrut(15));
        
        adicionarLabelSecao("POPULAÇÃO REGISTRADA NO MÊS");
        txtPopulacao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        form.add(txtPopulacao);
        
        form.add(Box.createVerticalStrut(20));
        form.add(new JSeparator());
        form.add(Box.createVerticalStrut(20));

        // Seção Dinâmica de Doenças
        for (Doenca d : doencasRef) {
            JPanel lineHeader = new JPanel(new BorderLayout());
            lineHeader.setOpaque(false);

            JLabel lbl = new JLabel(d.getNome().toUpperCase() + " (CASOS)");
            lbl.setFont(Texto.getInter(10f).deriveFont(Font.BOLD));
            
            // Botão de exclusão 
            JButton btnRemover = new JButton("🗑");
            btnRemover.setBorderPainted(false);
            btnRemover.setContentAreaFilled(false);
            btnRemover.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRemover.setForeground(Color.LIGHT_GRAY);
            btnRemover.addActionListener(e -> acaoRemoverCasos(d));
            
            lineHeader.add(lbl, BorderLayout.WEST);
            lineHeader.add(btnRemover, BorderLayout.EAST);
            form.add(lineHeader);
            
            JTextField txt = new JTextField(String.valueOf(d.getCasos())); 
            txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            txt.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 4, 0, 0, d.getCor()), 
                new EmptyBorder(5, 10, 5, 10)
            ));
            
            fieldsDoencas.add(txt);
            form.add(txt);
            form.add(Box.createVerticalStrut(15));
        }
        form.revalidate();
        form.repaint();
    }

    private void adicionarLabelSecao(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Texto.getInter(10f).deriveFont(Font.BOLD));
        form.add(l);
    }

    private void acaoRemoverCasos(Doenca d) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Deseja remover os registros de " + d.getNome() + " para este mês?",
            "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            int mes = comboMeses.getSelectedIndex() + 1;
            if (new CidadeDAO().removerCasos(cidade.getId(), d.getNome(), mes)) {
                atualizarDadosPorMes(); 
                if (onSave != null) onSave.run(); 
            }
        }
    }

    private void atualizarDadosPorMes() {
        int mes = comboMeses.getSelectedIndex() + 1;
        int ano = java.time.LocalDate.now().getYear();
        CidadeDAO dao = new CidadeDAO();
        
        // Sincroniza a população do mês selecionado
        dao.listarTodas().stream()
            .filter(c -> c.getId().equals(cidade.getId()))
            .findFirst()
            .ifPresent(cFresh -> {
                cFresh.getColetas().stream()
                    .filter(col -> col.getMes() == mes && col.getAno() == ano)
                    .findFirst()
                    .ifPresentOrElse(
                        col -> txtPopulacao.setText(String.valueOf(col.getPopulacaoNoMes())),
                        () -> txtPopulacao.setText(String.valueOf(cidade.getPopulacao()))
                    );
            });

        // Atualiza os indicadores para desenho dos inputs
        this.doencasRef = dao.buscarDoencasPorMes(cidade.getId(), mes);
        desenharCamposDoencas();
    }

    private void executarSalvamento() {
        try {
            int mes = comboMeses.getSelectedIndex() + 1;
            int pop = Integer.parseInt(txtPopulacao.getText().trim());
            CidadeDAO dao = new CidadeDAO();
            
            // Persiste cada campo individualmente
            for (int i = 0; i < fieldsDoencas.size(); i++) {
                int casos = Integer.parseInt(fieldsDoencas.get(i).getText().trim());
                Doenca d = doencasRef.get(i);
                d.setCasos(casos);
                
                // Chamada ao método sincronizado do DAO
                dao.salvarDados(cidade.getId(), d, mes, pop);
            }

            if (onSave != null) onSave.run();
            JOptionPane.showMessageDialog(this, "Dados sincronizados com sucesso.");
            dispose();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor, insira apenas valores numéricos.");
        }
    }
}