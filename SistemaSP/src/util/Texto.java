package util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;

public class Texto {
    private static Font inter;

    public static Font getInter(float size) {
        if (inter == null) {
            try {
                InputStream is = Texto.class.getResourceAsStream("/app/resources/Inter-Medium.ttf");
                inter = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(inter);
            } catch (FontFormatException | IOException e) {
                return new Font("SansSerif", Font.PLAIN, (int)size);
            }
        }
        return inter.deriveFont(size);
    }

    //Converte número do mês em nome para a UI
    public static String getMesNome(int mes) {
        String[] meses = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", 
                          "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        return (mes >= 1 && mes <= 12) ? meses[mes - 1] : "Mês Inválido";
    }
}