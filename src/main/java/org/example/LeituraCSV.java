package org.example;

import br.edu.icev.aed.forense.Alerta;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LeituraCSV {
    private String ultimoCaminho = null;
    private List<Alerta> cache = null;

    public List<Alerta> getAlertas(String caminhoArquivo) throws IOException {
        // Cache simples e rápido
        if (cache != null && caminhoArquivo != null && caminhoArquivo.equals(ultimoCaminho)) {
            return cache;
        }

        List<Alerta> alertas = new ArrayList<>(500_000);

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo), 65536)) {
            br.readLine(); // Pula header
            String linha;

            while ((linha = br.readLine()) != null) {
                int len = linha.length();
                if (len < 10) continue; // Linha muito curta

                int p1 = linha.indexOf(',');
                int p2 = linha.indexOf(',', p1 + 1);
                int p3 = linha.indexOf(',', p2 + 1);
                int p4 = linha.indexOf(',', p3 + 1);
                int p5 = linha.indexOf(',', p4 + 1);
                int p6 = linha.indexOf(',', p5 + 1);

                if (p6 == -1) continue; //linha incompleta

                try {
                    alertas.add(new Alerta(
                            Long.parseLong(linha, 0, p1, 10),
                            linha.substring(p1 + 1, p2),
                            linha.substring(p2 + 1, p3),
                            linha.substring(p3 + 1, p4),
                            linha.substring(p4 + 1, p5),
                            Integer.parseInt(linha, p5 + 1, p6, 10),
                            Long.parseLong(linha, p6 + 1, len, 10)
                    ));
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    // Ignora linha com erro
                }
            }
        }

        ultimoCaminho = caminhoArquivo;
        cache = alertas;
        return alertas;
    }
}