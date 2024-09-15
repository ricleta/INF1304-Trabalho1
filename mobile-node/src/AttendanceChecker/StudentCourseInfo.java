package AttendanceChecker;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class StudentCourseInfo {
    private LocalDate date;
    private String studentId;
    private String courseId;

    // Default constructor
    public StudentCourseInfo() {}

    // Parameterized constructor
    public StudentCourseInfo(LocalDate date, String studentId, String courseId) {
        this.date = date;
        this.studentId = studentId;
        this.courseId = courseId;
    }

    // Getters and setters
    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    @Override
    public String toString() {
        return "StudentCourseInfo{" +
               "date=" + date +
               ", studentId='" + studentId + '\'' +
               ", courseId='" + courseId + '\'' +
               '}';
    }
}
