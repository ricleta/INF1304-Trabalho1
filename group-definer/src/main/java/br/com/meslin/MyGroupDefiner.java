package br.com.meslin;

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

import main.java.br.com.meslin.auxiliar.UserJson;
import main.java.br.com.meslin.auxiliar.models.User;
import ckafka.data.Swap;
import main.java.ckafka.GroupDefiner;
import main.java.ckafka.GroupSelection;

public class MyGroupDefiner implements GroupSelection {
    /** Logger */
    final Logger logger = LoggerFactory.getLogger(GroupDefiner.class);
    private UserJson user_dto = new UserJson();

    public static void main(String[] args) {
        MyGroupDefiner MyGD = new MyGroupDefiner();
    }

    public MyGroupDefiner() {

        ObjectMapper objectMapper = new ObjectMapper();
        Swap swap = new Swap(objectMapper);
        new GroupDefiner(this, swap);
    }

    /**
     * Conjunto com todos os grupos que esse GroupDefiner controla.
     */
    public Set<Integer> groupsIdentification() {
        /**
         * 6000 -> Professores
         * 6001 -> Alunos
         * 6100 -> INF1304 - 3WA
         * 6200 -> T01
         * 6201 -> LABGRAD
         * 6202 -> Na Medida
         * 6300 -> INF1304 - 3WA - PRESENTE
         * 6400 -> INF1304 - 3WA - FALTA
         */
        Set<Integer> setOfGroups = new HashSet<Integer>();
        setOfGroups.add(6000);
        setOfGroups.add(6001);
        setOfGroups.add(6100);
        setOfGroups.add(6200);
        setOfGroups.add(6201);
        setOfGroups.add(6202);
        setOfGroups.add(6300);
        setOfGroups.add(6400);
        return setOfGroups;
    }

    /**
     * Conjunto com todos os grupos relativos a esse contextInfo.
     * Somente grupos controlados por esse GroupDefiner.
     * 
     * @param contextInfo context info
     */
    public Set<Integer> getNodesGroupByContext(ObjectNode contextInfo) {
        Set<Integer> setOfGroups = new HashSet<Integer>();

        System.out.println("recebendo contexto");
        String matricula = String.valueOf(contextInfo.get("matricula"));
        String local = String.valueOf(contextInfo.get("local"));
        System.out.println(matricula);
        System.out.println(local);
        User user = this.user_dto.getUser(Integer.parseInt(matricula));
        System.out.println(user.nome);
        return null;
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
