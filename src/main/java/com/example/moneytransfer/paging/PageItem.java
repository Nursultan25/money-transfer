package com.example.moneytransfer.paging;

import com.example.moneytransfer.Enums.PageItemType;
import lombok.*;


@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageItem {

    private PageItemType pageItemType;

    private int index;

    private boolean active;

}
