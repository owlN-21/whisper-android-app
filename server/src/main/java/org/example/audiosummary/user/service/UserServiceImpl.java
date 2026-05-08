package org.example.audiosummary.user.service;

import org.example.audiosummary.entity.User;
import org.example.audiosummary.user.exception.UserAlreadyExistsException;
import org.example.audiosummary.user.exception.UserHasTasksException;
import org.example.audiosummary.user.exception.UserNotFoundException;
import org.example.audiosummary.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public User createUser(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException(email);
        }

        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setEmail(email);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!user.getProcessingTasks().isEmpty()) {
            throw new UserHasTasksException(id);
        }

        userRepository.delete(user);
    }
}
