package ar.edu.unq.backend.picture;

import org.springframework.stereotype.Service;

@Service
public class PictureService {

    private final PictureRepository pictureRepository;

    public PictureService(PictureRepository pictureRepository) {
        this.pictureRepository = pictureRepository;
    }

    public String firstPictureUrl(Integer agencyPropertyId) {
        return pictureRepository.findByAgencyProperty_AgencyPropertyId(agencyPropertyId)
                .stream()
                .findFirst()
                .map(Picture::getUrl)
                .orElse(null);
    }
}
