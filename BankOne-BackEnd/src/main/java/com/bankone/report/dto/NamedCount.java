package com.bankone.report.dto;

import java.io.Serializable;

public record NamedCount(String name, long count) implements Serializable {
    private static final long serialVersionUID = 1L;
}
