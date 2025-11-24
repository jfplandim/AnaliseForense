package org.example;

import br.edu.icev.aed.forense.Alerta;
import br.edu.icev.aed.forense.AnaliseForenseAvancada;

import java.io.IOException;
import java.util.*;

public class MinhaAnalise implements AnaliseForenseAvancada {

    private final LeituraCSV leitura = new LeituraCSV();

    @Override
    public Set<String> encontrarSessoesInvalidas(String caminhoArquivo) throws IOException {
        List<Alerta> logs = leitura.getAlertas(caminhoArquivo);
        Set<String> invalidas = new HashSet<>(1024);
        //map: userId -> pilha de sessionId
        Map<String, ArrayDeque<String>> pilhas = new HashMap<>(2048);

        for (int i = 0; i < logs.size(); i++) {
            Alerta a = logs.get(i);
            String userId = a.getUserId();
            String sessionId = a.getSessionId();
            String action = a.getActionType();

            ArrayDeque<String> pilha = pilhas.get(userId);
            if (pilha == null) {
                pilha = new ArrayDeque<>(8);
                pilhas.put(userId, pilha);
            }

            if ("LOGIN".equals(action)) {
                //login aninha é invalido
                if (!pilha.isEmpty()) invalidas.add(sessionId);
                pilha.push(sessionId);
            } else if ("LOGOUT".equals(action)) {
                //logout sem login correspondente é invalido
                if (pilha.isEmpty() || !sessionId.equals(pilha.peek())) {
                    invalidas.add(sessionId);
                } else {
                    pilha.pop();
                }
            }
        }
        //sessões que ficaram abertas são invalidas
        for (ArrayDeque<String> pilha : pilhas.values()) {
            invalidas.addAll(pilha);
        }

        return invalidas;
    }

    // ----------------------------------------------------------------------
    // Desafio 2: Reconstruir Linha do Tempo
    // ----------------------------------------------------------------------

    @Override
    public List<String> reconstruirLinhaTempo(String caminhoArquivo, String sessionId) throws IOException {
        List<Alerta> logs = leitura.getAlertas(caminhoArquivo);

        Queue<String> fila = new LinkedList<>();

        for (Alerta alerta:logs) {
            if (Objects.equals(alerta.getSessionId(),sessionId)) {
                fila.offer(alerta.getActionType());
            }
        }
        List<String>timeline=new ArrayList<>(fila.size());
        while (!fila.isEmpty()){
            timeline.add(fila.poll());
        }
        return timeline;
    }

    // ----------------------------------------------------------------------
    // Desafio 3: Priorizar Alertas
    // ----------------------------------------------------------------------

    @Override
    public List<Alerta> priorizarAlertas(String caminhoArquivo, int n) throws IOException {
        if (n <= 0) return Collections.emptyList();

        List<Alerta> logs = leitura.getAlertas(caminhoArquivo);
        if (logs.isEmpty()) return Collections.emptyList();

        PriorityQueue<Alerta> pq = new PriorityQueue<>(logs.size(), Comparator.comparingInt(Alerta::getSeverityLevel).reversed());
        pq.addAll(logs);

        int size = Math.min(n, pq.size());
        List<Alerta> result = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            result.add(pq.poll());
        }

        return result;
    }

    // ----------------------------------------------------------------------
    // Desafio 4: Encontrar Picos de Transferência
    // ----------------------------------------------------------------------

    @Override
    public Map<Long, Long> encontrarPicosTransferencia(String caminhoArquivo) throws IOException {
        List<Alerta> eventos = leitura.getAlertas(caminhoArquivo);

        // Filtrar inline para evitar lista intermediária
        Map<Long, Long> resultado = new HashMap<>(eventos.size() / 2);
        // pilha mantem eventos em ordem decrescente de bytes
        ArrayDeque<Alerta> pilha = new ArrayDeque<>(1024);
        //itera em ordem reversa
        for (int i = eventos.size() - 1; i >= 0; i--) {
            Alerta atual = eventos.get(i);
            long bytes = atual.getBytesTransferred();

            if (bytes <= 0) continue; // Filtro inline
            //remove elementos menores/iguais
            while (!pilha.isEmpty() && pilha.peek().getBytesTransferred() <= bytes) {
                pilha.pop();
            }
            //se a pilha esta vazia, o topo é o proximo maior
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
        LeituraCSV leitor = new LeituraCSV();
        return leitor.getAlertas(caminhoArquivo);
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