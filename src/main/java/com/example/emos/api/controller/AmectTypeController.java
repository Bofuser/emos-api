package com.example.emos.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.common.util.R;
import com.example.emos.api.controller.form.*;
import com.example.emos.api.db.pojo.TbAmectType;
import com.example.emos.api.service.AmectTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/amect_type")
@Tag(name = "AmectTypeController", description = "罚款类型Web接口")
public class AmectTypeController {
    @Autowired
    private AmectTypeService amectTypeService;

    @GetMapping("/searchAllAmectType")
    @Operation(summary = "查询所有罚款类型")
    @SaCheckLogin
    public R searchAllAmectType() {
        ArrayList<TbAmectType> list = amectTypeService.searchAllAmectType();
        return R.ok().put("list", list);
    }

    @PostMapping("/searchAmectTypeByPage")
    @Operation(summary = "查询罚款类型分页记录")
    @SaCheckPermission(value = {"ROOT"})
    public R searchAmectTypeByPage(@Valid @RequestBody SearchAmectTypeByPageForm form){

        //获取查询分页的信息
        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1) * length;

        //将form 表单中的数据转换成 HashMap，封装到param中
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);

        //将start 数据存储到 param中
        param.put("start", start);

        //封装成PageUtils返回出去
        PageUtils pageUtils = amectTypeService.searchAmectTypeByPage(param);
        return R.ok().put("page", pageUtils);

    }

    @PostMapping("/insert")
    @Operation(summary = "添加罚款类型")
    @SaCheckPermission(value = {"ROOT"})
    public R insert(@Valid @RequestBody InsertAmectTypeForm form){

        TbAmectType tbAmectType = JSONUtil.parse(form).toBean(TbAmectType.class);
        int rows = amectTypeService.insert(tbAmectType);
        return R.ok().put("rows",rows);

    }

    @PostMapping("/searchById")
    @Operation(summary = "根据Id查询表单")
    @SaCheckPermission(value = {"ROOT"})
    public R searchById(@Valid @RequestBody searchAmectTypeByIdForm form){

        HashMap map = amectTypeService.searchById(form.getId());
        return R.ok(map);

    }

    @PostMapping("/update")
    @Operation(summary = "修改罚款类型")
    @SaCheckPermission(value = {"ROOT"})
    public R update(@Valid @RequestBody UpdateAmectTypeByIdForm form){

        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        int rows = amectTypeService.update(param);
        return R.ok().put("rows",rows);

    }

    @PostMapping("/delete")
    @Operation(summary = "删除罚款类型")
    @SaCheckPermission(value = {"ROOT"})
    public R delete(@Valid @RequestBody DeleteAmectTypeByIdsForm form){


        int rows = amectTypeService.deleteAmectTypeByIds(form.getIds());
        return R.ok().put("rows",rows);

    }



}
