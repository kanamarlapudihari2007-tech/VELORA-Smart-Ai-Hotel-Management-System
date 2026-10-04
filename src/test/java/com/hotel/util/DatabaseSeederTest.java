package com.hotel.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * DatabaseSeederTest - Verifies the 1-click demo database seeder for Stage 20.
 */
public class DatabaseSeederTest {

    @Test
    public void testResetAndSeedDemoData() {
        boolean success = DatabaseSeeder.resetAndSeedDemoData();
        assertTrue("DatabaseSeeder should return true on successful demo data reset", success);
    }
}
