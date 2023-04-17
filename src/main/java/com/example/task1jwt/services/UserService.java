package com.example.task1jwt.services;

import com.example.task1jwt.domains.User;
import com.example.task1jwt.domains.UserDetailsImpl;
import com.example.task1jwt.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return UserDetailsImpl.build(user);
    }

    public boolean existsUserByUsername(String username) {
        return repository.existsByUsername(username);
    }

    public boolean existsUserByEmail(String email) {
        return repository.existsByUsername(email);
    }

    public void createUser(User user) {
        repository.save(user);
    }
}
