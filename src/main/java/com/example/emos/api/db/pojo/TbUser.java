package com.example.emos.api.db.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * tb_user TbUser数据类型，用来存储数据
 * @author 
 */
@Data
public class TbUser implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 用户名
     */
    private String username;


    /**
     * 密码
     */
    private String password;

    /**
     * 长期授权字符串
     */
    private String openId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像网址
     */
    private String photo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别
     */
    private Object sex;

    /**
     * 手机号码
     */
    private String tel;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 入职日期
     */
    private Date hiredate;

    /**
     * 角色       需要在Web层中给其赋值★
     */
    private Object role;

    /**
     * 是否是超级管理员
     */
    private Boolean root;

    /**
     * 部门编号
     */
    private Integer deptId;

    /**
     * 状态  需要在Web层中给其赋值★
     */
    private Byte status;

    /**
     * 用户创建时间： 需要在Web层中给其赋值★
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}