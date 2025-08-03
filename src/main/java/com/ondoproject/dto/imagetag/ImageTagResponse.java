package com.ondoproject.dto.imagetag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ImageTagResponse {
    private Long id;
    private Long imageId;
    private Long tagId;
    private String tagName;
    private String createDateTime;
}
