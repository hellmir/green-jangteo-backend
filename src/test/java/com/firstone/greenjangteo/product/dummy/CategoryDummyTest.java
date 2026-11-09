package com.firstone.greenjangteo.product.dummy;

import com.firstone.greenjangteo.product.domain.model.Category;
import com.firstone.greenjangteo.product.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryDummyTest {
    private static final int CATEGORY_SIZE = 19;

    @InjectMocks
    private CategoryDummy categoryDummy;

    @Mock
    private CategoryRepository categoryRepository;

    @DisplayName("카테고리가 비어 있으면 기본 카테고리를 저장한다.")
    @Test
    void saveCategoriesWhenEmpty() {
        // given
        when(categoryRepository.count()).thenReturn(0L);

        // when
        categoryDummy.run(new DefaultApplicationArguments());

        // then
        verify(categoryRepository, times(CATEGORY_SIZE)).save(any(Category.class));
    }

    @DisplayName("카테고리가 이미 있으면 재기동해도 중복 저장하지 않는다.")
    @Test
    void doNotSaveCategoriesTwice() {
        // given
        when(categoryRepository.count()).thenReturn((long) CATEGORY_SIZE);

        // when
        categoryDummy.run(new DefaultApplicationArguments());

        // then
        verify(categoryRepository, never()).save(any(Category.class));
    }
}
