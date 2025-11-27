package com.travelmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

/**
 * Controller for the help page
 */
public class HelpController {

    @FXML
    private TextArea helpText;

    @FXML
    public void initialize() {
        String helpContent = """
                ═══════════════════════════════════════════════════════════
                     SMART MULTI-MODAL TRAVEL SCHEDULE MANAGER
                                    USER GUIDE
                ═══════════════════════════════════════════════════════════
                
                WELCOME!
                This application helps you plan long-distance journeys using
                buses and trains. You can create custom multi-leg trips and
                save them for future reference.
                
                ═══════════════════════════════════════════════════════════
                
                HOME PAGE - THREE MAIN SECTIONS:
                
                1. CREATE PLAN
                   - Design your custom travel itinerary step by step
                   
                2. SAVED PLANS
                   - View and manage your previously saved travel plans
                   
                3. AUTOMATIC ROUTE
                   - Let the system find optimal routes for you
                
                ═══════════════════════════════════════════════════════════
                
                HOW TO CREATE A PLAN:
                
                Step 1: Enter Journey Details
                   • Start Location: Where you're beginning your journey
                   • Destination: Where you want to go for this leg
                   • Date: When you plan to travel
                   • Click "Search" to find available schedules
                
                Step 2: Browse Available Schedules
                   • View all buses and trains for your route
                   • Schedules are sorted by departure time
                   • See details: company/train name, times, fares, seats
                
                Step 3: Add to Your Plan
                   • Select a schedule from the table
                   • Click "Add to Plan" to add it to your itinerary
                   • The destination becomes your new starting point
                   • Add more legs as needed for multi-stop journeys
                
                Step 4: Summarize Your Plan
                   • Click "Summarize" when you're done adding legs
                   • Review complete journey details
                   • See total fare calculation
                
                Step 5: Save Your Plan
                   • Enter a memorable name for your plan
                   • Click "Save Plan" to store it
                   • Access it later from "Saved Plans"
                
                ═══════════════════════════════════════════════════════════
                
                TIPS FOR BEST RESULTS:
                
                • Plan Multi-Stop Journeys: Add multiple legs to create
                  complex itineraries (e.g., Dhaka → Chittagong → Cox's Bazar)
                
                • Compare Options: Review different bus and train schedules
                  to find the best timing and prices
                
                • Save Multiple Plans: Create different plans for different
                  travel dates or route preferences
                
                • Check Availability: Note the available seats before booking
                
                • Reset Anytime: Use the "Reset" button to start over
                
                ═══════════════════════════════════════════════════════════
                
                UNDERSTANDING THE SCHEDULE TABLE:
                
                • Type: Bus or Train
                • Name: Company name (buses) or train name (trains)
                • Origin: Starting location
                • Destination: Ending location
                • Departure: Date and time of departure
                • Arrival: Date and time of arrival
                • Fare: Cost in BDT (৳)
                • Seats: Number of available seats
                
                ═══════════════════════════════════════════════════════════
                
                NEED MORE HELP?
                
                If you encounter any issues or have questions, please
                refer to this guide or contact support.
                
                Happy Travels! 🚌 🚂
                
                ═══════════════════════════════════════════════════════════
                """;
        
        helpText.setText(helpContent);
    }
}
