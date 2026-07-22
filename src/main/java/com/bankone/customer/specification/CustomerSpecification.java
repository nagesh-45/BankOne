package com.bankone.customer.specification;

import com.bankone.common.util.BusinessIdFormatter;
import com.bankone.customer.entity.Customer;
import org.springframework.data.jpa.domain.Specification;

public final class CustomerSpecification {

    private CustomerSpecification() {
    }

    public static Specification<Customer> containsText(String search) {

        return (root, query, criteriaBuilder) -> {

            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String trimmed = search.trim();
            Long codedId = BusinessIdFormatter.parseCustomerId(trimmed);

            if (codedId != null) {
                return criteriaBuilder.equal(root.get("customerId"), codedId);
            }

            String value = "%" + trimmed.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), value),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), value),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), value),
                    criteriaBuilder.like(root.get("phoneNumber"), value),
                    criteriaBuilder.like(criteriaBuilder.toString(root.get("customerId")), value)
            );
        };
    }
}
