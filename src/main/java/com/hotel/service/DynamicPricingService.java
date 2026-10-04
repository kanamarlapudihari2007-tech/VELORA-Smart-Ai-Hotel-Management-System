package com.hotel.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hotel.dao.AnalyticsDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.DynamicPricingRecommendation;
import com.hotel.model.HotelAnalytics;
import com.hotel.model.RevenueOptimizationReport;
import com.hotel.model.Room;
import com.hotel.model.RoomType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DynamicPricingService - Stage 19 AI Dynamic Room Pricing & Revenue Optimization Engine (RMS).
 * Synthesizes live hotel occupancy, day-of-week demand elasticity, and room category capacity
 * using Google Gemini (gemini-3.5-flash-lite) with deterministic algorithmic fallbacks.
 */
public class DynamicPricingService {

    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5-minute cache
    private static RevenueOptimizationReport cachedReport;
    private static long lastGeneratedTime = 0;

    private final RoomDAO roomDAO;
    private final AnalyticsDAO analyticsDAO;
    private final GeminiService geminiService;

    public DynamicPricingService() {
        this(new RoomDAO(), new AnalyticsDAO(), new GeminiService());
    }

    public DynamicPricingService(RoomDAO roomDAO, AnalyticsDAO analyticsDAO, GeminiService geminiService) {
        this.roomDAO = (roomDAO != null) ? roomDAO : new RoomDAO();
        this.analyticsDAO = (analyticsDAO != null) ? analyticsDAO : new AnalyticsDAO();
        this.geminiService = (geminiService != null) ? geminiService : new GeminiService();
    }

    /**
     * Generates a comprehensive AI Revenue Management Report.
     */
    public RevenueOptimizationReport generateReport(boolean forceRefresh) {
        long now = System.currentTimeMillis();

        if (!forceRefresh && cachedReport != null && (now - lastGeneratedTime) < CACHE_TTL_MS) {
            return cachedReport;
        }

        RevenueOptimizationReport report = new RevenueOptimizationReport();

        // 1. Gather live operational metrics
        HotelAnalytics analytics = analyticsDAO.getHotelAnalytics();
        List<RoomType> roomTypes = roomDAO.getAllRoomTypes();
        List<Room> allRooms = roomDAO.getAllRooms();

        double occupancyRate = analytics.getOccupancyRate();
        report.setOverallOccupancyRate(occupancyRate);
        report.setTotalRooms(analytics.getTotalRooms());
        report.setOccupiedRooms(analytics.getOccupiedRooms());
        report.setAvailableRooms(analytics.getAvailableRooms());

        LocalDate today = LocalDate.now();
        DayOfWeek dow = today.getDayOfWeek();
        report.setDayOfWeek(dow.name());
        boolean isWeekend = (dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
        report.setWeekend(isWeekend);

        // 2. Compute per-tier capacity & occupancy
        Map<Integer, Integer> totalPerType = new HashMap<>();
        Map<Integer, Integer> occupiedPerType = new HashMap<>();

        for (Room r : allRooms) {
            int tid = r.getRoomTypeId();
            totalPerType.put(tid, totalPerType.getOrDefault(tid, 0) + 1);
            if ("OCCUPIED".equals(r.getStatus()) || "RESERVED".equals(r.getStatus())) {
                occupiedPerType.put(tid, occupiedPerType.getOrDefault(tid, 0) + 1);
            }
        }

        // 3. Attempt Google Gemini AI Generation
        boolean aiSuccess = false;
        if (geminiService.isApiKeyConfigured()) {
            try {
                String systemInstruction = "You are an expert AI Hospitality Revenue Management Strategist (RMS).\n"
                        + "Analyze the hotel's live occupancy, day-of-week demand, and room category pricing to calculate optimal dynamic rates.\n"
                        + "Respond ONLY with a valid JSON object without markdown codeblocks or commentary.";

                String prompt = buildPrompt(report, roomTypes, totalPerType, occupiedPerType);
                String aiResponse = geminiService.callGenerateContent(systemInstruction, prompt);

                if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                    aiSuccess = parseAndApplyAiPricing(report, roomTypes, aiResponse, totalPerType, occupiedPerType);
                }
            } catch (Exception e) {
                System.err.println("Warning: Gemini dynamic pricing call failed: " + e.getMessage());
            }
        }

        // 4. Fallback to deterministic algorithmic RMS equations if AI is unavailable
        if (!aiSuccess) {
            applyAlgorithmicFallback(report, roomTypes, totalPerType, occupiedPerType, isWeekend, occupancyRate);
            report.setFromAI(false);
        } else {
            report.setFromAI(true);
        }

        report.setGeneratedTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a, MMM dd")));
        cachedReport = report;
        lastGeneratedTime = now;

        return report;
    }

