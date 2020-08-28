package org.own.main.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.own.main.config.HeaderValues;
import org.own.main.entities.CaseDoc;
import org.own.main.entities.HistoryDoc;
import org.own.main.repos.CaseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.SocketUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class FetcherService {

    @Autowired
    HeaderValues headerValues;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    CaseRepo caseRepo;

    DateFormat format = new SimpleDateFormat("MM/dd/yyyy");

    public void checkExisting() throws InterruptedException {
        for (CaseDoc doc : caseRepo.findAllByStatusIsNotIn(asList("Case Was Approved", "Card Was Mailed To Me"))) {
            checkCaseAnonymous(doc);
            Thread.sleep(1500);
        }
    }

    public void fetchCase(String caseNumber) {
        ResponseEntity<String> response = requestCaseByNumber(caseNumber);
//        ResponseEntity<String> response = getCaseByNumber(caseNumber);
        CaseDoc res = processAnonymous(caseNumber, response.getBody());
        if (res != null) {
            caseRepo.save(res);
        }
    }

    private void checkCaseAnonymous(CaseDoc caseDoc) {
        ResponseEntity<String> response = requestCaseByNumber(caseDoc.getNumber());
        CaseDoc res = processUpdateAnonymous(caseDoc, response.getBody());
        if (res != null) {
            caseRepo.save(res);
        }
    }

    private ResponseEntity<String> requestCaseByNumber(String caseNumber) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.set("Cache-Control", "no-cache");
        headers.set("Origin", "https://egov.uscis.gov");
        headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36");
        headers.set("Cookie", "JSESSIONID=9FDC9BAB16C3B25BF844E48FFBE4641B; 3cbb7032b4319f7a013efe7b56e63a2a=2e953c94e14b8bed5bd62168412c0f42; _ga=GA1.3.19917426.1597417041; _gid=GA1.3.378310820.1597417041");

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("changeLocale", "");
        map.add("completedActionsCurrentPage", "0");
        map.add("upcomingActionsCurrentPage", "0");
        map.add("appReceiptNum", caseNumber);
        map.add("caseStatusSearchBtn", "CHECK STATUS");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        return restTemplate.postForEntity("https://egov.uscis.gov/casestatus/mycasestatus.do",
            request, String.class);
    }

    private ResponseEntity<String> getCaseByNumber(String caseNumber) {
        try {
            return restTemplate.getForEntity(
                "https://egov.uscis.gov/casestatus/mycasestatus.do?appReceiptNum=" + caseNumber, String.class);
        } catch (HttpClientErrorException e) {
            log.error("Some error on case {}: {}", caseNumber, e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Some error on case {}: {}", caseNumber, e.getMessage());
            throw e;
        }
    }

    private void check(CaseDoc caseDoc) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", headerValues.getCookie());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange("https://egov.uscis.gov/casestatus/mycasestatus.do?appReceiptNum=" + caseDoc.getNumber(), HttpMethod.GET,
            entity, String.class);
        CaseDoc res = processWithHistory(caseDoc, response.getBody());
        if (res != null) {
            caseRepo.save(res);
        }
    }

    private CaseDoc processWithHistory(CaseDoc caseDoc, String document) {
        String caseNumber = caseDoc.getNumber();
        if (isLoginRequired(document)) {
            System.exit(0);
        }
        Document doc = Jsoup.parse(document);
        String headerStatus = doc.select(".uscis-seal .appointment-sec .rows h1").text();
        String status = doc.select(".uscis-seal .appointment-sec .rows p").text();
        if (!document.contains("-485") || !status.contains(caseNumber)) {
            log.info("No Found for {}: {}", caseNumber, headerStatus);
            return null;
        }
        if (Objects.equals(caseDoc.getStatus(), headerStatus) && !StringUtils.isEmpty(caseDoc.getPreviousStatus())) {
            log.info("No Updates for {}: {}", caseNumber, headerStatus);
            return null;
        }

        Matcher matcher = Pattern.compile("var completedActionListText = [\\W\\w]*];[\\W\\w]*var currentHistPage").matcher(document);
        Matcher matcherDate = Pattern.compile("var completedActionListDate = [\\W\\w]*];[\\W\\w]*var completedActionListText").matcher(document);
        List<HistoryDoc> historyDocs = new ArrayList<>();
        if (matcher.find() && matcherDate.find()) {
            String historyStr = matcher.group()
                .replace("var completedActionListText = [", "")
                .replace("var currentHistPage", "")
                .replace("];", "")
                .trim();
            String datesStr = matcherDate.group()
                .replace("var completedActionListDate = [", "")
                .replace("var completedActionListText", "")
                .replace("];", "")
                .trim();
            if (historyStr.length() > 4 && datesStr.length() > 4) {
                String[] history = historyStr.substring(1, historyStr.length() - 3).split("\",\"");
                String[] historyDates = datesStr.substring(1, datesStr.length() - 3).split("\",\"");
                try {
                    for (int i = 0; i < history.length; i++) {
                        historyDocs.add(HistoryDoc.builder()
                            .info(history[i])
                            .when(format.parse(historyDates[i]))
                            .build());
                    }
                } catch (Exception ex) {
                    log.error("Failed to parse history: {}", ex);
                }
            }
        }
        log.info("Updated {}: {}", caseNumber, headerStatus);
        return CaseDoc.builder()
            .id(caseDoc.getId())
            .number(caseNumber)
            .status(headerStatus)
            .description(status)
            .previousStatus(caseDoc.getStatus())
            .when(new Date())
            .history(historyDocs)
            .build();
    }

    private CaseDoc processUpdateAnonymous(CaseDoc caseDoc, String document) {
        String caseNumber = caseDoc.getNumber();
        Document doc = Jsoup.parse(document);
        String headerStatus = doc.select(".uscis-seal .appointment-sec .rows h1").text();
        String status = doc.select(".uscis-seal .appointment-sec .rows p").text();
        if (Objects.equals(caseDoc.getStatus(), headerStatus) && !StringUtils.isEmpty(caseDoc.getPreviousStatus())) {
            log.info("No Updates for {}: {}", caseNumber, headerStatus);
            return null;
        }
        log.info("Updated {}: {}", caseNumber, headerStatus);
        return CaseDoc.builder()
            .id(caseDoc.getId())
            .number(caseNumber)
            .status(headerStatus)
            .description(status)
            .previousStatus(caseDoc.getStatus())
            .when(new Date())
            .history(new ArrayList<>())
            .build();
    }

    private CaseDoc processAnonymous(String caseNumber, String document) {
        Document doc = Jsoup.parse(document);
        String headerStatus = doc.select(".uscis-seal .appointment-sec .rows h1").text();
        String status = doc.select(".uscis-seal .appointment-sec .rows p").text();
        if (!document.contains("-485") || !status.contains(caseNumber)) {
            log.info("No Found for {}: {}", caseNumber, headerStatus);
            return null;
        }

        CaseDoc result = CaseDoc.builder()
            .number(caseNumber)
            .status(headerStatus)
            .description(status)
            .when(new Date())
            .build();
        if (result.getDescription().contains("-485") || result.getStatus().contains("-485")
            || result.getHistory().stream().anyMatch(item -> item.getInfo().contains("-485"))) {
            log.info("Fetched for {}: {}", caseNumber, headerStatus);
            return result;
        }
        log.info("Not for I-485 - Case {}: {}", caseNumber, headerStatus);
        return null;
    }

    private boolean isLoginRequired(String document) {
        if (document.contains("Logout") && document.contains("My Cases")) {
            return false;
        }
        log.info("Session is expired");
        return true;
    }
}
