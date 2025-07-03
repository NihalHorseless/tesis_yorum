package org.example.tesis_yorum.repository;

import org.example.tesis_yorum.entity.Facility;
import org.example.tesis_yorum.entity.FacilityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {

    /**
     * Find facilities by type
     */
    List<Facility> findByType(FacilityType type);

    /**
     * Find active facilities
     */
    List<Facility> findByActiveTrue();

    /**
     * Find inactive facilities
     */
    List<Facility> findByActiveFalse();

    /**
     * Find facilities by type and active status
     */
    List<Facility> findByTypeAndActive(FacilityType type, Boolean active);

    /**
     * Find facilities by city
     */
    List<Facility> findByCity(String city);

    /**
     * Find facilities by city and active status
     */
    List<Facility> findByCityAndActive(String city, Boolean active);

    /**
     * Search facilities by name (case insensitive)
     */
    @Query("SELECT f FROM Facility f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%')) AND f.active = true")
    List<Facility> searchByName(@Param("name") String name);

    /**
     * Search facilities by name or description (case insensitive)
     */
    @Query("SELECT f FROM Facility f WHERE (LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND f.active = true")
    List<Facility> searchByNameOrDescription(@Param("keyword") String keyword);

    /**
     * Find facilities with approved reviews count
     */
    @Query("SELECT f FROM Facility f LEFT JOIN f.reviews r WHERE r.status = 'APPROVED' OR r.status IS NULL " +
            "GROUP BY f.id ORDER BY COUNT(r) DESC")
    List<Facility> findFacilitiesOrderByApprovedReviewCount();

    /**
     * Find facilities with average rating
     */
    @Query("SELECT f, AVG(r.rating) as avgRating FROM Facility f LEFT JOIN f.reviews r " +
            "WHERE r.status = 'APPROVED' OR r.status IS NULL GROUP BY f.id ORDER BY avgRating DESC")
    List<Object[]> findFacilitiesWithAverageRating();

    /**
     * Find facilities by type with review statistics
     */
    @Query("SELECT f, COUNT(r), AVG(r.rating) FROM Facility f LEFT JOIN f.reviews r " +
            "WHERE f.type = :type AND f.active = true AND (r.status = 'APPROVED' OR r.status IS NULL) " +
            "GROUP BY f.id ORDER BY COUNT(r) DESC")
    List<Object[]> findFacilitiesByTypeWithStats(@Param("type") FacilityType type);

    /**
     * Find distinct cities
     */
    @Query("SELECT DISTINCT f.city FROM Facility f WHERE f.city IS NOT NULL AND f.active = true ORDER BY f.city")
    List<String> findDistinctCities();

    /**
     * Count facilities by type
     */
    @Query("SELECT f.type, COUNT(f) FROM Facility f WHERE f.active = true GROUP BY f.type")
    List<Object[]> countFacilitiesByType();
}
