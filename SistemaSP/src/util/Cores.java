package util;

import java.awt.*;
import javax.swing.*;

public class Cores {
    public static boolean MODO_NOTURNO = true;

    //  CORES FIXAS 
    
    public static final Color BLACK       = new Color(0x050505); 
    public static final Color DARK        = new Color(0x181A1B); 
    public static final Color DARK_GREY   = new Color(0x242729); 
    public static final Color MID_GREY    = new Color(0x7A7A7A);
    public static final Color LIGHT_GREY  = new Color(0xC8C4BE);
    public static final Color BORDER      = new Color(0xE4E0DA);
    public static final Color OFF_WHITE   = new Color(0xF5F1EB);
    public static final Color WHITE       = new Color(0xFFFFFF);
    
    // Tons de Vinho 
    public static final Color WINE        = new Color(0x7A1E32);
    public static final Color WINE_LIGHT  = new Color(0x9B2840); // Corrigido aqui
    public static final Color WINE_PALE   = new Color(0xF3E8EB);

    //  MÉTODOS DE ESTILIZAÇÃO DE COMPONENTES 

    public static void estilizarBotaoVinho(JButton btn) {
        btn.setBackground(WINE);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true); 
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Inter", Font.BOLD, 12));
    }

    //  MÉTODOS DINÂMICOS 

    public static Color getHeader() {
        return BLACK; 
    }

    public static Color getFundo() { 
        return MODO_NOTURNO ? DARK : WHITE; 
    }

    public static Color getPainel() { 
        return MODO_NOTURNO ? DARK_GREY : OFF_WHITE; 
    }

    public static Color getTexto() { 
        return MODO_NOTURNO ? OFF_WHITE : BLACK; 
    }

    public static Color getTextoSecundario() { 
        return MODO_NOTURNO ? LIGHT_GREY : Color.GRAY; 
    }

    public static Color getBorda() { 
        return MODO_NOTURNO ? DARK_GREY : BORDER; 
    }
}