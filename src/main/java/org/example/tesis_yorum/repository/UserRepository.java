package org.example.tesis_yorum.repository;

import org.example.tesis_yorum.entity.User;
import org.example.tesis_yorum.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

}