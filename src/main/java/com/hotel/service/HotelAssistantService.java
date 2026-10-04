package com.hotel.service;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.ComplaintDAO;
import com.hotel.dao.ServiceRequestDAO;
import com.hotel.model.Booking;
import com.hotel.model.ChatMessage;
import com.hotel.model.Complaint;
import com.hotel.model.ServiceRequest;
import com.hotel.model.User;

import java.util.List;

/**
 * HotelAssistantService - Stage 16 AI Hotel Assistant & 24/7 Virtual Concierge ("Aura").
 * Combines Google Gemini generative capabilities with live database contextual grounding
 * (room reservation status, active requests, hotel amenities, timings, and policies).
 */
public class HotelAssistantService {

    private final GeminiService geminiService;
    private final BookingDAO bookingDAO;
    private final ServiceRequestDAO serviceRequestDAO;
    private final ComplaintDAO complaintDAO;

    public HotelAssistantService() {
        this.geminiService = new GeminiService();
        this.bookingDAO = new BookingDAO();
        this.serviceRequestDAO = new ServiceRequestDAO();
        this.complaintDAO = new ComplaintDAO();
    }

    public HotelAssistantService(GeminiService geminiService, BookingDAO bookingDAO,
                                 ServiceRequestDAO serviceRequestDAO, ComplaintDAO complaintDAO) {
        this.geminiService = (geminiService != null) ? geminiService : new GeminiService();
        this.bookingDAO = (bookingDAO != null) ? bookingDAO : new BookingDAO();
        this.serviceRequestDAO = (serviceRequestDAO != null) ? serviceRequestDAO : new ServiceRequestDAO();
        this.complaintDAO = (complaintDAO != null) ? complaintDAO : new ComplaintDAO();
    }

    /**
     * Processes a user question and generates an empathetic, contextually aware response.
     *
     * @param userMessage Question or query submitted by user
     * @param user Current logged-in user (optional / can be null for visitors)
     * @param history Previous chat turns in the current session
     * @return ChatMessage containing assistant reply and metadata
     */
    // Fast in-memory cache for recent responses to provide instantaneous replies on identical questions
    private static final java.util.Map<String, String> FAST_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public ChatMessage processQuery(String userMessage, User user, List<ChatMessage> history) {
        String cleanQuery = (userMessage != null) ? userMessage.trim() : "";
        if (cleanQuery.isEmpty()) {
            return new ChatMessage("assistant", "Hello! How may I assist you with your stay at AI Smart Hotel today? 🛎️", false);
        }

        // Fast-path cache lookup for instantaneous 0ms response
        String cacheKey = (user != null ? "u" + user.getId() : "anon") + ":" + cleanQuery.toLowerCase();
        if (FAST_CACHE.containsKey(cacheKey)) {
            return new ChatMessage("assistant", FAST_CACHE.get(cacheKey), true);
        }

        // Build live database context (current room, reservation, active requests)
        String liveContext = buildLiveContext(user);

        // 1. Attempt AI response via Google Gemini API
        if (geminiService.isApiKeyConfigured()) {
            try {
                String systemInstruction = "You are \"Aura\", the intelligent 5-star AI Virtual Concierge for AI Smart Hotel.\n"
                        + "Tone: Warm, luxurious, helpful, and concise (strictly 2 to 3 sentences).\n"
                        + "Hotel Knowledge:\n"
                        + "- Check-in 2:00 PM, Check-out 11:00 AM (late checkout until 1:00 PM upon request).\n"
                        + "- Breakfast 7:00-10:30 AM (Grand Dining Room, 2nd fl). Room Service 24/7 (dial 9).\n"
                        + "- Rooftop Infinity Pool 6:00 AM-10:00 PM (5th fl). Gym 24/7 (3rd fl). Lotus Spa 9:00 AM-9:00 PM (4th fl).\n"
                        + "- Wi-Fi: \"SmartHotel_Guest_HighSpeed\", password \"welcome_guest\". Front Desk: dial 0.\n"
                        + "- Complimentary secure underground parking & 24/7 valet service.\n"
                        + "- Room Types: Standard Single $1200, Deluxe Double $2500, Executive Suite $5000.\n"
                        + "Live Context:\n" + liveContext;

                StringBuilder promptBuilder = new StringBuilder();
                if (history != null && !history.isEmpty()) {
                    promptBuilder.append("Recent Conversation:\n");
                    int start = Math.max(0, history.size() - 4);
                    for (int i = start; i < history.size(); i++) {
                        ChatMessage m = history.get(i);
                        promptBuilder.append(m.getSender().equalsIgnoreCase("user") ? "Guest: " : "Aura: ")
                                     .append(m.getMessage()).append("\n");
                    }
                    promptBuilder.append("\n");
                }
                promptBuilder.append("Current Guest Message: ").append(cleanQuery);

                String reply = geminiService.callGenerateContent(systemInstruction, promptBuilder.toString());
                if (reply != null && !reply.trim().isEmpty()) {
                    if (FAST_CACHE.size() > 200) {
                        FAST_CACHE.clear();
                    }
                    FAST_CACHE.put(cacheKey, reply.trim());
                    return new ChatMessage("assistant", reply.trim(), true);
                }
            } catch (Exception e) {
                System.err.println("[HotelAssistantService] Gemini error: " + e.getMessage() + ". Using fallback concierge.");
            }
        }

        // 2. Deterministic Rule-Based Concierge Fallback
        String fallbackReply = buildFallbackResponse(cleanQuery, user);
        return new ChatMessage("assistant", fallbackReply, false);
    }

