package components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.Cidade;
import util.Cores;
import util.Texto;

public class CityChip extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int ICON_SIZE = 10; // Constante para manter o padrão visual

    public CityChip(Cidade cidade) {
        setOpaque(false); // Garante transparência para o fundo arredondado customizado
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(new EmptyBorder(5, 12, 5, 12));

        //Info da Cidade 
        //Painel vertical para alinhar Nome em cima de População
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblNome = new JLabel(cidade.getNome().toUpperCase());
        lblNome.setFont(Texto.getInter(11f).deriveFont(Font.BOLD));
        lblNome.setForeground(Cores.getTexto());
        
        JLabel lblPop = new JLabel(String.format("%,d hab", cidade.getPopulacao()));
        lblPop.setFont(Texto.getInter(9f));
        lblPop.setForeground(Cores.getTextoSecundario());

        info.add(lblNome);
        info.add(lblPop);
        add(info);
        
        // Espaçamento rígido entre o texto e os ícones
        add(Box.createRigidArea(new Dimension(10, 0)));

        //Alertas 
        // Usamos FlowLayout simples para os ícones ficarem lado a lado
        JPanel alertas = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        alertas.setOpaque(false);

        // Stream para filtrar doenças que atingiram o limiar de alerta
        cidade.getDoencas().stream()
            .filter(d -> d.getCasos() > (cidade.getPopulacao() * (d.getNome().equalsIgnoreCase("Dengue") ? 0.01 : 0.1)))
            .forEach(d -> alertas.add(new IconAlerta(d.getCor() != null ? d.getCor() : Cores.MID_GREY)));

        add(alertas);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Renderização do background do chip
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Preenchimento do painel (Puxa do utilitário de Cores)
        g2.setColor(Cores.getPainel());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

        // Borda sutil para dar profundidade
        g2.setColor(Cores.getBorda());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

        g2.dispose();
        super.paintComponent(g);
    }

    // Componente privado para evitar criação de classes externas desnecessárias
    private static class IconAlerta extends JComponent {
        private static final long serialVersionUID = 1L;
        private final Color cor;

        public IconAlerta(Color cor) {
            this.cor = cor;
            setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Desenha o círculo de alerta
            g2.setColor(cor);
            g2.fillOval(0, 0, ICON_SIZE, ICON_SIZE);
            
            g2.dispose();
        }
    }
}