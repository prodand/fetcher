package org.own.main.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.own.main.entities.CaseDoc;
import org.own.main.repos.CaseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class ParseService {

    @Autowired
    CaseRepo caseRepo;

    Pattern datePattern = Pattern.compile("(As of|On) [A-Z][a-z]{2,10} [0-9]{1,2}, [0-9]{4}");
    DateTimeFormatter format = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    public void updateDates() {
        for (CaseDoc doc : caseRepo.findAll()) {
            parseDate(doc);
        }
    }

    private void parseDate(CaseDoc doc) {
        Matcher matcher = datePattern.matcher(doc.getDescription());
        if (matcher.find()) {
            String dateStr = matcher.group()
                .replace("As of ", "")
                .replace("On ", "");
            LocalDate date = LocalDate.parse(dateStr, format);
            doc.setLastUpdate(date);
            caseRepo.save(doc);
        }
    }
}
