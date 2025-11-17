import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class AnaliseFAvançada {
   public List<String> ReconstruirLinhadoTempo(String caminhoArquivosCsv, String sessionID) throws IOException{
   Queue<String> filaAcoes = new ArrayDeque<>();
       try(BufferedReader br = new BufferedReader(new FileReader(caminhoArquivosCsv))){
           String linha;
           br.readLine();
           while ((linha=br.readLine())!=null){
               String[] campos = linha.split(",");
               if (campos[2].equals(sessionID)){
                   filaAcoes.add(campos[3]);
               }
           }
       }
       return new ArrayList<>(filaAcoes);
   }
}
