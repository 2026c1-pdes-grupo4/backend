package ar.edu.unq.backend.admin;

public record TopBuyerDTO(
        Integer userId,
        String username,
        Long purchases
) {
}