    /**
     * Gathers live context from the database for the active user.
     */
    public String buildLiveContext(User user) {
        if (user == null) {
            return "User is currently an unauthenticated visitor browsing the hotel portal.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Guest Name: ").append(user.getUsername()).append(" (Email: ").append(user.getEmail()).append(")\n");

        if ("GUEST".equalsIgnoreCase(user.getRole())) {
            try {
                int guestId = bookingDAO.getGuestIdByUserId(user.getId());
                Booking activeBooking = bookingDAO.getActiveBookingForGuest(guestId);

                if (activeBooking != null) {
                    sb.append("Active Reservation: Room ").append(activeBooking.getRoomNumber())
                      .append(" (").append(activeBooking.getTypeName()).append("), Booking Code: ")
                      .append(activeBooking.getBookingCode())
                      .append(", Check-in: ").append(activeBooking.getCheckInDate())
                      .append(", Check-out: ").append(activeBooking.getCheckOutDate())
                      .append(", Status: ").append(activeBooking.getStatus()).append("\n");

                    // Recent Service Requests
                    List<ServiceRequest> srs = serviceRequestDAO.getRequestsByGuestId(guestId);
                    if (srs != null && !srs.isEmpty()) {
                        sb.append("Recent Service Requests:\n");
                        int count = Math.min(3, srs.size());
                        for (int i = 0; i < count; i++) {
                            ServiceRequest sr = srs.get(i);
                            sb.append("- #SR-").append(sr.getId()).append(" [").append(sr.getRequestType()).append("] ")
                              .append(sr.getExtractedItems()).append(" (Status: ").append(sr.getStatus()).append(")\n");
                        }
                    }

                    // Recent Complaints
                    List<Complaint> cmps = complaintDAO.getComplaintsByGuestId(guestId);
                    if (cmps != null && !cmps.isEmpty()) {
                        sb.append("Recent Complaints:\n");
                        int count = Math.min(3, cmps.size());
                        for (int i = 0; i < count; i++) {
                            Complaint c = cmps.get(i);
                            sb.append("- #CMP-").append(c.getId()).append(" [").append(c.getCategory()).append("] ")
                              .append(c.getShortDescription()).append(" (Status: ").append(c.getStatus()).append(")\n");
                        }
                    }
                } else {
                    sb.append("Active Reservation: None found. Guest is not currently checked-in.\n");
                }
            } catch (Exception e) {
                sb.append("Active Reservation lookup error: ").append(e.getMessage()).append("\n");
            }
        } else {
            sb.append("User is a hotel staff/manager (Role: ").append(user.getRole()).append(").\n");
        }

        return sb.toString();
    }

