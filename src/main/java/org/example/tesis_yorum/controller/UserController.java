package org.example.tesis_yorum.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.tesis_yorum.entity.User;
import org.example.tesis_yorum.entity.UserRole;
import org.example.tesis_yorum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Tag(name = "Kullanıcılar", description = "Kullanıcı işlemleri")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @Operation(
            summary = "Yeni kullanıcı oluştur",
            description = "Yeni kullanıcı oluşturur.")
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = new User(request.getUsername(), request.getEmail(), request.getFullName());
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }


    @Operation(
            summary = "Bütün kullanıcıları göster",
            description = "Bütün kullanıcıları gösterir.")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Kullanıcıları ID'ye göre göster",
            description = "Kullanıcıları girilen ID'ye göre gösterir.")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Kullanıcıları kullanıcı ismine göre göster",
            description = "Kullanıcıları girilen kullanıcı ismine göre gösterir.")
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @Valid @RequestBody UpdateUserRequest request) {
        User userToUpdate = new User();
        userToUpdate.setUsername(request.getUsername());
        userToUpdate.setEmail(request.getEmail());
        userToUpdate.setFullName(request.getFullName());

        User updatedUser = userService.updateUser(id, userToUpdate);
        return ResponseEntity.ok(updatedUser);
    }


    // Request DTOs
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String fullName;
        private UserRole role = UserRole.USER;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }
    }

    public static class UpdateUserRequest {
        private String username;
        private String email;
        private String fullName;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }

}
