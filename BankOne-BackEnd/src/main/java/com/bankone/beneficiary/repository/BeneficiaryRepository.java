package com.bankone.beneficiary.repository;

import com.bankone.beneficiary.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByCustomerIdAndActiveTrueOrderByNicknameAsc(Long customerId);

    Optional<Beneficiary> findByBeneficiaryIdAndCustomerId(Long beneficiaryId, Long customerId);
}
