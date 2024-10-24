package br.com.meslin;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.fasterxml.jackson.databind.ObjectMapper;

import ckafka.data.Swap;
import ckafka.data.SwapData;
import main.java.application.ModelApplication;
import main.java.br.com.meslin.auxiliar;

public class ProcessingNode extends ModelApplication {
    private Swap swap;
    private ObjectMapper objectMapper;

    // Valid user input options
    private static final String OPTION_GROUPCAST = "G";
    private static final String OPTION_UNICAST = "I";
    private static final String OPTION_PN = "P";
    private static final String OPTION_EXIT = "Z";

    // The variable cannot be local because it is being used in a lambda function
    // Control of the eternal loop until it ends
    private UserJson user_dto = new UserJson();
    private boolean fim = false;

    /**
     * Constructor
     */
    public ProcessingNode() {
        this.objectMapper = new ObjectMapper();
        this.swap = new Swap(objectMapper);
    }

    /**
     * Main
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);
        ProcessingNode pn = new ProcessingNode();
        pn.runPN(keyboard);
    }

    /**
     * TODO
     */
    public void runPN(Scanner keyboard) {
        Map<String, Consumer<Scanner>> optionsMap = new HashMap<>();

        // Map options to corresponding functions
        optionsMap.put(OPTION_GROUPCAST, this::sendGroupcastMessage);
        optionsMap.put(OPTION_UNICAST, this::sendUnicastMessage);
//        optionsMap.put(OPTION_PN, this::sendMessageToPN);
        optionsMap.put(OPTION_EXIT, scanner -> fim = true);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(check_for_classes(), 0, 60000);

        while(!fim) {
            System.out.print("Message to (G)roup or (I)ndividual (P)rocessing Node (Z to end)? ");
            String linha = keyboard.nextLine().trim().toUpperCase();
            System.out.printf("Your option was %s.", linha);
            if(optionsMap.containsKey(linha)) optionsMap.get(linha).accept(keyboard);
            else System.out.printf("Invalid option %s.\n", linha);
        }
        keyboard.close();
        System.out.println("END!");
        System.exit(0);
    }

    public void start_scheduling(){
        try {
            Date now = new Date();
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(
                    check_for_classes, now, 60000);
        } catch (Exception e) {
            logger.error("Error scheduling SendLocationTask", e);
        }
        return;
    }
    
    public void check_for_classes(Turma turma){
        LocalDate current_date = new LocalDate();
        
        // 1 == Monday ... 7 == Sunday
        int day = current_date.getDayOfWeek().getValue();
        int hour = current_date.getHours();

        for (SalaHorario sala_horario : turma.salas_horarios) {
            if (sala_horario.horario == hour) {
                System.out.println("Class time");
                
                // send message to the group
                sendRecord(createRecord("GroupMessageTopic", turma.group, swap.SwapDataSerialization(createSwapData("Class time"))));
            }
        }
    }

    public void get_presence(String turma) {   
        User[] user_list = this.user_dto.getUserList();

        // for (User user: user_list) {
        //     if ()
        // }
    }        // public void get_presetn()

    /**
     * 
     */
    @Override
    public void recordReceived(ConsumerRecord record) {
        System.out.println(String.format("Message received from %s", record.key()));        
        try {
            SwapData data = swap.SwapDataDeserialization((byte[]) record.value());
            String text = new String(data.getMessage(), StandardCharsets.UTF_8);
            User[] user_list = this.user_dto.get_user_list();
            System.out.println("Message received = " + text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param keyboard
     */
    private void sendUnicastMessage(Scanner keyboard) {
        System.out.println("UUID:\nHHHHHHHH-HHHH-HHHH-HHHH-HHHHHHHHHHHH");
        String uuid = keyboard.nextLine();
        System.out.print("Message: ");
        String messageText = keyboard.nextLine();
        System.out.println(String.format("Sending |%s| to %s.", messageText, uuid));
    
        try {
            sendRecord(createRecord("PrivateMessageTopic", uuid, swap.SwapDataSerialization(createSwapData(messageText))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send groupcast message
     * @param keyboard
     */
    private void sendGroupcastMessage(Scanner keyboard) {
        /**create date
         * if it's time for a class or ending a class
         * send message to the specific group
        */
        Date current_time = new Date();
    
        System.out.print("Groupcast message. Enter the group number: ");
        String group = keyboard.nextLine();

        System.out.print("Enter the message: ");
        String messageText = keyboard.nextLine();

        System.out.println(String.format("Sending message %s to group %s.",
                                         messageText, group));
        try {
            sendRecord(createRecord("GroupMessageTopic", group,
                              swap.SwapDataSerialization(createSwapData(messageText))));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error SendGroupCastMessage", e);
        }
    }
}
