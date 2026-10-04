package com.hotel.service;

import com.hotel.dao.NotificationDAO;
import com.hotel.dao.StaffDAO;
import com.hotel.dao.TaskDAO;
import com.hotel.model.Staff;
import com.hotel.model.Task;

import java.util.List;

/**
 * TaskAssignmentService - Core intelligent dispatcher for the AI Smart Hotel Management System.
 * Implements deterministic multi-criteria staff assignment based on:
 * 1. Category Matching (Strict department filtering)
 * 2. Workload Balancing (40% weight - prefers staff with fewer active tasks)
 * 3. Availability State (30% weight - AVAILABLE preferred over BUSY, excludes OFF_DUTY)
 * 4. Performance Credits (20% weight - recognizes reliable and high-performing staff)
 * 5. Seniority / Experience (10% weight)
 */
public class TaskAssignmentService {

    private final StaffDAO staffDAO;
    private final TaskDAO taskDAO;
    private final NotificationDAO notificationDAO;

    public TaskAssignmentService() {
        this.staffDAO = new StaffDAO();
        this.taskDAO = new TaskDAO();
        this.notificationDAO = new NotificationDAO();
    }

    /**
     * Maps task parameters (type, raw category / request text) to the corresponding hotel Staff Category.
     */
    public String resolveTargetCategory(String taskType, String rawCategory) {
        if ("ROOM_CLEANING".equalsIgnoreCase(taskType)) {
            return "Housekeeping";
        }

        String cat = (rawCategory != null) ? rawCategory.toLowerCase() : "";

        if (cat.contains("housekeeping") || cat.contains("clean") || cat.contains("towel") || 
            cat.contains("linen") || cat.contains("bed") || cat.contains("pillow")) {
            return "Housekeeping";
        } else if (cat.contains("food") || cat.contains("beverage") || cat.contains("dining") || 
                   cat.contains("breakfast") || cat.contains("dinner") || cat.contains("water") || cat.contains("snack")) {
            return "Food Service";
        } else if (cat.contains("security") || cat.contains("noise") || cat.contains("disturbance") || cat.contains("lock")) {
            return "Security";
        } else if (cat.contains("reception") || cat.contains("front desk") || cat.contains("keycard") || cat.contains("billing")) {
            return "Reception";
        } else {
            // Default maintenance for repairs, plumbing, electrical, AC, etc.
            return "Maintenance";
        }
    }

    /**
     * Calculates the deterministic assignment score for a candidate staff member.
     * Score Formula:
     * - Workload (40 pts max)
     * - Availability (30 pts max)
     * - Performance Credits (20 pts max)
     * - Seniority / Experience (10 pts max)
     */
    public double calculateScore(Staff staff) {
        // Disqualify off-duty staff
        if ("OFF_DUTY".equalsIgnoreCase(staff.getWorkStatus())) {
            return -1.0;
        }

        // 1. Workload Score (40% weight)
        int activeTasks = staff.getActiveTaskCount();
        double workloadScore;
        if (activeTasks == 0) {
            workloadScore = 40.0;
        } else if (activeTasks == 1) {
            workloadScore = 30.0;
        } else if (activeTasks == 2) {
            workloadScore = 20.0;
        } else if (activeTasks == 3) {
            workloadScore = 10.0;
        } else {
            workloadScore = Math.max(0.0, 40.0 - (activeTasks * 10.0));
        }

        // 2. Availability Score (30% weight)
        double availabilityScore;
        if ("AVAILABLE".equalsIgnoreCase(staff.getWorkStatus())) {
            availabilityScore = 30.0;
        } else if ("BUSY".equalsIgnoreCase(staff.getWorkStatus())) {
            availabilityScore = 10.0; // Fallback if all staff are occupied
        } else {
            availabilityScore = 0.0;
        }

        // 3. Performance Credits Score (20% weight)
        // 100 credits = 10 pts, 150 = 15 pts, 200 = 20 pts max
        int credits = staff.getPerformanceCredits();
        double performanceScore = Math.min(20.0, Math.max(0.0, credits / 10.0));

        // 4. Seniority / Experience Score (10% weight)
        double seniorityScore = 10.0;

        return workloadScore + availabilityScore + performanceScore + seniorityScore;
    }

