package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.Cores;

public class Cidade {
    private final String id;
    private String nome;
    private long populacao;
    private final List<Coleta> coletas = new ArrayList<>();
    private final List<Doenca> doencas = new ArrayList<>();

    // Mapeamento de posições no mapa (Coordenadas X, Y)
    private static final Map<String, Point> POSICOES = new HashMap<>();

    static {
        POSICOES.put("1", new Point(351, 267));
        POSICOES.put("10", new Point(384, 328));
        POSICOES.put("11", new Point(245, 248));
        POSICOES.put("12", new Point(545, 193));
        POSICOES.put("13", new Point(225, 221));
        POSICOES.put("14", new Point(237, 303));
        POSICOES.put("15", new Point(279, 286));
        POSICOES.put("16", new Point(170, 341));
        POSICOES.put("17", new Point(233, 357));
        POSICOES.put("18", new Point(523, 252));
        POSICOES.put("19", new Point(310, 83));
        POSICOES.put("2", new Point(463, 187));
        POSICOES.put("20", new Point(728, 303));
        POSICOES.put("3", new Point(418, 410));
        POSICOES.put("4", new Point(437, 310));
        POSICOES.put("5", new Point(279, 237));
        POSICOES.put("6", new Point(632, 302));
        POSICOES.put("60", new Point(258, 420));
        POSICOES.put("61", new Point(727, 177));
        POSICOES.put("62", new Point(143, 483));
        POSICOES.put("63", new Point(156, 147));
        POSICOES.put("64", new Point(513, 360));
        POSICOES.put("65", new Point(862, 289));
        POSICOES.put("66", new Point(596, 85));
        POSICOES.put("67", new Point(188, 426));
        POSICOES.put("68", new Point(138, 285));
        POSICOES.put("7", new Point(170, 248));
        POSICOES.put("8", new Point(562, 293));
        POSICOES.put("80", new Point(555, 150));
        POSICOES.put("81", new Point(324, 145));
        POSICOES.put("82", new Point(226, 137));
        POSICOES.put("83", new Point(319, 112));
        POSICOES.put("85", new Point(210, 245));
        POSICOES.put("86", new Point(416, 80));
        POSICOES.put("87", new Point(541, 230));
        POSICOES.put("88", new Point(499, 338));
        POSICOES.put("89", new Point(206, 183));
        POSICOES.put("9", new Point(483, 309));
        POSICOES.put("90", new Point(406, 291));
    }

    public Cidade(String id, String nome, long populacao) {
        this.id = id;
        this.nome = nome;
        this.populacao = populacao;
    }

    //  GETTERS E SETTERS 
    public String getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public long getPopulacao() { return populacao; }
    public void setPopulacao(long populacao) { this.populacao = populacao; }

    public List<Doenca> getDoencas() { return doencas; }
    public List<Coleta> getColetas() { return coletas; }

    public int getMx() { return POSICOES.getOrDefault(id, new Point(0, 0)).x; }
    public int getMy() { return POSICOES.getOrDefault(id, new Point(0, 0)).y; }

    //Retorna as doenças filtradas por mês e ano.
    //Vincula os dados da coleta com os objetos Doenca que já possuem a cor do banco.
     
    public List<Doenca> getDoencasPorMes(int mes, int ano) {
        return this.coletas.stream()
                .filter(c -> c.getMes() == mes && c.getAno() == ano)
                .map(c -> {
                    // Busca na lista 'doencas' a instância que o CidadeDAO já carregou com a cor certa
                    return this.doencas.stream()
                            .filter(d -> d.getNome().equalsIgnoreCase(c.getDoenca()))
                            .findFirst()
                            .orElseGet(() -> {
                                // Fallback: Cria uma nova se não encontrar na lista principal
                                Doenca nova = new Doenca();
                                nova.setNome(c.getDoenca());
                                nova.setCasos(c.getCasos());
                                nova.setCor(Cores.MID_GREY); 
                                return nova;
                            });
                }) 
                .toList();
    }

    public int totalCasos() {
        // Usa o método getCases() ou getCasos() da sua classe Doenca
        return doencas.stream().mapToInt(Doenca::getCasos).sum();
    }

    public double taxaIncidencia() {
        if (populacao <= 0) return 0;
        return (totalCasos() / (double) populacao) * 100;
    }

    public void limparDados() {
        this.doencas.clear();
        this.coletas.clear();
    }
}