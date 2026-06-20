package ar.edu.unq.backend.favorite;

import ar.edu.unq.backend.agency_property.AgencyProperty;
import ar.edu.unq.backend.agency_property.AgencyPropertyRepository;
import ar.edu.unq.backend.auth.JwtAuthUtils;
import ar.edu.unq.backend.common.error.ErrorCode;
import ar.edu.unq.backend.common.exception.ConflictException;
import ar.edu.unq.backend.common.exception.ForbiddenException;
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
 * Servicio que gestiona los favoritos de los usuarios sobre publicaciones de propiedades.
 */
@Service
public class FavoriteService {

    private static final Logger log = LoggerFactory.getLogger(FavoriteService.class);

    private final FavoriteRepository favoriteRepository;
    private final AgencyPropertyRepository agencyPropertyRepository;
    private final UserRepository userRepository;
    private final JwtAuthUtils jwtAuthUtils;
    private final FavoriteMapper favoriteMapper;

    public FavoriteService(FavoriteRepository favoriteRepository, AgencyPropertyRepository agencyPropertyRepository,
                           UserRepository userRepository, JwtAuthUtils jwtAuthUtils, FavoriteMapper favoriteMapper) {
        this.favoriteRepository = favoriteRepository;
        this.agencyPropertyRepository = agencyPropertyRepository;
        this.userRepository = userRepository;
        this.jwtAuthUtils = jwtAuthUtils;
        this.favoriteMapper = favoriteMapper;
    }

    /**
     * Guarda una publicación como favorita para el usuario autenticado.
     * Registra automáticamente el precio vigente al momento de guardar.
     *
     * @param dto datos del favorito, incluyendo el id de la publicación, puntaje y comentario
     * @return el favorito creado como DTO de respuesta
     * @throws RuntimeException si el usuario o la publicación no existen,
     *                                 400 si la publicación ya fue guardada como favorita
     */
    public FavoriteResponseDTO save(FavoriteRequestDTO dto) {
        Integer userId = jwtAuthUtils.getCurrentId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Cannot save favorite: user not found. userId={}", userId);
                    return new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found");
                });

        AgencyProperty agencyProperty = agencyPropertyRepository.findById(dto.getAgencyPropertyId())
                .orElseThrow(() -> {
                    log.warn("Cannot save favorite: publication not found. agencyPropertyId={}", dto.getAgencyPropertyId());
                    return new NotFoundException(ErrorCode.AGENCY_PROPERTY_NOT_FOUND, "Agency property not found");
                });

        validateScore(dto.getScore());

        if (favoriteRepository.existsByUser_UserIdAndAgencyProperty_AgencyPropertyId(userId, agencyProperty.getAgencyPropertyId())) {
            log.warn("Rejecting favorite save: publication already favorited. userId={}, agencyPropertyId={}", userId, agencyProperty.getAgencyPropertyId());
            throw new ConflictException(ErrorCode.PUBLICATION_ALREADY_FAVORITED, "Publication already saved as favorite");
        }

        Favorite fav = new Favorite();
        fav.setUser(user);
        fav.setAgencyProperty(agencyProperty);
        fav.setSavedDate(LocalDate.now());
        fav.setSavedPrice(agencyProperty.getListedPrice());
        fav.setScore(dto.getScore());
        fav.setComment(dto.getComment());

        return favoriteMapper.mapToResponse(favoriteRepository.save(fav));
    }

    /**
     * Devuelve todos los favoritos del usuario autenticado.
     *
     * @return lista de favoritos del usuario como DTOs de respuesta
     */
    public List<FavoriteResponseDTO> findForCurrentUser() {
        Integer userId = jwtAuthUtils.getCurrentId();

        return favoriteRepository.findByUser_UserId(userId)
                .stream()
                .map(favoriteMapper::mapToResponse)
                .toList();
    }

    /**
     * Actualiza el puntaje y comentario de un favorito existente.
     * Solo el propietario del favorito puede modificarlo.
     *
     * @param id  identificador del favorito a actualizar
     * @param dto nuevos datos (puntaje y comentario)
     * @return el favorito actualizado como DTO de respuesta
     * @throws RuntimeException si el favorito no existe,
     *                                 403 si el usuario autenticado no es el dueño
     */
    public FavoriteResponseDTO update(Integer id, FavoriteRequestDTO dto) {
        Integer userId = jwtAuthUtils.getCurrentId();

        Favorite fav = favoriteRepository.findById(id).orElseThrow(() ->
        {
            log.warn("Cannot update favorite: favorite not found. favoriteId={}", id);
            return new NotFoundException(ErrorCode.FAVORITE_NOT_FOUND, "Favorite not found");
        });

        if (!fav.getUser().getUserId().equals(userId)) {
            log.warn("Rejecting favorite update: ownership mismatch. favoriteId={}, requesterUserId={}, ownerUserId={}",
                    id, userId, fav.getUser().getUserId());
            throw new ForbiddenException(
                    ErrorCode.CANNOT_MODIFY_OTHER_FAVORITE, "Cannot modify another user's favorite");
        }

        validateScore(dto.getScore());

        fav.setScore(dto.getScore());
        fav.setComment(dto.getComment());

        return favoriteMapper.mapToResponse(favoriteRepository.save(fav));
    }

    /**
     * Elimina un favorito por el id.
     * Solo el propietario del favorito puede eliminarlo.
     *
     * @param id identificador del favorito a eliminar
     * @throws RuntimeException si el favorito no existe,
     *                                 403 si el usuario autenticado no es el dueño
     */
    public void delete(Integer id) {
        Integer userId = jwtAuthUtils.getCurrentId();

        Favorite fav = favoriteRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot delete favorite: favorite not found. favoriteId={}", id);
                    return new NotFoundException(ErrorCode.FAVORITE_NOT_FOUND, "Favorite not found");
                });

        if (!fav.getUser().getUserId().equals(userId)) {
            log.warn("Rejecting favorite delete: ownership mismatch. favoriteId={}, requesterUserId={}, ownerUserId={}",
                    id, userId, fav.getUser().getUserId());
            throw new ForbiddenException(ErrorCode.CANNOT_DELETE_OTHER_FAVORITE, "Cannot delete another user's favorite");
        }

        favoriteRepository.delete(fav);
    }

    private void validateScore(Integer score) {
        if (score == null) {
            return;
        }

        if (score < 0 || score > 10) {
            log.warn("Rejecting favorite score: out of range. score={}", score);
            throw new ValidationException(
                    ErrorCode.INVALID_SCORE,
                    "Score must be between 0 and 10",
                    List.of("score must be between 0 and 10")
            );
        }
    }
}
