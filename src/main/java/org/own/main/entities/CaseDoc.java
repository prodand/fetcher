package org.own.main.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document
public class CaseDoc {
    @Id
    String id;
    String number;
    String status;
    String description;
    LocalDate lastUpdate;
    Date when;
    String previousStatus;
    @Builder.Default
    List<HistoryDoc> history = new ArrayList<>();
}
