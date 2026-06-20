package ar.edu.unq.backend.admin;

public record TopRankedPropertyDTO(
        Integer propertyId,
        String address,
        Double averageScore,
        Long ratings
) {
}

