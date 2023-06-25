package com.example.emos.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.common.util.R;
import com.example.emos.api.controller.form.*;
import com.example.emos.api.db.pojo.TbUser;
import com.example.emos.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

@RestController
@RequestMapping("/user")
@Tag(name = "UserController", description = "用户Web接口")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 生成登陆二维码的字符串
     */
    @GetMapping("/createQrCode")
    @Operation(summary = "生成二维码Base64格式的字符串")
    public R createQrCode() {
        HashMap map = userService.createQrCode();
        return R.ok(map);
    }

    /**
     * 检测登陆验证码
     *
     * @param form
     * @return
     */
    @PostMapping("/checkQrCode")
    @Operation(summary = "检测登陆验证码")
    public R checkQrCode(@Valid @RequestBody CheckQrCodeForm form) {
        boolean bool = userService.checkQrCode(form.getCode(), form.getUuid());
        return R.ok().put("result", bool);
    }

    /**
     * 微信小程序登录
     * @param form
     * @return
     */
    @PostMapping("/wechatLogin")
    @Operation(summary = "微信小程序登陆")
    public R wechatLogin(@Valid @RequestBody WechatLoginForm form) {
        HashMap map = userService.wechatLogin(form.getUuid());
        boolean result = (boolean) map.get("result");

        if (result) {
            int userId = (int) map.get("userId");
            StpUtil.setLoginId(userId);
            Set<String> permissions = userService.searchUserPermissions(userId);
            map.remove("userId");
            map.put("permissions", permissions);
        }
        return R.ok(map);
    }

    /**
     * 登陆成功后加载用户的基本信息
     */
    @GetMapping("/loadUserInfo")
    @Operation(summary = "登陆成功后加载用户的基本信息")
    @SaCheckLogin   //登录校验 —— 只有登录之后才能进入该方法。
    public R loadUserInfo() {
        int userId = StpUtil.getLoginIdAsInt();
        HashMap summary = userService.searchUserSummary(userId);
        return R.ok(summary);
    }

    /**
     * 登录校验，将前端中传递保存到loginForm中的username和password的数据传递过来保存到R数据类型中
     * @param form
     * @return
     */
    @PostMapping("/login")
    @Operation(summary = "系统登录")
    public R login(@Valid @RequestBody LoginForm form){
        //将web端传来的form对象转换为json/object对象，然后通过toBean将其转换成HashMap值
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        //用来查询后端是否有这个用户Id，返回成userId
        Integer userId = userService.login(param);
        //判断userId是否为空，将值保存到以R为数据类型中
        R r = R.ok().put("result", userId != null ? true : false);
        //如果用户存在，则继续查询其权限和 token值 返回给前端
        if (userId != null) {
            //会话登录，并设置登陆的 userId
            //StpUtil.setLoginId(userId);
            StpUtil.login(userId);
            //查询用户的权限列表，将返回的值保存到permission中，该功能用于不同权限的人使用不同的功能
            Set<String> permissions = userService.searchUserPermissions(userId);
            /*
             * 因为新版的Chrome浏览器不支持前端Ajax的withCredentials，
             * 导致Ajax无法提交Cookie，所以我们要取出生成的Token返回给前端，
             * 让前端保存在Storage中，然后每次在Ajax的Header上提交Token
             */
            //获取token值，并将值传递过去
            //getTokenInfo返回两个,一个对象包含两个关键属性：tokenName和tokenValue，下面调用tokenValue
            String token=StpUtil.getTokenInfo().getTokenValue();
            String password = form.getPassword();
            //往R对象中存放token值和permission的结果
            r.put("permissions",permissions).put("token",token).put("password", password);
        }
        return r;
    }

    /**
     * 退出系统登录
     * @return
     */
    @GetMapping("/logout")
    @Operation(summary = "退出系统")
    public R logout(){

        //这个工具类函数有两个作用(1)将redis缓存清除(2)将浏览器中保存的信息过期
        StpUtil.logout();
        //返回200码表示成功
        return R.ok();

    }

    /**
     * 修改用户密码
     * @param form
     * @return
     */
    @PostMapping("/updatePassword")
    @SaCheckLogin       //SaCheckLogin用来检验用户是否登录，登陆了才能进入该方法修改用户密码
    @Operation(summary = "修改密码")
    public R updatePassword(@Valid @RequestBody UpdatePasswordForm form){

        //获取用户的ID,这个函数通过浏览器中保存的token值逆转为userId
        int userId = StpUtil.getLoginIdAsInt();

        //初始化HashMap对象，并将为其赋值userId和password
        HashMap param = new HashMap<>(){{
            put("userId", userId);
            put("password", form.getPassword());
        }};
        //将用户信息param传入进行修改，然后返回修改条数rows
        int rows = userService.updatePassword(param);
        //返回修改条数到前端
        return R.ok().put("rows",rows);

    }

    @PostMapping("/searchUserByPage")
    @Operation(summary = "查询用户分页记录")
    @SaCheckPermission(value = {"ROOT", "USER:SELECT"}, mode = SaMode.OR)   //权限认证：必须具有指定权限才能进入该方法mode = SaMode.OR 表示会话具有其一权限即可以通过。如果mode = SaMode.AND 则表示需要符合全部权限才能通过
    public R searchUserByPage(@Valid @RequestBody SearchUserByPageForm form){ //form前端发送过来的数据,数据包括 page、length、name、sex、role、deptId、status

        //通过从前端返回的page和length算出start的值，start表示查询页数从第几条开始
        int page = form.getPage();
        int length = form.getLength();
        //计算起始页面
        int start = (page - 1) * length;
        /**
         * 将form中的数据转换成HashMap,封装到param中
         * form:SearchUserByPageForm(page=1, length=10, name=null, sex=null, role=null, deptId=null, status=null)
         */
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        //将start加入到param中，因此后面中length和start将会用得到
        /**
         * param为HashMap类型，存储为（key,value）形式
         * param: {length: 10, page: 1, start: start}
         */
        param.put("start",start);
        PageUtils pageUtils = userService.searchUserByPage(param);
        return R.ok().put("page",pageUtils);

    }

    @PostMapping("/insert")
    @Operation(summary = "添加用户")
    @SaCheckPermission(value = {"ROOT", "USER:SELECT"}, mode = SaMode.OR)   //权限认证
    public R insert(@Valid @RequestBody InsertUserForm form){   //从前端发送过来新增的用户数据

        //将发送过来的数据转为TbUser型，保存在TbUser中
        TbUser user = JSONUtil.parse(form).toBean(TbUser.class);
        /**
         * 之所以要在这里重新赋值status dept_id create_time是因为前端传参不能将这三个
         * 参数传过来，但数据表中需要这三者信息，故需要在web层中重新为其赋值，然后再重新传到后端数据库中
         */
        //设置用户状态为在职,即status = 1;
        user.setStatus((byte) 1);
        //添加用户角色: 之所以这么写是因为前端这边是调用数据表中的角色作为下拉框，需要我们获取选择的下拉框值，将其赋值给它
        user.setRole(JSONUtil.parseArray(form.getRole()).toString());
        //添加用户创建时间
        user.setCreateTime(new Date());
        //如果用户添加成功，则返回rows
        int rows = userService.insert(user);
        return R.ok().put("rows",rows);

    }

    @PostMapping("/update")
    @Operation(summary = "修改用户")
    @SaCheckPermission(value = {"ROOT", "USER:SELECT"}, mode = SaMode.OR)   //权限认证
    public R update(@Valid @RequestBody UpdateUserForm form){

        //将前端传送过来的数据转换为HashMap类型
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        //将传送过来的role类型转换为JSON格式
        param.replace("role", JSONUtil.parseArray(form.getRole()).toString());
        //获取后端返回的rows值
        int rows = userService.update(param);
        //当修改成功时通过清楚token值把用户踢下线重新登录
        if(rows == 1) {

            //清除绑定ID的token值
            StpUtil.logoutByLoginId(form.getUserId());

        }
        //返回rows给前端
        return R.ok().put("rows",rows);

    }

    @PostMapping("/deleteUserByIds")
    @Operation(summary = "删除用户")
    @SaCheckPermission(value = {"ROOT", "USER:SELECT"}, mode = SaMode.OR)   //权限认证
    public R deleteUserByIds(@Valid @RequestBody DeleteUserByIdsForm form){

        //获取用户的ID,这个函数通过浏览器中保存的token值逆转为userId
        Integer userId = StpUtil.getLoginIdAsInt();
        //判断用户不能删除自己的ID  contains函数表示判断集合中是否存在指定的元素
        if (ArrayUtil.contains(form.getIds(), userId)) {
            return R.error("您不能删除自己的帐户");
        }
        //接收从数据库那边返回的rows,将其逐个通过清除token值踢下线
        int rows = userService.deleteUserByIds(form.getIds());
        if(rows > 0){

            for (Integer ids: form.getIds()) {
                StpUtil.logoutByLoginId(ids);
            }

        }
        return R.ok().put("rows",rows);
    }


    @PostMapping("/searchById")
    @Operation(summary = "根据ID查找用户")
    @SaCheckPermission(value = {"ROOT", "USER:SELECT"}, mode = SaMode.OR)   //权限认证
    public R searchById(@Valid @RequestBody SearchUserByIdForm form) {
        HashMap map = userService.searchById(form.getUserId());
        return R.ok(map);
    }

    @GetMapping("/searchAllUser")
    @Operation(summary = "查询所有用户")
    @SaCheckLogin   //登录认证 —— 只有登录之后才能进入该方法
    public R searchAllUser() {
        ArrayList<HashMap> list = userService.searchAllUser();
        return R.ok().put("list", list);
    }


    @PostMapping("/searchNameAndDept")
    @Operation(summary = "查找员工姓名和部门")
    @SaCheckLogin
    public R searchNameAndDept(@Valid @RequestBody SearchNameAndDeptForm form){

        HashMap map = userService.searchNameAndDept(form.getId());
        return R.ok(map);

    }



}
