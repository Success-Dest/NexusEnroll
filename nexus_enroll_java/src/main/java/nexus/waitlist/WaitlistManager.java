package main.java.nexus.waitlist;

import java.util.*;

// Observer pattern: WaitlistManager is a Subject. NotificationService subscribes as Observer.
// When a seat opens, WaitlistManager notifies and returns the next student to promote.
public class WaitlistManager {
    private final Map<String, Deque<String>> waitlists = new HashMap<>(); // sectionId -> queue of studentId
    private final List<WaitlistObserver> observers = new ArrayList<>();

    public interface WaitlistObserver {
        void onSeatAvailable(String sectionId, String studentId);
    }

    public void addObserver(WaitlistObserver o) {
        observers.add(o);
    }

    public void removeObserver(WaitlistObserver o) {
        observers.remove(o);
    }

    public void addToWaitlist(String sectionId, String studentId) {
        waitlists.computeIfAbsent(sectionId, k -> new ArrayDeque<>()).addLast(studentId);
    }

    public Optional<String> popNext(String sectionId) {
        Deque<String> q = waitlists.get(sectionId);
        if (q == null || q.isEmpty())
            return Optional.empty();
        String studentId = q.removeFirst();
        // notify observers that this student is being promoted
        for (WaitlistObserver o : observers)
            o.onSeatAvailable(sectionId, studentId);
        return Optional.of(studentId);
    }
}
