package org.example;

import br.edu.icev.aed.forense.Alerta;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LeituraCSV {
    private List<Alerta> alertas;
    private boolean carregado = false;

    public List<Alerta> getAlertas(String caminhoArquivo) throws IOException {
        if (carregado) return alertas;

        alertas = new ArrayList<>(500_000);

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo), 32768)) {
            String linha = br.readLine(); // pula header

            while ((linha = br.readLine()) != null) {
                int p1 = linha.indexOf(',');
                int p2 = linha.indexOf(',', p1 + 1);
                int p3 = linha.indexOf(',', p2 + 1);
                int p4 = linha.indexOf(',', p3 + 1);
                int p5 = linha.indexOf(',', p4 + 1);
                int p6 = linha.indexOf(',', p5 + 1);

                // Ordem correta conforme header CSV:
                // ALERT_ID, USER_ID, IP_ADDRESS, SESSION_ID, TARGET_RESOURCE, SEVERITY, TIMESTAMP
                long alertId = Long.parseLong(linha.substring(0, p1));
                String userId = linha.substring(p1 + 1, p2);
                String ipAddress = linha.substring(p2 + 1, p3);
                String sessionId = linha.substring(p3 + 1, p4);
                String targetResource = linha.substring(p4 + 1, p5);
                int severity = Integer.parseInt(linha.substring(p5 + 1, p6));
                long timestamp = Long.parseLong(linha.substring(p6 + 1));

                alertas.add(new Alerta(
                        alertId, userId, ipAddress, sessionId,
                        targetResource, severity, timestamp
                ));
            }
        }

        carregado = true;
        return alertas;
    }
}
