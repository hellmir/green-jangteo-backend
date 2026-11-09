package com.firstone.greenjangteo.user.domain.store.repository;

import com.firstone.greenjangteo.user.domain.store.model.StoreName;
import com.firstone.greenjangteo.user.domain.store.model.entity.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static com.firstone.greenjangteo.user.domain.store.testutil.StoreTestConstant.STORE_NAME1;
import static com.firstone.greenjangteo.user.domain.store.testutil.StoreTestConstant.STORE_NAME2;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class StoreRepositoryTest {
    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DisplayName("대상 가게 이름의 존재 여부를 확인할 수 있다.")
    @Test
    void existsByStoreName() {
        // given
        StoreName storeName1 = StoreName.of(STORE_NAME1);
        StoreName storeName2 = StoreName.of(STORE_NAME2);

        Store store = Store.of(1L, STORE_NAME1);
        storeRepository.save(store);

        // when
        boolean result1 = storeRepository.existsByStoreName(storeName1);
        boolean result2 = storeRepository.existsByStoreName(storeName2);

        // then
        assertThat(result1).isTrue();
        assertThat(result2).isFalse();
    }
}