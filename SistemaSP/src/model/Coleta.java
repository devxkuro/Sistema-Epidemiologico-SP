package model;

public class Coleta {
    private String doenca;
    private int casos;
    private int mes;
    private int ano;           // Adicionado para controle temporal
    private int populacaoNoMes; // A "foto" da população naquela data

    // Construtor completo para uso com o Banco de Dados
    public Coleta(int mes, int ano, int casos, String doenca, int populacaoNoMes) {
        this.mes = mes;
        this.ano = ano;
        this.casos = casos;
        this.doenca = doenca;
        this.populacaoNoMes = populacaoNoMes;
    }

    //Calcula a taxa de incidência (Casos / População) * 100.
    //Este método garante que o gráfico mostre dados reais, mesmo que a população da cidade mude no futuro.
     
    public double getIncidencia() {
        if (populacaoNoMes <= 0) return 0.0;
        return ((double) casos / populacaoNoMes) * 100;
    }

    // Getters e Setters
    public String getDoenca() { return doenca; }
    public void setDoenca(String doenca) { this.doenca = doenca; }
    
    public int getCasos() { return casos; }
    public void setCasos(int casos) { this.casos = casos; }
    
    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    public int getPopulacaoNoMes() { return populacaoNoMes; }
    public void setPopulacaoNoMes(int populacaoNoMes) { this.populacaoNoMes = populacaoNoMes; }
}