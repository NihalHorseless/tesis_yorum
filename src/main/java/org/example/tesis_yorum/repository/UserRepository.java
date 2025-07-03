package org.example.tesis_yorum.repository;

import org.example.tesis_yorum.entity.User;
import org.example.tesis_yorum.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username or email
     */
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find users by role
     */
    List<User> findByRole(UserRole role);

    /**
     * Find all admin users
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN'")
    List<User> findAllAdmins();

    /**
     * Find all regular users
     */
    @Query("SELECT u FROM User u WHERE u.role = 'USER'")
    List<User> findAllRegularUsers();

    /**
     * Find users with reviews count
     */
    @Query("SELECT u FROM User u LEFT JOIN u.reviews r GROUP BY u.id ORDER BY COUNT(r) DESC")
    List<User> findUsersOrderByReviewCount();

    /**
     * Find users who have created reviews in a specific facility
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.reviews r WHERE r.facility.id = :facilityId")
    List<User> findUsersByFacilityId(@Param("facilityId") Long facilityId);
}