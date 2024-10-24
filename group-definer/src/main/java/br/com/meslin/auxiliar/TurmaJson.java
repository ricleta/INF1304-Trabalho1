package main.java.br.com.meslin.auxiliar;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import main.java.br.com.meslin.auxiliar.models.Turma;

/**
 * Class to read the JSON file with the class information
 */
public class TurmaJson {
    /** Variable that stores all classes read from the JSON file */
    private Turma[] turma_list = null;

    /**
     * Load the classes from the JSON file
     * @param filePath path to the JSON file
     * @return array with all classes read from the JSON file
     */
    private Turma[] loadTurmasFromFile(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        Turma[] turmas = null;

        try {
            FileInputStream inputStream = new FileInputStream(filePath);
            String text = IOUtils.toString(inputStream);
            
            // Read the JSON array directly into a Turma array
            turmas = objectMapper.readValue(text, Turma[].class);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return turmas;
    }

    /**
     * Constructor;
     * Loads the classes from the JSON file, the path to the JSON file is hardcoded 
     * and has to match the path in the container
     */
    public TurmaJson() {
        // Path to the JSON file
        String jsonFilePath = "/turmas.json";

        // Load the classes from the JSON file
        this.turma_list = loadTurmasFromFile(jsonFilePath);
    }

    /** 
     * Get the class from the class name
     * @param disciplina_turma class name
     * @return the class with the given name
     * @return null if the class is not found
     * @deprecated use getGroupIDFromTurma instead
    */
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

    /**
     * Get the group ID from the class name
     * @param disciplina_turma
     * @return the group ID
     * @return -1 if the class is not found
     */
    public int getGroupIDFromTurma(String disciplina_turma) {
        String[] parts = disciplina_turma.split(" ");
        String id_disciplina = parts[0];
        String id_turma = parts[1];

        System.out.println("id_disciplina: " + id_disciplina);
        System.out.println("id_turma: " + id_turma);

        /* 
         * 3000 -> INF1304 - 3WA
         * 3100 -> INF1748 - 3WA
         * 3101 -> INF1748 - 3WB 
        */
        if (id_disciplina.equals("inf1304") && id_turma.equals("3WA")) {
            return 3000;
        }
        if (id_disciplina.equals("inf1748") && id_turma.equals("3WA")) {
            return 3100;
        }
        if (id_disciplina.equals("inf1748") && id_turma.equals("3WB")) {
            return 3101;
        }

        return -1;
    }
}
