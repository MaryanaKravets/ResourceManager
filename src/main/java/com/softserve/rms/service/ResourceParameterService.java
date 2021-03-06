package com.softserve.rms.service;

import com.softserve.rms.dto.resourceParameter.ResourceParameterDTO;

import java.util.List;


import com.softserve.rms.dto.resourceParameter.ResourceParameterSaveDTO;
import com.softserve.rms.entities.ResourceParameter;
import com.softserve.rms.entities.ResourceTemplate;
import com.softserve.rms.exceptions.NotDeletedException;
import com.softserve.rms.exceptions.NotFoundException;
import com.softserve.rms.exceptions.resourceParameter.ResourceParameterCanNotBeModified;
import org.springframework.data.domain.Page;

public interface ResourceParameterService {

    /**
     * Method verifies if {@link ResourceParameter} could be added.
     *
     * @param templateId       {@link ResourceTemplate} id
     * @param parameterSaveDTO {@link ResourceParameterSaveDTO}
     * @return instance of {@link ResourceParameterDTO}
     * @throws ResourceParameterCanNotBeModified if the resource template is published
     * @author Halyna Yatseniuk
     */
    ResourceParameterDTO checkIfParameterCanBeSaved(Long templateId, ResourceParameterSaveDTO parameterSaveDTO);

    /**
     * Method finds one {@link ResourceParameter} by id.
     *
     * @param templateId  {@link ResourceTemplate} id
     * @param parameterId {@link ResourceParameterDTO} id
     * @return instance of {@link ResourceParameterDTO}
     * @throws NotFoundException if the resource parameter with provided id is not found
     * @author Andrii Bren
     */
    ResourceParameterDTO findByIdDTO(Long templateId, Long parameterId);

    /**
     * Method verifies if {@link ResourceParameter} could be updates.
     *
     * @param templateId       {@link ResourceTemplate} id
     * @param parameterId      {@link ResourceParameter} id
     * @param parameterSaveDTO {@link ResourceParameterSaveDTO}
     * @throws ResourceParameterCanNotBeModified if the resource template is published
     * @author Halyna Yatseniuk
     */
    ResourceParameterDTO checkIfParameterCanBeUpdated(Long templateId, Long parameterId, ResourceParameterSaveDTO parameterSaveDTO);

    /**
     * Method finds all {@link ResourceParameter} by {@link ResourceTemplate} id.
     *
     * @param templateId {@link ResourceTemplate} id
     * @return list of {@link ResourceParameterDTO}
     * @author Andrii Bren
     */
    Page<ResourceParameterDTO> findAllByTemplateId(Long templateId, Integer page, Integer pageSize);

    /**
     * Method verifies if {@link ResourceParameter} could be deleted.
     *
     * @param templateId  {@link ResourceTemplate} id
     * @param parameterId {@link ResourceParameter} id
     * @throws NotDeletedException               if the resource parameter with provided id is not deleted
     * @throws ResourceParameterCanNotBeModified if the resource template is published
     * @author Halyna Yatseniuk
     */
    void checkIfParameterCanBeDeleted(Long templateId, Long parameterId);
}
