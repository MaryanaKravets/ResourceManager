package com.softserve.rms.controller;

import com.softserve.rms.constants.HttpStatuses;
import com.softserve.rms.dto.resourceRecord.ResourceRecordDTO;
import com.softserve.rms.dto.resourceRecord.ResourceRecordSaveDTO;
import com.softserve.rms.entities.ResourceRecord;
import com.softserve.rms.entities.ResourceTemplate;
import com.softserve.rms.service.ResourceRecordService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.websocket.server.PathParam;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/resource-template/resource/{tableName}")
public class ResourceRecordController {
    private ResourceRecordService resourceRecordService;
    private static final Logger LOG = LoggerFactory.getLogger(ResourceRecordController.class);

    /**
     * Constructor with parameters
     *
     * @author Andrii Bren
     */
    @Autowired
    public ResourceRecordController(ResourceRecordService resourceRecordService) {
        this.resourceRecordService = resourceRecordService;
    }

    /**
     * The controller which saves a dynamic {@link ResourceRecord} in a table specified
     * by the reсorder.
     *
     * @param tableName   {@link ResourceTemplate} table name
     * @param resourceDTO instance of {@link ResourceRecordSaveDTO}
     * @return {@link ResponseEntity} with generic type {@link ResourceRecordDTO}
     * @author Andrii Bren
     */
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = HttpStatuses.CREATED),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
            @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST)
    })
    @PostMapping
    public HttpStatus save(@PathVariable String tableName, @Valid @RequestBody ResourceRecordSaveDTO resourceDTO) {
        LOG.info("Create a new Resource");
        resourceRecordService.save(tableName, resourceDTO);
        return HttpStatus.OK;
    }

    /**
     * Controller finds all dynamic {@link ResourceRecord} in a table specified
     * by the reсorder.
     *
     * @param tableName {@link ResourceTemplate} table name
     * @return {@link ResponseEntity} with generic type {@link ResourceRecordDTO}
     * @author Andrii Bren
     */
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping
    public ResponseEntity<Page<ResourceRecordDTO>> findAll(@PathVariable String tableName,
                                                           @RequestParam Optional<Integer> page,
                                                           @RequestParam Optional<Integer> pageSize) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(resourceRecordService.findAll(tableName, page.orElseGet(() -> 1), pageSize.orElseGet(() -> 5)));
    }

    /**
     * Controller finds a dynamic {@link ResourceRecord} in a table specified
     * by the reсorder.
     *
     * @param tableName {@link ResourceTemplate} table name
     * @param id        of {@link ResourceRecordDTO} id
     * @return {@link ResponseEntity} with generic type {@link ResourceRecordDTO}
     * @author Andrii Bren
     */
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
            @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResourceRecordDTO> findById(@PathVariable String tableName, @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(resourceRecordService.findByIdDTO(tableName, id));
    }

    /**
     * Controller which updates a dynamic {@link ResourceRecord}.
     *
     * @param tableName             {@link ResourceTemplate} table name
     * @param id                    of {@link ResourceRecordDTO} id
     * @param resourceRecordSaveDTO instance of {@link ResourceRecordSaveDTO}
     * @return {@link ResponseEntity} with generic type {@link ResourceRecordDTO}
     * @author Andrii Bren
     */
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
            @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST)
    })
    @PatchMapping("/{id}")
    public HttpStatus update(@PathVariable String tableName, @PathVariable Long id,
                             @Valid @RequestBody ResourceRecordSaveDTO resourceRecordSaveDTO) {
        resourceRecordService.update(tableName, id, resourceRecordSaveDTO);
        return HttpStatus.OK;
    }

    /**
     * Controller which deletes a dynamic {@link ResourceRecord} by id.
     *
     * @param tableName {@link ResourceTemplate} table name
     * @param id        {@link ResourceRecordDTO} id
     * @return {@link ResponseEntity} with generic type {@link Object}
     * @author Andrii Bren
     */
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
            @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable String tableName, @PathVariable Long id) {
        resourceRecordService.delete(tableName, id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for saving multiple photos
     *
     * @param files     to save.
     * @param tableName {@link ResourceTemplate} table name
     * @param id        {@link ResourceRecordDTO} id
     * @return {@link ResponseEntity}.
     * @author Mariia Shchur
     */
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
            @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST)
    })
    @PutMapping("/{id}/updatePhoto")
    public ResponseEntity changePhoto(@RequestPart List<MultipartFile> files,
                                      @PathVariable String tableName,
                                      @PathVariable Long id) {
        files.stream().forEach(photo -> resourceRecordService.changePhoto(photo, tableName, id));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for deleting all {@link ResourceRecord} photos
     *
     * @param tableName {@link ResourceTemplate} table name
     * @param id        {@link ResourceRecordDTO} id
     * @return {@link ResponseEntity}.
     * @author Mariia Shchur
     */
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
            @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST)
    })
    @DeleteMapping("/{id}/deletePhoto")
    public ResponseEntity deleteAllPhoto(@PathVariable String tableName, @PathVariable Long id) {
        resourceRecordService.deleteAllPhotos(tableName, id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for deleting specific photo of {@link ResourceRecord}
     *
     * @param tableName {@link ResourceTemplate} table name
     * @param id        {@link ResourceRecordDTO} id
     * @param photo      particular photo
     * @return {@link ResponseEntity}.
     * @author Mariia Shchur
     */
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
            @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST)
    })
    @DeleteMapping("/{id}/{photo}")
    public ResponseEntity deletePhoto(@PathVariable String tableName, @PathVariable Long id, @PathVariable String photo) {
        resourceRecordService.deletePhoto(tableName, id, photo);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for deleting specific document of {@link ResourceRecord}
     *
     * @param tableName {@link ResourceTemplate} table name
     * @param id        {@link ResourceRecordDTO} id
     * @param document
     * @return {@link ResponseEntity}.
     * @author Mariia Shchur
     */
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
            @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST)
    })
    @DeleteMapping("/{id}/{document}")
    public ResponseEntity deleteDocument(@PathVariable String tableName, @PathVariable Long id, @PathVariable String document) {
        resourceRecordService.deleteDocument(tableName, id, document);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for deleting all {@link ResourceRecord} documents
     *
     * @param tableName {@link ResourceTemplate} table name
     * @param id        {@link ResourceRecordDTO} id
     * @return {@link ResponseEntity}.
     * @author Mariia Shchur
     */
    @DeleteMapping("/{id}/deleteDocument")
    public ResponseEntity deleteAllDocuments(@PathVariable String tableName, @PathVariable Long id) {
        resourceRecordService.deleteAllDocuments(tableName, id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method for saving multiple documents
     *
     * @param files     to save.
     * @param tableName {@link ResourceTemplate} table name
     * @param id        {@link ResourceRecordDTO} id
     * @return {@link ResponseEntity}.
     * @author Mariia Shchur
     */
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
            @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST)
    })
    @PutMapping("/{id}/document")
    public ResponseEntity uploadDocuments(@RequestPart List<MultipartFile> files,
                                      @PathVariable String tableName,
                                      @PathVariable Long id) {
        files.stream().forEach(doc -> resourceRecordService.uploadDocument(doc, tableName, id));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
