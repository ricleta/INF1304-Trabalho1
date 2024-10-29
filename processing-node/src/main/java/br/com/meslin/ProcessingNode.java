package main.java.br.com.meslin;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays; 
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.meslin.Util;
import br.com.meslin.models.Turma;
import br.com.meslin.models.SalaHorario;
import br.com.meslin.models.User;
import br.com.meslin.UserJson;
import br.com.meslin.TurmaJson;
import ckafka.data.Swap;
import ckafka.data.SwapData;
import main.java.application.ModelApplication;

public class ProcessingNode extends ModelApplication{
    private Swap swap;
    private ObjectMapper objectMapper;
    //private static final Logger logger = LoggerFactory.getLogger(ProcessingNode.class);
    private static final ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
    private static final String GROUPS_LOG_FILE_PATH = "/groups_log.csv";
    private static final String ATTENDANCE_LOG_FILE_PATH = "/attendance_log.csv";
    private static final String PRESENCE_TABLE_PATH = "/presence_table.csv";


    // Valid user input options
    private static final String OPTION_GROUPCAST = "G";
    private static final String OPTION_UNICAST = "I";
    private static final String OPTION_PN = "P";
    private static final String OPTION_EXIT = "Z";

    // Valid commands
    private static final String COMMAND_REGISTER_PRESENCE = "Register";

    // The variable cannot be local because it is being used in a lambda function
    // Control of the eternal loop until it ends
    private UserJson user_dto = new UserJson();
    private TurmaJson turma_dto = new TurmaJson();
    private boolean fim = false;
    Map<String, Consumer<String[]>> commandMap = new HashMap<>();

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

        // Map commands to corresponding functions
        this.commandMap.put(COMMAND_REGISTER_PRESENCE, params -> registerPresence(params));
        // Timer timer = new Timer();
        // timer.scheduleAtFixedRate(check_for_classes(), 0, 60000);

