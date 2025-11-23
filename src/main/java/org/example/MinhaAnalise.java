package org.example;

import br.edu.icev.aed.forense.Alerta;
import br.edu.icev.aed.forense.AnaliseForenseAvancada;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


import java.util.ArrayList;
import java.util.List;

public class MinhaAnalise implements AnaliseForenseAvancada {

    private final LeituraCSV leitura = new LeituraCSV();

    // ----------------------------------------------------------------------
    // Desafio 1: Encontrar Sessões Inválidas
    // ----------------------------------------------------------------------

    @Override
    public Set<String> encontrarSessoesInvalidas(String caminhoArquivo) throws IOException {
        List<Alerta> logs = leitura.getAlertas(caminhoArquivo);
        Set<String> sessoesInvalidas = new HashSet<>();
        Map<String, Deque<String>> pilhas = new HashMap<>(2048);

        for (Alerta a: logs){
            String userId = a.getUserId();
            String sessionId = a.getSessionId();
            String actionId = a.getActionType();

            Deque<String> pilha = pilhas.get(userId);
                //adicionar pilha do usuario e fazer as verificações
                if (pilha == null) {
                    pilha = new ArrayDeque<>();
                    pilhas.put(userId, pilha);
                }
                //logica do login
                if (actionId.equals("LOGIN")) {

                    if (!pilha.isEmpty()) {
                        sessoesInvalidas.add(sessionId);
                    }
                    pilha.push(sessionId);
                }
                //logica do logout
                else if (actionId.equals("LOGOUT")) {
                    //se a pilha ta vazia
                    if (pilha.isEmpty()) {
                        sessoesInvalidas.add(sessionId);
                    } else {
                        if (!pilha.peek().equals(sessionId)) {
                            sessoesInvalidas.add(sessionId);
                        } else {
                            pilha.pop();
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
        List<Alerta> logs = lerAlertas(caminhoArquivo);
        List<String> linhaTempo = new ArrayList<>();

        for (Alerta alerta: logs){
            if (alerta.getSessionId().equals(sessionId)){
                linhaTempo.add(alerta.getActionType());
            }
        }
        return linhaTempo;
    }

    // ----------------------------------------------------------------------
    // Desafio 3: Priorizar Alertas
    // ----------------------------------------------------------------------

    @Override
    public List<Alerta> priorizarAlertas(String caminhoArquivo, int n) throws IOException {

        if (n<=0){
            return Collections.emptyList();
        }

        List<Alerta> logs=lerAlertas(caminhoArquivo);
        if (logs.isEmpty()) {
            return Collections.emptyList();
        }

        PriorityQueue<Alerta> alertasPrioritarios=new PriorityQueue<>((a1,a2)->Integer.compare(a2.nivelSeveridade,a1.nivelSeveridade));
alertasPrioritarios.addAll(logs);
List<Alerta> resultados=new ArrayList<>(n);

for (int i=0;i<n;i++){
    Alerta alerta = alertasPrioritarios.poll();
    if (alerta ==null){
        break;
    }
    resultados.add(alerta);
}


        return resultados;
    }

    // ----------------------------------------------------------------------
    // Desafio 4: Encontrar Picos de Transferência
    // ----------------------------------------------------------------------

    @Override
    public Map<Long, Long> encontrarPicosTransferencia(String caminhoArquivo) throws IOException {
        //pegar todos os alertas
        List<Alerta> eventos = leitura.getAlertas(caminhoArquivo);

        Map<Long, Long> resultado = new HashMap<>();
        Stack<Alerta> pilha = new Stack<>();
        //loop invertido
        for (int i = eventos.size() - 1; i >= 0; i--) {
            Alerta atual = eventos.get(i);

            // enquanto o topo da pilha tiver bytes <= bytes do atual vai desempilhe
            while (!pilha.isEmpty() && pilha.peek().getBytesTransferred() <= atual.getBytesTransferred()) {
                pilha.pop();
            }
            //se sobrou algo na pilha
            if (!pilha.isEmpty()) {
                resultado.put(atual.getTimestamp(), pilha.peek().getTimestamp());
            }
            pilha.push(atual);

        }

        return resultado;
    }

    // ----------------------------------------------------------------------
    // Desafio 5: Rastrear Contaminação
    // ----------------------------------------------------------------------

    @Override
    public Optional<List<String>> rastrearContaminacao(String caminhoArquivo, String recursoInicial, String recursoAlvo) throws IOException {
        List<Alerta> logs = lerAlertas(caminhoArquivo);

        // Caso especial: inicial == alvo
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

        // Recurso inicial não existe
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
            if (campos.length >= 7) {
                try {
                    long alertId = Long.parseLong(campos[0].trim());
                    int severity = Integer.parseInt(campos[5].trim());
                    long timestamp = Long.parseLong(campos[6].trim());

                    alertas.add(new Alerta(
                            alertId,
                            campos[1].trim(),
                            campos[2].trim(),
                            campos[3].trim(),
                            campos[4].trim(),
                            severity,
                            timestamp
                    ));
                } catch (NumberFormatException e) {
                    // Ignora linha com números inválidos
                }
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
            sessao.sort(Comparator.comparingLong(Alerta::getTimestamp));

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