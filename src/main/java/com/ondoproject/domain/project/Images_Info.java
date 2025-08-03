package com.ondoproject.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Images_Info {

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // auto increment
    private long id;
    private String ImageName;
    private boolean isShow;
    private String projectDescription;
    private String createDateTime;
    private boolean isBasic;
    private int index;
    private String imageURL;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private Project project;

    // ImageTag와의 관계 설정 - cascade로 자동 삭제 처리
    @OneToMany(mappedBy = "imagesInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ImageTag> imageTags;

}
