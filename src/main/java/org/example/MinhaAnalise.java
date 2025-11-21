package org.example;

import br.edu.icev.aed.forense.Alerta;
import br.edu.icev.aed.forense.AnaliseForenseAvancada;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MinhaAnalise implements AnaliseForenseAvancada{

    // ----------------------------------------------------------------------
    // Desafio 1: Encontrar Sessões Inválidas
    // ----------------------------------------------------------------------

    @Override
    public Set<String> encontrarSessoesInvalidas(String caminhoArquivo) throws IOException {
        Set<String> sessoesInvalidas = new HashSet<>();

        Map<String, Deque<String>> pilhas = new HashMap<>(2048);
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo), 32768)) {
            String linha = br.readLine();
            //processar cada linha
            while ((linha = br.readLine()) != null) {
                //uso do indexOf em vez do split para otimização da leitura
                int p1 = linha.indexOf(',');
                int p2 = linha.indexOf(',', p1 + 1);
                int p3 = linha.indexOf(',', p2 + 1);
                int p4 = linha.indexOf(',', p3 + 1);

                String userId = linha.substring(p1 + 1, p2);
                String sessionId = linha.substring(p2 + 1, p3);
                String action = linha.substring(p3 + 1, p4);

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
            List<Alerta> logs = lerAlertas(caminhoArquivo);
            if (recursoInicial.equals(recursoAlvo)) {
                boolean recursoExiste = logs.stream()
                        .anyMatch(alerta -> alerta.getTargetResource().equals(recursoInicial));
                if (recursoExiste) {
                    return Optional.of(Collections.singletonList(recursoInicial));
                } else {
                    return Optional.empty();
                }
            }
            Map<String, List<String>> grafo = construirGrafo(logs);
            if (!grafo.containsKey(recursoInicial)) {
                return Optional.empty();
            }
            return executarBFS(grafo, recursoInicial, recursoAlvo);
        }
        private List<Alerta> lerAlertas(String caminhoArquivo) throws IOException {
            List<Alerta> alertas = new ArrayList<>();
            List<String> linhas = Files.readAllLines(Paths.get(caminhoArquivo));
            for (int i = 1; i < linhas.size(); i++) {
                String linha = linhas.get(i).trim();
                if (linha.isEmpty()) continue;
                String[] campos = linha.split(",");
                if (campos.length >= 3) {
                }
            }
            return alertas;
        }
        private Map<String, List<String>> construirGrafo(List<Alerta> logs) {
            Map<String, List<String>> grafo = new HashMap<>();
            Map<String, List<Alerta>> logsPorSessao = new HashMap<>();
            for (Alerta alerta : logs) {
                String sessionId = alerta.getSessionId();
                logsPorSessao.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(alerta);
            }
            for (Map.Entry<String, List<Alerta>> entry : logsPorSessao.entrySet()) {
                List<Alerta> sessao = entry.getValue();
                for (int i = 0; i < sessao.size() - 1; i++) {
                    String recursoA = sessao.get(i).getTargetResource();
                    String recursoB = sessao.get(i + 1).getTargetResource();
                    grafo.computeIfAbsent(recursoA, k -> new ArrayList<>()).add(recursoB);
                }
                for (Alerta alerta : sessao) {
                    grafo.putIfAbsent(alerta.getTargetResource(), new ArrayList<>());
                }
            }
            return grafo;
        }
        private Optional<List<String>> executarBFS(
                Map<String, List<String>> grafo,
                String inicio,
                String alvo) {
            Queue<String> fila = new LinkedList<>();
            fila.add(inicio);
            Map<String, String> predecessor = new HashMap<>();
            predecessor.put(inicio, null);
            Set<String> visitados = new HashSet<>();
            visitados.add(inicio);
            while (!fila.isEmpty()) {
                String atual = fila.poll();
                if (atual.equals(alvo)) {
                    return Optional.of(reconstruirCaminho(predecessor, inicio, alvo));
                }
                List<String> vizinhos = grafo.getOrDefault(atual, Collections.emptyList());
                for (String vizinho : vizinhos) {
                    if (!visitados.contains(vizinho)) {
                        visitados.add(vizinho);
                        predecessor.put(vizinho, atual);
                        fila.add(vizinho);
                    }
                }
            }
            return Optional.empty();
        }
        private List<String> reconstruirCaminho(
                Map<String, String> predecessor,
                String inicio,
                String alvo) {
            List<String> caminho = new ArrayList<>();
            String atual = alvo;
            while (atual != null) {
                caminho.add(atual);
                atual = predecessor.get(atual);
            }
            Collections.reverse(caminho);
            return caminho;
    }
}