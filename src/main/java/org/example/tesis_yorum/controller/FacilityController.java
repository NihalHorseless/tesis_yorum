package org.example.tesis_yorum.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.tesis_yorum.entity.Facility;
import org.example.tesis_yorum.entity.FacilityType;
import org.example.tesis_yorum.service.FacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
@CrossOrigin(origins = "*")
@Tag(name = "Tesisler", description = "Tesis işlemleri")
public class FacilityController {

    private final FacilityService facilityService;

    @Autowired
    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }


    @Operation(
            summary = "Yeni bir Tesis oluştur",
            description = "Yeni bir Tesis oluşturur.")
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


    @Operation(
            summary = "Bütün Tesisleri Göster",
            description = "Bütün Tesisleri Gösterir.")
    @GetMapping
    public ResponseEntity<List<Facility>> getAllFacilities() {
        List<Facility> facilities = facilityService.getAllFacilities();
        return ResponseEntity.ok(facilities);
    }


    @Operation(
            summary = "ID'ye göre Tesis Göster",
            description = "Girilen ID parametresine göre Tesis Gösterir.")
    @GetMapping("/{id}")
    public ResponseEntity<Facility> getFacilityById(@PathVariable Long id) {
        Facility facility = facilityService.getFacilityById(id);
        return ResponseEntity.ok(facility);
    }


    @Operation(
            summary = "Tesis İsim veya Açıklamasına göre Tesis Göster",
            description = "Girilen Tesis İsim veya Açıklaması parametrelerine göre Tesis Gösterir.")
    @GetMapping("/search")
    public ResponseEntity<List<Facility>> searchFacilities(@RequestParam String q) {
        List<Facility> facilities = facilityService.searchFacilities(q);
        return ResponseEntity.ok(facilities);
    }


    @Operation(
            summary = "Tesis Güncelle",
            description = "Girilen Tesis ID parametrelerisine göre Tesis Günceller.")
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

    @Operation(
            summary = "Tesis Sil",
            description = "Girilen Tesis ID parametrelerisine göre Tesis Siler.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacility(@PathVariable Long id) {
        facilityService.deleteFacility(id);
        return ResponseEntity.noContent().build();
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
