package com.ondoproject.domain.about;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
public class About {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)  // auto increment
    private Long id;
    private String description;
    private String description2;
}
