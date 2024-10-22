package main.java.br.com.meslin.auxiliar;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import org.apache.commons.io.*;
import main.java.br.com.meslin.auxiliar.models.User;

public class UserJson {

    private User[] user_list = null;

    private User[] loadUsersFromFile(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        User[] users = null;

        try {
            FileInputStream inputStream = new FileInputStream(filePath);
            String text = IOUtils.toString(inputStream);
            // System.out.println(text);
            
            // Read the JSON array directly into a User array
            users = objectMapper.readValue(text, User[].class);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    public UserJson() {
        // Path to the JSON file
        String jsonFilePath = "/users.json";

        // Load the users from the JSON file
        this.user_list = loadUsersFromFile(jsonFilePath);
    }

    public User getUser(Integer matricula) {
        for (User user : this.user_list) {
            if (user.matricula == matricula) {
                return user;
            }
        }

        return null;
    }

    public User[] getUserList() {
        return this.user_list;
    }
}