    private String buildPrompt(RevenueOptimizationReport report, List<RoomType> roomTypes,
                               Map<Integer, Integer> totalPerType, Map<Integer, Integer> occupiedPerType) {
        StringBuilder sb = new StringBuilder();
        sb.append("Live Hotel Operations Status:\n");
        sb.append("- Overall Hotel Occupancy: ").append(String.format("%.1f", report.getOverallOccupancyRate())).append("%\n");
        sb.append("- Day of Week: ").append(report.getDayOfWeek()).append(" (Weekend Leisure Period: ").append(report.isWeekend()).append(")\n");
        sb.append("- Total Rooms: ").append(report.getTotalRooms()).append(", Currently Occupied/Reserved: ").append(report.getOccupiedRooms()).append("\n\n");
        sb.append("Room Categories & Baseline Pricing:\n");

        for (RoomType rt : roomTypes) {
            int tot = totalPerType.getOrDefault(rt.getId(), 0);
            int occ = occupiedPerType.getOrDefault(rt.getId(), 0);
            sb.append("• ").append(rt.getTypeName())
                    .append(" (ID: ").append(rt.getId()).append("): ")
                    .append("Base Rate: ₹").append(rt.getPricePerNight())
                    .append(", Inventory: ").append(occ).append("/").append(tot).append(" occupied\n");
        }

        sb.append("\nRespond with this exact JSON format:\n");
        sb.append("{\n");
        sb.append("  \"headline\": \"<1-sentence strategic market headline>\",\n");
        sb.append("  \"marketSummary\": \"<2-3 sentences explaining market demand, elasticity, and RevPAR strategy>\",\n");
        sb.append("  \"recommendations\": [\n");
        sb.append("    {\n");
        sb.append("      \"roomTypeId\": <id>,\n");
        sb.append("      \"typeName\": \"<Category Name>\",\n");
        sb.append("      \"recommendedPrice\": <new optimal price numeric>,\n");
        sb.append("      \"adjustmentPercent\": <float like 15.0 or -10.0>,\n");
        sb.append("      \"strategy\": \"SURGE_PRICING\" | \"OPTIMAL_EQUILIBRIUM\" | \"DEMAND_STIMULATION\",\n");
        sb.append("      \"economicRationale\": \"<1-2 sentences on why this price optimizes yield>\"\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}");

        return sb.toString();
    }

    private boolean parseAndApplyAiPricing(RevenueOptimizationReport report, List<RoomType> roomTypes,
                                           String json, Map<Integer, Integer> totalPerType, Map<Integer, Integer> occupiedPerType) {
        String cleanJson = json.trim();
        int start = cleanJson.indexOf('{');
        int end = cleanJson.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            cleanJson = cleanJson.substring(start, end + 1);
        }

        JsonObject root = JsonParser.parseString(cleanJson).getAsJsonObject();

        if (root.has("headline")) {
            report.setOverallStrategyHeadline(root.get("headline").getAsString());
        }
        if (root.has("marketSummary")) {
            report.setAiMarketSummary(root.get("marketSummary").getAsString());
        }

        if (root.has("recommendations") && root.get("recommendations").isJsonArray()) {
            JsonArray arr = root.getAsJsonArray("recommendations");
            List<DynamicPricingRecommendation> recs = new ArrayList<>();

            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                int typeId = o.has("roomTypeId") ? o.get("roomTypeId").getAsInt() : 0;
                String typeName = o.has("typeName") ? o.get("typeName").getAsString() : "";

                // Find baseline price from current roomTypes
                BigDecimal basePrice = BigDecimal.ZERO;
                for (RoomType rt : roomTypes) {
                    if (rt.getId() == typeId || rt.getTypeName().equalsIgnoreCase(typeName)) {
                        typeId = rt.getId();
                        typeName = rt.getTypeName();
                        basePrice = rt.getPricePerNight();
                        break;
                    }
                }

                BigDecimal recPrice = o.has("recommendedPrice") 
                        ? BigDecimal.valueOf(o.get("recommendedPrice").getAsDouble()).setScale(2, RoundingMode.HALF_UP)
                        : basePrice;
                double adj = o.has("adjustmentPercent") ? o.get("adjustmentPercent").getAsDouble() : 0.0;
                String strat = o.has("strategy") ? o.get("strategy").getAsString().toUpperCase() : "OPTIMAL_EQUILIBRIUM";
                String rationale = o.has("economicRationale") ? o.get("economicRationale").getAsString() : "";

                DynamicPricingRecommendation rec = new DynamicPricingRecommendation(
                        typeId, typeName, basePrice, recPrice, adj, strat, rationale
                );

                int tot = totalPerType.getOrDefault(typeId, 0);
                int occ = occupiedPerType.getOrDefault(typeId, 0);
                rec.setTotalRooms(tot);
                rec.setOccupiedRooms(occ);
                rec.setOccupancyRate(tot > 0 ? ((double) occ / tot) * 100.0 : 0.0);

                recs.add(rec);
            }

