package com.alfa.alfadairy.account;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByUserId(String userId);

    boolean existsByEmail(String email);

    Account findByEmail(String email);

    Account findByUserId(String userId);
}
