package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sensors")
@Getter
@Setter @NoArgsConstructor @AllArgsConstructor
public class Sensor {
    @Id
    private String id;

    private String hubId;
}
