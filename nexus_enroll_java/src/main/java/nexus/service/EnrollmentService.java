package main.java.nexus.service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import main.java.nexus.model.*;
import main.java.nexus.repo.*;
import main.java.nexus.validators.*;
import main.java.nexus.waitlist.WaitlistManager;

/*
 Facade + Coordinator: EnrollmentService provides a simple API for controllers.
 It uses Chain of Responsibility pattern by sequencing validators.
 Transactional behavior is simulated with synchronized blocks for demo.
*/
public class EnrollmentService {
    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final WaitlistManager waitlistManager;
    private final List<EnrollmentValidator> validators;
    private final AtomicInteger idGen = new AtomicInteger(1);

    public EnrollmentService(StudentRepository sr, CourseRepository cr, EnrollmentRepository er,
            WaitlistManager wl, List<EnrollmentValidator> validators) {
        this.studentRepo = sr;
        this.courseRepo = cr;
        this.enrollmentRepo = er;
        this.waitlistManager = wl;
        this.validators = validators;
    }

    public synchronized String enroll(String studentId, String sectionId, boolean adminOverride) {
        Optional<Student> sOpt = studentRepo.findById(studentId);
        Optional<Section> secOpt = courseRepo.findSection(sectionId);
        if (!sOpt.isPresent())
            return "Student not found";
        if (!secOpt.isPresent())
            return "Section not found";
        Student s = sOpt.get();
        Section sec = secOpt.get();

        if (!adminOverride) {
            for (EnrollmentValidator v : validators) {
                ValidationResult r = v.validate(s, sec);
                if (!r.ok) {
                    if (r.message.equals("Section is full")) {
                        // add to waitlist
                        waitlistManager.addToWaitlist(sectionId, studentId);
                        String eid = "E" + idGen.getAndIncrement();
                        Enrollment e = new Enrollment(eid, studentId, sectionId, EnrollmentStatus.WAITLISTED);
                        enrollmentRepo.save(e);
                        return "Added to waitlist: " + eid + " Reason: " + r.message;
                    }
                    return "Enrollment failed: " + r.message;
                }
            }
        }

        // attempt to reserve a seat
        boolean reserved = sec.tryEnroll();
        if (!reserved) {
            // unlikely if validators passed, but handle
            waitlistManager.addToWaitlist(sectionId, studentId);
            String eid = "E" + idGen.getAndIncrement();
            Enrollment e = new Enrollment(eid, studentId, sectionId, EnrollmentStatus.WAITLISTED);
            enrollmentRepo.save(e);
            return "Added to waitlist (race): " + eid;
        }

        String eid = "E" + idGen.getAndIncrement();
        Enrollment e = new Enrollment(eid, studentId, sectionId, EnrollmentStatus.ENROLLED);
        enrollmentRepo.save(e);
        return "Enrolled: " + eid;
    }

    public synchronized String drop(String studentId, String sectionId) {
        // naive: find one enrollment and drop
        List<Enrollment> list = enrollmentRepo.findByStudent(studentId);
        Optional<Enrollment> found = list.stream()
                .filter(en -> en.sectionId.equals(sectionId) && en.status == EnrollmentStatus.ENROLLED).findFirst();
        if (!found.isPresent())
            return "No enrollment found to drop";
        Enrollment en = found.get();
        en.status = EnrollmentStatus.DROPPED;
        // decrement seat
        courseRepo.findSection(sectionId).ifPresent(sec -> sec.dropOne());
        // promote waitlist
        Optional<String> next = waitlistManager.popNext(sectionId);
        if (next.isPresent()) {
            String nextStudent = next.get();
            // promote: attempt to enroll the student
            String res = enroll(nextStudent, sectionId, true); // adminOverride true to bypass prereq checks when
                                                               // promoting
            return "Dropped. Promoted: " + res;
        }
        return "Dropped. No waitlist promotions.";
    }
}
