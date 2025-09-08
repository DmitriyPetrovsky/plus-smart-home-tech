package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.ShoppingStoreRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingStoreService {
    private final ShoppingStoreRepository shoppingStoreRepository;

    @Transactional
    public ProductDto addProduct(ProductDto product) {
        Product productDb = ProductMapper.mapToProduct(product);
        productDb = shoppingStoreRepository.save(productDb);
        return ProductMapper.mapToProductDto(productDb);
    }

    @Transactional(readOnly = true)
    public ProductDto findProductById(UUID id) {
        Product product = getProductFromStore(id);
        return ProductMapper.mapToProductDto(product);
    }

    @Transactional
    public ProductDto updateProduct(ProductDto product) {
        getProductFromStore(product.getProductId());
        Product productUpdated = ProductMapper.mapToProduct(product);
        productUpdated = shoppingStoreRepository.save(productUpdated);
        return ProductMapper.mapToProductDto(productUpdated);
    }

    @Transactional
    public void removeProductFromStore(UUID productId) {
        Product product = getProductFromStore(productId);
        product.setProductState(ProductState.DEACTIVATE);
        shoppingStoreRepository.save(product);
    }

    @Transactional
    public void setProductQuantityState(SetProductQuantityStateRequest request) {
        Product product = getProductFromStore(request.getProductId());
        product.setQuantityState(request.getQuantityState());
        shoppingStoreRepository.save(product);
    }



    private Product getProductFromStore(UUID productId) {
        return shoppingStoreRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException("Product is not found"));
    }

    @Transactional
    public Page<ProductDto> getProductsByCategory(ProductCategory category, Pageable pageable) {
        return shoppingStoreRepository.findAllByProductCategory(category, pageable)
                .map(ProductMapper::mapToProductDto);
    }
}
