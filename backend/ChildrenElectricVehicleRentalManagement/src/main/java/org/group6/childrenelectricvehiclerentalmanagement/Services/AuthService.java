package org.group6.childrenelectricvehiclerentalmanagement.Services;

import lombok.RequiredArgsConstructor;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.UserNotExitsException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    public User GetUser(long id) throws Exception {
        return userRepository.findById(id).orElseThrow(() -> new UserNotExitsException(id));
    }
    public User GetUser(int id) throws Exception{
        return this.GetUser(Long.parseLong(String.valueOf(id)));
    }
}
