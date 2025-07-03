package org.example.tesis_yorum;

import org.example.tesis_yorum.entity.Facility;
import org.example.tesis_yorum.entity.User;
import org.example.tesis_yorum.entity.FacilityType;
import org.example.tesis_yorum.service.FacilityService;
import org.example.tesis_yorum.service.ReviewService;
import org.example.tesis_yorum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TesisYorumApplication implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private ReviewService reviewService;

    public static void main(String[] args) {
        SpringApplication.run(TesisYorumApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Create some test data when application starts
        initializeTestData();
    }

    private void initializeTestData() {
        try {
            // Create users
            User regularUser = userService.createRegularUser("zeynep_sonmez", "zeynep_sonmez34@hotmail.com", "Zeynep Sonmez");
            User admin = userService.createAdminUser("admin", "admin07@hotmail.com", "Admin");

            // Create facilities
            Facility hotel = facilityService.createFacility(
                    "Royal Tsar Belek",
                    FacilityType.HOTEL,
                    "5 Yıldızlı all-inclusive aile oteli",
                    "Belek, Serik/Antalya",
                    "Antalya"
            );

            Facility restaurant = facilityService.createFacility(
                    "Five Guys",
                    FacilityType.RESTAURANT,
                    "New York merkezli Hamburger zinciri",
                    "Levent",
                    "Istanbul"
            );

            // Create some reviews
            reviewService.createReview(
                    regularUser.getId(),
                    hotel.getId(),
                    "Muazzam hizmet, Muazzam kalite",
                    5
            );

            reviewService.createReview(
                    regularUser.getId(),
                    restaurant.getId(),
                    "Fiyatlar biraz tuzlu ama lezzetli",
                    4
            );

            System.out.println("✅ Test data initialized successfully!");
            System.out.println("📊 Created: 2 users, 2 facilities, 2 reviews");

        } catch (Exception e) {
            System.out.println("⚠️  Test data initialization failed: " + e.getMessage());
        }
    }
}
