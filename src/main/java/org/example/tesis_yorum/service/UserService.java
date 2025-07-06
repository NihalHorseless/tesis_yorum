package org.example.tesis_yorum.service;

import org.example.tesis_yorum.entity.User;
import org.example.tesis_yorum.entity.UserRole;
import org.example.tesis_yorum.exceptions.ResourceNotFoundException;
import org.example.tesis_yorum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        validateUserForCreation(user);
        return userRepository.save(user);
    }

    public User createRegularUser(String username, String email, String fullName) {
        User user = new User(username, email, fullName, UserRole.USER);
        return createUser(user);
    }

    public User createAdminUser(String username, String email, String fullName) {
        User user = new User(username, email, fullName, UserRole.ADMIN);
        return createUser(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = getUserById(id);

        // Check for username conflicts (if changed)
        if (!existingUser.getUsername().equals(updatedUser.getUsername())) {
            existingUser.setUsername(updatedUser.getUsername());
        }

        // Check for email conflicts (if changed)
        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new IllegalArgumentException ("Email already exists: " + updatedUser.getEmail());
            }
            existingUser.setEmail(updatedUser.getEmail());
        }

        existingUser.setFullName(updatedUser.getFullName());
        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }


    /**
     * Check if user is admin
     */
    @Transactional(readOnly = true)
    public boolean isAdmin(Long userId) {
        User user = getUserById(userId);
        return user.getRole() == UserRole.ADMIN;
    }

    private void validateUserForCreation(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException ("Username already exists: " + user.getUsername());
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException ("Email already exists: " + user.getEmail());
        }
    }
}
