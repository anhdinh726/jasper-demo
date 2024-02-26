package org.aptech.jasper_report.entity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private String itemName;
    private Integer price;
}

