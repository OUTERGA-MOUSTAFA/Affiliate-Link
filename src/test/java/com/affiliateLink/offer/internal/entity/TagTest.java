package com.affiliateLink.offer.internal.entity;          // ① نفس الـ package ديال Tag

import static org.assertj.core.api.Assertions.assertThat; // ② AssertJ

import org.junit.jupiter.api.Test;

class TagTest {                                            // ③ السمية: <الكلاس> + Test

    @Test                                                  // ④ JUnit: "هادي ميثود اختبار"
    void normalizeRemovesSpacesAndLowercases() {           // ⑤ السمية كتوصف السلوك

        // Given : كنوجدو الحالة
        String raw = "  JaVa ";

        // When : الفعل، سطر واحد كيعيط على الكلاس اللي كنختبرو
        String normalized = Tag.normalize(raw);

        // Then : كنتأكدو من النتيجة
        assertThat(normalized).isEqualTo("java");
    }
}