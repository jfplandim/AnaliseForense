package org.example;

import br.edu.icev.aed.forense.AnaliseForenseAvancada;
import br.edu.icev.aed.forense.Alerta;
import java.util.*;
import java.io.IOException;

public class MinhaAnalise implements AnaliseForenseAvancada{

    // ----------------------------------------------------------------------
    // Desafio 1: Encontrar Sessões Inválidas (Exemplo de Estrutura)
    // ----------------------------------------------------------------------

    @Override
    public Set<String> encontrarSessoesInvalidas(String caminhoArquivo) throws IOException {
        Set<String> sessoesInvalidas = new HashSet<>();

        //resolução desafio

        return sessoesInvalidas;
    }

    // ----------------------------------------------------------------------
    // Desafio 2: Reconstruir Linha do Tempo
    // ----------------------------------------------------------------------

    @Override
    public List<String> reconstruirLinhaTempo(String caminhoArquivo, String sessionId) throws IOException {
        List<String> linhaTempo = new ArrayList<>();

        //resolução desafio

        return linhaTempo;
    }

    // ----------------------------------------------------------------------
    // Desafio 3: Priorizar Alertas
    // ----------------------------------------------------------------------

    @Override
    public List<Alerta> priorizarAlertas(String caminhoArquivo, int n) throws IOException {

        //resolução desafio

        return new ArrayList<>();
    }

    // ----------------------------------------------------------------------
    // Desafio 4: Encontrar Picos de Transferência
    // ----------------------------------------------------------------------

    @Override
    public Map<Long, Long> encontrarPicosTransferencia(String caminhoArquivo) throws IOException {

        //resolução desafio

        return new HashMap<>();
    }

    // ----------------------------------------------------------------------
    // Desafio 5: Rastrear Contaminação
    // ----------------------------------------------------------------------

    @Override
    public Optional<List<String>> rastrearContaminacao(String caminhoArquivo, String recursoInicial, String recursoAlvo) throws IOException {

        //resolução desafio

        return Optional.empty();
    }
}






