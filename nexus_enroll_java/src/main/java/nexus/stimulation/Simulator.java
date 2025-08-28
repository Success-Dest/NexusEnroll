package main.java.nexus.stimulation;

import main.java.nexus.model.*;
import main.java.nexus.repo.*;
import main.java.nexus.waitlist.WaitlistManager;
import main.java.nexus.notification.NotificationService;
import main.java.nexus.validators.*;
import main.java.nexus.service.EnrollmentService;
import main.java.nexus.service.GradeService;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * Simulator - runs a sequence of scenarios to exercise the business logic.
 * Place at: src/main/java/nexus/Simulator.java
 */
public class Simulator {

    // shared components for scenarios
    private InMemoryStudentRepo sRepo;
    private InMemoryCourseRepo cRepo;
    private InMemoryEnrollmentRepo eRepo;
    private WaitlistManager wl;
    private NotificationService notifier;
    private EnrollmentService enrollSvc;
    private GradeService gradeSvc;

    private void initCore() {
        sRepo = new InMemoryStudentRepo();
        cRepo = new InMemoryCourseRepo();
        eRepo = new InMemoryEnrollmentRepo();
        wl = new WaitlistManager();
        notifier = new NotificationService();
        wl.addObserver(notifier);

        List<EnrollmentValidator> validators = Arrays.asList(
                new PrerequisiteValidator(cRepo),
                new CapacityValidator(),
                new TimeConflictValidator(eRepo));

        enrollSvc = new EnrollmentService(sRepo, cRepo, eRepo, wl, validators);
        gradeSvc = new GradeService();
    }

    public void runAll() throws Exception {
        System.out.println("\n=== NexusEnroll Simulator ===\n");
        initCore();
        scenarioPrerequisiteFailure();
        scenarioEnrollSuccess();
        scenarioCapacityAndWaitlistPromotion();
        scenarioAdminOverride();
        scenarioConcurrentEnrollments();
        scenarioDoubleEnrollAttempt();
        scenarioDropNonexistent();
        scenarioGradeSubmission();
        System.out.println("\n=== Simulation complete ===");
    }

    // SCENARIO 1
    private void scenarioPrerequisiteFailure() {
        System.out.println("\n--- Scenario 1: Prerequisite Failure (expect reject, no waitlist) ---");
        System.out.println("Description: A student without prerequisites attempts to enroll and should be rejected.");

        // Setup
        Student a = new Student("S_A", "Alice", "alice@example.com");
        sRepo.save(a);
        Course course = new Course("CS300", "Advanced Topics");
        course.prerequisites.add("CS200");
        cRepo.saveCourse(course);
        Section sec = new Section("SEC_A", "CS300", "F_A", 5, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(9, 0));
        cRepo.saveSection(sec);

        String res = enrollSvc.enroll(a.getId(), sec.sectionId, false);
        System.out.println("Result: " + res);
        System.out.println("Enrollments for student: " + eRepo.findByStudent(a.getId()).size());
        assertNoEnrollmentsFor(a.getId());
    }

    // SCENARIO 2
    private void scenarioEnrollSuccess() {
        System.out.println("\n--- Scenario 2: Successful Enroll (prereqs satisfied) ---");
        System.out.println("Description: Student with prerequisite enrolls successfully and seat count increments.");

        // Setup
        Student b = new Student("S_B", "Bob", "bob@example.com");
        b.addCompletedCourse("CS200");
        sRepo.save(b);

        Course course = new Course("CS300", "Advanced Topics");
        course.prerequisites.add("CS200");
        cRepo.saveCourse(course);

        Section sec = new Section("SEC_B", "CS300", "F_B", 3, DayOfWeek.TUESDAY, LocalTime.of(9, 0),
                LocalTime.of(10, 0));
        cRepo.saveSection(sec);

        String res = enrollSvc.enroll(b.getId(), sec.sectionId, false);
        System.out.println("Result: " + res);
        printSectionSummary(sec.sectionId);
    }

