package com.firstone.greenjangteo.product.dummy;

import com.firstone.greenjangteo.product.domain.dto.CategoryDto;
import com.firstone.greenjangteo.product.domain.model.Category;
import com.firstone.greenjangteo.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

@Profile("!vercel") // 콜드 스타트마다 실행되면 DB 왕복이 생기므로 Vercel에서는 스키마 생성 때 1회만 넣는다
@Component
@Transactional
@RequiredArgsConstructor
public class CategoryDummy implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    @Override
    public void run(ApplicationArguments args) {
        if (categoryRepository.count() > 0) {
            return;
        }

        String[] categoriesLevel1 = {
                "전자제품",
                "의류",
                "신발",
                "가구",
                "스포츠 용품",
                "도서",
                "음악 앨범",
                "화장품",
                "건강과 피트니스",
                "가정용품",
                "자동차 및 오토바이 부품",
                "액세서리 및 보석",
                "완구 및 게임",
                "예술 및 공예품",
                "여행 및 여행 용품",
                "반려동물 용품",
                "건축 및 건설 재료",
                "취미 및 수집품",
                "문구 및 사무용품"
        };

        String[] categoriesLevel2 = {
                "전자제품류",
                "가방",
                "신발류",
                "가구류",
                "스포츠용품류",
                "도서류",
                "앨범류",
                "화장품류",
                "피트니스용품류",
                "가정용품류",
                "자동차부품류",
                "액세서리류",
                "완구류",
                "공예품류",
                "여행용품류",
                "반려동물 용품류",
                "건축류",
                "수집품류",
                "문구류"
        };

        IntStream.range(0, categoriesLevel1.length)
                .mapToObj(i -> Category.of(CategoryDto.builder().firstCategory(categoriesLevel1[i]).secondCategory(categoriesLevel2[i]).build()))
                .forEach(categoryRepository::save);
    }
}