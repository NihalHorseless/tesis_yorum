package org.example.tesis_yorum.service;

import org.example.tesis_yorum.entity.Facility;
import org.example.tesis_yorum.entity.FacilityType;
import org.example.tesis_yorum.exceptions.ResourceNotFoundException;
import org.example.tesis_yorum.repository.FacilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacilityService {

    private final FacilityRepository facilityRepository;

    @Autowired
    public FacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    /**
     * Create a new facility
     */
    public Facility createFacility(Facility facility) {
        if (facility == null) {
            throw new IllegalArgumentException("Facility cannot be null");
        }
        return facilityRepository.save(facility);
    }

    /**
     * Create a facility with basic info
     */
    public Facility createFacility(String name, FacilityType type, String description, String address, String city) {
        Facility facility = new Facility(name, description, address, city, type);
        return createFacility(facility);
    }

    /**
     * Get facility by ID
     */
    @Transactional(readOnly = true)
    public Facility getFacilityById(Long id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with id: " + id));
    }

    /**
     * Get all facilities
     */
    @Transactional(readOnly = true)
    public List<Facility> getAllFacilities() {
        return facilityRepository.findAll();
    }

    /**
     * Get all active facilities
     */
    @Transactional(readOnly = true)
    public List<Facility> getAllActiveFacilities() {
        return facilityRepository.findByActiveTrue();
    }

    /**
     * Get facilities by type
     */
    @Transactional(readOnly = true)
    public List<Facility> getFacilitiesByType(FacilityType type) {
        return facilityRepository.findByType(type);
    }

    /**
     * Get active facilities by type
     */
    @Transactional(readOnly = true)
    public List<Facility> getActiveFacilitiesByType(FacilityType type) {
        return facilityRepository.findByTypeAndActive(type, true);
    }

    /**
     * Get facilities by city
     */
    @Transactional(readOnly = true)
    public List<Facility> getFacilitiesByCity(String city) {
        return facilityRepository.findByCity(city);
    }

    /**
     * Get active facilities by city
     */
    @Transactional(readOnly = true)
    public List<Facility> getActiveFacilitiesByCity(String city) {
        return facilityRepository.findByCityAndActive(city, true);
    }

    /**
     * Search facilities by name
     */
    @Transactional(readOnly = true)
    public List<Facility> searchFacilitiesByName(String name) {
        return facilityRepository.searchByName(name);
    }

    /**
     * Search facilities by keyword (name or description)
     */
    @Transactional(readOnly = true)
    public List<Facility> searchFacilities(String keyword) {
        return facilityRepository.searchByNameOrDescription(keyword);
    }

    /**
     * Get facilities ordered by review count
     */
    @Transactional(readOnly = true)
    public List<Facility> getFacilitiesOrderedByReviewCount() {
        return facilityRepository.findFacilitiesOrderByApprovedReviewCount();
    }

    /**
     * Get facilities with average ratings
     */
    @Transactional(readOnly = true)
    public List<FacilityWithRating> getFacilitiesWithAverageRating() {
        List<Object[]> results = facilityRepository.findFacilitiesWithAverageRating();
        return results.stream()
                .map(result -> new FacilityWithRating(
                        (Facility) result[0],
                        result[1] != null ? ((Number) result[1]).doubleValue() : 0.0
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get facilities by type with statistics
     */
    @Transactional(readOnly = true)
    public List<FacilityWithStats> getFacilitiesByTypeWithStats(FacilityType type) {
        List<Object[]> results = facilityRepository.findFacilitiesByTypeWithStats(type);
        return results.stream()
                .map(result -> new FacilityWithStats(
                        (Facility) result[0],
                        ((Number) result[1]).longValue(),
                        result[2] != null ? ((Number) result[2]).doubleValue() : 0.0
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get distinct cities
     */
    @Transactional(readOnly = true)
    public List<String> getDistinctCities() {
        return facilityRepository.findDistinctCities();
    }

    /**
     * Get facility count by type
     */
    @Transactional(readOnly = true)
    public Map<FacilityType, Long> getFacilityCountByType() {
        List<Object[]> results = facilityRepository.countFacilitiesByType();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (FacilityType) result[0],
                        result -> ((Number) result[1]).longValue()
                ));
    }

    /**
     * Update facility
     */
    public Facility updateFacility(Long id, Facility updatedFacility) {
        Facility existingFacility = getFacilityById(id);

        existingFacility.setName(updatedFacility.getName());
        existingFacility.setDescription(updatedFacility.getDescription());
        existingFacility.setAddress(updatedFacility.getAddress());
        existingFacility.setCity(updatedFacility.getCity());
        existingFacility.setType(updatedFacility.getType());

        return facilityRepository.save(existingFacility);
    }

    /**
     * Activate facility
     */
    public Facility activateFacility(Long id) {
        Facility facility = getFacilityById(id);
        facility.setActive(true);
        return facilityRepository.save(facility);
    }

    /**
     * Deactivate facility
     */
    public Facility deactivateFacility(Long id) {
        Facility facility = getFacilityById(id);
        facility.setActive(false);
        return facilityRepository.save(facility);
    }

    /**
     * Delete facility (hard delete)
     */
    public void deleteFacility(Long id) {
        Facility facility = getFacilityById(id);
        facilityRepository.delete(facility);
    }

    /**
     * Check if facility exists
     */
    @Transactional(readOnly = true)
    public boolean facilityExists(Long id) {
        return facilityRepository.existsById(id);
    }

    /**
     * Check if facility is active
     */
    @Transactional(readOnly = true)
    public boolean isFacilityActive(Long id) {
        Facility facility = getFacilityById(id);
        return facility.getActive();
    }

    // Helper classes for complex return types
    public static class FacilityWithRating {
        private final Facility facility;
        private final Double averageRating;

        public FacilityWithRating(Facility facility, Double averageRating) {
            this.facility = facility;
            this.averageRating = averageRating;
        }

        public Facility getFacility() { return facility; }
        public Double getAverageRating() { return averageRating; }
    }

    public static class FacilityWithStats {
        private final Facility facility;
        private final Long reviewCount;
        private final Double averageRating;

        public FacilityWithStats(Facility facility, Long reviewCount, Double averageRating) {
            this.facility = facility;
            this.reviewCount = reviewCount;
            this.averageRating = averageRating;
        }

        public Facility getFacility() { return facility; }
        public Long getReviewCount() { return reviewCount; }
        public Double getAverageRating() { return averageRating; }
    }
}
