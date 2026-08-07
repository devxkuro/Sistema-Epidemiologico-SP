package components;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import model.Doenca;
import util.Cores;
import util.Texto;

public class PiePanel extends JPanel {
    private List<Doenca> doencas;

    public PiePanel(List<Doenca> doencas) {
        this.doencas = doencas;
        setOpaque(false); // Mantém a transparência para compor com o fundo do dashboard
        setPreferredSize(new Dimension(400, 300)); 
    }

    // Atualiza os dados e solicita o redesenho do componente
    public void setDoencas(List<Doenca> doencas) {
        this.doencas = doencas;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Aborta a renderização se não houver dados para evitar os NullPointerException
        if (doencas == null || doencas.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create(); // criei uma cópia para não sujar o Graphics global
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Cálculo responsivo do tamanho do gráfico baseado na largura disponível
        int size = Math.min((int)(getWidth() * 0.4), getHeight() - 80);
        int xCircle = 25; 
        int yCircle = (getHeight() - size) / 2;

        // Stream para somar o total de casos e usar no cálculo de proporção
        double total = doencas.stream().mapToInt(Doenca::getCasos).sum();
        
        // Se não houver casos, desenha apenas um contorno vazio (Estado Vazio)
        if (total == 0) {
            g2.setColor(Cores.getBorda());
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(xCircle, yCircle, size, size);
            g2.dispose();
            return;
        }

        double startAngle = 90; // Inicia no topo do círculo
        for (Doenca d : doencas) {
            if (d.getCasos() == 0) continue;
            
            // Calcula o ângulo da fatia baseado na proporção sobre o total
            double arcAngle = (d.getCasos() / total) * 360;
            
            g2.setColor(d.getCor() != null ? d.getCor() : Cores.MID_GREY);

            // fillArc com ajuste de +1 grau no ângulo para evitar frestas de arredondamento (antialiasing)
            g2.fillArc(xCircle, yCircle, size, size, (int) Math.round(startAngle), (int) Math.ceil(arcAngle + 1));
            
            startAngle += arcAngle;
        }

        //Efeito Donut (Furo Central) 
        g2.setColor(Cores.getFundo());
        int donutSize = (int) (size * 0.65);
        int offset = (size - donutSize) / 2;
        g2.fillOval(xCircle + offset, yCircle + offset, donutSize, donutSize);
        
        desenharLegenda(g2, xCircle + size + 40, yCircle + (size / 10), total);
        g2.dispose(); // Libera os recursos gráficos explicitamente
    }

    private void desenharLegenda(Graphics2D g2, int x, int y, double total) {
        g2.setFont(Texto.getInter(13f));
        int currentY = y;

        for (Doenca d : doencas) {
            // Quadrado de cor da legenda
            g2.setColor(d.getCor() != null ? d.getCor() : Cores.MID_GREY);
            g2.fillRoundRect(x, currentY, 14, 14, 4, 4);
            
            // Cálculo de percentual formatado
            double percent = (total > 0) ? (d.getCasos() / total) * 100 : 0;
            String info = String.format("%s: %d (%.1f%%)", d.getNome(), d.getCasos(), percent);
            
            g2.setColor(Cores.getTexto());
            g2.drawString(info, x + 25, currentY + 12);
            currentY += 30; // Salto de linha para o próximo item
        }
    }
}