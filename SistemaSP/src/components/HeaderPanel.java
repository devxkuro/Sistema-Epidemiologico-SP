package components;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import model.Cidade;
import util.Cores;
import util.Texto;

public final class HeaderPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private final JPanel chipsRow;
    private Image logoIcon; 
    private List<Cidade> todasCidades = new ArrayList<>();
    private int indiceInicial = 0;
    private final int MAX_EXIBICAO = 4;
    
    private float alpha = 1.0f;
    private boolean escurecendo = true;
    private final Timer timerCiclo;
    private final Timer timerFade;

    public HeaderPanel(List<Cidade> cidades) {
        setBackground(Cores.BLACK);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 80)); 
        carregarLogo();

        // 1. Configuração dos Timers
        // Passo de 0.05f para um fade suave e perceptível
        timerFade = new Timer(25, e -> executarFade());
        
        timerCiclo = new Timer(5000, e -> { 
            if (todasCidades.size() > MAX_EXIBICAO) {
                escurecendo = true; 
                if (!timerFade.isRunning()) timerFade.start(); 
            }
        });

        // 2. Bloco Esquerdo (Barra de Destaque + Logo + Texto)
        JPanel leftContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        leftContainer.setOpaque(false);

        if (logoIcon != null) {
            JLabel lblIcon = new JLabel(new ImageIcon(logoIcon.getScaledInstance(55, 55, Image.SCALE_SMOOTH)));
            leftContainer.add(lblIcon);
        }

        JPanel infoGroup = new JPanel(new GridLayout(2, 1, 0, -5));
        infoGroup.setOpaque(false);
        
        JLabel lblNome = new JLabel("SISTEPID");
        lblNome.setForeground(Color.WHITE);
        lblNome.setFont(Texto.getInter(32f).deriveFont(Font.BOLD)); 
        
        JLabel lblSub = new JLabel("VIGILÂNCIA EPIDEMIOLÓGICA");
        lblSub.setForeground(new Color(0xAAAAAA)); 
        lblSub.setFont(Texto.getInter(11f).deriveFont(Font.BOLD));

        infoGroup.add(lblNome);
        infoGroup.add(lblSub);
        leftContainer.add(infoGroup);

        JPanel accentBar = new JPanel();
        accentBar.setBackground(Cores.WINE);
        accentBar.setPreferredSize(new Dimension(6, 80));

        add(accentBar, BorderLayout.WEST);
        add(leftContainer, BorderLayout.CENTER);

        // 3. Bloco Direito (Chips com o fade)
        chipsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 22)) {
            private static final long serialVersionUID = 1L;

            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // aqui o composite afeta o painel e os filhos (CityChips)
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                super.paint(g2); 
                g2.dispose();
            }
        };
        chipsRow.setOpaque(false);
        add(chipsRow, BorderLayout.EAST);

        atualizarChips(cidades);
    }

    private void executarFade() {
        if (escurecendo) {
            alpha -= 0.05f;
            if (alpha <= 0.0f) { 
                alpha = 0.0f; 
                escurecendo = false; 
                rotacionarIndices(); 
            }
        } else {
            alpha += 0.05f;
            if (alpha >= 1.0f) { 
                alpha = 1.0f; 
                timerFade.stop(); 
            }
        }
        chipsRow.repaint();
    }

    private void rotacionarIndices() {
        if (todasCidades.isEmpty()) return;
        // Avança um por um para criar o efeito de "rolagem" contínua
        indiceInicial = (indiceInicial + 1) % todasCidades.size();
        renderizarPaginaAtual();
    }

    private void renderizarPaginaAtual() {
        chipsRow.removeAll();
        if (todasCidades.isEmpty()) return;

        int qtd = Math.min(todasCidades.size(), MAX_EXIBICAO);
        for (int i = 0; i < qtd; i++) {
            int index = (indiceInicial + i) % todasCidades.size();
            // Certifica-se que o CityChip seja setOpaque(false)
            chipsRow.add(new CityChip(todasCidades.get(index)));
        }
        chipsRow.revalidate();
    }

    public void atualizarChips(List<Cidade> cidades) {
        this.todasCidades = (cidades != null) ? cidades : new ArrayList<>();
        renderizarPaginaAtual();
        
        if (todasCidades.size() > MAX_EXIBICAO) {
            if (!timerCiclo.isRunning()) timerCiclo.start();
        } else {
            timerCiclo.stop();
        }
    }

    private void carregarLogo() {
        try {
            java.net.URL imgUrl = getClass().getResource("/app/resources/logo-sp.png");
            if (imgUrl != null) {
                logoIcon = new ImageIcon(imgUrl).getImage();
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar logo do Header: " + e.getMessage());
        }
    }
}