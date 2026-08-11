package io.everyonecodes.project_module.services;

import io.everyonecodes.project_module.dtos.requests.ProductRequest;
import io.everyonecodes.project_module.dtos.responses.ProductResponse;
import io.everyonecodes.project_module.exceptions.ResourceNotFoundException;
import io.everyonecodes.project_module.models.Category;
import io.everyonecodes.project_module.models.Product;
import io.everyonecodes.project_module.models.User;
import io.everyonecodes.project_module.repositories.CategoryRepository;
import io.everyonecodes.project_module.repositories.ProductRepository;
import io.everyonecodes.project_module.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_Success() {
        // inputs and mock behaviors
        Long userId = 1L;
        Long categoryId = 5L;

        ProductRequest request = ProductRequest.builder()
                .name("Nude Brown Honey")
                .brand("MAC")
                .categoryId(categoryId)
                .openingDate(LocalDate.now())
                .periodAfterOpeningMonths(12)
                .startingWeightGrams(BigDecimal.valueOf(67.8))
                .build();

        User user = User.builder().id(userId).username("Felix").build();
        Category category = Category.builder().id(categoryId).name("Lipstick").build();

        Product savedProduct = Product.builder()
                .id(1L)
                .user(user)
                .category(category)
                .name(request.getName())
                .brand(request.getBrand())
                .openingDate(request.getOpeningDate())
                .periodAfterOpeningMonths(request.getPeriodAfterOpeningMonths())
                .startingWeightGrams(request.getStartingWeightGrams())
                .currentWeightGrams(request.getStartingWeightGrams())
                .isFinished(false)
                .build();

        // behavioral instructions
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse response = productService.createProduct(userId, request);

        // check results
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCategoryName()).isEqualTo("Lipstick");
        assertThat(response.getCurrentWeightGrams()).isEqualTo(BigDecimal.valueOf(67.8));
        assertThat(response.isFinished()).isFalse();

        // check execution
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_ThrowsException_WhenUserNotFound() {
        Long userId = 67L;
        ProductRequest request = ProductRequest.builder().categoryId(1L).build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(userId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User with ID 67 not found");

        verify(productRepository, never()).save(any(Product.class));
    }
}
