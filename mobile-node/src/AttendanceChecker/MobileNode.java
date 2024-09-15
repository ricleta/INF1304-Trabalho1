package AttendanceChecker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import ckafka.data.SwapData;
import lac.cnclib.net.NodeConnection;
import lac.cnclib.sddl.message.Message;
import main.java.ckafka.mobile.CKMobileNode;
import main.java.ckafka.mobile.tasks.SendLocationTask;

public class MobileNode extends CKMobileNode {
    // Opções válidas de entrada do usuário
    private static final String OPTION_UNICAST = "I";
    private static final String OPTION_PN = "P";
    private static final String OPTION_EXIT = "Z";

    // a variável não pode ser local porque está sendo usada em uma função lambda
    // Controle do loop eterno até que ele termine
    private boolean fim = false;

    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);
        MobileNode mn = new MobileNode();
        mn.runMN(keyboard);

        // Calls close() to properly close MN method after shut down
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                mn.saveStudentCourseInfoToJson("student_course_info.json", new StudentCourseInfo(LocalDate.now(), UUID.randomUUID().toString(), UUID.randomUUID().toString()));
            } catch (IOException e) {
                e.printStackTrace();
        }
            close();
        }));
    }

    private void runMN(Scanner keyboard) {
        Map<String, Consumer<Scanner>> optionsMap = new HashMap<>();

        // Mapeia as opções para as funções correspondentes
        optionsMap.put(OPTION_UNICAST, this::sendUnicastMessage);
        optionsMap.put(OPTION_PN, this::sendMessageToPN);
        optionsMap.put(OPTION_EXIT, scanner -> fim = true);

        while (!fim) {
            System.out.print("Mensagem para (I)ndivíduo, (P)rocessing Node ou (Z) para terminar? ");
            String linha = keyboard.nextLine().trim().toUpperCase();
            System.out.printf("Sua opção foi %s.", linha);
            if (optionsMap.containsKey(linha)) optionsMap.get(linha).accept(keyboard);
            else System.out.printf("Opção %s inválida.\n", linha);
        }
        keyboard.close();
        System.out.println("FIM!");
        System.exit(0);
    }

    @Override
    public void connected(NodeConnection nodeConnection) {
        try {
            logger.debug("Connected");
            final SendLocationTask sendLocationTask = new SendLocationTask(this);
            this.scheduledFutureLocationTask = this.threadPool.scheduleWithFixedDelay(
                sendLocationTask, 5000, 60000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("Error scheduling SendLocationTask", e);
        }
    }

    @Override
    public void disconnected(NodeConnection nodeConnection) {
        logger.debug("Disconnected");
    }

    @Override
    public void internalException(NodeConnection nodeConnection, Exception e) {
        logger.error("Internal exception", e);
    }

    @Override
    public void unsentMessages(NodeConnection nodeConnection, List<Message> list) {
        logger.warn("Unsent messages: " + list.size());
    }

    private void sendUnicastMessage(Scanner keyboard) {
        System.out.println("Mensagem unicast. Entre com o UUID do indivíduo: ");
        String uuid = keyboard.nextLine();

        // Create StudentCourseInfo with random IDs and save as JSON
        StudentCourseInfo info = new StudentCourseInfo(LocalDate.now(), uuid, UUID.randomUUID().toString());
        try {
            saveStudentCourseInfoToJson("unicast_message.json", info);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToPN(Scanner keyboard) {
        System.out.print("Entre com a mensagem: ");
        String messageText = keyboard.nextLine();

        // Create StudentCourseInfo and save as JSON
        StudentCourseInfo info = new StudentCourseInfo(LocalDate.now(), UUID.randomUUID().toString(), messageText);
        try {
            saveStudentCourseInfoToJson("pn_message.json", info);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void newMessageReceived(NodeConnection nodeConnection, Message message) {
        try {
            SwapData swp = fromMessageToSwapData(message);
            String topic = swp.getTopic();

            // For simplicity, assume messages are JSON files containing the required data
            if (topic.equals("PrivateMessageTopic") || topic.equals("AppModel")) {
                String messageContent = new String(swp.getMessage(), StandardCharsets.UTF_8);
                // Simulate reading JSON from message content
                StudentCourseInfo receivedInfo = readStudentCourseInfoFromJson(messageContent);
                logger.info("Received message: " + receivedInfo);
            }
        } catch (Exception e) {
            logger.error("Error reading new message received");
        }
    }

    private void saveStudentCourseInfoToJson(String filename, StudentCourseInfo info) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print
        objectMapper.writeValue(new File(filename), info);
    }

    public static StudentCourseInfo readStudentCourseInfoFromJson(String filename) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filename), StudentCourseInfo.class);
    }
}