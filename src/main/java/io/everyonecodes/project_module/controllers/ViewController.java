package io.everyonecodes.project_module.controllers;

import io.everyonecodes.project_module.dtos.requests.*;
import io.everyonecodes.project_module.dtos.responses.UserResponse;
import io.everyonecodes.project_module.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final UserService userService;
    private final ProductService productService;
    private final ProjectService projectService;
    private final ProjectProductService projectProductService;
    private final UsageLogService usageLogService;
    private final CategoryService categoryService;

    @GetMapping("/login")
    public String showLoginPage() {
        // No need to add a loginRequest object to the model anymore!
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, Model model) {
        try {
            // Retrieve user profile directly using the parameter string
            UserResponse response = userService.loginUser(username);
            return "redirect:/dashboard/" + response.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Username not found.");
            model.addAttribute("username", username); // Pre-fills input field so they don't have to retype it
            return "login";
        }
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerRequest", new UserRegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute UserRegisterRequest request, Model model) {
        try {
            userService.registerUser(request);
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerRequest", request);
            return "register";
        }
    }

    @GetMapping("/dashboard/{userId}")
    public String showDashboard(@PathVariable Long userId, Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("products", productService.getActiveCollection(userId));
        model.addAttribute("projects", projectService.getProjectsByUser(userId));
        return "dashboard";
    }

    @GetMapping("/users/{userId}/collection")
    public String showCollection(@PathVariable Long userId, Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("products", productService.getActiveCollection(userId));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("productRequest", new ProductRequest());
        return "collection";
    }

    @PostMapping("/users/{userId}/collection/add")
    public String addProduct(@PathVariable Long userId, @ModelAttribute ProductRequest request) {
        productService.createProduct(userId, request);
        return "redirect:/users/" + userId + "/collection";
    }

    @GetMapping("/users/{userId}/empties")
    public String showEmpties(@PathVariable Long userId, Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("products", productService.getEmptiesCollection(userId));
        return "empties";
    }

    @GetMapping("/users/{userId}/projects")
    public String showProjectsList(@PathVariable Long userId, Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("projects", projectService.getProjectsByUser(userId));
        model.addAttribute("projectRequest", new ProjectRequest());
        return "projects";
    }

    @PostMapping("/users/{userId}/projects/add")
    public String addProject(@PathVariable Long userId, @ModelAttribute ProjectRequest request) {
        projectService.createProject(userId, request);
        return "redirect:/users/" + userId + "/projects";
    }

    @GetMapping("/projects/{projectId}/detail/{userId}")
    public String showProjectDetail(@PathVariable Long projectId, @PathVariable Long userId, Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("project", projectService.getProjectById(projectId));
        model.addAttribute("projectProducts", projectProductService.getProductsInProject(projectId));
        model.addAttribute("allActiveProducts", productService.getActiveCollection(userId));
        model.addAttribute("linkRequest", new ProjectProductLinkRequest());
        return "project-detail";
    }

    @PostMapping("/projects/{projectId}/edit/{userId}")
    public String editProject(@PathVariable Long projectId, @PathVariable Long userId, @ModelAttribute ProjectRequest request) {
        projectService.updateProject(projectId, request);
        return "redirect:/projects/" + projectId + "/detail/" + userId;
    }

    @PostMapping("/projects/{projectId}/add-product/{userId}")
    public String linkProduct(@PathVariable Long projectId, @PathVariable Long userId,
                              @RequestParam Long productId, @ModelAttribute ProjectProductLinkRequest request) {
        projectProductService.addProductToProject(projectId, productId, request);
        return "redirect:/projects/" + projectId + "/detail/" + userId;
    }

    @GetMapping("/products/{productId}/detail/{userId}")
    public String showProductDetail(
            @PathVariable Long productId,
            @PathVariable Long userId,
            @RequestParam(required = false) String returnUrl, // Capture return path
            Model model) {

        model.addAttribute("userId", userId);
        model.addAttribute("product", productService.getProductById(productId));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("history", usageLogService.getProductUsageHistory(productId));
        model.addAttribute("activeProjects", projectService.getProjectsByUser(userId));
        model.addAttribute("currentProjects", projectProductService.getProjectsForProduct(productId));

        // Safe Fallback: Default to collection view if returnUrl is missing
        String fallbackUrl = "/users/" + userId + "/collection";
        model.addAttribute("returnUrl", (returnUrl != null && !returnUrl.isEmpty()) ? returnUrl : fallbackUrl);

        model.addAttribute("logRequest", new UsageLogRequest());
        model.addAttribute("linkRequest", new ProjectProductLinkRequest());
        return "product-detail";
    }

    @PostMapping("/products/{productId}/edit/{userId}")
    public String editProduct(
            @PathVariable Long productId,
            @PathVariable Long userId,
            @RequestParam(required = false) String returnUrl, // Accept parameter
            @ModelAttribute ProductRequest request) {
        productService.updateProduct(productId, request);

        // Append returnUrl to redirect
        return "redirect:/products/" + productId + "/detail/" + userId + "?returnUrl=" + returnUrl;
    }

    @PostMapping("/products/{productId}/use/{userId}")
    public String useProduct(
            @PathVariable Long productId,
            @PathVariable Long userId,
            @RequestParam(required = false) String returnUrl, // Accept parameter
            @ModelAttribute UsageLogRequest request) {
        usageLogService.logUsage(productId, request);

        // Append returnUrl to redirect
        return "redirect:/products/" + productId + "/detail/" + userId + "?returnUrl=" + returnUrl;
    }

    @PostMapping("/products/{productId}/link-project/{userId}")
    public String linkProjectFromProductPage(
            @PathVariable Long productId,
            @PathVariable Long userId,
            @RequestParam Long projectId,
            @RequestParam(required = false) String returnUrl, // Accept parameter
            @ModelAttribute ProjectProductLinkRequest request) {
        projectProductService.addProductToProject(projectId, productId, request);

        // Append returnUrl to redirect
        return "redirect:/products/" + productId + "/detail/" + userId + "?returnUrl=" + returnUrl;
    }

    @PostMapping("/users/{userId}/categories/add")
    public String addCategory(@PathVariable Long userId, @RequestParam String categoryName) {
        try {
            categoryService.createCategory(new CategoryRequest(categoryName));
        } catch (IllegalArgumentException e) {
        }
        return "redirect:/users/" + userId + "/collection";
    }
}