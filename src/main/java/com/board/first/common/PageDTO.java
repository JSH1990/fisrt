package com.board.first.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO {
    private int boardMSeq;
    private int nowPage = 0;
    private int totalCount = 0;
    private String searchCate = "";
    private String searchValue = "";

    public static PageDTO of(int boardMSeq, int nowPage, int totalCount, String searchCate, String searchValue){
        PageDTO dto = new PageDTO();
        dto.boardMSeq = boardMSeq;
        dto.nowPage = nowPage;
        dto.totalCount = totalCount;
        dto.searchCate = searchCate;
        dto.searchValue = searchValue;
        return dto;
    }
}
