package ar.edu.unq.backend.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Servicio que gestiona las operaciones sobre usuarios.
 */
@Service
public class UserService {

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
     * @throws ResponseStatusException 400 si el nombre de usuario o email ya están en uso
     */
    public UserResponseDTO create(UserRequestDTO dto) {

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setProfileType(dto.getProfileType());

        return userMapper.mapToResponse(userRepository.save(user));
    }

    /**
     * Devuelve la lista de todos los usuarios registrados.
     *
     * @return lista de usuarios como DTOs de respuesta
     */
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::mapToResponse)
                .toList();
    }

    /**
     * Busca un usuario por el id.
     *
     * @param id identificador del usuario
     * @return el usuario encontrado como DTO de respuesta
     * @throws ResponseStatusException 404 si el usuario no existe
     */
    public UserResponseDTO findById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return userMapper.mapToResponse(user);
    }

    /**
     * Actualiza los datos de un usuario existente.
     * Si se envía una contraseña, se re-encripta antes de persistir.
     *
     * @param id  identificador del usuario a actualizar
     * @param dto nuevos datos del usuario
     * @return el usuario actualizado como DTO de respuesta
     * @throws ResponseStatusException 404 si el usuario no existe,
     *                                 400 si el nuevo username o email ya están en uso
     */
    public UserResponseDTO update(Integer id, UserRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.getUsername().equals(dto.getUsername()) && userRepository.existsByUsername(dto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setProfileType(dto.getProfileType());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return userMapper.mapToResponse(userRepository.save(user));
    }

    /**
     * Elimina un usuario por el id.
     *
     * @param id identificador del usuario a eliminar
     * @throws ResponseStatusException 404 si el usuario no existe
     */
    public void delete(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userRepository.delete(user);
    }
}
