package com.affiliateLink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;

import com.jayway.jsonpath.JsonPath;

public final class ProblemDetailMatchers {

    private ProblemDetailMatchers() {
    }

    // la réponse est bien un ProblemDetail avec le bon code, le bon titre et le bon content-type
    public static ResultMatcher problem(HttpStatus expected) {
        return result -> {
            status().is(expected.value()).match(result);
            content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON).match(result);
            jsonPath("$.status").value(expected.value()).match(result);
            jsonPath("$.title").value(expected.getReasonPhrase()).match(result);
        };
    }

    public static ResultMatcher detail(String expected) {
        return jsonPath("$.detail").value(expected);
    }

    public static ResultMatcher detailContains(String part) {
        return result -> {
            String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
            String detail = JsonPath.read(body, "$.detail");
            assertThat(detail).contains(part);
        };
    }
}