package com.firstone.greenjangteo.product.controller;

import com.firstone.greenjangteo.product.service.ProductService;
import com.firstone.greenjangteo.product.service.UnavailableProductSearchService;
import com.firstone.greenjangteo.user.security.CustomAuthenticationEntryPoint;
import com.firstone.greenjangteo.user.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = ProductController.class)
@Import(UnavailableProductSearchService.class)
@WithMockUser
class ProductSearchUnavailableTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @DisplayName("Elasticsearch가 없으면 상품명 검색은 503을 반환한다.")
    @Test
    void searchByProductName() throws Exception {
        mockMvc.perform(get("/products/keyword").param("keyword", "사과"))
                .andExpect(status().isServiceUnavailable());
    }

    @DisplayName("Elasticsearch가 없으면 카테고리 검색은 503을 반환한다.")
    @Test
    void searchByCategory() throws Exception {
        mockMvc.perform(get("/products/category").param("category", "과일"))
                .andExpect(status().isServiceUnavailable());
    }

    @DisplayName("Elasticsearch가 없으면 자동 완성은 503을 반환한다.")
    @Test
    void autoComplete() throws Exception {
        mockMvc.perform(get("/products/auto-complete").param("keyword", "사"))
                .andExpect(status().isServiceUnavailable());
    }

    @DisplayName("Elasticsearch가 없으면 상품 문서 저장은 503을 반환한다.")
    @Test
    void saveProductDocument() throws Exception {
        mockMvc.perform(post("/productDocuments").with(csrf()))
                .andExpect(status().isServiceUnavailable());
    }

    @DisplayName("Elasticsearch가 없으면 상품 일괄 등록은 503을 반환한다.")
    @Test
    void addProducts() throws Exception {
        mockMvc.perform(post("/products").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productSaveRequestList\":[]}"))
                .andExpect(status().isServiceUnavailable());
    }
}
