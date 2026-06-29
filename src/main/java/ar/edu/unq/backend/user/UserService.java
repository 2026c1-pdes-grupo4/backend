package ar.edu.unq.backend.user;

import ar.edu.unq.backend.common.error.ErrorCode;
import ar.edu.unq.backend.common.exception.ConflictException;
import ar.edu.unq.backend.common.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que gestiona las operaciones sobre usuarios.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    /**
     * Crea un nuevo usuario.
     *
     * @param dto datos del usuario a registrar
     * @return el usuario creado representado como DTO de respuesta
     * @throws RuntimeException si el nombre de usuario o email ya están en uso
     */
    public UserResponseDTO create(UserRequestDTO dto) {

        if (userRepository.existsByUsername(dto.getUsername())) {
            log.error("Rejecting user creation: username already exists. username={}", dto.getUsername());
            throw new ConflictException(ErrorCode.USERNAME_ALREADY_EXISTS, "Username already exists");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.error("Rejecting user creation: email already exists. email={}", dto.getEmail());
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setProfileType(dto.getProfileType());

        UserResponseDTO result = userMapper.mapToResponse(userRepository.save(user));
        log.info("User created. userId={}, username={}, profileType={}", result.getId(), dto.getUsername(), dto.getProfileType());
        return result;
    }

    /**
     * Devuelve la lista de todos los usuarios registrados.
     *
     * @return lista de usuarios como DTOs de respuesta
     */
    public List<UserResponseDTO> findAll() {
        List<UserResponseDTO> users = userRepository.findAll()
                .stream()
                .map(userMapper::mapToResponse)
                .toList();
        log.info("findAll users. count={}", users.size());
        return users;
    }

    /**
     * Busca un usuario por el id.
     *
     * @param id identificador del usuario
     * @return el usuario encontrado como DTO de respuesta
     * @throws RuntimeException si el usuario no existe
     */
    public UserResponseDTO findById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found. userId={}", id);
                    return new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found");
                });

        log.info("User found. userId={}, username={}", user.getUserId(), user.getUsername());
        return userMapper.mapToResponse(user);
    }

    /**
     * Actualiza los datos de un usuario existente.
     * Si se envía una contraseña, se re-encripta antes de persistir.
     *
     * @param id  identificador del usuario a actualizar
     * @param dto nuevos datos del usuario
     * @return el usuario actualizado como DTO de respuesta
     * @throws RuntimeException si el usuario no existe,
     *                                 400 si el nuevo username o email ya están en uso
     */
    public UserResponseDTO update(Integer id, UserRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot update user because it does not exist. userId={}", id);
                    return new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found");
                });

        if (!user.getUsername().equals(dto.getUsername()) && userRepository.existsByUsername(dto.getUsername())) {
            log.error("Rejecting user update: username already exists. userId={}, username={}", id, dto.getUsername());
            throw new ConflictException(ErrorCode.USERNAME_ALREADY_EXISTS, "Username already exists");
        }

        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            log.error("Rejecting user update: email already exists. userId={}, email={}", id, dto.getEmail());
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email already exists");
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setProfileType(dto.getProfileType());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        UserResponseDTO result = userMapper.mapToResponse(userRepository.save(user));
        log.info("User updated. userId={}, username={}", id, dto.getUsername());
        return result;
    }

    /**
     * Elimina un usuario por el id.
     *
     * @param id identificador del usuario a eliminar
     * @throws RuntimeException si el usuario no existe
     */
    public void delete(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot delete user because it does not exist. userId={}", id);
                    return new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found");
                });

        userRepository.delete(user);
        log.info("User deleted. userId={}", id);
    }
}
