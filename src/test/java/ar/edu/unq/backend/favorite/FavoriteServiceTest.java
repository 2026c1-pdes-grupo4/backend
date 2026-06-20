package ar.edu.unq.backend.favorite;

import ar.edu.unq.backend.agency_property.AgencyProperty;
import ar.edu.unq.backend.agency_property.AgencyPropertyRepository;
import ar.edu.unq.backend.auth.JwtAuthUtils;
import ar.edu.unq.backend.common.exception.ForbiddenException;
import ar.edu.unq.backend.common.exception.ValidationException;
import ar.edu.unq.backend.user.User;
import ar.edu.unq.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

	@Mock
	private FavoriteRepository favoriteRepository;
	@Mock
	private AgencyPropertyRepository agencyPropertyRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private JwtAuthUtils jwtAuthUtils;
	@Mock
	private FavoriteMapper favoriteMapper;

	@InjectMocks
	private FavoriteService favoriteService;

	@Test
	void saveThrowsWhenScoreIsOutOfRange() {
		FavoriteRequestDTO dto = new FavoriteRequestDTO();
		dto.setAgencyPropertyId(1);
		dto.setScore(15);

		User user = new User();
		user.setUserId(1);

		AgencyProperty listing = new AgencyProperty();
		listing.setAgencyPropertyId(1);

		when(jwtAuthUtils.getCurrentId()).thenReturn(1);
		when(userRepository.findById(1)).thenReturn(Optional.of(user));
		when(agencyPropertyRepository.findById(1)).thenReturn(Optional.of(listing));

		assertThrows(ValidationException.class, () -> favoriteService.save(dto));
	}

	@Test
	void updateThrowsWhenFavoriteBelongsToAnotherUser() {
		FavoriteRequestDTO dto = new FavoriteRequestDTO();
		dto.setScore(5);

		User owner = new User();
		owner.setUserId(2);

		Favorite favorite = new Favorite();
		favorite.setFavoriteId(10);
		favorite.setUser(owner);

		when(jwtAuthUtils.getCurrentId()).thenReturn(1);
		when(favoriteRepository.findById(10)).thenReturn(Optional.of(favorite));

		assertThrows(ForbiddenException.class, () -> favoriteService.update(10, dto));
	}

	@Test
	void deleteThrowsWhenFavoriteBelongsToAnotherUser() {
		User owner = new User();
		owner.setUserId(2);

		Favorite favorite = new Favorite();
		favorite.setFavoriteId(10);
		favorite.setUser(owner);

		when(jwtAuthUtils.getCurrentId()).thenReturn(1);
		when(favoriteRepository.findById(10)).thenReturn(Optional.of(favorite));

		assertThrows(ForbiddenException.class, () -> favoriteService.delete(10));
	}

	@Test
	void findForCurrentUserMapsResult() {
		Favorite favorite = new Favorite();
		when(jwtAuthUtils.getCurrentId()).thenReturn(1);
		when(favoriteRepository.findByUser_UserId(1)).thenReturn(java.util.List.of(favorite));
		when(favoriteMapper.mapToResponse(favorite)).thenReturn(new FavoriteResponseDTO());

		assertEquals(1, favoriteService.findForCurrentUser().size());
	}

	@Test
	void saveReturnsMappedFavoriteWhenValid() {
		FavoriteRequestDTO dto = new FavoriteRequestDTO();
		dto.setAgencyPropertyId(1);
		dto.setScore(8);
		dto.setComment("ok");

		User user = new User();
		user.setUserId(1);

		AgencyProperty listing = new AgencyProperty();
		listing.setAgencyPropertyId(1);
		listing.setListedPrice(100.0);

		Favorite saved = new Favorite();
		saved.setFavoriteId(5);
		saved.setSavedDate(LocalDate.now());

		when(jwtAuthUtils.getCurrentId()).thenReturn(1);
		when(userRepository.findById(1)).thenReturn(Optional.of(user));
		when(agencyPropertyRepository.findById(1)).thenReturn(Optional.of(listing));
		when(favoriteRepository.existsByUser_UserIdAndAgencyProperty_AgencyPropertyId(1, 1)).thenReturn(false);
		when(favoriteRepository.save(any(Favorite.class))).thenReturn(saved);
		when(favoriteMapper.mapToResponse(saved)).thenReturn(new FavoriteResponseDTO());

		assertNotNull(favoriteService.save(dto));
	}

	@Test
	void updateReturnsMappedFavoriteWhenOwnerMatches() {
		FavoriteRequestDTO dto = new FavoriteRequestDTO();
		dto.setScore(6);

		User owner = new User();
		owner.setUserId(1);

		Favorite favorite = new Favorite();
		favorite.setFavoriteId(10);
		favorite.setUser(owner);

		when(jwtAuthUtils.getCurrentId()).thenReturn(1);
		when(favoriteRepository.findById(10)).thenReturn(Optional.of(favorite));
		when(favoriteRepository.save(favorite)).thenReturn(favorite);
		when(favoriteMapper.mapToResponse(favorite)).thenReturn(new FavoriteResponseDTO());

		assertNotNull(favoriteService.update(10, dto));
		assertEquals(6, favorite.getScore());
	}

	@Test
	void deleteRemovesFavoriteWhenOwnerMatches() {
		User owner = new User();
		owner.setUserId(1);

		Favorite favorite = new Favorite();
		favorite.setFavoriteId(10);
		favorite.setUser(owner);

		when(jwtAuthUtils.getCurrentId()).thenReturn(1);
		when(favoriteRepository.findById(10)).thenReturn(Optional.of(favorite));

		favoriteService.delete(10);
		verify(favoriteRepository).delete(favorite);
	}
}


