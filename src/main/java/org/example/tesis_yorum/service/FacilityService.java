package org.example.tesis_yorum.service;

import org.example.tesis_yorum.entity.Facility;
import org.example.tesis_yorum.entity.FacilityType;
import org.example.tesis_yorum.exceptions.ResourceNotFoundException;
import org.example.tesis_yorum.repository.FacilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FacilityService {

    private final FacilityRepository facilityRepository;

    @Autowired
    public FacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }


    public Facility createFacility(Facility facility) {
        if (facility == null) {
            throw new IllegalArgumentException("Facility cannot be null");
        }
        return facilityRepository.save(facility);
    }


    public Facility createFacility(String name, FacilityType type, String description, String address, String city) {
        Facility facility = new Facility(name, description, address, city, type);
        return createFacility(facility);
    }


    @Transactional(readOnly = true)
    public Facility getFacilityById(Long id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with id: " + id));
    }


    @Transactional(readOnly = true)
    public List<Facility> getAllFacilities() {
        return facilityRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Facility> searchFacilities(String keyword) {
        return facilityRepository.searchByNameOrDescription(keyword);
    }

    public Facility updateFacility(Long id, Facility updatedFacility) {
        Facility existingFacility = getFacilityById(id);

        existingFacility.setName(updatedFacility.getName());
        existingFacility.setDescription(updatedFacility.getDescription());
        existingFacility.setAddress(updatedFacility.getAddress());
        existingFacility.setCity(updatedFacility.getCity());
        existingFacility.setType(updatedFacility.getType());

        return facilityRepository.save(existingFacility);
    }


    public void deleteFacility(Long id) {
        Facility facility = getFacilityById(id);
        facilityRepository.delete(facility);
    }

}
