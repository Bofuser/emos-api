package com.example.emos.api.service.impl;

import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbRoleDao;
import com.example.emos.api.db.pojo.TbRole;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private TbRoleDao roleDao;

    @Override
    public ArrayList<HashMap> searchAllRole() {
        ArrayList<HashMap> list = roleDao.searchAllRole();
        return list;
    }

    /**
     * 通过id查询将数据渲染到添加或修改表单中
     * @param id
     * @return
     */
    @Override
    public HashMap searchById(int id) {
        HashMap map = roleDao.searchById(id);
        return map;
    }

    /**
     * 查询角色页数
     * @param param{roleName,page,length,start}
     * @return pageUtils{list, totalCount, pageIndex, pageSize}
     */
    @Override
    public PageUtils searchRolePage(HashMap param) {//controller层中传送的数据param{roleName,page,length,start}

        //接收Dao层返回的数据
        ArrayList<HashMap> list = roleDao.searchRolePage(param);
        long count = roleDao.searchRoleCount(param);
        //获取计算好的start和length
        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");
        //同通过条件查询获得的Dao数据保存到pageUtils中，传到controller层中。注意，pageUtils工具类将这四个参数封装为:page{list, totalCount, pageIndex, pageSize}
        PageUtils pageUtils = new PageUtils(list, count, start, length);

        return pageUtils;
    }


    /**
     * 插入角色语句，插入成功返回rows
     * @param role
     * @return
     */
    @Override
    public int insert(TbRole role) {
        int rows = roleDao.insert(role);
        return rows;
    }

    /**
     * 查询部门中的user用户id，并把他们踢下线
     * @param roleId
     * @return list
     */
    @Override
    public ArrayList<Integer> searchUserIdByRoleId(int roleId) {
        ArrayList<Integer> list = roleDao.searchUserIdByRoleId(roleId);
        return list;
    }

    /**
     * 修改用户信息
     * @param role
     * @return rows
     */
    @Override
    public int update(TbRole role) {

        int rows = roleDao.update(role);
        return rows;
    }

    /**
     * 删除用户角色，先判断其是否内置角色再删除
     * @param ids
     * @return
     */
    @Override
    public int deleteRoleByIds(Integer[] ids) {

        //判断其是否是内联角色
        if(!roleDao.searchCanDelete(ids)){
            throw new EmosException("无法删除关联用户的角色");
        }
        //删除成功返回rows
        int rows = roleDao.deleteRoleByIds(ids);
        return rows;
    }


}
