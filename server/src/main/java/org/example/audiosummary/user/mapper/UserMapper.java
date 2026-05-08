package org.example.audiosummary.user.mapper;

import org.example.audiosummary.user.dto.UserResponse;
import org.example.audiosummary.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}