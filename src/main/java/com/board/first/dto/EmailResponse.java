package com.board.first.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailResponse {
    private boolean duplicated;
    private String code;
}
