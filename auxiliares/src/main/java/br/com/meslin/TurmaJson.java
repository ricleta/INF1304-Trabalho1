package br.com.meslin;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import java.util.Set;
import java.util.HashSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import br.com.meslin.models.SalaHorario;
import br.com.meslin.models.Turma;

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
            String text = IOUtils.toString(inputStream, "UTF-8");
            
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

        for (Turma turma : this.turma_list) {
            for (SalaHorario salaHorario : turma.salas_horarios) {
                salaHorario.set_dados_horario(turma.duracao);
            }
        }
    }

    /** 
     * Get the class from the class name
     * @param disciplina_turma class name
     * @return the class with the given name
     * @return null if the class is not found
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

    public Turma getTurma(int groupID) {
        for (Turma turma : this.turma_list) {
            if (turma.group == groupID || turma.group_attending == groupID || turma.group_absent == groupID) {
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

        /* 
         * 3000 -> INF1304 - 3WA
         * 3100 -> INF1748 - 3WA
         * 3101 -> INF1748 - 3WB 
        */
        for (Turma turma : this.turma_list) {
            if (turma.id_turma.equals(id_turma) && turma.disciplina.equals(id_disciplina)) {
                return turma.group;
            }
        }

        return -1;
    }

    public Set<Integer> getGroupsFromStudentAttendance(String nome_turma, int dayOfWeek, String hour, String location)
    {
        Set<Integer> groups = new HashSet<Integer>();
        Turma turma = getTurma(nome_turma);
        LocalTime currentTime = LocalTime.parse(hour, DateTimeFormatter.ofPattern("HH:mm")).withSecond(0).withNano(0);

        for (SalaHorario salaHorario : turma.salas_horarios) 
        {
            //println("Checking " + salaHorario.sala + " " + salaHorario.horario);
            if (salaHorario.getDayOfWeek() == dayOfWeek) 
            {
                //System.out.println("Day of week matches | Is class time = " + salaHorario.isClassTime(currentTime));
                if (salaHorario.isClassTime(currentTime)) 
                {
                    //System.out.println(turma.disciplina + "is in class");
                    if (salaHorario.sala.equals(location)) 
                    {
                        //System.out.println("Student is in class");
                        groups.add(turma.group_attending);
                    } else {
                        groups.add(turma.group_absent);
                    }
                }
            }
        }
        return groups;
    }

    public Turma[] getTurmas() {
        return this.turma_list;
    }
}
