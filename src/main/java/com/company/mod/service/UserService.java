package com.company.mod.service;

import com.company.mod.entity.User;
import com.company.mod.repository.UserRepository;
import com.company.mod.security.PasswordEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncryptionService passwordEncryptionService;

    public boolean isPasswordValid(String login, String password) throws Exception {
        User user = userRepository.findByLogin(login);
        if (user == null) {
            return false;
        }
        return passwordEncryptionService.validatePassword(password, user.getPassword());
    }

    @Transactional
    public boolean createUser(String login, String password) throws Exception {
        if (userExists(login)) {
            throw new IllegalArgumentException("User already exists");
        }
        String hashedPassword = passwordEncryptionService.generateHash(password);
        User user = new User();
        user.setLogin(login);
        user.setPassword(hashedPassword);
        userRepository.save(user);
        return true;
    }

    public boolean userExists(String login) {
        return userRepository.findByLogin(login) != null;
    }
}
