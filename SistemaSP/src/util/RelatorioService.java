package util;

import java.util.List;
import java.util.Map;
import java.util.TreeMap; // isso deve mantém os meses em ordem (1, 2, 3...)
import java.util.stream.Collectors;
import model.Cidade;
import model.Coleta;

public class RelatorioService {

    //Soma os casos de uma doença em todas as cidades em um período específico.
     
    public int calcularTotalRegional(List<Cidade> cidades, String nomeDoenca, int mes, int ano) {
        return cidades.stream()
                .flatMap(c -> c.getColetas().stream())
                .filter(col -> col.getDoenca().equalsIgnoreCase(nomeDoenca) 
                            && col.getMes() == mes 
                            && col.getAno() == ano)
                .mapToInt(Coleta::getCasos)
                .sum();
    }

    //Calcula a incidência usando a população histórica gravada nas coletas.
     
    public double calcularIncidenciaRegional(List<Cidade> cidades, String nomeDoenca, int mes, int ano) {
        // Somamos a população que cada cidade tinha NAQUELE mês
        long populacaoRegionalNoMes = cidades.stream()
                .flatMap(c -> c.getColetas().stream())
                .filter(col -> col.getDoenca().equalsIgnoreCase(nomeDoenca) 
                            && col.getMes() == mes 
                            && col.getAno() == ano)
                .mapToLong(Coleta::getPopulacaoNoMes)
                .sum();

        int totalCasos = calcularTotalRegional(cidades, nomeDoenca, mes, ano);
        
        if (populacaoRegionalNoMes == 0) return 0;
        return ((double) totalCasos / populacaoRegionalNoMes) * 100;
    }

    //Gera o ranking baseado nas coletas do mês/ano, não na lista estática de doenças.
     
    public List<Cidade> gerarRankingCidades(List<Cidade> cidades, String nomeDoenca, int mes, int ano) {
        return cidades.stream()
                .sorted((c1, c2) -> {
                    int casos1 = obterCasosPorColeta(c1, nomeDoenca, mes, ano);
                    int casos2 = obterCasosPorColeta(c2, nomeDoenca, mes, ano);
                    return Integer.compare(casos2, casos1); // Ordem decrescente
                })
                .collect(Collectors.toList());
    }

    //Ajustado para usar Coleta::getMes e retornar um mapa ordenado para o gráfico.
     
    public Map<Integer, Integer> obterTendenciaTemporal(Cidade cidade, String nomeDoenca, int ano) {
        return cidade.getColetas().stream()
                .filter(c -> c.getDoenca().equalsIgnoreCase(nomeDoenca) && c.getAno() == ano)
                .collect(Collectors.groupingBy(
                        Coleta::getMes,
                        TreeMap::new, // Garante que o mapa venha ordenado por mês (1, 2, 3...)
                        Collectors.summingInt(Coleta::getCasos)
                ));
    }

    //Método auxiliar para buscar casos em um ponto específico do tempo.
     
    private int obterCasosPorColeta(Cidade c, String nomeDoenca, int mes, int ano) {
        return c.getColetas().stream()
                .filter(col -> col.getDoenca().equalsIgnoreCase(nomeDoenca) 
                            && col.getMes() == mes 
                            && col.getAno() == ano)
                .mapToInt(Coleta::getCasos)
                .findFirst()
                .orElse(0);
    }
}