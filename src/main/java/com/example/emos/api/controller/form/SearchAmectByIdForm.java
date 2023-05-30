package com.example.emos.api.controller.form;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "根据ID查询罚款记录表单")
public class SearchAmectByIdForm {

    @NotNull
    @Min(value = 1, message = "id不能小于1")
    private Integer id;

}
