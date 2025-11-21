package org.example;

import br.edu.icev.aed.forense.Alerta;
import br.edu.icev.aed.forense.AnaliseForenseAvancada;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class MinhaAnalise implements AnaliseForenseAvancada{

    // ----------------------------------------------------------------------
    // Desafio 1: Encontrar Sessões Inválidas
    // ----------------------------------------------------------------------

    @Override
    public Set<String> encontrarSessoesInvalidas(String caminhoArquivo) throws IOException {
        Set<String> sessoesInvalidas = new HashSet<>();

        Map<String, Deque<String>> pilhas = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha = br.readLine();
            //processar cada linha
            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",", -1);
                String userId = campos[1];
                String sessionId = campos[2];
                String action = campos[3];

                Deque<String> pilha = pilhas.get(userId);
                //adicionar pilha do usuario e fazer as verificações
                if (pilha == null) {
                    pilha = new ArrayDeque<>();
                    pilhas.put(userId, pilha);
                }
                //logica do login
                if (action.equals("LOGIN")) {

                    if (!pilha.isEmpty()) {
                        sessoesInvalidas.add(sessionId);
                    }
                    pilha.push(sessionId);
                }
                //logica do logout
                else if (action.equals("LOGOUT")) {
                    //se a pilha ta vazia
                    if (pilha.isEmpty()) {
                        sessoesInvalidas.add(sessionId);
                    }
                    else {
                        if (!pilha.peek().equals(sessionId)) {
                            sessoesInvalidas.add(sessionId);
                        }
                        else {
                            pilha.pop();
                        }
                    }
                }
            }
        }
        //sessoes que ficaram abertas
        for (Deque<String> pilha : pilhas.values()) {
            while (!pilha.isEmpty()) {
                sessoesInvalidas.add(pilha.pop());
            }
        }
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






