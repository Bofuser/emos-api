package com.example.emos.api.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Schema(description = "删除罚款类型")
@Data
public class DeleteAmectTypeByIdsForm {

    @NotEmpty(message = "ids不能为空")
    @Schema(description = "罚款ID")
    private Integer[] ids;
}
