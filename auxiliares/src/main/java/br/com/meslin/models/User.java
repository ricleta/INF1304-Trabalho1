package br.com.meslin.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class to store the information of a user
 * including the registration number, the name and the classes which the user is enrolled in
 */
public class User {

    public int matricula;

    public String nome;

    public String[] turmas;

    public User() {

    }
}