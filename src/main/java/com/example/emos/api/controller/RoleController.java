package com.example.emos.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.common.util.R;
import com.example.emos.api.controller.form.*;
import com.example.emos.api.db.pojo.TbRole;
import com.example.emos.api.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/role")
@Tag(name = "RoleController", description = "角色Web接口")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @GetMapping("/searchAllRole")
    @Operation(summary = "查询所有角色")
    public R searchAllRole() {
        ArrayList<HashMap> list = roleService.searchAllRole();
        return R.ok().put("list", list);
    }

    @PostMapping("/searchById")
    @Operation(summary = "根据ID查询角色")
    @SaCheckPermission(value = {"ROOT", "ROLE:SELECT"}, mode = SaMode.OR)
    public R searchById(@Valid @RequestBody SearchRoleByIdForm form) {
        HashMap map = roleService.searchById(form.getId());
        return R.ok(map);
    }


    @PostMapping("/searchRoleByPage")
    @Operation(summary = "查询角色分页数据")
    @SaCheckPermission(value = {"ROOT", "ROLE:SELECT"}, mode = SaMode.OR)
    public R searchRoleByPage(@Valid @RequestBody SearchRoleByPageForm form){   //form{roleName,page,length}

        //从form中获取page和length计算start
        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1) * length;    //page从1开始

        //将form表单中的数据转换成HashMap类型
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        //将start数据存储到param中
        param.put("start",start);
        //将param传到service层中，并获得返回的数据库中的值。pageUtils{list, totalCount, pageIndex, pageSize}
        PageUtils pageUtils = roleService.searchRolePage(param);
        //返回给前端页面
        return R.ok().put("page",pageUtils);

    }

    @PostMapping("/insert")
    @Operation(summary = "添加角色")
    @SaCheckPermission(value = {"ROOT", "ROLE:SELECT"}, mode = SaMode.OR)
    public R insert(@Valid @RequestBody InsertRoleForm form){  //form{roleName, permission, desc}

        //初始化类的对象
        TbRole role = new TbRole();
        //将前端传送过来的form中的属性值赋值到role对象中
        role.setRoleName(form.getRoleName());
        role.setPermissions(JSONUtil.parseArray(form.getPermissions()).toString());
        role.setDesc(form.getDesc());

        //获取后端传送过来的值rows
        int rows = roleService.insert(role);
        return R.ok().put("rows",rows);

    }

    @PostMapping("/update")
    @Operation(summary = "修改角色")
    @SaCheckPermission(value = {"ROOT", "ROLE:SELECT"}, mode = SaMode.OR)
    public R update(@Valid @RequestBody UpdateRoleForm form){   //form{id, roleName, permission, desc, change}

        //将form中属性存储为TbRole数据类型
        TbRole role = new TbRole();
        role.setId(form.getId());
        role.setRoleName(form.getRoleName());
        role.setPermissions(JSONUtil.parseArray(form.getPermissions()).toString());
        role.setDesc(form.getDesc());

        //修改成功后返回rows
        int rows = roleService.update(role);
        //如果修改成功，并且用户修改了该角色的关联权限
        if(rows == 1 && form.getChanged()){

            //把角色关联的用户踢下线
            ArrayList<Integer> list = roleService.searchUserIdByRoleId(form.getId());
            list.forEach(userId -> {
                StpUtil.logoutByLoginId(list);
            });
        }

        return R.ok().put("rows",rows);

    }


    @PostMapping("/deleteRoleByIds")
    @Operation(description = "删除角色记录")
    @SaCheckPermission(value = {"ROOT", "ROLE:SELECT"}, mode = SaMode.OR)
    public R deleteRoleByIds(@Valid @RequestBody DeleteRoleByIdsForm form){ //form{ids}

        //传送需要删除的角色ids
        int rows = roleService.deleteRoleByIds(form.getIds());

        return R.ok().put("rows",rows);


    }




}
