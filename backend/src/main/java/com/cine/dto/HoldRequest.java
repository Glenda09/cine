package com.cine.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class HoldRequest {
    @NotNull
    private Long funcionId;
    @NotEmpty
    private List<Long> asientoIds;
}

