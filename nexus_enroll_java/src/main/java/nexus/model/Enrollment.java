package main.java.nexus.model;

public class Enrollment {
    public final String enrollmentId;
    public final String studentId;
    public final String sectionId;
    public EnrollmentStatus status;

    public Enrollment(String enrollmentId, String studentId, String sectionId, EnrollmentStatus status) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = status;
    }
}
