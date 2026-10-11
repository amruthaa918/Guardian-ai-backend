package com.guardianai.service;

import com.guardianai.dto.LocationRequest;
import com.guardianai.dto.LocationResponse;
import com.guardianai.entity.Location;
import com.guardianai.entity.User;
import com.guardianai.repository.LocationRepository;
import com.guardianai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LocationService locationService;

    private User sampleUser;
    private Location sampleLocation;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Amrutha", "amrutha@example.com", "pass", "9876543210", OffsetDateTime.now());
        sampleLocation = new Location(1L, sampleUser, 17.3850, 78.4867, OffsetDateTime.now());
    }

    @Test
    @DisplayName("Should successfully record location for user")
    void testSaveLocation() {
        LocationRequest request = new LocationRequest(17.3850, 78.4867);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(locationRepository.save(any(Location.class))).thenReturn(sampleLocation);

        LocationResponse response = locationService.saveLocation(1L, request);

        assertNotNull(response);
        assertEquals(17.3850, response.getLatitude());
        assertEquals(78.4867, response.getLongitude());
    }

    @Test
    @DisplayName("Should retrieve latest location for user")
    void testGetLatestLocation() {
        when(locationRepository.findFirstByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(sampleLocation));

        LocationResponse response = locationService.getLatestLocation(1L);

        assertNotNull(response);
        assertEquals(17.3850, response.getLatitude());
        assertEquals(78.4867, response.getLongitude());
    }

    @Test
    @DisplayName("Should retrieve location history")
    void testGetLocationHistory() {
        when(locationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(sampleLocation));

        List<LocationResponse> history = locationService.getLocationHistory(1L);

        assertEquals(1, history.size());
        assertEquals(17.3850, history.get(0).getLatitude());
    }
}
