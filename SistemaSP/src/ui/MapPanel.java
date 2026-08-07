package ui;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import model.Cidade;
import util.Cores;

public class MapPanel extends JPanel {
    private List<Cidade> cidades;
    private final Consumer<Cidade> onSelect;
    private Image mapaImg;
    private Cidade hovered = null;
    
    private double currentScale = 1.0;
    private int offX = 0, offY = 0;

    public MapPanel(List<Cidade> cidades, Consumer<Cidade> onSelect) {
        this.cidades = cidades;
        this.onSelect = onSelect;
        
        setOpaque(false); 
        carregarMapa();
        configurarEventos();
    }

    private void carregarMapa() {
        try {
            java.net.URL imgUrl = getClass().getResource("/app/resources/mapa-rmsp.png");
            if (imgUrl != null) {
                mapaImg = new ImageIcon(imgUrl).getImage();
            }
        } catch (Exception e) { 
            System.err.println("Erro ao carregar mapa: " + e.getMessage()); 
        }
    }

    private void configurarEventos() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Cidade c = detectar(e.getPoint());
                if (c != hovered) {
                    hovered = c;
                    setCursor(new Cursor(c != null ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Cidade c = detectar(e.getPoint());
                if (c != null) onSelect.accept(c);
            }
        });
    }

    public void setCidades(List<Cidade> cidades) {
        this.cidades = cidades;
        repaint();
    }

    private Cidade detectar(Point p) {
        if (mapaImg == null || cidades == null) return null;
        for (Cidade c : cidades) {
            int cx = offX + (int) (c.getMx() * currentScale);
            int cy = offY + (int) (c.getMy() * currentScale);
            // Área de clique de 20px (generosa para facilitar a UX)
            if (p.distance(cx, cy) < 20) return c;
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        
        // Fundo limpo
        g.setColor(Cores.getFundo());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        if (mapaImg != null) {
            renderizarMapaEPontos(g);
        }
    }

    private void renderizarMapaEPontos(Graphics2D g) {
        int iw = mapaImg.getWidth(null);
        int ih = mapaImg.getHeight(null);
        
        // Cálculo de escala mantendo uns 10% de respiro
        currentScale = Math.min((double) getWidth() / iw, (double) getHeight() / ih) * 0.9;
        int nw = (int) (iw * currentScale);
        int nh = (int) (ih * currentScale);
        
        offX = (getWidth() - nw) / 2;
        offY = (getHeight() - nh) / 2;

        g.drawImage(mapaImg, offX, offY, nw, nh, null);

        if (cidades == null) return;

        for (Cidade c : cidades) {
            int cx = offX + (int) (c.getMx() * currentScale);
            int cy = offY + (int) (c.getMy() * currentScale);
            boolean isHover = (c == hovered);
            
            // 1. Aura de destaque (Sutil, mas farmando)
            g.setColor(new Color(122, 30, 50, isHover ? 80 : 20)); 
            g.fillOval(cx - 12, cy - 12, 24, 24);
            
            // 2. Círculo central (Vinho se selecionado, Branco/Transparente se não)
            if (isHover) {
                g.setColor(Cores.WINE);
            } else {
                Color base = Cores.MODO_NOTURNO ? Color.LIGHT_GRAY : Color.WHITE;
                g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 180));
            }
            g.fillOval(cx - 6, cy - 6, 12, 12);
            
            // 3. Contorno
            g.setColor(Cores.WINE);
            g.setStroke(new BasicStroke(1.5f)); 
            g.drawOval(cx - 6, cy - 6, 12, 12);
        }
    }
}