    // SCENARIO 3
    private void scenarioCapacityAndWaitlistPromotion() {
        System.out.println("\n--- Scenario 3: Capacity Full -> Waitlist -> Promotion on Drop ---");
        System.out.println(
                "Description: First student fills capacity; second is waitlisted; after drop, waitlist promoted.");

        // Setup
        Student s1 = new Student("S1", "Carol", "c@example.com");
        s1.addCompletedCourse("PR1");
        sRepo.save(s1);
        Student s2 = new Student("S2", "Dave", "d@example.com");
        s2.addCompletedCourse("PR1");
        sRepo.save(s2);

        Course course = new Course("BUS101", "Introduction to Business"); // no prereqs
        cRepo.saveCourse(course);

        Section sec = new Section("SEC_C", "BUS101", "F_C", 1, DayOfWeek.WEDNESDAY, LocalTime.of(10, 0),
                LocalTime.of(11, 0));
        cRepo.saveSection(sec);

        System.out.println("Enroll S1 (should be enrolled): " + enrollSvc.enroll(s1.getId(), sec.sectionId, false));
        System.out.println("Enroll S2 (should be waitlisted): " + enrollSvc.enroll(s2.getId(), sec.sectionId, false));
        printSectionSummary(sec.sectionId);

        System.out.println("Now drop S1 (should promote S2): " + enrollSvc.drop(s1.getId(), sec.sectionId));
        printSectionSummary(sec.sectionId);

        // show student statuses
        System.out.println("S1 enrollments: " + summarizeEnrollmentsForStudent(s1.getId()));
        System.out.println("S2 enrollments: " + summarizeEnrollmentsForStudent(s2.getId()));
    }

    // SCENARIO 4
    private void scenarioAdminOverride() {
        System.out.println("\n--- Scenario 4: Admin Override ---");
        System.out.println(
                "Description: Admin forces enrollment bypassing prerequisites or capacity checks (admin flag).");

        Student s = new Student("S_ADMIN", "Eve", "eve@example.com");
        sRepo.save(s);
        Course course = new Course("HIST100", "World History");
        course.prerequisites.add("HIST50");
        cRepo.saveCourse(course);
        Section sec = new Section("SEC_ADMIN", "HIST100", "F_H", 1, DayOfWeek.THURSDAY, LocalTime.of(13, 0),
                LocalTime.of(14, 0));
        cRepo.saveSection(sec);

        String res = enrollSvc.enroll(s.getId(), sec.sectionId, true); // adminOverride = true
        System.out.println("Result: " + res);
        printSectionSummary(sec.sectionId);
    }

