package com.campusflow.dao;

import com.campusflow.model.User;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findByUsername(String username);
    Optional<User> findById(int id);
    boolean save(User user);
    boolean update(User user);
}

