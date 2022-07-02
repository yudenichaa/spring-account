package dev.nightzen.account.persistance;

import dev.nightzen.account.business.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    List<User> findByOrderByIdAsc();
}
