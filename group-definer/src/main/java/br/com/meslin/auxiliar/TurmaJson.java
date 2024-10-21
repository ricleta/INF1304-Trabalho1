package main.java.br.com.meslin.auxiliar;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import main.java.br.com.meslin.auxiliar.models.Turma;

public class TurmaJson {

    private Turma[] turma_list = null;

    private Turma[] loadTurmasFromFile(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        Turma[] turmas = null;

        try {
            FileInputStream inputStream = new FileInputStream(filePath);
            String text = IOUtils.toString(inputStream);
            // System.out.println(text);
            
            // Read the JSON array directly into a Turma array
            turmas = objectMapper.readValue(text, Turma[].class);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return turmas;
    }

    public TurmaJson() {
        // Path to the JSON file
        String jsonFilePath = "/turmas.json";

        // Load the turmas from the JSON file
        this.turma_list = loadTurmasFromFile(jsonFilePath);
    }

    public Turma getTurma(String disciplina_turma) {
        String[] parts = disciplina_turma.split(" ");
        String id_disciplina = parts[0];
        String id_turma = parts[1];

        for (Turma turma : this.turma_list) {
            if (turma.id_turma.equals(id_turma) && turma.disciplina.equals(id_disciplina)) {
                return turma;
            }
        }

        return null;
    }

    public int getGroupIDFromTurma(String disciplina_turma) {
        String[] parts = disciplina_turma.split(" ");
        String id_disciplina = parts[0];
        String id_turma = parts[1];

        System.out.println("id_disciplina: " + id_disciplina);
        System.out.println("id_turma: " + id_turma);

        if (id_disciplina.equals("inf1304") && id_turma.equals("3WA")) {
            return 6000;
        }

        return -1;
    }
}
