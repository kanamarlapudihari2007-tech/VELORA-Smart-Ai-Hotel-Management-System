package com.hotel.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hotel.dao.AnalyticsDAO;
import com.hotel.model.HotelAnalytics;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * OperationsAnalyticsService - Stage 17 AI Hotel Operations Analytics & Manager Executive Intelligence.
 * Synthesizes real-time occupancy, revenue, complaints, and staff workloads using Google Gemini
 * to provide automated executive briefings, risk alerts, and managerial action points.
 */
public class OperationsAnalyticsService {

    private final AnalyticsDAO analyticsDAO;
    private final GeminiService geminiService;

    // In-memory cache for the AI briefing (5-minute TTL) to ensure sub-second dashboard loading
    private static HotelAnalytics cachedAnalytics = null;
    private static long lastBriefingGeneratedTime = 0;
    private static final long BRIEFING_CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

    public OperationsAnalyticsService() {
        this.analyticsDAO = new AnalyticsDAO();
        this.geminiService = new GeminiService();
    }

    public OperationsAnalyticsService(AnalyticsDAO analyticsDAO, GeminiService geminiService) {
        this.analyticsDAO = (analyticsDAO != null) ? analyticsDAO : new AnalyticsDAO();
        this.geminiService = (geminiService != null) ? geminiService : new GeminiService();
    }

    /**
     * Retrieves hotel analytics with an intelligent AI executive briefing.
     * 
     * @param forceRefresh If true, bypasses in-memory briefing cache and generates fresh AI analysis.
     * @return Fully populated HotelAnalytics instance.
     */
    public HotelAnalytics getExecutiveAnalytics(boolean forceRefresh) {
        long now = System.currentTimeMillis();

        // 1. Fetch fresh live database metrics
        HotelAnalytics analytics = analyticsDAO.getHotelAnalytics();

        // 2. Reuse cached AI briefing if valid and not force-refreshed
        if (!forceRefresh && cachedAnalytics != null && (now - lastBriefingGeneratedTime) < BRIEFING_CACHE_TTL_MS) {
            analytics.setAiExecutiveHeadline(cachedAnalytics.getAiExecutiveHeadline());
            analytics.setAiOperationalBriefing(cachedAnalytics.getAiOperationalBriefing());
            analytics.setAiRiskAlerts(cachedAnalytics.getAiRiskAlerts());
            analytics.setAiActionableRecommendations(cachedAnalytics.getAiActionableRecommendations());
            analytics.setFromAI(cachedAnalytics.isFromAI());
            analytics.setGeneratedTimestamp(cachedAnalytics.getGeneratedTimestamp());
            return analytics;
        }

        // 3. Generate AI Executive Briefing via Google Gemini
        if (geminiService.isApiKeyConfigured()) {
            try {
                String systemInstruction = "You are an expert AI Hotel Operations Director for AI Smart Hotel.\n"
                        + "Analyze the provided real-time operational and financial hotel metrics to generate an executive briefing for the General Manager.\n"
                        + "Tone: Executive, analytical, data-driven, and actionable.\n\n"
                        + "Strict Output Format: Output strictly a single JSON object with these exact keys:\n"
                        + "1. \"headline\": A powerful 1-sentence executive summary (e.g. \"Strong 75% Occupancy with Manageable Maintenance Load\").\n"
                        + "2. \"operationalBriefing\": 2 concise sentences summarizing revenue health, room utilization, and task resolution velocity.\n"
                        + "3. \"riskAlerts\": 1-2 bullet sentences highlighting urgent complaints, high-priority issues, or staff capacity constraints.\n"
                        + "4. \"actionableRecommendations\": 2 specific operational recommendations for the manager on duty.\n"
                        + "Do not include markdown codeblocks or extra text.";

                String metricsPrompt = buildMetricsPrompt(analytics);

                String aiResponse = geminiService.callGenerateContent(systemInstruction, metricsPrompt);
                boolean parsed = parseAndApplyAiBriefing(analytics, aiResponse);
                if (parsed) {
                    analytics.setFromAI(true);
                    analytics.setGeneratedTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a, MMM dd")));
                    cachedAnalytics = analytics;
                    lastBriefingGeneratedTime = now;
                    return analytics;
                }
            } catch (Exception e) {
                System.err.println("[OperationsAnalyticsService] Gemini briefing error: " + e.getMessage() + ". Using heuristic briefing.");
            }
        }

        // 4. Deterministic Heuristic Briefing Fallback
        applyHeuristicBriefing(analytics);
        analytics.setFromAI(false);
        analytics.setGeneratedTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a, MMM dd")));
        cachedAnalytics = analytics;
        lastBriefingGeneratedTime = now;
        return analytics;
    }