    // SCENARIO 5
    private void scenarioConcurrentEnrollments() throws Exception {
        System.out.println("\n--- Scenario 5: Concurrent Enrollments (robust stress test) ---");
        System.out.println(
                "Description: Many students attempt to enroll concurrently; ensure seat count == capacity and others waitlisted.");

        int capacity = 4;
        int attempts = 20;
        String courseId = "PAR101";
        String sectionId = "SEC_PAR";

        Course c = new Course(courseId, "Parallel Systems");
        cRepo.saveCourse(c);
        Section sec = new Section(sectionId, courseId, "F_PAR", capacity, DayOfWeek.FRIDAY, LocalTime.of(10, 0),
                LocalTime.of(11, 0));
        cRepo.saveSection(sec);

        for (int i = 0; i < attempts; i++) {
            Student s = new Student("T" + i, "TStudent" + i, "t" + i + "@ex.com");
            sRepo.save(s);
        }

        ExecutorService ex = Executors.newFixedThreadPool(Math.min(8, attempts));
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(attempts);
        List<Future<String>> futures = new ArrayList<>();

        // submit tasks; each task signals doneLatch when finished (always)
        for (int i = 0; i < attempts; i++) {
            final String sid = "T" + i;
            futures.add(ex.submit(() -> {
                try {
                    // wait for the coordinated start
                    startLatch.await();
                    // perform enroll - enroll() is synchronized but should return quickly
                    String r = enrollSvc.enroll(sid, sectionId, false);
                    return r;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return "Interrupted";
                } finally {
                    doneLatch.countDown();
                }
            }));
        }

        // start all workers
        long globalTimeoutSeconds = 20; // maximum time to wait for entire scenario
        startLatch.countDown();

        // wait for tasks to finish with timeout
        boolean finishedInTime = doneLatch.await(globalTimeoutSeconds, TimeUnit.SECONDS);
        if (!finishedInTime) {
            System.out
                    .println("Warning: Not all tasks finished within timeout. Attempting to cancel remaining tasks...");
            // cancel remaining futures
            for (Future<String> f : futures) {
                if (!f.isDone())
                    f.cancel(true);
            }
        }

        // collect results (non-blocking: use isDone() / get with tiny timeout)
        int completed = 0;
        int interrupted = 0;
        for (Future<String> f : futures) {
            try {
                if (f.isDone() && !f.isCancelled()) {
                    String res = f.get(100, TimeUnit.MILLISECONDS);
                    // optionally print a few sample results
                    if (completed < 10)
                        System.out.println(" - result: " + res);
                    completed++;
                } else if (f.isCancelled()) {
                    interrupted++;
                } else {
                    // not done but not cancelled (rare after doneLatch timeout)
                    try {
                        String res = f.get(200, TimeUnit.MILLISECONDS);
                        if (completed < 10)
                            System.out.println(" - result: " + res);
                        completed++;
                    } catch (Exception exx) {
                        System.out.println(
                                " - future unresolved: " + exx.getClass().getSimpleName() + " " + exx.getMessage());
                    }
                }
            } catch (CancellationException ce) {
                interrupted++;
            } catch (TimeoutException te) {
                System.out.println(" - result retrieval timeout for one future");
            }
        }

        ex.shutdownNow();
        if (!ex.awaitTermination(2, TimeUnit.SECONDS)) {
            System.out.println("Executor did not terminate after shutdownNow.");
        }

        // summary
        printSectionSummary(sectionId);
        List<Enrollment> list = eRepo.findBySection(sectionId);
        long enrolled = list.stream().filter(e -> e.status == EnrollmentStatus.ENROLLED).count();
        long wait = list.stream().filter(e -> e.status == EnrollmentStatus.WAITLISTED).count();
        System.out.printf("Summary: enrolled=%d, waitlisted=%d (capacity=%d)  completedTasks=%d  cancelled=%d%n",
                enrolled, wait, capacity, completed, interrupted);
    }

    // SCENARIO 6
    private void scenarioDoubleEnrollAttempt() {
        System.out.println("\n--- Scenario 6: Double Enroll Attempt (same student tries twice) ---");
        System.out.println(
                "Description: A student attempts to enroll twice in the same section; system should not create duplicate ENROLLED records.");

        Student s = new Student("DUP1", "Frank", "frank@example.com");
        s.addCompletedCourse("ANY");
        sRepo.save(s);
        Course c = new Course("MATH100", "Calculus");
        cRepo.saveCourse(c);
        Section sec = new Section("SEC_DUP", "MATH100", "F_M", 2, DayOfWeek.MONDAY, LocalTime.of(15, 0),
                LocalTime.of(16, 0));
        cRepo.saveSection(sec);

        System.out.println("First attempt: " + enrollSvc.enroll(s.getId(), sec.sectionId, false));
        System.out.println("Second attempt: " + enrollSvc.enroll(s.getId(), sec.sectionId, false));

        // count enrollments for student/section
        List<Enrollment> byStudent = eRepo.findByStudent(s.getId());
        long enrolledCount = byStudent.stream()
                .filter(en -> en.sectionId.equals(sec.sectionId) && en.status == EnrollmentStatus.ENROLLED).count();
        System.out.println("Enrolled records for student in that section: " + enrolledCount);
    }

