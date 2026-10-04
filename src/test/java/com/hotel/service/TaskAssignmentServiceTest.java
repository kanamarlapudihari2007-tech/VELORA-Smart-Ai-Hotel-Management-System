package com.hotel.service;

import com.hotel.model.Staff;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TaskAssignmentServiceTest - Unit tests verifying Stage 12 automatic staff task assignment logic.
 */
public class TaskAssignmentServiceTest {

    private TaskAssignmentService service;

    @Before
    public void setUp() {
        service = new TaskAssignmentService();
    }

    @Test
    public void testCategoryResolution() {
        // 1. Maintenance
        assertEquals("Maintenance", service.resolveTargetCategory("COMPLAINT", "AC not working in room 204"));
        assertEquals("Maintenance", service.resolveTargetCategory("COMPLAINT", "Plumbing leak in bathroom"));
        assertEquals("Maintenance", service.resolveTargetCategory("COMPLAINT", "Electrical switch sparking"));

        // 2. Housekeeping
        assertEquals("Housekeeping", service.resolveTargetCategory("ROOM_CLEANING", null));
        assertEquals("Housekeeping", service.resolveTargetCategory("SERVICE_REQUEST", "Need 2 extra towels and linen"));
        assertEquals("Housekeeping", service.resolveTargetCategory("COMPLAINT", "Bed sheet is dirty"));

        // 3. Food Service
        assertEquals("Food Service", service.resolveTargetCategory("SERVICE_REQUEST", "Order breakfast and drinking water"));
        assertEquals("Food Service", service.resolveTargetCategory("SERVICE_REQUEST", "Dining snacks"));

        // 4. Security
        assertEquals("Security", service.resolveTargetCategory("COMPLAINT", "Loud noise disturbance next door"));

        // 5. Reception
        assertEquals("Reception", service.resolveTargetCategory("SERVICE_REQUEST", "Keycard not opening room"));
    }

    @Test
    public void testScoreCalculationForOffDuty() {
        Staff offDuty = new Staff();
        offDuty.setWorkStatus("OFF_DUTY");
        offDuty.setActiveTaskCount(0);
        offDuty.setPerformanceCredits(200);

        double score = service.calculateScore(offDuty);
        assertEquals("Off-duty staff must be disqualified (-1.0)", -1.0, score, 0.001);
    }

    @Test
    public void testWorkloadBalancingPreference() {
        // John: Available, 0 active tasks, 100 credits
        Staff john = new Staff();
        john.setId(1);
        john.setWorkStatus("AVAILABLE");
        john.setActiveTaskCount(0);
        john.setPerformanceCredits(100);

        // Mike: Available, 2 active tasks, 150 credits (higher credits, but higher workload)
        Staff mike = new Staff();
        mike.setId(2);
        mike.setWorkStatus("AVAILABLE");
        mike.setActiveTaskCount(2);
        mike.setPerformanceCredits(150);

        double johnScore = service.calculateScore(john);
        double mikeScore = service.calculateScore(mike);

        // John: 40 (workload) + 30 (avail) + 10 (credits) + 10 (seniority) = 90.0
        // Mike: 20 (workload) + 30 (avail) + 15 (credits) + 10 (seniority) = 75.0
        assertTrue("Workload balance must favor John (0 tasks) over Mike (2 tasks)", johnScore > mikeScore);
        assertEquals(90.0, johnScore, 0.001);
        assertEquals(75.0, mikeScore, 0.001);
    }

    @Test
    public void testAvailabilityPreference() {
        // Alice: Available, 1 active task, 100 credits
        Staff alice = new Staff();
        alice.setId(1);
        alice.setWorkStatus("AVAILABLE");
        alice.setActiveTaskCount(1);
        alice.setPerformanceCredits(100);

        // Bob: Busy, 1 active task, 100 credits
        Staff bob = new Staff();
        bob.setId(2);
        bob.setWorkStatus("BUSY");
        bob.setActiveTaskCount(1);
        bob.setPerformanceCredits(100);

        double aliceScore = service.calculateScore(alice);
        double bobScore = service.calculateScore(bob);

        assertTrue("Available staff must score higher than Busy staff with same workload", aliceScore > bobScore);
        assertEquals(80.0, aliceScore, 0.001); // 30 + 30 + 10 + 10 = 80
        assertEquals(60.0, bobScore, 0.001);   // 30 + 10 + 10 + 10 = 60
    }

    @Test
    public void testPerformanceCreditsWeight() {
        // High performer vs average performer when workload and availability are identical
        Staff star = new Staff();
        star.setId(1);
        star.setWorkStatus("AVAILABLE");
        star.setActiveTaskCount(1);
        star.setPerformanceCredits(180); // 18 pts

        Staff avg = new Staff();
        avg.setId(2);
        avg.setWorkStatus("AVAILABLE");
        avg.setActiveTaskCount(1);
        avg.setPerformanceCredits(100); // 10 pts

        double starScore = service.calculateScore(star);
        double avgScore = service.calculateScore(avg);

        assertTrue("Higher performance credits must break ties positively", starScore > avgScore);
        assertEquals(88.0, starScore, 0.001); // 30 + 30 + 18 + 10 = 88
        assertEquals(80.0, avgScore, 0.001); // 30 + 30 + 10 + 10 = 80
    }
}