    private String buildMetricsPrompt(HotelAnalytics a) {
        return "Real-Time Hotel Metrics:\n"
                + "- Total Rooms: " + a.getTotalRooms() + " (Occupied: " + a.getOccupiedRooms() 
                + ", Reserved: " + a.getReservedRooms() + ", Available: " + a.getAvailableRooms() 
                + ", Cleaning: " + a.getCleaningRooms() + ", Maintenance: " + a.getMaintenanceRooms() + ")\n"
                + "- Current Occupancy Rate: " + a.getOccupancyRate() + "%\n"
                + "- Revenue: $" + String.format("%.2f", a.getTotalRevenue()) 
                + " (Collected: $" + String.format("%.2f", a.getPaidRevenue()) 
                + ", Pending: $" + String.format("%.2f", a.getPendingRevenue()) + ")\n"
                + "- Active Check-ins: " + a.getActiveCheckInsCount() + " guests in-house\n"
                + "- Operational Tasks: " + a.getTotalTasks() + " total (" + a.getCompletedTasks() + " completed, "
                + a.getInProgressTasks() + " in-progress, " + a.getPendingTasks() + " pending)\n"
                + "- Complaints: " + a.getTotalComplaints() + " total (" + a.getUrgentComplaints() + " urgent/high priority, "
                + a.getPendingComplaints() + " pending resolution)\n"
                + "- Complaint Categories: Maintenance=" + a.getComplaintsByCategory().getOrDefault("Maintenance", 0)
                + ", Housekeeping=" + a.getComplaintsByCategory().getOrDefault("Housekeeping", 0)
                + ", Food Service=" + a.getComplaintsByCategory().getOrDefault("Food Service", 0)
                + ", Reception=" + a.getComplaintsByCategory().getOrDefault("Reception", 0)
                + ", Security=" + a.getComplaintsByCategory().getOrDefault("Security", 0) + "\n"
                + "- Active Staff on Duty: " + a.getAvailableStaffCount() + " available, " + a.getBusyStaffCount() + " busy out of " + a.getTotalStaffCount() + " staff";
    }

    private boolean parseAndApplyAiBriefing(HotelAnalytics a, String rawJson) {
        if (rawJson == null || rawJson.trim().isEmpty()) {
            return false;
        }

        try {
            String clean = rawJson.trim();
            int start = clean.indexOf('{');
            int end = clean.lastIndexOf('}');
            if (start != -1 && end != -1 && end > start) {
                clean = clean.substring(start, end + 1);
            }

            JsonObject obj = JsonParser.parseString(clean).getAsJsonObject();
            if (obj.has("headline")) {
                a.setAiExecutiveHeadline(obj.get("headline").getAsString());
            }
            if (obj.has("operationalBriefing")) {
                a.setAiOperationalBriefing(obj.get("operationalBriefing").getAsString());
            }
            if (obj.has("riskAlerts")) {
                a.setAiRiskAlerts(obj.get("riskAlerts").getAsString());
            }
            if (obj.has("actionableRecommendations")) {
                a.setAiActionableRecommendations(obj.get("actionableRecommendations").getAsString());
            }
            return true;
        } catch (Exception e) {
            System.err.println("[OperationsAnalyticsService] JSON parse error: " + e.getMessage());
            return false;
        }
    }

    private void applyHeuristicBriefing(HotelAnalytics a) {
        // Dynamic headline based on occupancy & urgent issues
        if (a.getUrgentComplaints() > 0) {
            a.setAiExecutiveHeadline("⚠️ Operational Alert: " + a.getUrgentComplaints() + " Urgent Room Issue(s) Require Priority Attention at " + a.getOccupancyRate() + "% Occupancy");
        } else if (a.getOccupancyRate() >= 70.0) {
            a.setAiExecutiveHeadline("📈 Strong High-Yield Occupancy (" + a.getOccupancyRate() + "%) with Stable Operational Workflow");
        } else {
            a.setAiExecutiveHeadline("🏨 Steady Hotel Operations with " + a.getAvailableRooms() + " Available Rooms Ready for Guest Check-in");
        }

        a.setAiOperationalBriefing("Hotel occupancy is currently at " + a.getOccupancyRate() + "% with " + a.getActiveCheckInsCount() 
                + " active guest stays generating $" + String.format("%.2f", a.getTotalRevenue()) 
                + " in total revenue ($" + String.format("%.2f", a.getPaidRevenue()) + " collected). "
                + "Staff has resolved " + a.getCompletedTasks() + " operational tasks with " 
                + a.getAvailableStaffCount() + " employees currently available on duty.");

        if (a.getUrgentComplaints() > 0) {
            a.setAiRiskAlerts("• Attention: " + a.getUrgentComplaints() + " high-urgency complaint(s) currently open. Rapid resolution recommended to maintain guest satisfaction.");
        } else {
            a.setAiRiskAlerts("• No critical escalations detected. All departments are operating within standard SLA response times.");
        }

        a.setAiActionableRecommendations("1. Prioritize room turnaround for " + a.getCleaningRooms() + " room(s) marked for cleaning to maximize same-day booking availability.\n"
                + "2. Maintain proactive communication with checked-in guests via the Aura Virtual Concierge to capture feedback early.");
    }
}
