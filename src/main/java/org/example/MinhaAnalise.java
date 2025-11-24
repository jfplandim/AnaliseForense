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
//
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
        List<Alerta> logs = leitura.getAlertas(caminhoArquivo);

        if (recursoInicial.equals(recursoAlvo)) {
            for (int i = 0; i < logs.size(); i++) {
                if (recursoInicial.equals(logs.get(i).getTargetResource())) {
                    return Optional.of(Collections.singletonList(recursoInicial));
                }
            }
            return Optional.empty();
        }

        Map<String, List<String>> grafo = construirGrafo(logs);
        if (!grafo.containsKey(recursoInicial)) return Optional.empty();

        return executarBFS(grafo, recursoInicial, recursoAlvo);
    }

    private Map<String, List<String>> construirGrafo(List<Alerta> logs) {
        Map<String, List<Alerta>> sessions = new HashMap<>(1024);

        for (int i = 0; i < logs.size(); i++) {
            Alerta a = logs.get(i);
            sessions.computeIfAbsent(a.getSessionId(), k -> new ArrayList<>(16)).add(a);
        }

        Map<String, List<String>> grafo = new HashMap<>(sessions.size() * 2);

        for (List<Alerta> sessao : sessions.values()) {
            if (sessao.size() > 1) {
                sessao.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
            }

            for (int i = 0; i < sessao.size() - 1; i++) {
                String a = sessao.get(i).getTargetResource();
                String b = sessao.get(i + 1).getTargetResource();
                grafo.computeIfAbsent(a, k -> new ArrayList<>(4)).add(b);
            }

            for (int i = 0; i < sessao.size(); i++) {
                grafo.putIfAbsent(sessao.get(i).getTargetResource(), new ArrayList<>(0));
            }
        }

        return grafo;
    }

    private Optional<List<String>> executarBFS(Map<String, List<String>> grafo, String inicio, String alvo) {
        ArrayDeque<String> fila = new ArrayDeque<>(256);
        fila.add(inicio);

        Map<String, String> pred = new HashMap<>(grafo.size());
        pred.put(inicio, null);

        Set<String> visitados = new HashSet<>(grafo.size());
        visitados.add(inicio);

        while (!fila.isEmpty()) {
            String atual = fila.poll();

            if (atual.equals(alvo)) {
                List<String> caminho = new ArrayList<>(32);
                while (atual != null) {
                    caminho.add(atual);
                    atual = pred.get(atual);
                }
                Collections.reverse(caminho);
                return Optional.of(caminho);
            }

            List<String> vizinhos = grafo.get(atual);
            if (vizinhos != null) {
                for (int i = 0; i < vizinhos.size(); i++) {
                    String viz = vizinhos.get(i);
                    if (!visitados.contains(viz)) {
                        visitados.add(viz);
                        pred.put(viz, atual);
                        fila.add(viz);
                    }
                }
            }
        }

        return Optional.empty();
    }
}