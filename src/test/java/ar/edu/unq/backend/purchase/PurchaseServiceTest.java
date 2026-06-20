package ar.edu.unq.backend.purchase;

import ar.edu.unq.backend.agency_property.AgencyProperty;
import ar.edu.unq.backend.agency_property.AgencyPropertyRepository;
import ar.edu.unq.backend.auth.JwtAuthUtils;
import ar.edu.unq.backend.common.exception.ValidationException;
import ar.edu.unq.backend.property.Property;
import ar.edu.unq.backend.user.User;
import ar.edu.unq.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private AgencyPropertyRepository agencyPropertyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtAuthUtils jwtAuthUtils;
    @Mock
    private PurchaseMapper purchaseMapper;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    void buyThrowsWhenPropertyAlreadySold() {
        PurchaseRequestDTO dto = new PurchaseRequestDTO();
        dto.setAgencyPropertyId(20);

        User user = new User();
        user.setUserId(1);

        Property property = new Property();
        property.setAvailable(false);

        AgencyProperty listing = new AgencyProperty();
        listing.setAgencyPropertyId(20);
        listing.setProperty(property);

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(agencyPropertyRepository.findById(20)).thenReturn(Optional.of(listing));

        assertThrows(ValidationException.class, () -> purchaseService.buy(dto));
    }

    @Test
    void buyMarksPropertyAsUnavailable() {
        PurchaseRequestDTO dto = new PurchaseRequestDTO();
        dto.setAgencyPropertyId(20);

        User user = new User();
        user.setUserId(1);

        Property property = new Property();
        property.setAvailable(true);

        AgencyProperty listing = new AgencyProperty();
        listing.setAgencyPropertyId(20);
        listing.setProperty(property);
        listing.setListedPrice(100000.0);

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(agencyPropertyRepository.findById(20)).thenReturn(Optional.of(listing));
        when(purchaseRepository.save(org.mockito.ArgumentMatchers.any(Purchase.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(purchaseMapper.mapToResponse(org.mockito.ArgumentMatchers.any(Purchase.class)))
                .thenReturn(new PurchaseResponseDTO());

        purchaseService.buy(dto);

        assertFalse(property.getAvailable());
        verify(agencyPropertyRepository).save(listing);
    }

    @Test
    void findForCurrentUserMapsResults() {
        Purchase purchase = new Purchase();

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(purchaseRepository.findByUser_UserId(1)).thenReturn(List.of(purchase));
        when(purchaseMapper.mapToResponse(purchase)).thenReturn(new PurchaseResponseDTO());

        assertEquals(1, purchaseService.findForCurrentUser().size());
    }

    @Test
    void findForCurrentAgencyMapsResults() {
        Purchase purchase = new Purchase();

        when(jwtAuthUtils.getCurrentId()).thenReturn(5);
        when(purchaseRepository.findByAgencyProperty_Agency_AgencyId(5)).thenReturn(List.of(purchase));
        when(purchaseMapper.mapToResponse(purchase)).thenReturn(new PurchaseResponseDTO());

        assertEquals(1, purchaseService.findForCurrentAgency().size());
    }
}


