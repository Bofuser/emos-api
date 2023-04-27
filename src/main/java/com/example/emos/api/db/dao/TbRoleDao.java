package com.example.emos.api.db.dao;

import com.example.emos.api.db.pojo.TbRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;


@Mapper
public interface TbRoleDao {

    public ArrayList<HashMap> searchAllRole();

    public HashMap searchById(int id);

    /**
     * 查询角色页数
     * @param param
     * @return list
     */
    public ArrayList<HashMap> searchRolePage (HashMap param);

    /**
     * 查询角色数量
     * @param param
     * @return count
     */
    public long searchRoleCount (HashMap param);

    /**
     *插入新部门
     * @param role
     * @return rows
     */
    public int insert(TbRole role);

    /**
     * 查询某个角色关联的用户，要把他踢下线，所以要把这些用户查询出来
     * @param roleId
     * @return
     */
    public ArrayList<Integer> searchUserIdByRoleId(int roleId);

    /**
     * 修改部门信息，但不能修改超级管理员
     * @param role
     * @return
     */
    public int update(TbRole role);

    /**
     * 用来查询判断是否可以删除的id
     * @param ids
     * @return
     */
    public boolean searchCanDelete(Integer[] ids);

    /**
     * 删除角色
     * @param ids
     * @return rows
     */
    public int deleteRoleByIds(Integer[] ids);

}