    /**
     * Automatically assigns a newly created task to the most suitable eligible staff member.
     * @param taskId The task ID in the 'tasks' table
     * @param taskType ROOM_CLEANING, SERVICE_REQUEST, or COMPLAINT
     * @param category Raw category or request type
     * @param priority URGENT, HIGH, NORMAL, LOW
     * @param roomNumber The room number associated with the task
     * @return Assigned Staff object, or null if no staff available
     */
    public Staff assignTaskAutomatically(int taskId, String taskType, String category, String priority, String roomNumber) {
        return assignTaskInternal(taskId, taskType, category, priority, roomNumber, -1);
    }

    /**
     * Reassigns a task when an employee declines the assignment.
     */
    public Staff reassignTask(int taskId, int decliningStaffId) {
        Task task = taskDAO.getTaskById(taskId);
        if (task == null) {
            return null;
        }

        // Clear current assignment
        taskDAO.clearTaskAssignment(taskId);

        // Attempt reassignment excluding the declining employee
        Staff newStaff = assignTaskInternal(taskId, task.getTaskType(), task.getNotes(), task.getPriority(), task.getRoomNumber(), decliningStaffId);

        if (newStaff == null) {
            // Alert Manager that the task was declined and could not be automatically reassigned
            notificationDAO.notifyAllManagers(
                "Task #" + taskId + " Declined",
                "Task #" + taskId + " (Room " + task.getRoomNumber() + ") was declined by staff #" + decliningStaffId + " and no other staff is currently available. Manual intervention required."
            );
        }

        return newStaff;
    }

    private Staff assignTaskInternal(int taskId, String taskType, String rawCategory, String priority, String roomNumber, int excludedStaffId) {
        String targetCategory = resolveTargetCategory(taskType, rawCategory);

        // Fetch all staff belonging to the resolved department with their live workloads
        List<Staff> candidates = staffDAO.getStaffWithWorkloadByCategory(targetCategory);

        Staff bestStaff = null;
        double bestScore = -1.0;

        for (Staff candidate : candidates) {
            if (candidate.getId() == excludedStaffId) {
                continue; // Skip excluded/declined staff
            }

            double score = calculateScore(candidate);
            if (score > bestScore) {
                bestScore = score;
                bestStaff = candidate;
            } else if (Double.compare(score, bestScore) == 0 && bestStaff != null) {
                // Tie-breaker 1: Lower active workload
                if (candidate.getActiveTaskCount() < bestStaff.getActiveTaskCount()) {
                    bestStaff = candidate;
                }
                // Tie-breaker 2: Higher performance credits
                else if (candidate.getPerformanceCredits() > bestStaff.getPerformanceCredits()) {
                    bestStaff = candidate;
                }
            }
        }

        if (bestStaff != null) {
            // Perform atomic database update with concurrency protection
            boolean assigned = taskDAO.assignTaskAtomic(taskId, bestStaff.getId());

            if (assigned) {
                // Send real-time notification to the selected staff member
                String pLabel = (priority != null && !priority.isEmpty()) ? priority : "NORMAL";
                String title = "New " + pLabel + " " + targetCategory + " Task";
                String message = "New " + pLabel + " priority task #" + taskId + " assigned to you for Room " + (roomNumber != null ? roomNumber : "N/A") + ".";

                notificationDAO.createNotification(bestStaff.getUserId(), title, message);
                return bestStaff;
            }
        }

        // If no candidate is available or atomic assignment could not claim the task:
        String pLabel = (priority != null && !priority.isEmpty()) ? priority : "NORMAL";
        String alertTitle = "Unassigned " + pLabel + " Task #" + taskId;
        String alertMsg = "No suitable " + targetCategory + " employee is currently available for " + pLabel + " priority task #" + taskId + " (Room " + (roomNumber != null ? roomNumber : "N/A") + "). Manual manager assignment required.";

        notificationDAO.notifyAllManagers(alertTitle, alertMsg);
        return null;
    }
}
