package main.java.nexus.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

public class Section {
    public final String sectionId;
    public final String courseId;
    public final String instructorId;
    public final int capacity;
    private final AtomicInteger enrolledCount = new AtomicInteger(0);
    // simple schedule representation: day and start/end times (for demo)
    public final DayOfWeek day;
    public final LocalTime start, end;

    public Section(String sectionId, String courseId, String instructorId, int capacity,
            DayOfWeek day, LocalTime start, LocalTime end) {
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.instructorId = instructorId;
        this.capacity = capacity;
        this.day = day;
        this.start = start;
        this.end = end;
    }

    public int getEnrolledCount() {
        return enrolledCount.get();
    }

    public boolean isFull() {
        return enrolledCount.get() >= capacity;
    }

    public boolean tryEnroll() {
        while (true) {
            int cur = enrolledCount.get();
            if (cur >= capacity)
                return false;
            if (enrolledCount.compareAndSet(cur, cur + 1))
                return true;
        }
    }

    public boolean dropOne() {
        while (true) {
            int cur = enrolledCount.get();
            if (cur <= 0)
                return false;
            if (enrolledCount.compareAndSet(cur, cur - 1))
                return true;
        }
    }
}
