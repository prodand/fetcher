package org.own.main.repos;

import org.own.main.entities.CaseDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface CaseRepo extends MongoRepository<CaseDoc, Integer> {

    List<CaseDoc> findAllByStatusIsNotIn(List<String> exclude);
}
