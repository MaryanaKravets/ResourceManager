package com.softserve.rms.dto.resource;

import com.softserve.rms.entities.ResourceParameter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDTO {
    private Long id;
    private String name;
    private String description;
    private Long resourceTemplateId;
    private Long userId;
    private HashMap<String, Object> parameters;
}