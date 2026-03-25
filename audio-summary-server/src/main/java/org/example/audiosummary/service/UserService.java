package org.example.audiosummary.service;


import org.example.audiosummary.entity.User;

public interface UserService {
    User createUser(String email);
    User getUserById(Long id);
    User getUserByEmail(String email);
}