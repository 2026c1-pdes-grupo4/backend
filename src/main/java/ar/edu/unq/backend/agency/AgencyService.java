package ar.edu.unq.backend.agency;

import ar.edu.unq.backend.auth.JwtAuthUtils;
import ar.edu.unq.backend.common.error.ErrorCode;
import ar.edu.unq.backend.common.exception.ConflictException;
import ar.edu.unq.backend.common.exception.NotFoundException;
import ar.edu.unq.backend.user.User;
import ar.edu.unq.backend.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que gestiona las operaciones sobre las agencias.
 */
@Service
public class AgencyService {

    private static final Logger log = LoggerFactory.getLogger(AgencyService.class);

    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;
    private final JwtAuthUtils jwtAuthUtils;
    private final PasswordEncoder passwordEncoder;
    private final AgencyMapper agencyMapper;

    public AgencyService(AgencyRepository agencyRepository,
                         UserRepository userRepository,
                         JwtAuthUtils jwtAuthUtils,
                         PasswordEncoder passwordEncoder,
                         AgencyMapper agencyMapper) {
        this.agencyRepository = agencyRepository;
        this.userRepository = userRepository;
        this.jwtAuthUtils = jwtAuthUtils;
        this.passwordEncoder = passwordEncoder;
        this.agencyMapper = agencyMapper;
    }

    /**
     * Registra una nueva agencia.
     *
     * @param dto datos de la agencia a crear
     * @return la agencia creada como DTO de respuesta
     * @throws RuntimeException si el username o email ya están registrados
     */
    public AgencyResponseDTO create(AgencyRequestDTO dto) {

        if (agencyRepository.existsByUsername(dto.getUsername())) {
            log.warn("Rejecting agency creation: username already exists. username={}", dto.getUsername());
            throw new ConflictException(ErrorCode.USERNAME_ALREADY_EXISTS, "Username already exists");
        }

        if (agencyRepository.existsByEmail(dto.getEmail())) {
            log.warn("Rejecting agency creation: email already exists. email={}", dto.getEmail());
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email already exists");
        }

        Integer adminId = jwtAuthUtils.getCurrentId();
        User adminUser = userRepository.findById(adminId)
                .orElseThrow(() -> {
                    log.warn("Admin user not found for agency creation. adminId={}", adminId);
                    return new NotFoundException(ErrorCode.ADMIN_USER_NOT_FOUND, "Admin user not found");
                });

        Agency agency = new Agency();
        agency.setUsername(dto.getUsername());
        agency.setEmail(dto.getEmail());
        agency.setPassword(passwordEncoder.encode(dto.getPassword()));
        agency.setAdminUser(adminUser);

        return agencyMapper.mapToResponse(agencyRepository.save(agency));
    }

    /**
     * Devuelve la lista de todas las agencias registradas.
     *
     * @return lista de agencias como DTOs de respuesta
     */
    public List<AgencyResponseDTO> findAll() {
        return agencyRepository.findAll()
                .stream()
                .map(agencyMapper::mapToResponse)
                .toList();
    }

    /**
     * Busca una agencia dado un id.
     *
     * @param id identificador de la agencia
     * @return la agencia encontrada como DTO de respuesta
     * @throws RuntimeException si la agencia no existe
     */
    public AgencyResponseDTO findById(Integer id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Agency not found. agencyId={}", id);
                    return new NotFoundException(ErrorCode.AGENCY_NOT_FOUND, "Agency not found");
                });

        return agencyMapper.mapToResponse(agency);
    }

    /**
     * Actualiza los datos de una agencia existente.
     * Si se envía una contraseña, se re-encripta antes de persistir.
     *
     * @param id  identificador de la agencia a actualizar
     * @param dto nuevos datos de la agencia
     * @return la agencia actualizada como DTO de respuesta
     * @throws RuntimeException si la agencia no existe,
     *                                 400 si el nuevo username o email ya están en uso
     */
    public AgencyResponseDTO update(Integer id, AgencyRequestDTO dto) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot update agency because it does not exist. agencyId={}", id);
                    return new NotFoundException(ErrorCode.AGENCY_NOT_FOUND, "Agency not found");
                });

        if (!agency.getUsername().equals(dto.getUsername()) && agencyRepository.existsByUsername(dto.getUsername())) {
            log.warn("Rejecting agency update: username already exists. agencyId={}, username={}", id, dto.getUsername());
            throw new ConflictException(ErrorCode.USERNAME_ALREADY_EXISTS, "Username already exists");
        }

        if (!agency.getEmail().equals(dto.getEmail()) && agencyRepository.existsByEmail(dto.getEmail())) {
            log.warn("Rejecting agency update: email already exists. agencyId={}, email={}", id, dto.getEmail());
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email already exists");
        }

        agency.setUsername(dto.getUsername());
        agency.setEmail(dto.getEmail());


        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            agency.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return agencyMapper.mapToResponse(agencyRepository.save(agency));
    }

    /**
     * Elimina una agencia por el id.
     *
     * @param id identificador de la agencia a eliminar
     * @throws RuntimeException si la agencia no existe
     */
    public void delete(Integer id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot delete agency because it does not exist. agencyId={}", id);
                    return new NotFoundException(ErrorCode.AGENCY_NOT_FOUND, "Agency not found");
                });

        agencyRepository.delete(agency);
    }
}
