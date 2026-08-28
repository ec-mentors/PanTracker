package io.everyonecodes.project_module.services;

import io.everyonecodes.project_module.dtos.requests.ProductRequest;
import io.everyonecodes.project_module.dtos.responses.ProductResponse;
import io.everyonecodes.project_module.exceptions.ResourceNotFoundException;
import io.everyonecodes.project_module.models.Category;
import io.everyonecodes.project_module.models.Product;
import io.everyonecodes.project_module.models.User;
import io.everyonecodes.project_module.repositories.CategoryRepository;
import io.everyonecodes.project_module.repositories.ProductRepository;
import io.everyonecodes.project_module.repositories.UsageLogRepository;
import io.everyonecodes.project_module.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final UsageLogRepository usageLogRepository;

    //creating a new product
    @Transactional
    public ProductResponse createProduct(Long userId, ProductRequest request) {
        // check if User and Category exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found."));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category with ID " + request.getCategoryId() + " not found."));

        // map DTO to Product model
        Product product = Product.builder()
                .user(user)
                .category(category)
                .name(request.getName())
                .brand(request.getBrand())
                .purchaseDate(request.getPurchaseDate())
                .openingDate(request.getOpeningDate())
                .periodAfterOpeningMonths(request.getPeriodAfterOpeningMonths())
                .startingWeightGrams(request.getStartingWeightGrams())
                .currentWeightGrams(request.getStartingWeightGrams())
                .rating(request.getRating())
                .isFinished(false)
                .build();

        // save and return as response
        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    // get all active products of a user
    @Transactional(readOnly = true)
    public List<ProductResponse> getActiveCollection(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User with ID " + userId + " not found.");
        }

        return productRepository.findByUserIdAndIsFinished(userId, false)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // get all finished products of a user
    @Transactional(readOnly = true)
    public List<ProductResponse> getEmptiesCollection(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User with ID " + userId + " not found.");
        }

        return productRepository.findByUserIdAndIsFinished(userId, true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // get single product by id
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found."));

        return mapToResponse(product);
    }

    // update product details
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        // fetch product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found."));

        // fetch category if changed in the update form
        if (!product.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category with ID " + request.getCategoryId() + " not found."));
            product.setCategory(category);
        }

        // update mutable catalog fields
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setPurchaseDate(request.getPurchaseDate());
        product.setOpeningDate(request.getOpeningDate());
        product.setPeriodAfterOpeningMonths(request.getPeriodAfterOpeningMonths());
        product.setStartingWeightGrams(request.getStartingWeightGrams());
        product.setRating(request.getRating());

        if (request.getIsFinished() != null) {
            product.setFinished(request.getIsFinished());

            if (request.getIsFinished()) {
                product.setCurrentWeightGrams(BigDecimal.ZERO);
            }
        }

        // save and return response DTO
        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    // delete product from db
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found."));

        productRepository.delete(product);
    }

    private ProductResponse mapToResponse(Product product) {
        long totalUses = usageLogRepository.countByProductId(product.getId());
        LocalDate expirationDate = product.getOpeningDate()
                .plusMonths(product.getPeriodAfterOpeningMonths());

        boolean expired = LocalDate.now().isAfter(expirationDate);

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .categoryName(product.getCategory().getName())
                .purchaseDate(product.getPurchaseDate())
                .openingDate(product.getOpeningDate())
                .periodAfterOpeningMonths(product.getPeriodAfterOpeningMonths())
                .startingWeightGrams(product.getStartingWeightGrams())
                .currentWeightGrams(product.getCurrentWeightGrams())
                .rating(product.getRating())
                .isFinished(product.isFinished())
                .totalUses((int) totalUses)
                .expirationDate(expirationDate)
                .expired(expired)
                .build();
    }
}
