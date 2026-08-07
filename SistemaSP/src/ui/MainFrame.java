package ui;

import components.HeaderPanel;
import dao.CidadeDAO; 
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import model.*;
import util.Cores;

public class MainFrame extends JFrame {
    private List<Cidade> cidades;
    private InfoPanel infoPanel;
    private MapPanel mapPanel;
    private HeaderPanel headerPanel;
    private Cidade selecionada;
    private final CidadeDAO cidadeDAO = new CidadeDAO(); 

    public MainFrame() {
        configurarJanela();
        
        // Carrega os dados do banco (ou fallback silencioso)
        this.cidades = carregarDados();
        
        inicializarComponentes();
        configurarLayout();
    }

    private void configurarJanela() {
        setTitle("SISTEPID - Vigilância Epidemiológica");
        setSize(1366, 768);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Cores.getFundo());
        setLayout(new BorderLayout());

        // Ícone
        try {
            java.net.URL url = getClass().getResource("/app/resources/logo-sp.png");
            if (url != null) setIconImage(new ImageIcon(url).getImage());
        } catch (Exception e) {
            // Silencioso se o ícone não existir no Arch
        }
    }

    private void inicializarComponentes() {
        // Callback para atualizar tudo quando houver save no EditDialog
        Runnable atualizarGeral = () -> {
            this.cidades = cidadeDAO.listarTodas(); // Recarrega do banco
            if (selecionada != null) infoPanel.exibir(selecionada);
            mapPanel.setCidades(cidades);
            headerPanel.atualizarChips(cidades);
        };

        infoPanel = new InfoPanel(atualizarGeral, cidades); 

        mapPanel = new MapPanel(cidades, c -> {
            this.selecionada = c;
            infoPanel.exibir(c);
        });

        headerPanel = new HeaderPanel(cidades);
    }

    private void configurarLayout() {
        add(headerPanel, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapPanel, infoPanel);
        split.setDividerLocation(850);
        split.setDividerSize(1);
        split.setBorder(BorderFactory.createEmptyBorder()); 
        split.setBackground(Cores.getFundo());
        
        // Customização da linha divisória para ser minimalista
        split.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(Cores.getBorda());
                        g.fillRect(0, 0, getSize().width, getSize().height);
                    }
                };
            }
        });

        add(split, BorderLayout.CENTER);
    }

    private List<Cidade> carregarDados() {
        List<Cidade> lista = cidadeDAO.listarTodas();
        return (lista != null && !lista.isEmpty()) ? lista : new ArrayList<>();
    }
}