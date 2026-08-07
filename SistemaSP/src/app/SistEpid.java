package app;

import javax.swing.*;
import ui.MainFrame;

//Ponto de entrada principal do SISTEPID.
 
public class SistEpid {
    public static void main(String[] args) {
        // Otimizações de renderização para Linux/Arch
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            configurarAparencia();
            
            // Inicializa e exibe a tela principal diretamente
            MainFrame main = new MainFrame();
            main.setVisible(true);
        });
    }

    private static void configurarAparencia() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
            // Se o sistema falhar em pegar o LookAndFeel nativo, o Java usa o padrão (CrossPlatform)
            System.err.println("Aviso: Não foi possível carregar o tema nativo.");
        }
    }
}