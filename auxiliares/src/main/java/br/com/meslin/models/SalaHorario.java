package br.com.meslin.models;

/**
 * Class to store the room and the time of a class
 */
public class SalaHorario {
    public String sala;
    public String horario;
    
    public SalaHorario() {
    }

    public String getTimeString() {
        String parts[] = this.horario.split(" ");
        String time = parts[1].strip();

        // Ensure the time is in "HH:mm" format
        String[] timeParts = time.split(":");

        if (timeParts.length == 2) 
        {
            String hour = timeParts[0].strip();
            String minute = timeParts[1].strip();

            // Add leading zero to hour if necessary
            if (hour.length() == 1) {
                hour = "0" + hour;
            }

            if (minute.length() == 1) {
                minute = "0" + minute;
            }

                return hour + ":" + minute;
            } 
        else 
        {
            // Handle invalid time format
            System.out.println("Invalid time format: " + time);
            return null;
        }
    }

    public int getDayOfWeek() {
        String parts[] = this.horario.split(" ");
        String day = parts[0].strip();

        int dayOfWeek;
        switch (day.toLowerCase()) {
            case "segunda":
                dayOfWeek = 1;
                break;
            case "terca":
                dayOfWeek = 2;
                break;
            case "quarta":
                dayOfWeek = 3;
                break;
            case "quinta":
                dayOfWeek = 4;
                break;
            case "sexta":
                dayOfWeek = 5;
                break;
            case "sabado":
                dayOfWeek = 6;
                break;
            case "domingo":
                dayOfWeek = 7;
                break;
            default:
                throw new IllegalArgumentException("Invalid day: " + day);
        }

        return dayOfWeek;
    }
}