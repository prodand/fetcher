package org.own.main.rest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.own.main.entities.CaseDoc;
import org.own.main.repos.CaseRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CasesController {

    CaseRepo caseRepo;

    @GetMapping("cases")
    public ResponseEntity listAll() {
        List<CaseDoc> entities = caseRepo.findAll();
        return ResponseEntity.ok(entities);
    }
}
