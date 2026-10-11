package com.guardianai.service;

import com.guardianai.dto.LocationRequest;
import com.guardianai.dto.LocationResponse;
import com.guardianai.entity.Location;
import com.guardianai.entity.User;
import com.guardianai.exception.ResourceNotFoundException;
import com.guardianai.repository.LocationRepository;
import com.guardianai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public LocationService(LocationRepository locationRepository, UserRepository userRepository) {
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public LocationResponse saveLocation(Long userId, LocationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Location location = new Location();
        location.setUser(user);
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());

        Location saved = locationRepository.save(location);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public LocationResponse getLatestLocation(Long userId) {
        Location location = locationRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No location record found for this user"));
        return mapToResponse(location);
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> getLocationHistory(Long userId) {
        return locationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private LocationResponse mapToResponse(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getCreatedAt()
        );
    }
}
