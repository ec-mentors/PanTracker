package io.everyonecodes.project_module.fullIntegrationTests;

import io.everyonecodes.project_module.dtos.requests.UsageLogRequest;
import io.everyonecodes.project_module.models.*;
import io.everyonecodes.project_module.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UsageLogFullIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectProductRepository projectProductRepository;

    @Autowired
    private UsageLogRepository usageLogRepository;

    @Test
    void logUsageSuccessWithProjectAndWeightHitsZeroAutoFinishesProduct() throws Exception {
        // create all necessary requirements
        User user = userRepository.findByUsername("Stefan")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("Stefan")
                        .email("stefan@example.com")
                        .build()));

        Category category = categoryRepository.findByName("Eyeliner")
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name("Eyeliner")
                        .build()));

        Product product = productRepository.save(Product.builder()
                .user(user)
                .category(category)
                .name("Red Eyeliner")
                .brand("Gucci")
                .openingDate(LocalDate.of(2026, 1, 15))
                .periodAfterOpeningMonths(12)
                .startingWeightGrams(BigDecimal.valueOf(15.00))
                .currentWeightGrams(BigDecimal.valueOf(2.50))
                .isFinished(false)
                .build());

        Project project = projectRepository.save(Project.builder()
                .user(user)
                .name("Summer Project")
                .startDate(LocalDate.of(2026, 6, 1))
                .build());

        // link product to project in junction table
        ProjectProductId junctionId = new ProjectProductId(project.getId(), product.getId());
        projectProductRepository.save(ProjectProduct.builder()
                .id(junctionId)
                .project(project)
                .product(product)
                .goalType("USE_X_TIMES")
                .targetUses(10)
                .build());

        // construct HTTP request body
        UsageLogRequest request = UsageLogRequest.builder()
                .projectId(project.getId())
                .useDate(LocalDate.of(2026, 8, 28))
                .weightRecorded(BigDecimal.ZERO)
                .notes("Completely used up today!")
                .build();

        // execute HTTP request
        mockMvc.perform(post("/api/products/{productId}/logs", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.productName").value("Red Eyeliner"))
                .andExpect(jsonPath("$.projectId").value(project.getId()))
                .andExpect(jsonPath("$.weightRecorded").value(0))
                .andExpect(jsonPath("$.notes").value("Completely used up today!"));

        // check if log entry was written to the database
        assertThat(usageLogRepository.count()).isEqualTo(1);

        // check if product was marked finished in the DB because weight hit 0
        Optional<Product> dbProductOpt = productRepository.findById(product.getId());
        assertThat(dbProductOpt).isPresent();
        Product dbProduct = dbProductOpt.get();
        assertThat(dbProduct.getCurrentWeightGrams()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dbProduct.isFinished()).isTrue();

        // check if dynamic project usage count was correctly updated to 1
        long currentUses = usageLogRepository.countByProductIdAndProjectId(product.getId(), project.getId());
        assertThat(currentUses).isEqualTo(1);
    }

    @Test
    void logUsageFailureHeavierWeightRollsBackTransaction() throws Exception {
        User user = userRepository.findByUsername("Mustafa") // Use a different username to prevent conflicts
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("Mustafa")
                        .email("mustafa@example.com")
                        .build()));

        Category category = categoryRepository.findByName("Foundation")
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name("Foundation")
                        .build()));

        Product product = productRepository.save(Product.builder()
                .user(user)
                .category(category)
                .name("Silk Foundation")
                .openingDate(LocalDate.of(2026, 1, 15))
                .periodAfterOpeningMonths(12)
                .currentWeightGrams(BigDecimal.valueOf(15.00))
                .isFinished(false)
                .build());

        UsageLogRequest request = UsageLogRequest.builder()
                .weightRecorded(BigDecimal.valueOf(20.00))
                .build();

        mockMvc.perform(post("/api/products/{productId}/logs", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(usageLogRepository.count()).isEqualTo(0);

        Optional<Product> dbProductOpt = productRepository.findById(product.getId());
        assertThat(dbProductOpt).isPresent();
        assertThat(dbProductOpt.get().getCurrentWeightGrams()).isEqualByComparingTo(BigDecimal.valueOf(15.00));
    }
}