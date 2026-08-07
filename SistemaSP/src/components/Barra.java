package components;

import java.awt.*;
import javax.swing.*;
import util.Cores;

public class Barra extends JComponent {
    private final float pct;
    private final Color cor;
    private final String info;

    public Barra(float pct, Color cor, String info) {
        this.pct = Math.min(1.0f, Math.max(0.0f, pct));
        this.cor = cor;
        this.info = info;
        setPreferredSize(new Dimension(0, 24));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int larguraUtil = getWidth() - (info != null ? 100 : 0);
        int yBarra = (getHeight() - 10) / 2; 

        // 1. Fundo da trilha (Dinâmico)
        g.setColor(Cores.MODO_NOTURNO ? new Color(0x333333) : new Color(0xEEEEEE));
        g.fillRoundRect(0, yBarra, larguraUtil, 10, 4, 4);

        // 2. Pogrésso
        g.setColor(cor);
        g.fillRoundRect(0, yBarra, (int) (larguraUtil * pct), 10, 4, 4);

        // 3. Info (Consolas para visual técnico)
        if (info != null) {
            g.setColor(Cores.getTextoSecundario());
            g.setFont(new Font("Consolas", Font.PLAIN, 11));
            g.drawString(info, larguraUtil + 10, yBarra + 9);
        }
    }
}