            if (!recs.isEmpty()) {
                report.setRecommendations(recs);
                return true;
            }
        }
        return false;
    }

    /**
     * Algorithmic deterministic revenue management equations.
     */
    public void applyAlgorithmicFallback(RevenueOptimizationReport report, List<RoomType> roomTypes,
                                         Map<Integer, Integer> totalPerType, Map<Integer, Integer> occupiedPerType,
                                         boolean isWeekend, double overallOccupancy) {

        List<DynamicPricingRecommendation> recs = new ArrayList<>();

        double modifier = 0.0;
        String generalStrategy = "OPTIMAL_EQUILIBRIUM";

        if (overallOccupancy >= 70.0) {
            modifier += 0.20; // +20% high demand surge
            generalStrategy = "SURGE_PRICING";
        } else if (overallOccupancy <= 35.0) {
            modifier -= 0.12; // -12% demand stimulation discount
            generalStrategy = "DEMAND_STIMULATION";
        } else {
            modifier += 0.05; // +5% modest optimization
        }

        if (isWeekend) {
            modifier += 0.08; // +8% weekend leisure bump
        }

        for (RoomType rt : roomTypes) {
            BigDecimal base = rt.getPricePerNight();
            double tierModifier = modifier;

            // Luxury suites have less price sensitivity; discount single rooms more aggressively
            if (rt.getTypeName().toLowerCase().contains("suite")) {
                if (tierModifier > 0) tierModifier += 0.05;
            } else if (rt.getTypeName().toLowerCase().contains("single")) {
                if (tierModifier < 0) tierModifier -= 0.03;
            }

            double newPriceDbl = Math.round(base.doubleValue() * (1.0 + tierModifier) / 10.0) * 10.0;
            BigDecimal recPrice = BigDecimal.valueOf(newPriceDbl).setScale(2, RoundingMode.HALF_UP);
            double adjPercent = Math.round(tierModifier * 1000.0) / 10.0;

            String strategy = tierModifier > 0.05 ? "SURGE_PRICING" 
                    : (tierModifier < -0.05 ? "DEMAND_STIMULATION" : "OPTIMAL_EQUILIBRIUM");

            String rationale;
            if (strategy.equals("SURGE_PRICING")) {
                rationale = "High demand elasticity allows capturing yield premiums without risking reservation velocity.";
            } else if (strategy.equals("DEMAND_STIMULATION")) {
                rationale = "Promotional discounted rate applied to improve occupancy rates and stimulate room bookings.";
            } else {
                rationale = "Balanced competitive pricing aligned with current market equilibrium.";
            }

            DynamicPricingRecommendation rec = new DynamicPricingRecommendation(
                    rt.getId(), rt.getTypeName(), base, recPrice, adjPercent, strategy, rationale
            );

            int tot = totalPerType.getOrDefault(rt.getId(), 0);
            int occ = occupiedPerType.getOrDefault(rt.getId(), 0);
            rec.setTotalRooms(tot);
            rec.setOccupiedRooms(occ);
            rec.setOccupancyRate(tot > 0 ? ((double) occ / tot) * 100.0 : 0.0);

            recs.add(rec);
        }

        report.setRecommendations(recs);

        if (generalStrategy.equals("SURGE_PRICING")) {
            report.setOverallStrategyHeadline("High Occupancy Surge: Capitalizing on Inelastic Demand to Maximize RevPAR");
            report.setAiMarketSummary("With occupancy exceeding threshold capacity, surge pricing has been algorithmically calibrated to optimize gross margin per occupied room.");
        } else if (generalStrategy.equals("DEMAND_STIMULATION")) {
            report.setOverallStrategyHeadline("Low Occupancy Stimulus: Promotional Pricing Activated to Drive Volume");
            report.setAiMarketSummary("Current occupancy is tracking below baseline target; promotional tariffs have been deployed across categories to increase conversion.");
        } else {
            report.setOverallStrategyHeadline("Balanced Market Equilibrium: Yield Optimization with Stable Demand");
            report.setAiMarketSummary("Hotel occupancy and booking velocity remain in healthy equilibrium. Moderate yield adjustments are active to safeguard rate integrity.");
        }
    }
}
