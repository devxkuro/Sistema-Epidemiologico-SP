package model;

import java.awt.Color;

public class Doenca {
    private String nome;
    private int casos;
    private Color cor;

    // Construtor completo
    public Doenca(String nome, int casos, Color cor) {
        this.nome = nome;
        this.casos = casos;
        this.cor = cor;
    }

    // Construtor vazio pra facilitar o preenchimento 
    public Doenca() {}

    //GETTERS 
    public String getNome() { return nome; }
    public int getCasos() { return casos; }
    public int getCases() { return getCasos(); }
    public Color getCor() { return cor; }

    //SETTERS
    public void setNome(String nome) { this.nome = nome; }
    public void setCasos(int casos) { this.casos = casos; }
    public void setCases(int cases) { this.casos = cases; }

    //Define a cor a partir de uma String HEX diretamente do banco de dados.
    
    public void setCorFromHex(String hex) {
        if (hex != null && !hex.isEmpty()) {
            try {
                this.cor = Color.decode(hex);
            } catch (NumberFormatException e) {
                this.cor = Color.GRAY; // Fallback caso o hex seja inválido
            }
        }
    }

    public void setCor(Color cor) { this.cor = cor; }
}