package com.ondoproject.dto.image;

import lombok.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ImagesDTO {
    private Long id;
    private String imageURL;
    private String fileName;
    private String createDateTime;
    private String projectName;
    private String description;
    private int index;
    private boolean isShow;
    private boolean isBasic;
    private List<TagInfo> tags;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class TagInfo {
        private Long id;
        private String tagName;
        private String createDateTime;
    }
}
