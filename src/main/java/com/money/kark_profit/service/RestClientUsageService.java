package com.money.kark_profit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestClientUsageService {

//    private final RestClientFactory restClientFactory;
//
//    public TestSuccess sendHttpPostRequest() {
//        RestClientHttpProvider client = restClientFactory.getClient("test-application");
//        return client.post(
//                "http://localhost:8083/mock-success",
//                null,
//                TestSuccess.class);
//    }
//
//    public AccountDetailResponse sendHttpGetRequest() {
//        RestClientHttpProvider client = restClientFactory.getClient("test-application");
//        return client.get(
//                "http://localhost:8083/check-account",
//                AccountDetailResponse.class);
//    }
//
//    //Retry
//    public TestSuccess sendHttpPostRequestErrorRetry() {
//        RestClientHttpProvider client = restClientFactory.getClient("test-application");
//        return client.post(
//                "http://localhost:8083/mock-error",
//                null,
//                TestSuccess.class);
//    }
//
//    public TestSuccess sendHttpPostRequestTimeoutRetry() {
//        RestClientHttpProvider client = restClientFactory.getClient("test-application");
//        return client.post(
//                "http://localhost:8083/mock-timeout",
//                null,
//                TestSuccess.class);
//    }
//
//    public GenericResponse sendHttpGetRequestOrds() {
//        RestClientHttpProvider client = restClientFactory.getClient("ords-connector");
//        return client.get(
//                "http://10.123.16.13:7012/ords/FCUBS_REST/LoanAccountEnquiry/loan_account_number/9991008038765",
//                GenericResponse.class);
//    }
}
