package com.affiliateLink.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void notFoundReturns404() throws Exception {
        mockMvc.perform(get("/test/not-found")).andExpect(status().isNotFound());
    }

    @Test
    void conflictReturns409() throws Exception {
        mockMvc.perform(get("/test/conflict")).andExpect(status().isConflict());
    }

    @Test
    void illegalArgumentReturns400() throws Exception {
        mockMvc.perform(get("/test/illegal")).andExpect(status().isBadRequest());
    }

    @Test
    void unexpectedErrorReturns500() throws Exception {
        mockMvc.perform(get("/test/boom")).andExpect(status().isInternalServerError());
    }

    @RestController
    static class ThrowingController {
        @GetMapping("/test/not-found") void notFound() { throw new ResourceNotFoundException("x"); }
        @GetMapping("/test/conflict")  void conflict()  { throw new ConflictException("x"); }
        @GetMapping("/test/illegal")   void illegal()   { throw new IllegalArgumentException("x"); }
        @GetMapping("/test/boom")      void boom()      { throw new IllegalStateException("x"); }
    }
}