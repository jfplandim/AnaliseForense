import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnaliseFAvançada {
    public List<String>ReconstruirLinhaTempo(String caminhoArq,String sessaoId) throws IOException{

        List<String> linhaTempo = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(new FileReader(caminhoArq))){
            String linha=br.readLine();
            while((linha=br.readLine())!=null){
                int idx1=linha.indexOf(",");
                int idx2=linha.indexOf(",",idx1+1);
                int idx3=linha.indexOf(",",idx2+1);
                String sessao=linha.substring(idx2+1,idx3);

                if (sessao.equals(sessaoId)){
                    String action=linha.substring(idx3+1);
                    linhaTempo.add(action);
                }
            }
        }
        return linhaTempo;
    }
}