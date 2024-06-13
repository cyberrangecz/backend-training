package cz.muni.ics.kypo.training.facade.detection;

import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.api.dto.cheatingdetection.CheatingDetectionDTO;
import cz.muni.ics.kypo.training.api.dto.export.FileToReturnDTO;
import cz.muni.ics.kypo.training.exceptions.InternalServerErrorException;
import cz.muni.ics.kypo.training.mapping.mapstruct.detection.CheatingDetectionMapper;
import cz.muni.ics.kypo.training.persistence.model.detection.CheatingDetection;
import cz.muni.ics.kypo.training.service.UserService;
import cz.muni.ics.kypo.training.service.detection.CheatingDetectionExportService;
import cz.muni.ics.kypo.training.service.detection.CheatingDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

/**
 * The type Cheating Detection facade.
 */
@Service
@Transactional
public class CheatingDetectionExportFacade {


    private final CheatingDetectionService cheatingDetectionService;

    public final UserService userService;

    private final CheatingDetectionMapper cheatingDetectionMapper;
    private final CheatingDetectionExportService cheatingDetectionExportService;

    /**
     * Instantiates a new Cheating detection facade.
     *
     * @param cheatingDetectionService       the cheating detection service
     * @param userService                    the user service
     * @param cheatingDetectionMapper        the cheating detection mapper
     * @param cheatingDetectionExportService the cheating detection export service
     */
    @Autowired
    public CheatingDetectionExportFacade(CheatingDetectionService cheatingDetectionService,
                                         UserService userService,
                                         CheatingDetectionMapper cheatingDetectionMapper,
                                         CheatingDetectionExportService cheatingDetectionExportService) {
        this.cheatingDetectionService = cheatingDetectionService;
        this.userService = userService;
        this.cheatingDetectionMapper = cheatingDetectionMapper;
        this.cheatingDetectionExportService = cheatingDetectionExportService;
    }


    /**
     * Exports Cheating Detection to file
     *
     * @param cheatingDetectionId the id of the cheating detection to be exported
     * @return the file containing cheating detection, {@link FileToReturnDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalRO
    public FileToReturnDTO archiveCheatingDetectionResults(Long cheatingDetectionId) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            CheatingDetection cheatingDetection = cheatingDetectionService.findCheatingDetectionById(cheatingDetectionId);
            CheatingDetectionDTO cheatingDetectionDTO = cheatingDetectionMapper.mapToDTO(cheatingDetection);

            cheatingDetectionExportService.writeCheatingDetection(zos, cheatingDetectionId, cheatingDetectionDTO);
            cheatingDetectionExportService.writeAnswerSimilarityDetectionEvents(zos, cheatingDetectionId);
            cheatingDetectionExportService.writeLocationSimilarityDetectionEvents(zos, cheatingDetectionId);
            cheatingDetectionExportService.writeTimeProximityDetectionEvents(zos, cheatingDetectionId);
            cheatingDetectionExportService.writeMinimalSolveTimeDetectionEvents(zos, cheatingDetectionId);
            cheatingDetectionExportService.writeNoCommandsDetectionEvents(zos, cheatingDetectionId);
            cheatingDetectionExportService.writeForbiddenCommandsDetectionEvents(zos, cheatingDetectionId);
            cheatingDetectionExportService.writeTraineeParticipantGroups(zos, cheatingDetectionId);

            zos.closeEntry();
            zos.close();
            FileToReturnDTO fileToReturnDTO = new FileToReturnDTO();
            fileToReturnDTO.setContent(baos.toByteArray());
            fileToReturnDTO.setTitle("cheating-detection-" + cheatingDetection.getId().toString());
            return fileToReturnDTO;
        } catch (IOException ex) {
            throw new InternalServerErrorException("The .zip file was not created since there were some processing error.", ex);
        }
    }
}