        while(!fim) {
            this.start_scheduling();
            this.logAttendance(this.countTimeInClass());
            
            try{
                Thread.sleep(60000);
            }
            catch(InterruptedException e){
                System.out.println("Error: " + e);
            }
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
                new TimerTask() {
                    @Override
                    public void run() 
                    {
                        // get all turmas
                        Turma[] turmas = turma_dto.getTurmas();
                        for (Turma turma : turmas) 
                        {
                            check_for_classes(turma);
                        }
                    }
                }, now, 60000);
        
            } catch (Exception e) {
            logger.error("Error scheduling SendLocationTask", e);
        }
        return;
    }
    
    public void check_for_classes(Turma turma) {
        LocalDate currentDate = LocalDate.now(zoneId);
        LocalTime currentTime = LocalTime.now(zoneId).withSecond(0).withNano(0);
    
        int dayOfWeek = currentDate.getDayOfWeek().getValue();
    
        for (SalaHorario salaHorario : turma.salas_horarios) {
            try {
                int scheduledDay = salaHorario.getDayOfWeek();
                LocalTime scheduledStartTime = LocalTime.parse(salaHorario.getTimeString(), DateTimeFormatter.ofPattern("HH:mm")).minusMinutes(10);
                
                // since we already subtracted 10 minutes from scheduledStartTime, 
                // we only need to add the hours to get 10 min before the end time
                LocalTime scheduledEndTime = scheduledStartTime.plusHours((turma.duracao));
                
                if (scheduledDay == dayOfWeek) 
                {
                    if (scheduledStartTime.equals(currentTime))
                    {
                        String message = String.format("Class for %s starts soon in room %s", turma.id_turma, salaHorario.sala);
                        System.out.println(message);
                        
                        // Send message to the group
                        try {
                            sendRecord(createRecord("GroupMessageTopic", String.valueOf(turma.group), swap.SwapDataSerialization(createSwapData(message))));
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("Error SendGroupCastMessage", e);
                        }
                    }

                    if (scheduledEndTime.equals(currentTime))
                    {
                        String message = String.format("Class for %s ends soon in room %s", turma.id_turma, salaHorario.sala);
                        System.out.println(message);
                        
                        // Send message to the group
                        try 
                        {
                            sendRecord(createRecord("GroupMessageTopic", String.valueOf(turma.group), swap.SwapDataSerialization(createSwapData(message))));
                        } catch (Exception e) 
                        {
                            e.printStackTrace();
                            logger.error("Error SendGroupCastMessage", e);
                        }
                    }
                }

            } catch (NumberFormatException | DateTimeParseException e) {
                System.out.println("Error parsing schedule: " + salaHorario + " - " + e.getMessage());
            }
        }
    }

    public void registerPresence(String[] params) {
        Turma turmaObj = this.turma_dto.getTurma(String.format("%s %s", params[0], params[1]));
        String classDate = params[2];
        Float threshold = Float.parseFloat(params[3]);
        Map<String, Map<String, Map<String,Integer>>> userGroupCount = this.countTimeInClass();
        try (PrintWriter writer = new PrintWriter(new FileWriter(PRESENCE_TABLE_PATH, true))) 
        {
            for (String matricula : userGroupCount.keySet()) 
            {
                Map<String, Map<String, Integer>> dateMap = userGroupCount.get(matricula);
                Map<String, Integer> groupCounts = dateMap.get(classDate);
                
                for (Map.Entry<String, Integer> entry : groupCounts.entrySet()) 
                {
                    String groupId = entry.getKey();
                    int count = entry.getValue();
                    try
                    {
                        Turma turma = turma_dto.getTurma(Integer.parseInt(groupId));
                        if (turma.group == turmaObj.group) {    
                            int duration = turma.duracao * 60; // Assume duration is in minutes

                            // Check if count is at least 80% of the duration
                            if (count >= threshold * duration) {
                                writer.println(classDate + "," + turma.disciplina + " " + turma.id_turma + "," + matricula + ",PRESENTE");
                            } else {
                                writer.println(classDate + "," + turma.disciplina + " " + turma.id_turma + "," + matricula + ",FALTA");
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error getting turma by attending group: " + e.getMessage());
                    }
                    
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing to presence_table: " + e.getMessage());
        }
    }

    private void executeCommand(String fullCommand) {
        String[] args = fullCommand.split(" ");
        String command = args[0];
        String[] params = Arrays.copyOfRange(args, 1, args.length);
        if (this.commandMap.containsKey(command)) {
            this.commandMap.get(command).accept(params);
        } else {
            System.out.println("Invalid command");
        }
    }

    /**
     * 
     */
    @Override
    public void recordReceived(ConsumerRecord record) {
        System.out.println(String.format("Command received from %s", record.key()));        
        try {
            SwapData data = swap.SwapDataDeserialization((byte[]) record.value());
            String text = new String(data.getMessage(), StandardCharsets.UTF_8);
            User[] user_list = this.user_dto.getUserList();
            System.out.println("Command received = " + text);
            executeCommand(text);
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

    // Method to read the log file and count group occurrences
    public Map<String, Map<String, Map<String, Integer>>> countTimeInClass() {
        Map<String, Map<String, Map<String, Integer>>> userGroupCount = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(GROUPS_LOG_FILE_PATH))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String date = parts[0];
                String hora = parts[1];
                String matricula = parts[2];
                String groupsString = parts[3];

                // Split the group string into individual group IDs
                String[] groups = groupsString.equals("None") ? new String[0] : groupsString.split(", ");

                // Initialize nested maps
                userGroupCount.putIfAbsent(matricula, new HashMap<>());
                userGroupCount.get(matricula).putIfAbsent(date, new HashMap<>());

                for (String group : groups) {
                    try {
                        Turma turma = turma_dto.getTurma(Integer.parseInt(group));
                        String attendingGroup = String.valueOf(turma.group_attending);
                        String turmaGroup = String.valueOf(turma.group);

                        userGroupCount.get(matricula).get(date).putIfAbsent(turmaGroup, 0);

                        // Check if the current group corresponds to the attending group
                        if (group.equals(attendingGroup)) {
                            // Update the userGroupCount using turma.group as the key
                            userGroupCount.get(matricula).get(date).put(turmaGroup, 
                                userGroupCount.get(matricula).get(date).get(turmaGroup) + 1);
                        }   
                    } catch (Exception e) {
                        System.err.println("Error getting turma by group: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading log file: " + e.getMessage());
        }

        return userGroupCount;
    }

    // Method to log attendance based on group counts and class duration
    public void logAttendance(Map<String, Map<String, Map<String, Integer>>> userGroupCount) 
    {
        // String currentDate = LocalDate.now(zoneId).toString();

        try (PrintWriter writer = new PrintWriter(new FileWriter(ATTENDANCE_LOG_FILE_PATH, true))) 
        {
            for (String matricula : userGroupCount.keySet()) 
            {
                Map<String, Map<String, Integer>> dateMap = userGroupCount.get(matricula);
                
                for (String date : dateMap.keySet()) 
                {
                    Map<String, Integer> groupCounts = dateMap.get(date);
                    
                    for (Map.Entry<String, Integer> entry : groupCounts.entrySet()) 
                    {
                        String groupId = entry.getKey();
                        int count = entry.getValue();
                        
                        try
                        {
                            Turma turma = turma_dto.getTurma(Integer.parseInt(groupId));
                            int duration = turma.duracao * 60; // Assume duration is in minutes

                            // Check if count is at least 80% of the duration
                            if (count >= 0.8 * duration) {
                                writer.println(date + "," + turma.disciplina + " " + turma.id_turma + "," + matricula + ",Present," + count);
                            } else {
                                writer.println(date + "," + turma.disciplina + " " + turma.id_turma + "," + matricula + ",Absent," + count);
                            }
                        } catch (Exception e) {
                            System.err.println("Error getting turma by attending group: " + e.getMessage());
                        }
                        
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing to attendance log: " + e.getMessage());
        }
    }
}