    // SCENARIO 7
    private void scenarioDropNonexistent() {
        System.out.println("\n--- Scenario 7: Drop Non-existent Enrollment ---");
        System.out.println("Description: Attempt to drop a student who is not enrolled; should handle gracefully.");

        Student s = new Student("ND1", "Grace", "grace@example.com");
        sRepo.save(s);
        Course c = new Course("PHIL101", "Intro to Philosophy");
        cRepo.saveCourse(c);
        Section sec = new Section("SEC_NULL", "PHIL101", "F_PH", 2, DayOfWeek.WEDNESDAY, LocalTime.of(14, 0),
                LocalTime.of(15, 0));
        cRepo.saveSection(sec);

        String dropRes = enrollSvc.drop(s.getId(), sec.sectionId);
        System.out.println("Drop result: " + dropRes);
    }

    // SCENARIO 8
    private void scenarioGradeSubmission() {
        System.out.println("\n--- Scenario 8: Grade Submission Batch ---");
        System.out.println(
                "Description: Submit a batch with valid and invalid grades; GradeService should process valid ones and report invalid entries.");

        // Prepare a few enrollments to associate grades with
        Student s1 = new Student("G1", "Heidi", "h@example.com");
        s1.addCompletedCourse("X");
        sRepo.save(s1);
        Student s2 = new Student("G2", "Ian", "i@example.com");
        s2.addCompletedCourse("X");
        sRepo.save(s2);
        Course c = new Course("ENG201", "Literature");
        cRepo.saveCourse(c);
        Section sec = new Section("SEC_G", "ENG201", "F_G", 5, DayOfWeek.THURSDAY, LocalTime.of(11, 0),
                LocalTime.of(12, 0));
        cRepo.saveSection(sec);

        // Enroll them using admin override for speed
        enrollSvc.enroll(s1.getId(), sec.sectionId, true);
        enrollSvc.enroll(s2.getId(), sec.sectionId, true);

        Map<String, String> batch = new LinkedHashMap<>();
        batch.put(s1.getId() + "|" + sec.sectionId, "A");
        batch.put(s2.getId() + "|" + sec.sectionId, "X"); // invalid grade
        batch.put("UNKNOWN|SEC_X", "B"); // invalid key but should be handled gracefully

        // capture output for display
        gradeSvc.submitGradesBatch(batch);
        // For demo we don't assert; we show console output
    }

    /* ---------- Utilities ---------- */

    private void printSectionSummary(String sectionId) {
        System.out.println("Section summary for " + sectionId + ":");
        Optional<Section> secOpt = cRepo.findSection(sectionId);
        if (secOpt.isPresent()) {
            Section s = secOpt.get();
            System.out.printf("  capacity=%d, enrolledCount=%d, isFull=%b%n", s.capacity, s.getEnrolledCount(),
                    s.isFull());
        } else {
            System.out.println("  section not found");
        }
        List<Enrollment> list = eRepo.findBySection(sectionId);
        if (list.isEmpty()) {
            System.out.println("  No enrollments recorded.");
            return;
        }
        list.forEach(en -> System.out.printf("  Enrollment: %s student=%s status=%s%n", en.enrollmentId, en.studentId,
                en.status));
    }

    private void assertNoEnrollmentsFor(String studentId) {
        List<Enrollment> list = eRepo.findByStudent(studentId);
        if (!list.isEmpty()) {
            System.out.println("Unexpected enrollments found for " + studentId + ":");
            list.forEach(e -> System.out.println("  " + e.enrollmentId + " " + e.sectionId + " " + e.status));
        } else {
            System.out.println("No enrollments recorded for student (expected).");
        }
    }

    private String summarizeEnrollmentsForStudent(String studentId) {
        List<Enrollment> list = eRepo.findByStudent(studentId);
        if (list.isEmpty())
            return "None";
        StringBuilder sb = new StringBuilder();
        for (Enrollment e : list) {
            sb.append(String.format("[%s %s %s] ", e.enrollmentId, e.sectionId, e.status));
        }
        return sb.toString();
    }
}
