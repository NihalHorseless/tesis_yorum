package org.example.tesis_yorum.repository;

import org.example.tesis_yorum.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {

    @Query("SELECT f FROM Facility f WHERE (LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Facility> searchByNameOrDescription(@Param("keyword") String keyword);

}
