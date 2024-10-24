package main.java.br.com.meslin.auxiliar.models;

/**
 * Class to store the information of a class
 * including the class id, the course id, the lecturing professor, the duration, 
 * the group and the pairs of rooms and times of the classes
 */
public class Turma {
    public String id_turma;

    public String disciplina;

    public String professor;

    public int duracao;

    public int group;
    
    public SalaHorario[] salas_horarios;

    public Turma() {
    }
}