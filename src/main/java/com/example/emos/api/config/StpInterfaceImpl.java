package com.example.emos.api.config;

import cn.dev33.satoken.stp.StpInterface;
import com.example.emos.api.db.dao.TbUserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * 	Sa-Token的配置类，用于获取用户权限和角色
 */
@Component  // 保证此类被SpringBoot扫描，完成Sa-Token的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {
    @Autowired
    private TbUserDao userDao;

    /**
     * 调用数据库查询用户的权限，将用户的权限存储到 List 中， 然后就可以使用注解进行权限校验了
     * 返回一个用户所拥有的权限集合。如返回一个{ROOT, AMECT:SELECT}
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginKey) {
        int userId = Integer.parseInt(loginId.toString());
        Set<String> permissions = userDao.searchUserPermissions(userId);
        ArrayList list = new ArrayList();
        list.addAll(permissions);
        return list;
    }


    /**
     * 返回一个用户所拥有的角色标识集合
     * 如 经理、部门主管等角色
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginKey) {
        //因为本项目不需要用到角色判定，所以这里就返回一个空的ArrayList对象
        ArrayList<String> list = new ArrayList<String>();
        return list;
    }

}