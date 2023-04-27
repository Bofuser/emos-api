package com.example.emos.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.common.util.R;
import com.example.emos.api.controller.form.*;
import com.example.emos.api.db.pojo.TbDept;
import com.example.emos.api.service.DeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/dept")
@Tag(name = "DeptController", description = "部门Web接口")
public class DeptController {

    @Autowired
    private DeptService deptService;

    @GetMapping("/searchAllDept")
    @Operation(summary = "查询所有部门")
    public R searchAllDept() {
        ArrayList<HashMap> list = deptService.searchAllDept();
        return R.ok().put("list", list);
    }

    @PostMapping("/searchById")
    @Operation(summary = "根据ID查询部门")
    @SaCheckPermission(value = {"ROOT", "DEPT:SELECT"}, mode = SaMode.OR)
    public R searchById(@Valid @RequestBody SearchDeptByIdForm form) {
        HashMap map = deptService.searchById(form.getId());
        return R.ok(map);
    }


    @PostMapping("/searchDeptByPage")
    @Operation(summary = "查询部门分页数据")
    @SaCheckPermission(value = {"ROOT", "DEPT:SELECT"}, mode = SaMode.OR)
    public R searchDeptByPage (@Valid @RequestBody SearchDeptByPageForm form){    //form{deptName, page, length }

        //将page和length提取出来
        int page = form.getPage();
        int length = form.getLength();
        //算出start
        int start = (page - 1) * length;
        //将form中的数据转换为HashMap形式
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);

        param.put("start", start);

        //获取根据param条件查询的数据
        PageUtils pageUtils = deptService.searchDeptByPage(param);

        return R.ok().put("page", pageUtils);

    }


    @PostMapping("/insert")
    @Operation(summary = "新增部门")
    @SaCheckPermission(value = {"ROOT", "DEPT:SELECT"}, mode = SaMode.OR)
    public R insert(@Valid @RequestBody InsertDeptForm form){   //form{deptName, tel, email, desc}

        //将form类型转换为TbDept类型
        TbDept dept = JSONUtil.parse(form).toBean(TbDept.class);
        //添加成功后返回rows
        int rows = deptService.insert(dept);
        return R.ok().put("rows",rows);

    }

    @PostMapping("/update")
    @Operation(summary = "修改部门")
    @SaCheckPermission(value = {"ROOT", "DEPT:SELECT"}, mode = SaMode.OR)
    public R update(@Valid @RequestBody UpdateDeptForm form){   //form{id, deptName, tel, email, desc}

        //声明TbDept对象
        TbDept dept = new TbDept();
        //将从前端发送过来的form数据存为TbDept对象，发给后端
        dept.setId(form.getId());
        dept.setDeptName(form.getDeptName());
        dept.setTel(form.getTel());
        dept.setEmail(form.getEmail());
        dept.setDesc(form.getDesc());
        //发送dept数据过去
        int rows = deptService.update(dept);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/deleteDeptByIds")
    @Operation(summary = "删除部门")
    @SaCheckPermission(value = {"ROOT", "DEPT:SELECT"}, mode = SaMode.OR)
    public R deleteDeptByIds(@Valid @RequestBody DeleteDeptByIdsForm form){ //form{ids}

        //获取form中的id，传过去
        int rows = deptService.deleteDeptByIds(form.getIds());
        return R.ok().put("rows", rows);

    }


}