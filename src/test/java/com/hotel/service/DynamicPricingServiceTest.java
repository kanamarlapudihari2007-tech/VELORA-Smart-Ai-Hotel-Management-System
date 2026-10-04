package com.hotel.service;

import com.hotel.model.DynamicPricingRecommendation;
import com.hotel.model.RevenueOptimizationReport;
import com.hotel.model.RoomType;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * DynamicPricingServiceTest - Unit tests for Stage 19 AI Dynamic Pricing & Revenue Optimization formulas.
 */
public class DynamicPricingServiceTest {

    private DynamicPricingService service;
    private List<RoomType> mockRoomTypes;
    private Map<Integer, Integer> totalPerType;
    private Map<Integer, Integer> occupiedPerType;

    @Before
    public void setUp() {
        service = new DynamicPricingService();

        mockRoomTypes = new ArrayList<>();
        mockRoomTypes.add(new RoomType(1, "Standard Single", new BigDecimal("1200.00"), "Single room", 1));
        mockRoomTypes.add(new RoomType(2, "Deluxe Double", new BigDecimal("2500.00"), "Double room", 2));
        mockRoomTypes.add(new RoomType(3, "Executive Suite", new BigDecimal("5000.00"), "Suite room", 4));

        totalPerType = new HashMap<>();
        totalPerType.put(1, 2);
        totalPerType.put(2, 2);
        totalPerType.put(3, 2);

        occupiedPerType = new HashMap<>();
    }

    @Test
    public void testHighOccupancySurgePricing() {
        RevenueOptimizationReport report = new RevenueOptimizationReport();
        // High occupancy: 85%
        service.applyAlgorithmicFallback(report, mockRoomTypes, totalPerType, occupiedPerType, false, 85.0);

        assertNotNull(report.getRecommendations());
        assertEquals(3, report.getRecommendations().size());
        assertTrue(report.getOverallStrategyHeadline().contains("Surge"));

        for (DynamicPricingRecommendation rec : report.getRecommendations()) {
            assertEquals("SURGE_PRICING", rec.getStrategy());
            assertTrue("Recommended price should be higher than current price",
                    rec.getRecommendedPrice().compareTo(rec.getCurrentPrice()) > 0);
            assertTrue("Adjustment percent should be positive", rec.getAdjustmentPercent() > 0);
            assertNotNull(rec.getEconomicRationale());
        }
    }

    @Test
    public void testLowOccupancyDemandStimulation() {
        RevenueOptimizationReport report = new RevenueOptimizationReport();
        // Low occupancy: 20%
        service.applyAlgorithmicFallback(report, mockRoomTypes, totalPerType, occupiedPerType, false, 20.0);

        assertNotNull(report.getRecommendations());
        assertEquals(3, report.getRecommendations().size());
        assertTrue(report.getOverallStrategyHeadline().contains("Stimulus"));

        for (DynamicPricingRecommendation rec : report.getRecommendations()) {
            assertEquals("DEMAND_STIMULATION", rec.getStrategy());
            assertTrue("Recommended price should be lower than current price",
                    rec.getRecommendedPrice().compareTo(rec.getCurrentPrice()) < 0);
            assertTrue("Adjustment percent should be negative", rec.getAdjustmentPercent() < 0);
        }
    }

    @Test
    public void testWeekendLeisureSurgeFactor() {
        RevenueOptimizationReport weekdayReport = new RevenueOptimizationReport();
        service.applyAlgorithmicFallback(weekdayReport, mockRoomTypes, totalPerType, occupiedPerType, false, 50.0);

        RevenueOptimizationReport weekendReport = new RevenueOptimizationReport();
        service.applyAlgorithmicFallback(weekendReport, mockRoomTypes, totalPerType, occupiedPerType, true, 50.0);

        // Weekend prices should be higher than weekday prices at same occupancy
        BigDecimal weekdaySuite = weekdayReport.getRecommendations().get(2).getRecommendedPrice();
        BigDecimal weekendSuite = weekendReport.getRecommendations().get(2).getRecommendedPrice();

        assertTrue("Weekend suite rate should exceed weekday rate", weekendSuite.compareTo(weekdaySuite) > 0);
    }
}
