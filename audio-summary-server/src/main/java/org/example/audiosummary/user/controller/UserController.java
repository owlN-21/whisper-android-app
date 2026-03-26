package org.example.audiosummary.user.controller;

import jakarta.validation.Valid;
import org.example.audiosummary.user.dto.CreateUserRequest;
import org.example.audiosummary.user.dto.UserResponse;
import org.example.audiosummary.entity.User;
import org.example.audiosummary.user.mapper.UserMapper;
import org.example.audiosummary.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request){
        User user = userService.createUser(request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id){
        User user = userService.getUserById(id);
        return  ResponseEntity.ok(userMapper.toResponse(user));
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email){
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id){
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
