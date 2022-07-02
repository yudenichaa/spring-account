package dev.nightzen.account.persistance;

import dev.nightzen.account.business.entity.SecurityEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityEventRepository extends CrudRepository<SecurityEvent, Long> {
    Iterable<SecurityEvent> findByOrderByIdAsc();
}
