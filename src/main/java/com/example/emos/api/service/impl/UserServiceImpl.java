package com.example.emos.api.service.impl;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbUserDao;
import com.example.emos.api.db.pojo.TbUser;
import com.example.emos.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

//    @Value("${workflow.url}")
//    private String workflow;

//    @Value("${emos.code}")
//    private String code;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbUserDao userDao;

    @Override
    public HashMap createQrCode() {
        //生成uuid
        String uuid = IdUtil.simpleUUID();
        //key是uuid，value的false代表该uuid没有被使用，防止该二维码被重复扫码
        redisTemplate.opsForValue().set(uuid, false, 5, TimeUnit.MINUTES);
        //设置二维码大小信息
        QrConfig config = new QrConfig();
        config.setHeight(160);
        config.setWidth(160);
        config.setMargin(1);
        //把生成的二维码图片转换成base64字符串
        String base64 = QrCodeUtil.generateAsBase64("login@@@" + uuid, config, ImgUtil.IMAGE_TYPE_JPG);
        //将uuid和base64字符串通过map的形式返回出去
        HashMap map = new HashMap() {{
            put("uuid", uuid);
            put("pic", base64);
        }};
        return map;
    }

    @Override
    public boolean checkQrCode(String code, String uuid) {
        //通过uuid查询userId是否存在，存在则表示用户已经登录
        boolean bool = redisTemplate.hasKey(uuid);
        if (bool) {
            //当用户登录时把uuid和code值写入Redis
            String openId = getOpenId(code);
            long userId = userDao.searchIdByOpenId(openId);
            redisTemplate.opsForValue().set(uuid, userId);
        }
        return bool;
    }

    /**
     * 用于把code临时授权转换成OpenId
     * @param code
     * @return
     */
    private String getOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap map = new HashMap();
        map.put("appid", appId);
        map.put("secret", appSecret);
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String response = HttpUtil.post(url, map);
        JSONObject json = JSONUtil.parseObj(response);
        String openId = json.getStr("openid");
        if (openId == null || openId.length() == 0) {
            throw new RuntimeException("临时登陆凭证错误");
        }
        return openId;
    }

    @Override
    public HashMap wechatLogin(String uuid) {
        HashMap map = new HashMap();
        boolean result = false;
        //判断Redis缓存的UUID对应的Value是否为非false，就算用户登陆了
        if (redisTemplate.hasKey(uuid)) {
            String value = redisTemplate.opsForValue().get(uuid).toString();
            if (!"false".equals(value)) {
                result = true;
                //删除Redis中的UUID，防止二维码被重刷
                redisTemplate.delete(uuid);
                //把Value的字符串转换成整数,赋值成userId
                int userId = Integer.parseInt(value);
                map.put("userId", userId);
            }
        }
        map.put("result", result);
        return map;
    }

    @Override
    public Set<String> searchUserPermissions(int userId) {
        Set<String> permissions = userDao.searchUserPermissions(userId);
        return permissions;
    }

    @Override
    public HashMap searchById(int userId) {
        HashMap map = userDao.searchById(userId);
        return map;
    }

    @Override
    public HashMap searchUserSummary(int userId) {
        HashMap map = userDao.searchUserSummary(userId);
        return map;
    }

    @Override
    public ArrayList<HashMap> searchAllUser() {
        ArrayList<HashMap> list = userDao.searchAllUser();
        return list;
    }

    /**
     * 将从Dao层中查询的userId传到业务层，并返回出去Web层
     * @param param param 为前端输入的username和password，经由web层转换为HashMap类型的param
     * @return
     */
    @Override
    public Integer login(HashMap param) {

        Integer userId = userDao.login(param);

        return userId;
    }

    /**
     * 更新密码成功后返回 rows条数
     * @param param
     * @return
     */
    @Override
    public int updatePassword(HashMap param) {
        int rows = userDao.updatePassword(param);
        return rows;
    }

    /**
     * 将条件查询的用户信息返回出来
     * @param param
     * @return
     */
    @Override
    public PageUtils searchUserByPage(HashMap param) {

        //将数据库中根据条件param参数查询的员工数据保存到list中，list为前端页面的展示条数
        ArrayList<HashMap> list = userDao.searchUserByPage(param);
        //根据条件param查找的用户总条数count
        long count = userDao.searchUserCount(param);
        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");
        //将所有的查询数据信息封装成pageUtils返回出去
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        //pageUtils中有totalCount, pageSize, totalPage, pageIndex, 和list（用户信息）。
        return pageUtils;
    }

    @Override
    public int insert(TbUser user) {

        //插入成功后将返回rows，将rows返回到web层
        int rows = userDao.insert(user);
        return rows;
    }

    @Override
    public int update(HashMap param) {
        //修改成功后返回rows=1
        int rows = userDao.update(param);
        return rows;
    }

    @Override
    public int deleteUserByIds(Integer[] ids) {
        //删除用户成功后返回rows数
        int rows = userDao.deleteUserByIds(ids);
        return rows;
    }

    @Override
    public ArrayList<String> searchUserRoles(int userId) {

        //查询用户角色，每个用户有多个角色，故将其存储为ArrayList形式
        ArrayList<String> list = userDao.searchUserRoles(userId);
        return list;
    }

    @Override
    public HashMap searchNameAndDept(int userId) {

        HashMap map = userDao.searchNameAndDept(userId);
        return map;
    }


}
