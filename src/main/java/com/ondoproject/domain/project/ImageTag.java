package com.ondoproject.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ondoproject.domain.tag.Tag;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "image_tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "images_info_id", referencedColumnName = "id")
    @JsonIgnore
    private Images_Info imagesInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", referencedColumnName = "id")
    private Tag tag;

    // 생성 시간 등 추가 필드가 필요하면 여기에 추가
    private String createDateTime;

    // 편의 메서드
    public static ImageTag createImageTag(Images_Info imagesInfo, Tag tag) {
        return ImageTag.builder()
                .imagesInfo(imagesInfo)
                .tag(tag)
                .createDateTime(java.time.LocalDateTime.now().toString())
                .build();
    }
}
