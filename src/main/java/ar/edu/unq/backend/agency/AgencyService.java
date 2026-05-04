package ar.edu.unq.backend.agency;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Servicio que gestiona las operaciones sobre las agencias.
 */
@Service
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AgencyMapper agencyMapper;

    public AgencyService(AgencyRepository agencyRepository, PasswordEncoder passwordEncoder, AgencyMapper agencyMapper) {
        this.agencyRepository = agencyRepository;
        this.passwordEncoder = passwordEncoder;
        this.agencyMapper = agencyMapper;
    }

    /**
     * Registra una nueva agencia.
     *
     * @param dto datos de la agencia a crear
     * @return la agencia creada como DTO de respuesta
     * @throws ResponseStatusException 400 si el username o email ya están registrados
     */
    public AgencyResponseDTO create(AgencyRequestDTO dto) {

        if (agencyRepository.existsByUsername(dto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        if (agencyRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        Agency agency = new Agency();
        agency.setUsername(dto.getUsername());
        agency.setEmail(dto.getEmail());
        agency.setPassword(passwordEncoder.encode(dto.getPassword()));

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
     * @throws ResponseStatusException 404 si la agencia no existe
     */
    public AgencyResponseDTO findById(Integer id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agency not found"));

        return agencyMapper.mapToResponse(agency);
    }

    /**
     * Actualiza los datos de una agencia existente.
     * Si se envía una contraseña, se re-encripta antes de persistir.
     *
     * @param id  identificador de la agencia a actualizar
     * @param dto nuevos datos de la agencia
     * @return la agencia actualizada como DTO de respuesta
     * @throws ResponseStatusException 404 si la agencia no existe,
     *                                 400 si el nuevo username o email ya están en uso
     */
    public AgencyResponseDTO update(Integer id, AgencyRequestDTO dto) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agency not found"));

        if (!agency.getUsername().equals(dto.getUsername()) && agencyRepository.existsByUsername(dto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        if (!agency.getEmail().equals(dto.getEmail()) && agencyRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
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
     * @throws ResponseStatusException 404 si la agencia no existe
     */
    public void delete(Integer id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agency not found"));

        agencyRepository.delete(agency);
    }
}
