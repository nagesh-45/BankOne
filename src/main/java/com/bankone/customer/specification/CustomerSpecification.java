
package com.bankone.customer.specification;

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

            String value = "%" + search.toLowerCase() + "%";

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
