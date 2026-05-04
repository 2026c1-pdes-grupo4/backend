package ar.edu.unq.backend.purchase;

import ar.edu.unq.backend.auth.JwtAuthUtils;
import ar.edu.unq.backend.agency_property.AgencyProperty;
import ar.edu.unq.backend.agency_property.AgencyPropertyRepository;
import ar.edu.unq.backend.user.User;
import ar.edu.unq.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio que gestiona las compras de publicaciones de propiedades.
 */
@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final AgencyPropertyRepository agencyPropertyRepository;
    private final UserRepository userRepository;
    private final JwtAuthUtils jwtAuthUtils;
    private final PurchaseMapper purchaseMapper;

    public PurchaseService(PurchaseRepository purchaseRepository, AgencyPropertyRepository agencyPropertyRepository,
                           UserRepository userRepository, JwtAuthUtils jwtAuthUtils, PurchaseMapper purchaseMapper) {
        this.purchaseRepository = purchaseRepository;
        this.agencyPropertyRepository = agencyPropertyRepository;
        this.userRepository = userRepository;
        this.jwtAuthUtils = jwtAuthUtils;
        this.purchaseMapper = purchaseMapper;
    }

    /**
     * Registra la compra de una publicación ({@link AgencyProperty}) por parte del usuario autenticado.
     * Marca la publicación como no disponible al concretarse la compra.
     *
     * @param dto datos de la compra con el id de la publicación
     * @return la compra realizada como DTO de respuesta
     * @throws ResponseStatusException 404 si el usuario o la publicación no existen,
     *                                 400 si la propiedad ya fue vendida
     */
    public PurchaseResponseDTO buy(PurchaseRequestDTO dto) {
        Integer userId = jwtAuthUtils.getCurrentId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        AgencyProperty agencyProperty = agencyPropertyRepository.findById(dto.getAgencyPropertyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agency property not found"));

        if (!agencyProperty.getAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Property already sold");
        }

        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setAgencyProperty(agencyProperty);
        purchase.setPurchasePrice(agencyProperty.getListedPrice());
        purchase.setPurchaseDate(LocalDate.now());

        agencyProperty.setAvailable(false);

        purchaseRepository.save(purchase);
        agencyPropertyRepository.save(agencyProperty);

        return purchaseMapper.mapToResponse(purchase);
    }

    /**
     * Devuelve todas las compras realizadas por el usuario autenticado.
     *
     * @return lista de compras del usuario como DTOs de respuesta
     */
    public List<PurchaseResponseDTO> findForCurrentUser() {
        Integer userId = jwtAuthUtils.getCurrentId();

        return purchaseRepository.findByUser_UserId(userId)
                .stream()
                .map(purchaseMapper::mapToResponse)
                .toList();

    }

    /**
     * Devuelve todas las compras de publicaciones pertenecientes a la agencia autenticada.
     *
     * @return lista de compras asociadas a la agencia como DTOs de respuesta
     */
    public List<PurchaseResponseDTO> findForCurrentAgency() {
        Integer agencyId = jwtAuthUtils.getCurrentId();

        return purchaseRepository.findByAgencyProperty_Agency_AgencyId(agencyId)
                .stream()
                .map(purchaseMapper::mapToResponse)
                .toList();
    }
}
