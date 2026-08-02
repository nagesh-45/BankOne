package com.bankone.sharding.lab;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShardLabCustomerRepository extends JpaRepository<ShardLabCustomer, UUID> {
}
