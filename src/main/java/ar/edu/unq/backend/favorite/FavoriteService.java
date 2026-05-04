package ar.edu.unq.backend.favorite;

import ar.edu.unq.backend.agency_property.AgencyProperty;
import ar.edu.unq.backend.agency_property.AgencyPropertyRepository;
import ar.edu.unq.backend.auth.JwtAuthUtils;
import ar.edu.unq.backend.user.User;
import ar.edu.unq.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio que gestiona los favoritos de los usuarios sobre publicaciones de propiedades.
 */
@Service
public class FavoriteService {

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
     * @throws ResponseStatusException 404 si el usuario o la publicación no existen,
     *                                 400 si la publicación ya fue guardada como favorita
     */
    public FavoriteResponseDTO save(FavoriteRequestDTO dto) {
        Integer userId = jwtAuthUtils.getCurrentId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        AgencyProperty agencyProperty = agencyPropertyRepository.findById(dto.getAgencyPropertyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agency property not found"));

        if (favoriteRepository.existsByUser_UserIdAndAgencyProperty_AgencyPropertyId(userId, agencyProperty.getAgencyPropertyId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Publication already saved as favorite");
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
     * @throws ResponseStatusException 404 si el favorito no existe,
     *                                 403 si el usuario autenticado no es el dueño
     */
    public FavoriteResponseDTO update(Integer id, FavoriteRequestDTO dto) {
        Integer userId = jwtAuthUtils.getCurrentId();

        Favorite fav = favoriteRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite not found"));

        if (!fav.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Cannot modify another user's favorite");
        }

        fav.setScore(dto.getScore());
        fav.setComment(dto.getComment());

        return favoriteMapper.mapToResponse(favoriteRepository.save(fav));
    }

    /**
     * Elimina un favorito por el id.
     * Solo el propietario del favorito puede eliminarlo.
     *
     * @param id identificador del favorito a eliminar
     * @throws ResponseStatusException 404 si el favorito no existe,
     *                                 403 si el usuario autenticado no es el dueño
     */
    public void delete(Integer id) {
        Integer userId = jwtAuthUtils.getCurrentId();

        Favorite fav = favoriteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite not found"));

        if (!fav.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete another user's favorite");
        }

        favoriteRepository.delete(fav);
    }
}
