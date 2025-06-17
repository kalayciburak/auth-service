package com.kalayciburak.authservice.repository;

import com.kalayciburak.authservice.model.entity.User;
import com.kalayciburak.commonjpa.repository.BaseRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends BaseRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
