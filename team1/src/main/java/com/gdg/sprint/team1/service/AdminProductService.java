package com.gdg.sprint.team1.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.gdg.sprint.team1.dto.admin.CreateProductRequest;
import com.gdg.sprint.team1.dto.product.PaginationInfo;
import com.gdg.sprint.team1.dto.product.ProductDetailDto;
import com.gdg.sprint.team1.dto.product.ProductListDto;
import com.gdg.sprint.team1.dto.product.ProductListResponse;
import com.gdg.sprint.team1.dto.product.SearchInfo;
import com.gdg.sprint.team1.dto.admin.UpdateProductRequest;
import com.gdg.sprint.team1.entity.Product;
import com.gdg.sprint.team1.exception.ProductNotFoundException;
import com.gdg.sprint.team1.repository.ProductRepository;
import com.gdg.sprint.team1.repository.ProductSpecs;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public ProductListResponse getAdminProductList(
        Integer page, Integer limit, Product.ProductStatus status, String search) {

        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (limit != null && limit > 0 && limit <= 100) ? limit : 20;

        Pageable pageable = PageRequest.of(
            pageNumber,
            pageSize,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<Product> spec = (root, query, criteriaBuilder) ->
            criteriaBuilder.conjunction();

        if (search != null && !search.isBlank()) {
            spec = spec.and(ProductSpecs.search(search));
        }

        if (status != null) {
            spec = spec.and(ProductSpecs.status(status.name()));
        }

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductListDto> items = productPage.getContent()
            .stream()
            .map(this::toListDto)
            .toList();

        PaginationInfo pagination = new PaginationInfo(
            page != null ? page : 1,
            pageSize,
            productPage.getTotalElements(),
            productPage.getTotalPages(),
            productPage.hasNext(),
            productPage.hasPrevious()
        );

        SearchInfo searchInfo = new SearchInfo(
            search,
            (int) productPage.getTotalElements(),
            status != null ? Map.of("status", status) : Map.of()
        );

        return new ProductListResponse(items, searchInfo, pagination);
    }

    @Transactional(readOnly = true)
    public ProductDetailDto getAdminProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        return toDetailDto(product);
    }

    @Transactional
    public ProductDetailDto createProduct(CreateProductRequest request, MultipartFile image) {
        String imageUrl = s3Service.uploadFile(image);

        Product product = Product.create(
            request.name(),
            request.description(),
            request.price(),
            request.stock(),
            imageUrl
        );

        Product saved = productRepository.save(product);
        log.info("상품 생성 완료: {}", saved.getId());

        return toDetailDto(saved);
    }

    @Transactional
    public ProductDetailDto updateProduct(Long productId, UpdateProductRequest request, MultipartFile image) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        product.update(
            request.name(),
            request.description(),
            request.price(),
            request.stock()
        );

        if (image != null && !image.isEmpty()) {
            if (product.getImageUrl() != null) {
                s3Service.deleteFile(product.getImageUrl());
            }

            String newImageUrl = s3Service.uploadFile(image);
            product.updateImageUrl(newImageUrl);
        }

        log.info("상품 수정 완료: {}", productId);
        return toDetailDto(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        product.markAsInactive();
        log.info("상품 비활성화 완료: {}", productId);
    }

    private ProductListDto toListDto(Product p) {
        return new ProductListDto(
            p.getId(),
            p.getName(),
            p.getDescription(),
            p.getPrice(),
            p.getStock(),
            p.getProductStatus().name(),
            p.getImageUrl(),
            p.getCreatedAt(),
            p.getUpdatedAt()
        );
    }

    private ProductDetailDto toDetailDto(Product p) {
        return new ProductDetailDto(
            p.getId(),
            p.getName(),
            p.getDescription(),
            p.getPrice(),
            p.getStock(),
            p.getProductStatus().name(),
            p.getImageUrl(),
            p.getCreatedAt(),
            p.getUpdatedAt()
        );
    }
}