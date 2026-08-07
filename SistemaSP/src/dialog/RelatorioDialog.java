package dialog;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import model.Cidade;
import model.Coleta;
import util.Cores;
import util.Texto;

//Diálogo de análise avançada (Dashboard). 
//Permite comparar a evolução epidemiológica de uma cidade foco com outras da região.
 
public class RelatorioDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final List<Cidade> todasCidades;
    private final Cidade cidadeFoco;
    private final Set<Cidade> cidadesSelecionadas = new HashSet<>();
    private final Map<String, Color> mapaCores = new HashMap<>(); // Mapeia ID da cidade para uma cor fixa no gráfico
    private final List<PontoGrafico> pontosNaTela = new ArrayList<>(); // Armazena coordenadas para detecção de mouse (tooltip)
    
    private JComboBox<String> cbDoenca;
    private JComboBox<Integer> cbAno;
    private JComboBox<String> cbMesRanking;
    private JPanel rankingPanel;
    private JPanel tendenciaPanel;
    private PontoGrafico pontoAtivo = null;

    private final String[] MESES = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
    
    // Paleta para as cidades secundárias (comparativo)
    private final Color[] CORES_PALETA = {
        new Color(0x4A90E2), new Color(0x50E3C2), new Color(0xF5A623), 
        new Color(0xB8E986), new Color(0xBD10E0), new Color(0x9013FE), 
        new Color(0x417505), new Color(0x444444)
    };

    public RelatorioDialog(JFrame parent, Cidade foco, List<Cidade> todas) {
        super(parent, "SISTEPID - Analisador Regional", true);
        this.todasCidades = todas;
        this.cidadeFoco = foco;
        
        atribuirCoresCidades();
        
        setSize(1300, 800);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Cores.getFundo());
        setLayout(new BorderLayout());

        // Inicialização das seções da interface
        setupFiltrosTop();
        setupSeletorCidades();
        setupGrafico();
        setupRankingLateral();

        atualizarDashboard(); // Primeira renderização dos dados
    }

   
     //Define uma cor única para cada cidade para garantir consistência visual no gráfico e ranking.
    
    private void atribuirCoresCidades() {
        int corIndex = 0;
        for (Cidade c : todasCidades) {
            if (c.getId().equals(cidadeFoco.getId())) {
                mapaCores.put(c.getId(), Cores.WINE); // Cidade principal sempre em destaque (Wine)
            } else {
                mapaCores.put(c.getId(), CORES_PALETA[corIndex % CORES_PALETA.length]);
                corIndex++;
            }
        }
    }

    //Barra superior com filtros de Doença e Ano.
   
    private void setupFiltrosTop() {
        final JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 15));
        topo.setBackground(Cores.getPainel());
        topo.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Cores.getBorda()));
        
        final JLabel lblTitulo = new JLabel("FOCO: " + cidadeFoco.getNome().toUpperCase());
        lblTitulo.setFont(Texto.getInter(16f).deriveFont(Font.BOLD));
        lblTitulo.setForeground(Cores.getTexto());

        // Coleta nomes únicos de doenças de todas as cidades para o filtro
        final Set<String> nomesDoencas = todasCidades.stream()
                .flatMap(c -> c.getDoencas().stream())
                .map(model.Doenca::getNome)
                .collect(Collectors.toCollection(TreeSet::new));

        cbDoenca = new JComboBox<>(nomesDoencas.toArray(String[]::new));
        cbAno = new JComboBox<>(new Integer[]{2024, 2025, 2026});
        cbAno.setSelectedItem(LocalDate.now().getYear());

        topo.add(lblTitulo);
        topo.add(Box.createHorizontalStrut(30));
        topo.add(criarLabelFiltro("Doença:")); topo.add(cbDoenca);
        topo.add(criarLabelFiltro("Ano:")); topo.add(cbAno);
        
        // Listeners para atualizar os gráficos ao mudar o filtro
        cbDoenca.addActionListener(e -> atualizarDashboard());
        cbAno.addActionListener(e -> atualizarDashboard());
        
        add(topo, BorderLayout.NORTH);
    }

    private JLabel criarLabelFiltro(String texto) {
        final JLabel l = new JLabel(texto);
        l.setFont(Texto.getInter(11f));
        l.setForeground(Cores.getTextoSecundario());
        return l;
    }

    //Painel lateral esquerdo com checkboxes para selecionar cidades para comparação.
    
    private void setupSeletorCidades() {
        final JPanel containerEsquerdo = new JPanel(new BorderLayout());
        containerEsquerdo.setBackground(Cores.getFundo());
        containerEsquerdo.setPreferredSize(new Dimension(280, 0));
        containerEsquerdo.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Cores.getBorda()));

        final JLabel lblTitulo = new JLabel("COMPARAR COM");
        lblTitulo.setFont(Texto.getInter(11f).deriveFont(Font.BOLD));
        lblTitulo.setForeground(Cores.getTextoSecundario());
        lblTitulo.setBorder(new EmptyBorder(25, 20, 10, 20));
        containerEsquerdo.add(lblTitulo, BorderLayout.NORTH);

        final JPanel listaCidades = new JPanel();
        listaCidades.setLayout(new BoxLayout(listaCidades, BoxLayout.Y_AXIS));
        listaCidades.setBackground(Cores.getFundo());
        listaCidades.setBorder(new EmptyBorder(0, 10, 10, 10));

        for (final Cidade c : todasCidades) {
            if (c.getId().equals(cidadeFoco.getId())) continue; // Pula a cidade foco (já está fixa)

            final JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
            item.setOpaque(false);
            item.setMaximumSize(new Dimension(280, 38));
            
            // Pequeno círculo indicativo da cor da cidade no gráfico
            final JPanel indicador = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(mapaCores.get(c.getId()));
                    g2.fillOval(0, 0, 10, 10);
                }
            };
            indicador.setPreferredSize(new Dimension(10, 10));
            indicador.setOpaque(false);
            
            final JCheckBox chk = new JCheckBox(c.getNome());
            chk.setFont(Texto.getInter(13f));
            chk.setForeground(Cores.getTexto());
            chk.setOpaque(false);
            chk.setFocusPainted(false);
            
            chk.addActionListener(e -> {
                if (chk.isSelected()) cidadesSelecionadas.add(c);
                else cidadesSelecionadas.remove(c);
                atualizarDashboard(); 
            });

            item.add(indicador);
            item.add(chk);
            listaCidades.add(item);
        }

        final JScrollPane scrollSeletor = new JScrollPane(listaCidades);
        scrollSeletor.setBorder(null);
        scrollSeletor.setOpaque(false);
        scrollSeletor.getViewport().setOpaque(false);
        scrollSeletor.getVerticalScrollBar().setUnitIncrement(12);
            
        containerEsquerdo.add(scrollSeletor, BorderLayout.CENTER);
        add(containerEsquerdo, BorderLayout.WEST);
    }

    //Área central onde o gráfico de linhas (tendência) é desenhado.
    
    private void setupGrafico() {
        tendenciaPanel = new JPanel() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                desenharGraficoEvolucao(g);
            }
        };
        tendenciaPanel.setBackground(Cores.getFundo());
        
        // Lógica de Hover: Detecta se o mouse está sobre um nó do gráfico para exibir valor
        tendenciaPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                PontoGrafico encontrado = null;
                for (PontoGrafico p : pontosNaTela) {
                    if (e.getPoint().distance(p.x, p.y) < 15) {
                        encontrado = p;
                        break;
                    }
                }
                if (encontrado != pontoAtivo) {
                    pontoAtivo = encontrado;
                    tendenciaPanel.setCursor(new Cursor(encontrado != null ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    tendenciaPanel.repaint();
                }
            }
        });

        add(tendenciaPanel, BorderLayout.CENTER);
    }

    //Painel lateral direito para o ranking mensal comparativo.
    
    private void setupRankingLateral() {
        final JPanel containerDireita = new JPanel(new BorderLayout());
        containerDireita.setPreferredSize(new Dimension(320, 0));
        containerDireita.setBackground(Cores.getFundo());
        containerDireita.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Cores.getBorda()));

        final JPanel header = new JPanel(new GridLayout(2, 1, 0, 5));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 20, 10, 20));

        final JLabel lbl = new JLabel("RANKING DO MÊS");
        lbl.setFont(Texto.getInter(12f).deriveFont(Font.BOLD));
        lbl.setForeground(Cores.getTexto());
        
        cbMesRanking = new JComboBox<>(MESES);
        cbMesRanking.setSelectedIndex(LocalDate.now().getMonthValue() - 1); 
        cbMesRanking.addActionListener(e -> atualizarDashboard());

        header.add(lbl);
        header.add(cbMesRanking);

        rankingPanel = new JPanel();
        rankingPanel.setLayout(new BoxLayout(rankingPanel, BoxLayout.Y_AXIS));
        rankingPanel.setOpaque(false);
        rankingPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        containerDireita.add(header, BorderLayout.NORTH);
        
        final JScrollPane scrollRanking = new JScrollPane(rankingPanel);
        scrollRanking.setOpaque(false);
        scrollRanking.getViewport().setOpaque(false);
        scrollRanking.setBorder(null);
        
        containerDireita.add(scrollRanking, BorderLayout.CENTER);
        add(containerDireita, BorderLayout.EAST);
    }

    //Atualiza os dados de todos os painéis após mudanças nos filtros ou seleção de cidades.
    
    private void atualizarDashboard() {
        final String doencaSel = (String) cbDoenca.getSelectedItem();
        final int mesSelIndex = cbMesRanking.getSelectedIndex() + 1;
        if (doencaSel == null) return;

        rankingPanel.removeAll();
        
        // 1. Obtém o valor da cidade foco para calcular o delta (diferença) das outras
        final int casosFoco = getCasosMes(cidadeFoco, doencaSel, mesSelIndex);

        final List<Cidade> exibirNoRanking = new ArrayList<>(cidadesSelecionadas);
        exibirNoRanking.add(cidadeFoco);

        // Ordena a lista do maior número de casos para o menor
        exibirNoRanking.sort((c1, c2) -> Integer.compare(
            getCasosMes(c2, doencaSel, mesSelIndex), 
            getCasosMes(c1, doencaSel, mesSelIndex)
        ));

        final int maxRanking = exibirNoRanking.stream()
                .mapToInt(c -> getCasosMes(c, doencaSel, mesSelIndex))
                .max().orElse(100);

        for (final Cidade c : exibirNoRanking) {
            final int n = getCasosMes(c, doencaSel, mesSelIndex);
            boolean ehFoco = c.getId().equals(cidadeFoco.getId());
            
            // Adiciona a barra visual de ranking
            rankingPanel.add(criarBarraRanking(c.getNome(), n, maxRanking, mapaCores.get(c.getId()), ehFoco, casosFoco));
            rankingPanel.add(Box.createVerticalStrut(15));
        }

        rankingPanel.revalidate();
        rankingPanel.repaint();
        if (tendenciaPanel != null) tendenciaPanel.repaint(); // Redesenha o gráfico de linhas
    }

    //Desenha manualmente os eixos, grades e legendas do gráfico de evolução.
     
    private void desenharGraficoEvolucao(Graphics g) {
        final Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        pontosNaTela.clear(); // Limpa pontos da renderização anterior

        final int w = tendenciaPanel.getWidth(), h = tendenciaPanel.getHeight();
        final int mL = 80, mR = 40, mTB = 60; // Margens: Left, Right, Top/Bottom
        final int gW = w - mL - mR, gH = h - 2 * mTB;
        final String doencaSel = (String) cbDoenca.getSelectedItem();

        if (doencaSel == null) return;

        // Calcula o valor máximo de casos para definir a escala vertical
        int maxReal = 0;
        final List<Cidade> monitoradas = new ArrayList<>(cidadesSelecionadas);
        monitoradas.add(cidadeFoco);
        for (final Cidade c : monitoradas) {
            for (final Coleta col : c.getColetas()) {
                if (col.getDoenca().equalsIgnoreCase(doencaSel) && col.getCasos() > maxReal) maxReal = col.getCasos();
            }
        }
        final int maxEscala = (maxReal == 0) ? 100 : ((maxReal / 50) + 1) * 50;

        // Desenha as linhas de grade horizontais e rótulos de valores
        g2.setColor(Cores.getBorda());
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= 4; i++) {
            final int y = (mTB + gH) - (i * gH / 4);
            g2.drawLine(mL, y, mL + gW, y);
            g2.setColor(Cores.getTextoSecundario());
            g2.drawString(String.format("%,d", (maxEscala * i) / 4), mL - 60, y + 5);
            g2.setColor(Cores.getBorda());
        }

        // Desenha os rótulos dos meses no eixo X
        final int stepX = (gW / 11);
        for (int i = 0; i < 12; i++) {
            final int x = mL + (i * stepX);
            g2.drawString(MESES[i], x - 10, mTB + gH + 25);
        }

        // Desenha as linhas de tendência para cada cidade selecionada
        for (final Cidade c : cidadesSelecionadas) {
            plotarLinha(g2, c, doencaSel, mapaCores.get(c.getId()), mL, mTB, stepX, gH, maxEscala, false);
        }
        // Plota a cidade foco por último para ficar por cima e com traço mais grosso
        plotarLinha(g2, cidadeFoco, doencaSel, mapaCores.get(cidadeFoco.getId()), mL, mTB, stepX, gH, maxEscala, true);

        // Desenha o balão de informação se houver hover do mouse
        if (pontoAtivo != null) desenharTooltip(g2);
    }

    //Plota a linha de evolução e os nós (círculos) de uma cidade específica.
     
    private void plotarLinha(Graphics2D g2, Cidade c, String doenca, Color cor, int mL, int mTB, int stepX, int gH, int max, boolean destaque) {
        final List<Coleta> coletas = c.getColetas().stream()
                .filter(col -> col.getDoenca().equalsIgnoreCase(doenca))
                .sorted(Comparator.comparingInt(Coleta::getMes))
                .collect(Collectors.toList());

        if (coletas.isEmpty()) return;

        g2.setColor(cor);
        g2.setStroke(new BasicStroke(destaque ? 3.0f : 1.5f));

        for (int i = 0; i < coletas.size(); i++) {
            final Coleta atual = coletas.get(i);
            final int x = mL + (atual.getMes() - 1) * stepX;
            final int y = (mTB + gH) - (atual.getCasos() * gH / max);

            // Adiciona ponto à lista de detecção para o Tooltip
            pontosNaTela.add(new PontoGrafico(x, y, atual.getCasos(), c.getNome()));

            if (i < coletas.size() - 1) {
                final Coleta prox = coletas.get(i + 1);
                final int x2 = mL + (prox.getMes() - 1) * stepX;
                final int y2 = (mTB + gH) - (prox.getCasos() * gH / max);
                g2.drawLine(x, y, x2, y2); // Conecta os pontos com uma linha
            }
            g2.fillOval(x - 4, y - 4, 8, 8); // Desenha o nó do mês
        }
    }

    // Desenha o balão flutuante (tooltip) no gráfico.
     
    private void desenharTooltip(Graphics2D g2) {
        final String txt = pontoAtivo.cidade + ": " + String.format("%,d", pontoAtivo.valor);
        g2.setFont(Texto.getInter(12f).deriveFont(Font.BOLD));
        final int tw = g2.getFontMetrics().stringWidth(txt) + 20;
        
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(pontoAtivo.x - tw/2, pontoAtivo.y - 40, tw, 28, 8, 8);
        g2.setColor(Color.WHITE);
        g2.drawString(txt, pontoAtivo.x - (tw-20)/2, pontoAtivo.y - 21);
    }

    private int getCasosMes(Cidade c, String doenca, int mes) {
        return c.getColetas().stream()
                .filter(col -> col.getDoenca().equalsIgnoreCase(doenca) && col.getMes() == mes)
                .mapToInt(Coleta::getCasos).findFirst().orElse(0);
    }

    //Constrói o componente visual de uma linha no ranking lateral.
     
    private JPanel criarBarraRanking(String nome, int casos, int max, Color cor, boolean ehFoco, int casosFoco) {
        final JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(300, 55));
        
        // Container superior: Nome da Cidade e Delta (Diferença em relação ao foco)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        final JLabel lbl = new JLabel(nome.toUpperCase());
        lbl.setFont(Texto.getInter(10f).deriveFont(Font.BOLD));
        lbl.setForeground(ehFoco ? cor : Cores.getTexto());
        topPanel.add(lbl, BorderLayout.WEST);

        // Lógica do Delta: Compara se a cidade tem mais ou menos casos que a cidade principal
        if (!ehFoco) {
            int delta = casos - casosFoco;
            JLabel lblDelta = new JLabel();
            lblDelta.setFont(Texto.getInter(9f).deriveFont(Font.BOLD));
            
            if (delta > 0) {
                lblDelta.setText("+" + delta); // Mais crítica que o foco
                lblDelta.setForeground(new Color(0x2ECC71)); // Verde (como é doença, maior que o foco é destacado)
            } else if (delta < 0) {
                lblDelta.setText(String.valueOf(delta)); // Menos crítica que o foco
                lblDelta.setForeground(new Color(0xE74C3C)); // Vermelho
            } else {
                lblDelta.setText("0");
                lblDelta.setForeground(Color.GRAY);
            }
            topPanel.add(lblDelta, BorderLayout.EAST);
        }

        // Barra de progresso para visualização da magnitude dos casos
        final JProgressBar bar = new JProgressBar(0, max);
        bar.setValue(casos);
        bar.setUI(new BasicProgressBarUI()); 
        bar.setForeground(cor);
        bar.setBackground(Cores.getBorda());
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(0, 8));

        final JLabel lblValor = new JLabel(String.format("%,d", casos));
        lblValor.setFont(Texto.getInter(10f));
        lblValor.setForeground(Cores.getTextoSecundario());

        p.add(topPanel, BorderLayout.NORTH);
        p.add(bar, BorderLayout.CENTER);
        p.add(lblValor, BorderLayout.EAST);
        
        return p;
    }

    //Classe auxiliar para armazenar dados espaciais de um ponto plotado no gráfico.
     
    private static class PontoGrafico {
        final int x, y, valor;
        final String cidade;
        PontoGrafico(int x, int y, int valor, String cidade) {
            this.x = x; this.y = y; this.valor = valor; this.cidade = cidade;
        }
    }
}