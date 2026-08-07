package ui;

import components.PiePanel;
import dialog.EditDialog;
import dialog.RelatorioDialog;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.Cidade;
import model.Coleta;
import model.Doenca;
import util.Cores;
import util.Texto;

public class InfoPanel extends JPanel {
    private final JPanel content;
    private Cidade cidadeAtual;
    private final List<Cidade> listaGeral; 
    private final Runnable onUpdate; 
    private int mesAtivo = java.time.LocalDate.now().getMonthValue(); 

    public InfoPanel(Runnable onUpdate, List<Cidade> listaGeral) {
        this.onUpdate = onUpdate;
        this.listaGeral = listaGeral;
        
        setLayout(new BorderLayout());
        setBackground(Cores.getFundo());
        // Ajuste na borda externa para alinhar com o scroll
        setBorder(new EmptyBorder(40, 20, 40, 0)); 
        
        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Cores.getFundo());
        
        // CORREÇÃO: Padding interno de 25px para proteger os números da barra de rolagem, tava cortando antes
        content.setBorder(new EmptyBorder(0, 0, 0, 25)); 
        
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scroll, BorderLayout.CENTER);
    }

    public void setMesAtivo(int mes) {
        this.mesAtivo = mes;
    }

    public void exibir(Cidade cidade) {
        this.cidadeAtual = cidade;
        content.removeAll(); 
        
        setBackground(Cores.getFundo());
        content.setBackground(Cores.getFundo());

        int anoAtual = 2026;
        
        long populacaoNoMes = cidade.getColetas().stream()
                .filter(c -> c.getMes() == mesAtivo && c.getAno() == anoAtual)
                .mapToLong(Coleta::getPopulacaoNoMes)
                .findFirst()
                .orElse(cidade.getPopulacao());

        // Busca doenças com as cores vinculadas do banco de dados
        List<Doenca> doencasDoMes = cidade.getDoencasPorMes(mesAtivo, anoAtual);
        int totalCasosMes = doencasDoMes.stream().mapToInt(Doenca::getCasos).sum();

        // 1. Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Cores.getFundo());
        header.setMaximumSize(new Dimension(1500, 40)); 
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tag = new JLabel("CIDADE | DADOS DE " + Texto.getMesNome(mesAtivo).toUpperCase());
        tag.setFont(Texto.getInter(10f));
        tag.setForeground(Cores.getTextoSecundario());
        header.add(tag, BorderLayout.WEST);

        JButton btnMenu = new JButton("⋮");
        btnMenu.setFont(new Font("Serif", Font.BOLD, 22));
        btnMenu.setForeground(Cores.getTextoSecundario());
        btnMenu.setBorderPainted(false);
        btnMenu.setContentAreaFilled(false);
        btnMenu.setFocusPainted(false);
        btnMenu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnMenu.addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem edit = new JMenuItem("Editar Dados");
            JMenuItem report = new JMenuItem("Abrir Dashboard");
            String textoTema = Cores.MODO_NOTURNO ? "Modo Claro ☀️" : "Modo Noturno 🌙";
            JMenuItem themeToggle = new JMenuItem(textoTema);
            
            themeToggle.addActionListener(a -> {
                Cores.MODO_NOTURNO = !Cores.MODO_NOTURNO;
                Window win = SwingUtilities.getWindowAncestor(this);
                if (win != null) {
                    SwingUtilities.updateComponentTreeUI(win);
                    exibir(cidadeAtual);
                }
            });

            edit.addActionListener(a -> new EditDialog((Frame)SwingUtilities.getWindowAncestor(this), cidade, onUpdate).setVisible(true));
            
            report.addActionListener(a -> new RelatorioDialog((JFrame)SwingUtilities.getWindowAncestor(this), cidade, listaGeral).setVisible(true));

            menu.add(edit);
            menu.add(report);
            menu.addSeparator();
            menu.add(themeToggle);
            menu.show(btnMenu, 0, btnMenu.getHeight());
        });
        
        header.add(btnMenu, BorderLayout.EAST);
        content.add(header);

        // 2. Nome da Cidade
        JLabel nome = new JLabel(cidade.getNome().toUpperCase());
        nome.setFont(Texto.getInter(32f).deriveFont(Font.BOLD));
        nome.setForeground(Cores.getTexto()); 
        nome.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(nome);
        content.add(Box.createVerticalStrut(30));

        // 3. Cards
        JPanel cards = new JPanel(new GridLayout(1, 2, 15, 0));
        cards.setBackground(Cores.getFundo());
        cards.setMaximumSize(new Dimension(1500, 80));
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);

        cards.add(criarCard("POPULAÇÃO NO MÊS", String.format("%,d", populacaoNoMes), false));
        cards.add(criarCard("CASOS EM " + Texto.getMesNome(mesAtivo).toUpperCase(), String.format("%,d", totalCasosMes), true));
        content.add(cards);
        content.add(Box.createVerticalStrut(40));

        // 4. Gráfico de Rosca (PiePanel corrigido para não ter frestas)
        PiePanel pie = new PiePanel(doencasDoMes); 
        pie.setPreferredSize(new Dimension(450, 320)); 
        pie.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(pie);
        content.add(Box.createVerticalStrut(40));

        // 5. Ranking de Doenças
        JLabel labelDoencas = new JLabel("DISTRIBUIÇÃO POR DOENÇA");
        labelDoencas.setFont(Texto.getInter(10f));
        labelDoencas.setForeground(Cores.getTextoSecundario());
        labelDoencas.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(labelDoencas);
        content.add(Box.createVerticalStrut(15));

        for (Doenca d : doencasDoMes) {
            int pct = (totalCasosMes == 0) ? 0 : (int) ((d.getCasos() * 100.0) / totalCasosMes);
            content.add(criarLinhaDoenca(d, pct));
            content.add(Box.createVerticalStrut(15));
        }

        content.revalidate();
        content.repaint();
    }

    private JPanel criarCard(String titulo, String valor, boolean destacar) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Cores.getPainel()); 
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel t = new JLabel(titulo);
        t.setFont(Texto.getInter(10f));
        t.setForeground(destacar ? Cores.WINE : Cores.getTextoSecundario());
        
        JLabel v = new JLabel(valor);
        v.setFont(Texto.getInter(24f));
        v.setForeground(destacar ? Cores.WINE : Cores.getTexto()); 

        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return card;
    }

    private JPanel criarLinhaDoenca(Doenca d, int pct) {
        JPanel linha = new JPanel(new BorderLayout());
        linha.setBackground(Cores.getFundo());
        linha.setMaximumSize(new Dimension(1500, 45));
        linha.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel nomeDoenca = new JLabel(d.getNome());
        nomeDoenca.setFont(Texto.getInter(14f));
        nomeDoenca.setForeground(Cores.getTexto());
        
        JLabel total = new JLabel(String.valueOf(d.getCasos())); 
        total.setFont(Texto.getInter(14f));
        total.setForeground(d.getCor());

        linha.add(nomeDoenca, BorderLayout.WEST);
        linha.add(total, BorderLayout.EAST);
        
        JPanel barra = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Cores.MODO_NOTURNO ? Color.decode("#333333") : new Color(0xEEEEEE));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.setColor(d.getCor());
                int larguraPreenchimento = (int) (getWidth() * (pct / 100.0));
                g2.fillRoundRect(0, 0, larguraPreenchimento, getHeight(), 4, 4);
            }
        };

        barra.setPreferredSize(new Dimension(0, 6));
        barra.setOpaque(false);
        linha.add(barra, BorderLayout.SOUTH);

        return linha;
    }
}