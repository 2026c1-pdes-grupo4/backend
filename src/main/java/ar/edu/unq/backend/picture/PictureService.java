package ar.edu.unq.backend.picture;

import ar.edu.unq.backend.agency_property.AgencyProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Persiste una imagen asociada a una publicación, si la URL es válida.
     * Si la URL es nula o en blanco, no se hace nada.
     *
     * @param agencyProperty publicación a la que se asocia la imagen
     * @param url            URL de la imagen (opcional)
     */
    public void saveForListing(AgencyProperty agencyProperty, String url) {
        if (url == null || url.isBlank()) return;
        Picture p = new Picture();
        p.setAgencyProperty(agencyProperty);
        p.setUrl(url.trim());
        pictureRepository.save(p);
    }

    /**
     * Reemplaza la imagen de una publicación existente.
     * Si la URL es nula, no se realiza ningún cambio (se conserva la imagen actual).
     * Si la URL es válida, se elimina la imagen anterior y se guarda la nueva.
     *
     * @param agencyProperty publicación cuya imagen se actualiza
     * @param url            nueva URL de imagen, o {@code null} para no modificar
     */
    @Transactional
    public void updateForListing(AgencyProperty agencyProperty, String url) {
        if (url == null) return;
        pictureRepository.deleteByAgencyProperty_AgencyPropertyId(agencyProperty.getAgencyPropertyId());
        if (!url.isBlank()) {
            Picture p = new Picture();
            p.setAgencyProperty(agencyProperty);
            p.setUrl(url.trim());
            pictureRepository.save(p);
        }
    }
}
