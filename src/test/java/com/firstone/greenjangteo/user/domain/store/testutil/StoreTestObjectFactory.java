package com.firstone.greenjangteo.user.domain.store.testutil;

import com.firstone.greenjangteo.product.domain.model.Category;
import com.firstone.greenjangteo.product.domain.model.Product;
import com.firstone.greenjangteo.user.domain.store.dto.StoreRequestDto;
import com.firstone.greenjangteo.user.domain.store.model.entity.Store;

import java.time.LocalDateTime;

public class StoreTestObjectFactory {
    private static final String FIRST_CATEGORY = "가전제품";
    private static final String SECOND_CATEGORY = "주방가전";

    public static Store createStore(Long sellerId, String storeName, String description, String imageUrl) {
        Store store = Store.of(sellerId, storeName);

        StoreRequestDto storeRequestDto = new StoreRequestDto(storeName, description, imageUrl);
        store.update(storeRequestDto);

        return store;
    }

    public static Product createProduct(Store store, String productName, int price, int inventory) {
        return Product.builder()
                .store(store)
                .name(productName)
                .price(price)
                .description(productName)
                .inventory(inventory)
                .salesRate(0)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }

    public static Category createCategory() {
        return Category.builder()
                .firstCategory(FIRST_CATEGORY)
                .secondCategory(SECOND_CATEGORY)
                .build();
    }
}
