package org.example.audiosummary.user.service;

import org.example.audiosummary.entity.User;

public interface UserService {
  User createUser(String email);

  User getUserById(Long id);

  User getUserByEmail(String email);

  void deleteUserById(Long id);
}
