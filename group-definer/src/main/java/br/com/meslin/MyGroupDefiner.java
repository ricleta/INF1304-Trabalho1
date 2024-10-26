package main.java.br.com.meslin;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StringArrayDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import br.com.meslin.TurmaJson;
import br.com.meslin.UserJson;
import br.com.meslin.models.User;
import br.com.meslin.models.Turma;
import br.com.meslin.models.SalaHorario;
import ckafka.data.Swap;
import main.java.ckafka.GroupDefiner;
import main.java.ckafka.GroupSelection;

public class MyGroupDefiner implements GroupSelection {
    /** Logger */
    final Logger logger = LoggerFactory.getLogger(GroupDefiner.class);
    
    /** objects to help read and manipulate user */
    private UserJson user_dto = new UserJson();
    
    /** objects to help read and manipulate turma */
    private TurmaJson turma_dto = new TurmaJson();

    public static void main(String[] args) {
        MyGroupDefiner MyGD = new MyGroupDefiner();
    }

    /**
     * Constructor
     */
    public MyGroupDefiner() {
        ObjectMapper objectMapper = new ObjectMapper();
        Swap swap = new Swap(objectMapper);
        new GroupDefiner(this, swap);
    }

    /**
     * Set with all the groups that this GroupDefiner controls.
     * @return set with all the groups that this GroupDefiner manages
     */
    public Set<Integer> groupsIdentification() {
        /**
         * 6000 -> Professors
         * 6001 -> Students
         * 3000 -> INF1304 - 3WA
         * 3100 -> INF1748 - 3WA
         * 3101 -> INF1748 - 3WB
         * 16500 -> T01
         * 16501 -> LABGRAD
         * 16502 -> L420
         * 16503 -> L522
         * 16001 -> INF1304 - 3WA - PRESENT
         * 16002 -> INF1304 - 3WA - ABSENT
         * 16003 -> INF1748 - 3WA - PRESENT
         * 16004 -> INF1748 - 3WA - ABSENT
         * 16005 -> INF1748 - 3WB - PRESENT
         * 16006 -> INF1748 - 3WB - ABSENT
         */
        Set<Integer> setOfGroups = new HashSet<Integer>();
     
        setOfGroups.add(6000);
        setOfGroups.add(6001);
        setOfGroups.add(3000);
        setOfGroups.add(3100);
        setOfGroups.add(3101);
        setOfGroups.add(16500);
        setOfGroups.add(16501);
        setOfGroups.add(16502);
        setOfGroups.add(16503);
        setOfGroups.add(16001);
        setOfGroups.add(16002);
        setOfGroups.add(16003);
        setOfGroups.add(16004);
        setOfGroups.add(16005);
        setOfGroups.add(16006);

        return setOfGroups;
    }

    /**
     * Function to get user group ID from location
     * @param location
     * @return group ID
     */
    private int getGroupIDFromLocation(String location)
    {
        System.out.println("Location: " + location);
        location = location.replace("\"", "");
        switch (location) {
            case "T01":
                return 16500;
            case "LABGRAD":
                return 16501;
            case "L420":
                return 16502;
            case "L522":
                return 16503;
            default:
                return -1;
        }
    }

    /**
     * Set with all the groups related to this contextInfo.
     * Only groups controlled by this GroupDefiner.
     * 
     * @param contextInfo context info
     * @return set with all the groups related to this contextInfo
     */
    public Set<Integer> getNodesGroupByContext(ObjectNode contextInfo) {
        Set<Integer> setOfGroups = new HashSet<Integer>();
        System.out.println("#--------------# Receiving context #--------------#");
        
        String matricula = String.valueOf(contextInfo.get("matricula"));
        String local = String.valueOf(contextInfo.get("local"));
        String data = String.valueOf(contextInfo.get("date"));

        System.out.println("Matricula: " + matricula);
        System.out.println("Local: " + local);
        System.out.println("Data: " + data);

        User user = this.user_dto.getUser(Integer.parseInt(matricula));
        System.out.println("Nome: " + user.nome);

        for (String turma : user.turmas) {
            // Turma turma_obj = this.turma_dto.getTurma(turma);
            // System.out.println(turma_obj.disciplina);
            // System.out.println(turma_obj.id_turma);
            // System.out.println(turma_obj.professor);
            // System.out.println("Group ID = " + turma_obj.group);

            // for (SalaHorario sala_horario : turma_obj.salas_horarios) {
            //     System.out.println(sala_horario.sala);
            //     System.out.println(sala_horario.horario);
            // }

            try {
                int groupId = this.turma_dto.getGroupIDFromTurma(turma);
                
                if (groupId != -1) {
                    setOfGroups.add(groupId);
                } else {
                    logger.error("Invalid group ID for turma: " + turma);
                }
            } catch (Exception e) {
                logger.error("Exception occurred while getting group ID for turma: " + turma, e);
            }
        }
        
        try {
            int groupId = this.getGroupIDFromLocation(local);
            
            if (groupId != -1) {
                setOfGroups.add(groupId);
            } else {
                logger.error("Invalid group ID for local: " + local);
            }
        } catch (Exception e) {
            logger.error("Exception occurred while getting group ID for turma: " + local, e);
        }

        System.out.println(setOfGroups);
        return setOfGroups;
    }

    /**
     * 
     */
    public String kafkaConsumerPrefix() {
        return "gd.one.consumer";
    }

    /**
     * 
     */
    public String kafkaProducerPrefix() {
        return "gd.one.producer";
    }
}
