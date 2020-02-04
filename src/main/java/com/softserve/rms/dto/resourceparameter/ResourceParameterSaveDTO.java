package com.softserve.rms.dto.resourceparameter;

import com.softserve.rms.entities.ParameterType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceParameterSaveDTO {
    private String name;
    private ParameterType parameterType;
    private String pattern;
    private Long resourceTemplateId;
    private ResourceRelationDTO resourceRelationDTO;
}