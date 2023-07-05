package com.example.emos.api.db.pojo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TbAmectType {
    private Integer id;
    private String type;
    private BigDecimal money;
    //是否为系统内置的罚款类型
    private Boolean systemic;
}
