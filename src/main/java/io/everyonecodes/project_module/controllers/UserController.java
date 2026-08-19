package io.everyonecodes.project_module.controllers;

import io.everyonecodes.project_module.dtos.requests.UserRegisterRequest;
import io.everyonecodes.project_module.dtos.responses.UserResponse;
import io.everyonecodes.project_module.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        UserResponse response = userService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/login")
    ResponseEntity<UserResponse> loginUser (@RequestParam String username) {
        UserResponse response = userService.loginUser(username);

        return ResponseEntity.ok(response);
    }
}
