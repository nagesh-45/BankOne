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

            // 1) Business code: C00009 / c00009 / C9
            Long codedId = BusinessIdFormatter.parseCustomerId(trimmed);
            if (codedId != null) {
                return criteriaBuilder.equal(root.get("customerId"), codedId);
            }

            // 3) Raw numeric id: 9
            Long rawId = parseRawId(trimmed);
            if (rawId != null) {
                return criteriaBuilder.equal(root.get("customerId"), rawId);
            }

            String value = "%" + trimmed.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), value),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), value),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), value),
                    criteriaBuilder.like(root.get("phoneNumber"), value)
            );
        };
    }

    private static Long parseRawId(String value) {
        if (!value.matches("\\d+")) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
