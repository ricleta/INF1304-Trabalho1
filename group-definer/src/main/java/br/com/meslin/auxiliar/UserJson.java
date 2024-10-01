package main.java.br.com.meslin.auxiliar;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.io.*;
import main.java.br.com.meslin.auxiliar.models.User;

public class UserJson {

    private User[] user_list = new User[1];
    private User test = null;

    private User[] loadUsersFromFile(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        User[] users = null;
        try {
            // Read JSON file and map it to User[] array
            // users = objectMapper.readValue(new File(filePath), User.class);
            // User t = new User(123, "ARNALDO", new String[] { "inf1304", "inf1748" });
            // System.out.println(objectMapper.writeValueAsString(t));
            File f = new File(filePath);
            FileInputStream inputStream = new FileInputStream(filePath);
            String text = IOUtils.toString(inputStream);
            System.out.println(text);
            this.test = objectMapper.readValue(text, User.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    public UserJson() {
        // Path to the JSON file
        String jsonFilePath = "/Users/franciscofleury/Documents/inf1304/PresenceNet/group-definer/src/main/java/br/com/meslin/auxiliar/users.json";

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
}