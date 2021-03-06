package com.softserve.rms.service.implementation;

import com.softserve.rms.constants.ErrorMessage;
import com.softserve.rms.dto.resourceRecord.ResourceRecordDTO;
import com.softserve.rms.dto.resourceRecord.ResourceRecordSaveDTO;
import com.softserve.rms.entities.ResourceRecord;
import com.softserve.rms.entities.ResourceTemplate;
import com.softserve.rms.entities.User;
import com.softserve.rms.exceptions.NotFoundException;
import com.softserve.rms.exceptions.resourseTemplate.ResourceTemplateIsNotPublishedException;
import com.softserve.rms.repository.ResourceRecordRepository;
import com.softserve.rms.service.ResourceRecordService;
import com.softserve.rms.service.ResourceTemplateService;
import com.softserve.rms.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.softserve.rms.util.PaginationUtil.validatePage;
import static com.softserve.rms.util.PaginationUtil.validatePageSize;

/**
 * Implementation of {@link ResourceRecordService}
 *
 * @author Andrii Bren
 */
@Service
public class ResourceRecordServiceImpl implements ResourceRecordService {
    private ResourceRecordRepository resourceRecordRepository;
    private ResourceTemplateService resourceTemplateService;
    private UserService userService;
    private FileStorageServiceImpl fileStorageService;
    private ModelMapper modelMapper = new ModelMapper();
    private String endpointUrl;

    /**
     * Constructor with parameters
     *
     * @author Andrii Bren
     */
    @Autowired
    public ResourceRecordServiceImpl(ResourceRecordRepository resourceRecordRepository, ResourceTemplateService resourceTemplateService, UserService userService, FileStorageServiceImpl fileStorageService, @Value("${ENDPOINT_URL}") String endpointUrl) {
        this.resourceRecordRepository = resourceRecordRepository;
        this.resourceTemplateService = resourceTemplateService;
        this.fileStorageService = fileStorageService;
        this.userService = userService;
        this.endpointUrl = endpointUrl;
    }

    /**
     * {@inheritDoc}
     *
     * @author Andrii Bren
     */
    @Override
    public void save(String tableName, ResourceRecordSaveDTO resourceDTO) throws NotFoundException {
        checkIfResourceTemplateIsPublished(tableName);
        ResourceRecord resourceRecord = new ResourceRecord();
        resourceRecord.setName(resourceDTO.getName());
        resourceRecord.setDescription(resourceDTO.getDescription());
        Principal principal = SecurityContextHolder.getContext().getAuthentication();
        resourceRecord.setUser(userService.getUserByEmail(principal.getName()));
        resourceRecord.setPhotosNames(null);
        resourceRecord.setDocumentNames(null);
        resourceRecord.setParameters(resourceDTO.getParameters());
        resourceRecordRepository.save(tableName, resourceRecord);
    }

    /**
     * {@inheritDoc}
     *
     * @author Andrii Bren
     */
    @Override
    public ResourceRecordDTO findByIdDTO(String tableName, Long id) throws NotFoundException {
        ResourceRecord resourceRecord = findById(tableName, id);
        if (resourceRecord.getPhotosNames() != null) {
            resourceRecord.setPhotosNames(generateUrlForFiles(resourceRecord.getPhotosNames()));
        }
        if (resourceRecord.getDocumentNames() != null) {
            resourceRecord.setDocumentNames((generateUrlForFiles(resourceRecord.getDocumentNames())));
        }
        return modelMapper.map(resourceRecord, ResourceRecordDTO.class);
    }

