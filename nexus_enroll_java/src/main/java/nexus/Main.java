package main.java.nexus;

import main.java.nexus.stimulation.Simulator;


public class Main {

    public static void main(String[] args) throws Exception {
        Simulator sim = new Simulator();
        sim.runAll();
    }
    
    // public static void main(String[] args) {
    //     // Setup repositories
    //     InMemoryStudentRepo sRepo = new InMemoryStudentRepo();
    //     InMemoryCourseRepo cRepo = new InMemoryCourseRepo();
    //     InMemoryEnrollmentRepo eRepo = new InMemoryEnrollmentRepo();

    //     // Create sample data
    //     Student s1 = new Student("S1", "Alice", "alice@example.com");
    //     Student s2 = new Student("S2", "Bob", "bob@example.com");
    //     s2.addCompletedCourse("CS101"); // Bob has prereq
    //     sRepo.save(s1);
    //     sRepo.save(s2);

    //     Course cs102 = new Course("CS102", "Intro to Algo");
    //     cs102.prerequisites.add("CS101");
    //     cRepo.saveCourse(cs102);

    //     Section secA = new Section("SEC1", "CS102", "F1", 1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0));
    //     cRepo.saveSection(secA);

    //     // Waitlist manager & notification
    //     WaitlistManager wl = new WaitlistManager();
    //     NotificationService notifier = new NotificationService();
    //     wl.addObserver(notifier);

    //     // Validators chain
    //     List<EnrollmentValidator> validators = Arrays.asList(
    //             new PrerequisiteValidator(cRepo),
    //             new CapacityValidator(),
    //             new TimeConflictValidator(eRepo));

    //     EnrollmentService enrollSvc = new EnrollmentService(sRepo, cRepo, eRepo, wl, validators);
    //     GradeService gradeSvc = new GradeService();

    //     // Demo flows
    //     System.out.println("--- Attempt enroll Alice (no prereq) ---");
    //     System.out.println(enrollSvc.enroll("S1", "SEC1", false));

    //     System.out.println("--- Attempt enroll Bob (has prereq) ---");
    //     System.out.println(enrollSvc.enroll("S2", "SEC1", false));

    //     System.out.println("--- Alice drops -> should promote Bob from waitlist ---");
    //     System.out.println(enrollSvc.drop("S1", "SEC1"));

    //     System.out.println("--- Submit grades batch ---");
    //     Map<String, String> batch = new HashMap<>();
    //     batch.put("S2|SEC1", "A");
    //     batch.put("S1|SEC1", "X"); // invalid grade
    //     gradeSvc.submitGradesBatch(batch);

    //     System.out.println("Demo complete.");

        
    // }
}
