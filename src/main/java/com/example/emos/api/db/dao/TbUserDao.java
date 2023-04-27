package com.example.emos.api.db.dao;

import com.example.emos.api.db.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

@Mapper
public interface TbUserDao {
    public Set<String> searchUserPermissions(int userId);

    public HashMap searchById(int userId);

    public Integer searchIdByOpenId(String openId);

    public HashMap searchUserSummary(int userId);

    public HashMap searchUserInfo(int userId);

    public Integer searchDeptManagerId(int id);

    public Integer searchGmId();

    public ArrayList<HashMap> searchAllUser();

    /**
     * 查找登录id
     * @param param
     * @return
     */
    public Integer login(HashMap param);

    /**
     * 修改用户密码
     * @param param
     * @return
     */
    public int updatePassword(HashMap param);

    /**
     * 查询分页数据
     * @param param
     * @return
     */
    public ArrayList<HashMap> searchUserByPage (HashMap param);

    /**
     * 查询用户数
     * @param param
     * @return
     */
    public long searchUserCount (HashMap param);

    /**
     * 新增用户功能，插入用户数据，插入成功后返回rows,故用int声明函数
     * @param user
     * @return
     */
    public int insert(TbUser user);

    /**
     * 修改用户功能，和新增用户功能一样，修改成功后返回rows
     * @param param
     * @return
     */
    public int update(HashMap param);

    /**
     * 删除用户，传参用户id，删除成功后返回rows数
     * @param ids
     * @return
     */
    public int deleteUserByIds(Integer[] ids);

    /**
     * 查询用户角色，用来判断用户权限
     */
    public ArrayList<String> searchUserRoles(int userId);

    /**
     * 通过查询远端流取出里面的参会人userId
     * @param userId
     * @return
     */
    public HashMap searchNameAndDept(int userId);

}