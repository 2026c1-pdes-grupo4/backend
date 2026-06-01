package ar.edu.unq.backend.user;

import ar.edu.unq.backend.common.exception.ConflictException;
import ar.edu.unq.backend.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createThrowsWhenUsernameExists() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("buyer");
        dto.setEmail("buyer@cth.com");

        when(userRepository.existsByUsername("buyer")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.create(dto));
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.findById(99));
    }

    @Test
    void createEncodesPasswordAndSaves() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("buyer");
        dto.setEmail("buyer@cth.com");
        dto.setPassword("1234");
        dto.setProfileType(ProfileType.BUYER);

        User saved = new User();
        saved.setUserId(1);

        when(userRepository.existsByUsername("buyer")).thenReturn(false);
        when(userRepository.existsByEmail("buyer@cth.com")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("ENC");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(userMapper.mapToResponse(saved)).thenReturn(new UserResponseDTO());

        userService.create(dto);

        verify(passwordEncoder).encode("1234");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createThrowsWhenEmailExists() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("buyer");
        dto.setEmail("buyer@cth.com");

        when(userRepository.existsByUsername("buyer")).thenReturn(false);
        when(userRepository.existsByEmail("buyer@cth.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.create(dto));
    }

    @Test
    void updateEncodesPasswordWhenPresent() {
        User entity = new User();
        entity.setUserId(1);
        entity.setUsername("buyer");
        entity.setEmail("buyer@cth.com");

        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("buyer");
        dto.setEmail("buyer@cth.com");
        dto.setPassword("newpass");

        when(userRepository.findById(1)).thenReturn(Optional.of(entity));
        when(passwordEncoder.encode("newpass")).thenReturn("ENC");
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.mapToResponse(entity)).thenReturn(new UserResponseDTO());

        userService.update(1, dto);

        verify(passwordEncoder).encode("newpass");
    }

    @Test
    void updateThrowsWhenUserMissing() {
        when(userRepository.findById(55)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(55, new UserRequestDTO()));
    }

    @Test
    void updateThrowsWhenUsernameAlreadyExists() {
        User entity = new User();
        entity.setUserId(1);
        entity.setUsername("old");
        entity.setEmail("old@cth.com");

        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("new");
        dto.setEmail("old@cth.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(entity));
        when(userRepository.existsByUsername("new")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.update(1, dto));
    }

    @Test
    void updateThrowsWhenEmailAlreadyExists() {
        User entity = new User();
        entity.setUserId(1);
        entity.setUsername("same");
        entity.setEmail("old@cth.com");

        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("same");
        dto.setEmail("new@cth.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(entity));
        when(userRepository.existsByEmail("new@cth.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.update(1, dto));
    }

    @Test
    void findAllMapsEntities() {
        User entity = new User();
        when(userRepository.findAll()).thenReturn(java.util.List.of(entity));
        when(userMapper.mapToResponse(entity)).thenReturn(new UserResponseDTO());

        assertEquals(1, userService.findAll().size());
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.delete(1));
    }

    @Test
    void deleteRemovesUserWhenExists() {
        User entity = new User();
        when(userRepository.findById(1)).thenReturn(Optional.of(entity));

        userService.delete(1);

        verify(userRepository).delete(entity);
    }
}

