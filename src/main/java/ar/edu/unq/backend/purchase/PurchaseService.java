package ar.edu.unq.backend.purchase;

import ar.edu.unq.backend.auth.JwtAuthUtils;
import ar.edu.unq.backend.agency_property.AgencyProperty;
import ar.edu.unq.backend.agency_property.AgencyPropertyRepository;
import ar.edu.unq.backend.common.error.ErrorCode;
import ar.edu.unq.backend.common.exception.NotFoundException;
import ar.edu.unq.backend.common.exception.ValidationException;
import ar.edu.unq.backend.user.User;
import ar.edu.unq.backend.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio que gestiona las compras de publicaciones de propiedades.
 */
@Service
public class PurchaseService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseService.class);

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
     * @throws RuntimeException si el usuario o la publicación no existen,
     *                                 400 si la propiedad ya fue vendida
     */
    public PurchaseResponseDTO buy(PurchaseRequestDTO dto) {
        Integer userId = jwtAuthUtils.getCurrentId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Cannot register purchase: user not found. userId={}", userId);
                    return new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found");
                });

        AgencyProperty agencyProperty = agencyPropertyRepository.findById(dto.getAgencyPropertyId())
                .orElseThrow(() -> {
                    log.error("Cannot register purchase: publication not found. agencyPropertyId={}", dto.getAgencyPropertyId());
                    return new NotFoundException(ErrorCode.AGENCY_PROPERTY_NOT_FOUND, "Agency property not found");
                });

        if (!agencyProperty.getProperty().getAvailable()) {
            log.error("Rejecting purchase: property already sold. agencyPropertyId={}", dto.getAgencyPropertyId());
            throw new ValidationException(ErrorCode.PROPERTY_ALREADY_SOLD, "Property already sold");
        }

        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setAgencyProperty(agencyProperty);
        purchase.setPurchasePrice(agencyProperty.getListedPrice());
        purchase.setPurchaseDate(LocalDate.now());

        agencyProperty.getProperty().setAvailable(false);

        purchaseRepository.save(purchase);
        agencyPropertyRepository.save(agencyProperty);

        log.info("Purchase registered. userId={}, agencyPropertyId={}, price={}",
                userId, dto.getAgencyPropertyId(), purchase.getPurchasePrice());
        return purchaseMapper.mapToResponse(purchase);
    }

    /**
     * Devuelve todas las compras realizadas por el usuario autenticado.
     *
     * @return lista de compras del usuario como DTOs de respuesta
     */
    public List<PurchaseResponseDTO> findForCurrentUser() {
        Integer userId = jwtAuthUtils.getCurrentId();
        log.info("Fetching purchases for user. ");

        List<PurchaseResponseDTO> result = purchaseRepository.findByUser_UserId(userId)
                .stream()
                .map(purchaseMapper::mapToResponse)
                .toList();

        log.info("Purchases fetched for user. count={}",result.size());
        return result;
    }

    /**
     * Devuelve todas las compras de publicaciones pertenecientes a la agencia autenticada.
     *
     * @return lista de compras asociadas a la agencia como DTOs de respuesta
     */
    public List<PurchaseResponseDTO> findForCurrentAgency() {
        Integer agencyId = jwtAuthUtils.getCurrentId();
        log.info("Fetching sales for agency.");

        List<PurchaseResponseDTO> result = purchaseRepository.findByAgencyProperty_Agency_AgencyId(agencyId)
                .stream()
                .map(purchaseMapper::mapToResponse)
                .toList();

        log.info("Sales fetched for agency. count={}", result.size());
        return result;
    }
}