    /**
     * {@inheritDoc}
     *
     * @author Andrii Bren
     */
    public ResourceRecord findById(String tableName, Long id) throws NotFoundException {
        checkIfResourceTemplateIsPublished(tableName);
        return resourceRecordRepository.findById(tableName, id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.CAN_NOT_FIND_A_RESOURCE_TABLE.getMessage() + id));
    }

    /**
     * {@inheritDoc}
     *
     * @author Andrii Bren
     */
    @Override
    public Page<ResourceRecordDTO> findAll(String tableName, Integer page, Integer pageSize) throws NotFoundException {
        checkIfResourceTemplateIsPublished(tableName);
        Integer validPage = validatePage(page);
        Integer validPageSize = validatePageSize(pageSize);
        Page<ResourceRecord> resourceRecords = resourceRecordRepository.findAll(tableName, validPage, validPageSize);
        resourceRecords.forEach(resource -> {
            if (resource.getPhotosNames() != null) {
                resource.setPhotosNames(generateUrlForFiles(resource.getPhotosNames()));
            }
        });
        resourceRecords.forEach(resource -> {
            if (resource.getDocumentNames() != null) {
                resource.setDocumentNames((generateUrlForFiles(resource.getDocumentNames())));
            }
        });
        return resourceRecords
                .map(resourceRecord -> modelMapper.map(resourceRecord, ResourceRecordDTO.class));
    }

    /**
     * {@inheritDoc}
     *
     * @author Andrii Bren
     */
    @Override
    public void update(String tableName, Long id, ResourceRecordSaveDTO resourceRecordSaveDTO)
            throws NotFoundException {
        checkIfResourceTemplateIsPublished(tableName);
        ResourceRecord resourceRecord = findById(tableName, id);
        if (resourceRecordSaveDTO.getName() != null) {
            resourceRecord.setName(resourceRecordSaveDTO.getName());
        }
        if (resourceRecordSaveDTO.getDescription() != null) {
            resourceRecord.setDescription(resourceRecordSaveDTO.getDescription());
        }
        if (resourceRecordSaveDTO.getParameters() != null) {
            resourceRecord.setParameters(resourceRecordSaveDTO.getParameters());
        }
        resourceRecordRepository.update(tableName, id, resourceRecord);
    }

    /**
     * {@inheritDoc}
     *
     * @author Andrii Bren
     */
    @Override
    public void delete(String tableName, Long id) throws NotFoundException {
        checkIfResourceTemplateIsPublished(tableName);
        ResourceRecord resourceRecord = findById(tableName, id);
        if (resourceRecord.getPhotosNames() != null) {
            deleteFileFromS3(resourceRecord.getPhotosNames());
        }
        if (resourceRecord.getDocumentNames() != null) {
            deleteFileFromS3(resourceRecord.getDocumentNames());
        }
        resourceRecordRepository.delete(tableName, id);
    }

    private void checkIfResourceTemplateIsPublished(String tableName) {
        ResourceTemplate resourceTemplate = resourceTemplateService.findByTableName(tableName);
        if (!resourceTemplate.getIsPublished()) {
            throw new ResourceTemplateIsNotPublishedException(
                    ErrorMessage.RESOURCE_TEMPLATE_IS_NOT_PUBLISHED.getMessage() + tableName);
        }
    }

    /**
     * {@inheritDoc }
     *
     * @author Mariia Shchur
     */
    @Override
    public void changePhoto(MultipartFile files,
                            String tableName, Long id) {
        ResourceRecord resourceRecord = findById(tableName, id);
        String photoName = fileStorageService.uploadFile(files);
        if (resourceRecord.getPhotosNames() != null) {
            resourceRecord.setPhotosNames(resourceRecord.getPhotosNames() + photoName + ',');
        } else {
            resourceRecord.setPhotosNames(photoName + ',');
        }
        resourceRecordRepository.update(tableName, id, resourceRecord);
    }

    /**
     * {@inheritDoc }
     *
     * @author Mariia Shchur
     */
    @Override
    public void uploadDocument(MultipartFile files,
                            String tableName, Long id) {
        ResourceRecord resourceRecord = findById(tableName, id);
        String documentName = fileStorageService.uploadFile(files);
        if (resourceRecord.getDocumentNames() != null) {
            resourceRecord.setDocumentNames((resourceRecord.getDocumentNames() + documentName + ','));
        } else {
            resourceRecord.setDocumentNames(documentName + ',');
        }
        resourceRecordRepository.update(tableName, id, resourceRecord);
    }

    /**
     * {@inheritDoc }
     *
     * @author Mariia Shchur
     */
    @Override
    public void deleteAllPhotos(String tableName, Long id) {
        ResourceRecord resourceRecord = findById(tableName, id);
        deleteFileFromS3(resourceRecord.getPhotosNames());
        resourceRecord.setPhotosNames(null);
        resourceRecordRepository.update(tableName, id, resourceRecord);
    }

    /**
     * {@inheritDoc }
     *
     * @author Mariia Shchur
     */
    @Override
    public void deletePhoto(String tableName, Long id, String photo) {
        ResourceRecord resourceRecord = findById(tableName, id);
        Stream.of(resourceRecord.getPhotosNames().split(",")).
                filter(p -> p.equals(photo)).
                forEach(q -> fileStorageService.deleteFile(q));
        resourceRecord.setPhotosNames(resourceRecord.getPhotosNames().replace((photo + ','), ""));
        resourceRecordRepository.update(tableName, id, resourceRecord);
    }

    /**
     * {@inheritDoc }
     *
     * @author Mariia Shchur
     */
    @Override
    public void deleteAllDocuments(String tableName, Long id) {
        ResourceRecord resourceRecord = findById(tableName, id);
        deleteFileFromS3(resourceRecord.getDocumentNames());
        resourceRecord.setDocumentNames(null);
        resourceRecordRepository.update(tableName, id, resourceRecord);
    }

    /**
     * {@inheritDoc }
     *
     * @author Mariia Shchur
     */
    @Override
    public void deleteDocument(String tableName, Long id, String document) {
        ResourceRecord resourceRecord = findById(tableName, id);
        Stream.of(resourceRecord.getDocumentNames().split(",")).
                filter(p -> p.equals(document)).
                forEach(q -> fileStorageService.deleteFile(q));
        resourceRecord.setDocumentNames(resourceRecord.getDocumentNames().replace((document + ','), ""));
        resourceRecordRepository.update(tableName, id, resourceRecord);
    }

    /**
     * Method that delete files from S3 bucket
     *
     * @param allFiles
     * @author Mariia Shchur
     */
    private void deleteFileFromS3(String allFiles) {
        Stream.of(allFiles.split(",")).
                forEach(file -> fileStorageService.deleteFile(file));
    }


    /**
     * Method that generate full url for all photos
     *
     * @param allFiles
     * @author Mariia Shchur
     */
    private String generateUrlForFiles(String allFiles) {
        StringBuilder result = new StringBuilder();
        Stream.of(allFiles.split(",")).
                forEach(file -> result.append(endpointUrl + file + ','));
        return (result.toString());
    }

}
