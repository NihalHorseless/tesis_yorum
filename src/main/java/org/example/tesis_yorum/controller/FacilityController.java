package org.example.tesis_yorum.controller;

import jakarta.validation.Valid;
import org.example.tesis_yorum.entity.Facility;
import org.example.tesis_yorum.entity.FacilityType;
import org.example.tesis_yorum.service.FacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facilities")
@CrossOrigin(origins = "*")
public class FacilityController {

    private final FacilityService facilityService;

    @Autowired
    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    /**
     * Create a new facility
     * POST /api/facilities
     */
    @PostMapping
    public ResponseEntity<Facility> createFacility(@Valid @RequestBody CreateFacilityRequest request) {
        Facility facility = new Facility(
                request.getName(),
                request.getDescription(),
                request.getAddress(),
                request.getCity(),
                request.getType()
        );

        Facility createdFacility = facilityService.createFacility(facility);
        return new ResponseEntity<>(createdFacility, HttpStatus.CREATED);
    }

    /**
     * Get all facilities
     * GET /api/facilities
     */
    @GetMapping
    public ResponseEntity<List<Facility>> getAllFacilities(@RequestParam(defaultValue = "false") boolean activeOnly) {
        List<Facility> facilities = activeOnly ?
                facilityService.getAllActiveFacilities() :
                facilityService.getAllFacilities();
        return ResponseEntity.ok(facilities);
    }

    /**
     * Get facility by ID
     * GET /api/facilities/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Facility> getFacilityById(@PathVariable Long id) {
        Facility facility = facilityService.getFacilityById(id);
        return ResponseEntity.ok(facility);
    }

    /**
     * Get facilities by type
     * GET /api/facilities/type/{type}
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Facility>> getFacilitiesByType(@PathVariable FacilityType type,
                                                              @RequestParam(defaultValue = "false") boolean activeOnly) {
        List<Facility> facilities = activeOnly ?
                facilityService.getActiveFacilitiesByType(type) :
                facilityService.getFacilitiesByType(type);
        return ResponseEntity.ok(facilities);
    }

    /**
     * Get facilities by city
     * GET /api/facilities/city/{city}
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<Facility>> getFacilitiesByCity(@PathVariable String city,
                                                              @RequestParam(defaultValue = "false") boolean activeOnly) {
        List<Facility> facilities = activeOnly ?
                facilityService.getActiveFacilitiesByCity(city) :
                facilityService.getFacilitiesByCity(city);
        return ResponseEntity.ok(facilities);
    }

    /**
     * Search facilities
     * GET /api/facilities/search?q={keyword}
     */
    @GetMapping("/search")
    public ResponseEntity<List<Facility>> searchFacilities(@RequestParam String q) {
        List<Facility> facilities = facilityService.searchFacilities(q);
        return ResponseEntity.ok(facilities);
    }

    /**
     * Get facilities with ratings
     * GET /api/facilities/with-ratings
     */
    @GetMapping("/with-ratings")
    public ResponseEntity<List<FacilityService.FacilityWithRating>> getFacilitiesWithRatings() {
        List<FacilityService.FacilityWithRating> facilities = facilityService.getFacilitiesWithAverageRating();
        return ResponseEntity.ok(facilities);
    }

    /**
     * Get facilities by type with statistics
     * GET /api/facilities/type/{type}/stats
     */
    @GetMapping("/type/{type}/stats")
    public ResponseEntity<List<FacilityService.FacilityWithStats>> getFacilitiesByTypeWithStats(@PathVariable FacilityType type) {
        List<FacilityService.FacilityWithStats> facilities = facilityService.getFacilitiesByTypeWithStats(type);
        return ResponseEntity.ok(facilities);
    }

    /**
     * Get facilities ordered by review count
     * GET /api/facilities/popular
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Facility>> getPopularFacilities() {
        List<Facility> facilities = facilityService.getFacilitiesOrderedByReviewCount();
        return ResponseEntity.ok(facilities);
    }

    /**
     * Get distinct cities
     * GET /api/facilities/cities
     */
    @GetMapping("/cities")
    public ResponseEntity<List<String>> getDistinctCities() {
        List<String> cities = facilityService.getDistinctCities();
        return ResponseEntity.ok(cities);
    }

    /**
     * Get facility count by type
     * GET /api/facilities/stats/by-type
     */
    @GetMapping("/stats/by-type")
    public ResponseEntity<Map<FacilityType, Long>> getFacilityCountByType() {
        Map<FacilityType, Long> stats = facilityService.getFacilityCountByType();
        return ResponseEntity.ok(stats);
    }

    /**
     * Update facility
     * PUT /api/facilities/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Facility> updateFacility(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateFacilityRequest request) {
        Facility facilityToUpdate = new Facility();
        facilityToUpdate.setName(request.getName());
        facilityToUpdate.setDescription(request.getDescription());
        facilityToUpdate.setAddress(request.getAddress());
        facilityToUpdate.setCity(request.getCity());
        facilityToUpdate.setType(request.getType());

        Facility updatedFacility = facilityService.updateFacility(id, facilityToUpdate);
        return ResponseEntity.ok(updatedFacility);
    }

    /**
     * Activate facility
     * PATCH /api/facilities/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Facility> activateFacility(@PathVariable Long id) {
        Facility facility = facilityService.activateFacility(id);
        return ResponseEntity.ok(facility);
    }

    /**
     * Deactivate facility
     * PATCH /api/facilities/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Facility> deactivateFacility(@PathVariable Long id) {
        Facility facility = facilityService.deactivateFacility(id);
        return ResponseEntity.ok(facility);
    }

    /**
     * Delete facility
     * DELETE /api/facilities/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacility(@PathVariable Long id) {
        facilityService.deleteFacility(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if facility exists
     * GET /api/facilities/{id}/exists
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> checkFacilityExists(@PathVariable Long id) {
        boolean exists = facilityService.facilityExists(id);
        return ResponseEntity.ok(exists);
    }

    // Request DTOs
    public static class CreateFacilityRequest {
        private String name;
        private String description;
        private String address;
        private String city;
        private FacilityType type;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public FacilityType getType() { return type; }
        public void setType(FacilityType type) { this.type = type; }
    }

    public static class UpdateFacilityRequest {
        private String name;
        private String description;
        private String address;
        private String city;
        private FacilityType type;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public FacilityType getType() { return type; }
        public void setType(FacilityType type) { this.type = type; }
    }
}