    /**
     * Deterministic concierge rules answering standard and custom hotel FAQs, room requests, and live DB status.
     */
    public String buildFallbackResponse(String query, User user) {
        String lower = (query != null) ? query.toLowerCase().trim() : "";

        // 1. Live Room & Booking Status
        if (lower.contains("my room") || lower.contains("my booking") || lower.contains("my stay") ||
            lower.contains("my reservation") || lower.contains("booking code") || lower.contains("booking status") ||
            lower.contains("check out date") || lower.contains("checkout date")) {
            if (user != null && "GUEST".equalsIgnoreCase(user.getRole())) {
                try {
                    int guestId = bookingDAO.getGuestIdByUserId(user.getId());
                    Booking activeBooking = bookingDAO.getActiveBookingForGuest(guestId);
                    if (activeBooking != null) {
                        return "🛎️ You have an active reservation in Room " + activeBooking.getRoomNumber()
                                + " (" + activeBooking.getTypeName() + ") under Booking Code " + activeBooking.getBookingCode()
                                + ". Check-in: " + activeBooking.getCheckInDate() + ", Check-out: " + activeBooking.getCheckOutDate()
                                + ". Status: " + activeBooking.getStatus() + ".";
                    }
                } catch (Exception ignored) {}
            }
            return "🛎️ You don't have an active checked-in room right now. You can easily view available rooms and make a reservation through our 'Book Room' page!";
        }

        // 2. Breakfast & Morning Dining
        if (lower.contains("breakfast") || lower.contains("morning meal") || lower.contains("brunch")) {
            return "🍳 Breakfast is served daily from 7:00 AM to 10:30 AM at the Grand Dining Room on the 2nd Floor. Complimentary buffet is included with all Executive Suite bookings, and room delivery is available via Room Service!";
        }

        // 3. Lunch, Dinner, Dining & Room Service
        if (lower.contains("food") || lower.contains("dinner") || lower.contains("lunch") || lower.contains("dining") ||
            lower.contains("restaurant") || lower.contains("room service") || lower.contains("menu") || lower.contains("eat") ||
            lower.contains("order food") || lower.contains("snack")) {
            return "🍽️ Our Grand Dining Room serves Lunch from 12:30 PM – 3:00 PM and Dinner from 7:00 PM – 11:00 PM. In addition, 24/7 in-room dining is available anytime through our 'Room Services' portal or by dialing 9 from your room phone!";
        }

        // 4. Beverages, Coffee, Tea & Water
        if (lower.contains("coffee") || lower.contains("tea") || lower.contains("water") || lower.contains("drink") ||
            lower.contains("mineral water") || lower.contains("kettle") || lower.contains("ice")) {
            return "☕ Complimentary bottled water and coffee/tea making facilities are provided in your room and replenished daily. Need extra bottles of chilled water or fresh coffee? Simply request via our 'Room Services' portal or dial 9!";
        }

        // 5. Dietary, Vegetarian, Vegan & Halal
        if (lower.contains("veg") || lower.contains("vegan") || lower.contains("halal") || lower.contains("gluten") || lower.contains("allergy")) {
            return "🥗 We cater extensively to dietary preferences! All our dining venues and 24/7 room service offer certified Vegetarian, Vegan, Halal, and Gluten-Free menus. Please notify our culinary team when placing your order.";
        }

        // 6. Bar, Lounge & Alcohol
        if (lower.contains("bar") || lower.contains("cocktail") || lower.contains("wine") || lower.contains("beer") ||
            lower.contains("alcohol") || lower.contains("lounge") || lower.contains("pub")) {
            return "🍸 The Sapphire Lounge on the Ground Floor offers artisanal cocktails, vintage wines, and light bites daily from 5:00 PM to 1:00 AM.";
        }

        // 7. Swimming Pool
        if (lower.contains("pool") || lower.contains("swim") || lower.contains("jacuzzi")) {
            return "🏊 Our rooftop infinity swimming pool is located on the 5th floor, open daily from 6:00 AM to 10:00 PM. Complimentary heated towels and poolside refreshments are provided.";
        }

        // 8. Gym / Fitness Center
        if (lower.contains("gym") || lower.contains("fitness") || lower.contains("workout") || lower.contains("treadmill") || lower.contains("weights")) {
            return "🏋️ The Fitness Center is located on the 3rd floor and is accessible 24/7 with your room keycard. It features state-of-the-art cardio machines, free weights, and complimentary water.";
        }

        // 9. Spa & Wellness
        if (lower.contains("spa") || lower.contains("massage") || lower.contains("sauna") || lower.contains("steam") || lower.contains("facial")) {
            return "✨ The Lotus Spa & Wellness center is on the 4th floor, open from 9:00 AM to 9:00 PM. Dial 0 or visit the spa reception to book holistic massages, facials, and sauna sessions.";
        }

        // 10. Wi-Fi & Internet
        if (lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("internet") || lower.contains("network") || lower.contains("password")) {
            return "📶 Connect to complimentary high-speed Wi-Fi network \"SmartHotel_Guest_HighSpeed\" with password \"welcome_guest\". Seamless coverage is available throughout all guest rooms and hotel amenities.";
        }

        // 11. Towels, Toiletries & Amenities (Housekeeping items)
        if (lower.contains("towel") || lower.contains("soap") || lower.contains("shampoo") || lower.contains("dental") ||
            lower.contains("toothbrush") || lower.contains("toothpaste") || lower.contains("comb") || lower.contains("shaving") ||
            lower.contains("slipper") || lower.contains("bathrobe") || lower.contains("toiletr")) {
            return "🧴 We provide complimentary luxury toiletries, plush bath towels, dental kits, and slippers! You can request instant replenishment through our 'Room Services' page or dial 0, and Housekeeping will deliver them in 10-15 minutes.";
        }

        // 12. Linens, Pillows, Blankets, Iron & Appliances
        if (lower.contains("pillow") || lower.contains("blanket") || lower.contains("sheet") || lower.contains("bedsheet") ||
            lower.contains("iron") || lower.contains("ironing") || lower.contains("hairdryer") || lower.contains("adapter") || lower.contains("charger")) {
            return "🛏️ Extra feather/memory-foam pillows, warm blankets, steam irons, ironing boards, and international plug adapters are readily available. Place a request via 'Room Services' or dial 0 for swift delivery.";
        }

        // 13. Room Cleaning & Housekeeping
        if (lower.contains("clean") || lower.contains("housekeeping") || lower.contains("trash") || lower.contains("make up room") || lower.contains("dirty")) {
            return "🧹 Daily housekeeping service runs between 8:00 AM and 8:00 PM. To schedule immediate room cleaning or fresh linen change, please use the 'Room Services' portal or press the 'Make Up Room' indicator by your room door.";
        }

        // 14. Laundry & Dry Cleaning
        if (lower.contains("laundry") || lower.contains("dry clean") || lower.contains("wash clothes") || lower.contains("press")) {
            return "🧺 Same-day laundry and dry cleaning services are available! Please place garments in the laundry bag inside your wardrobe and notify Front Desk before 10:00 AM for return by 6:00 PM.";
        }

        // 15. Check-in & Check-out Policies
        if (lower.contains("checkout") || lower.contains("check-out") || lower.contains("check out") ||
            lower.contains("checkin") || lower.contains("check-in") || lower.contains("check in") ||
            lower.contains("late checkout") || lower.contains("early checkin")) {
            return "🚪 Standard check-in is at 2:00 PM and check-out is at 11:00 AM. Complimentary late check-out until 1:00 PM is available upon request at the Front Desk, subject to availability.";
        }

        // 16. Luggage & Baggage Storage
        if (lower.contains("luggage") || lower.contains("baggage") || lower.contains("store my bag") || lower.contains("holding bags") || lower.contains("bellboy")) {
            return "🧳 Complimentary secure luggage storage is available 24/7 at the Concierge desk in the Main Lobby. Our bell team will happily safeguard your bags before check-in or after check-out.";
        }

        // 17. Room Keys & Access
        if (lower.contains("key") || lower.contains("keycard") || lower.contains("locked out") || lower.contains("card not working")) {
            return "🔑 If your keycard is malfunctioning or misplaced, please visit the Front Desk with a valid photo ID. Our reception team will instantly encode a new secure keycard for you.";
        }

        // 18. Parking & Valet
        if (lower.contains("park") || lower.contains("valet") || lower.contains("car park") || lower.contains("garage")) {
            return "🚗 Complimentary secure underground parking and 24/7 valet service are available for all registered guests at the main hotel entrance portico.";
        }

        // 19. Airport Transfer, Taxis & Transportation
        if (lower.contains("airport") || lower.contains("shuttle") || lower.contains("taxi") || lower.contains("cab") || lower.contains("uber") || lower.contains("transport")) {
            return "🚕 Private airport transfers can be scheduled at the Concierge desk for $45. We can also hail an authorized city taxi or assist with on-demand ride shares within 5 minutes. Dial 0 to book!";
        }

        // 20. Room Rates & Pricing
        if (lower.contains("price") || lower.contains("rate") || lower.contains("cost") || lower.contains("how much") || lower.contains("tariff") || lower.contains("suite")) {
            return "🏨 Our premier accommodations include: Standard Single ($1,200/night), Deluxe Double ($2,500/night), and Executive Suite ($5,000/night with VIP lounge access & breakfast). View live availability under 'Book Room'!";
        }

        // 21. Air Conditioning, TV & In-Room Controls
        if (lower.contains("ac") || lower.contains("air condition") || lower.contains("temperature") || lower.contains("heating") ||
            lower.contains("tv") || lower.contains("remote") || lower.contains("channel")) {
            return "📺 Your room features individual climate control on the wall panel and an Ultra-HD Smart TV with streaming apps and premium channels. Dial 0 if you need technical assistance or battery replacements.";
        }

        // 22. Medical, Emergency & First Aid
        if (lower.contains("doctor") || lower.contains("medical") || lower.contains("medicine") || lower.contains("first aid") ||
            lower.contains("pharmacy") || lower.contains("hospital") || lower.contains("emergency") || lower.contains("sick")) {
            return "🚑 A first-aid kit is available at the Front Desk, and a registered doctor is on-call 24/7. Dial 0 immediately for any urgent medical care or assistance with nearest 24-hour pharmacies.";
        }

        // 23. Smoking & Pet Policies
        if (lower.contains("smok") || lower.contains("cigarette") || lower.contains("vape")) {
            return "🚭 AI Smart Hotel is a 100% smoke-free property. Designated outdoor smoking pavilions are provided in the Ground Floor garden terrace.";
        }
        if (lower.contains("pet") || lower.contains("dog") || lower.contains("cat") || lower.contains("animal")) {
            return "🐾 Service animals are warmly welcomed throughout the property. Dedicated pet-friendly rooms on the 1st floor are available upon prior reservation.";
        }

        // 24. Front Desk & Reception Contact
        if (lower.contains("front desk") || lower.contains("reception") || lower.contains("phone") || lower.contains("call") ||
            lower.contains("contact") || lower.contains("operator") || lower.contains("manager")) {
            return "📞 You can reach the Front Desk 24/7 by dialing 0 from your in-room telephone or visiting the lobby on the Ground Floor. For Room Service, dial 9!";
        }

        // 25. Location & Local Recommendations
        if (lower.contains("location") || lower.contains("address") || lower.contains("where are you") || lower.contains("direction") ||
            lower.contains("attraction") || lower.contains("shopping") || lower.contains("mall") || lower.contains("sightseeing")) {
            return "📍 AI Smart Hotel is located in the heart of Downtown Central, within walking distance of premier shopping districts, museums, and waterfront promenades. Visit our Concierge desk in the lobby for curated local tours!";
        }

        // 26. Intelligent Handler for Custom Delivery / Ordering Requests
        if (lower.contains("bring") || lower.contains("send") || lower.contains("need") || lower.contains("get") ||
            lower.contains("order") || lower.contains("deliver") || lower.contains("request") || lower.contains("want")) {
            return "🛎️ I would be delighted to assist with your request! You can dispatch this directly to our hotel staff through our 'Room Services' or 'Complaints' portal, or simply dial 9 (Room Service) / 0 (Front Desk) from your room phone for immediate delivery within 10-15 minutes!";
        }

        // 27. Universal Courteous 5-Star Hotel Guidance for any other custom question
        return "✨ Thank you for reaching out! Our 24/7 Concierge and Front Desk team is at your complete service. Please dial 0 from your in-room telephone or visit the Main Lobby on the Ground Floor, and our team will be delighted to take care of this for you immediately!";
    }
}
