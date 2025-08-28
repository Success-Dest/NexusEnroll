package main.java.nexus.notification;

import main.java.nexus.waitlist.WaitlistManager;

// Observer implementation that sends notifications (console for PoC)
public class NotificationService implements WaitlistManager.WaitlistObserver {
    @Override
    public void onSeatAvailable(String sectionId, String studentId) {
        // In a real system, send email/SMS. PoC -> print
        System.out.printf("[Notification] Student %s: seat available in section %s\n", studentId, sectionId);
    }
}
