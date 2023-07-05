package com.example.emos.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.common.util.R;
import com.example.emos.api.controller.form.ApprovalTaskForm;
import com.example.emos.api.controller.form.SearchApprovalContentForm;
import com.example.emos.api.controller.form.SearchTaskByPageForm;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.ApprovalService;
import com.example.emos.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

@RestController
@RequestMapping("/approval")
@Tag(name = "ApprovalController", description = "任务审批Web接口")
@Slf4j
public class ApprovalController {

    @Value("${workflow.url}")
    private String workflow;

    @Value("${emos.code}")
    private String code;

    @Value("${emos.tcode}")
    private String tcode;

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private UserService userService;

    @PostMapping("/searchTaskByPage")
    @Operation(summary = "查询分页任务列表")
    @SaCheckPermission(value = {"WORKFLOW:APPROVAL", "FILE:ARCHIVE"}, mode = SaMode.OR)
    public R searchTaskByPage(@Valid @RequestBody SearchTaskByPageForm form){

        //获取表单form传送的param，将form表单传输过去
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        //获取操作的用户信息，将用户信息传到userId
        int userId = StpUtil.getLoginIdAsInt();
        param.put("userId", userId);
        //将从TbUser数据库中查询到的角色权限值封装成role,存储到HashMap中,用于HttpRequest请求发送给工作流项目中的角色权限
        param.put("role", userService.searchUserRoles(userId));
        //将从工作流中查询的结果存储封装到PageUtils工具类中
        PageUtils pageUtils = approvalService.searchTaskByPage(param);
        return R.ok().put("page",pageUtils);
    }


    /**
     * 获取 会议审批内容和BPMN实时进度图
     * @param form
     * @return
     */
    @PostMapping("/searchApprovalContent")
    @Operation(summary = "查询任务详情")
    @SaCheckPermission(value = {"WORKFLOW:APPROVAL", "FILE:ARCHIVE"}, mode = SaMode.OR)
    public R searchApprovalContent(@Valid @RequestBody SearchApprovalContentForm form){

        //将表单中的内容转成HashMap模式
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        //获取操作的用户信息，将用户信息传到userId
        int userId = StpUtil.getLoginIdAsInt();
        param.put("userId", userId);
        //将从TbUser数据库中查询到的角色权限值封装成role,存储到HashMap中
        param.put("role", userService.searchUserRoles(userId));
        //获取content中的内容
        HashMap content = approvalService.searchApprovalContent(param);
        //返回content中的内容
        return R.ok().put("content",content);

    }

    @GetMapping("/searchApprovalBpmn")
    @Operation(summary = "获取BPMN图形")
    @SaCheckPermission(value = {"WORKFLOW:APPROVAL", "FILE:ARCHIVE"}, mode = SaMode.OR)
    public void searchApprovalBpmn(String instanceId, HttpServletResponse response){

        //判断instanceId不能为空，instanceId 为工作流实例ID
        if(StrUtil.isBlankIfStr(instanceId)){
            throw new EmosException("instanceId不能为空");
        }
        //将code、tcode和instanceId传入过去
        HashMap param = new HashMap(){{

            put("code", code);
            put("tcode", tcode);
            put("instanceId", instanceId);

        }};
        //传送url到http响应，获取响应数据
        String url = workflow + "/workflow/searchApprovalBpmn";
        HttpResponse resp = HttpRequest.post(url).header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(param)).execute();
        //发送响应之后，如果状态码成功则用IO流对拷
        if(resp.getStatus() == 200){

            //这段代码是将返回的io流对拷
            try(
                    InputStream in = resp.bodyStream();
                    BufferedInputStream bin = new BufferedInputStream(in);
                    OutputStream out = response.getOutputStream();
                    BufferedOutputStream bout = new BufferedOutputStream(out)
            ) {
                IOUtils.copy(bin,bout);
            } catch (Exception e) {
                log.error("执行异常", e);
            }
        } else {
            log.error("获取工作流BPMN图失败");
            throw new EmosException("获取工作流BPMN图失败");
        }
    }

    /**
     * 审批查询任务
     * @param form
     * @return
     */
    @PostMapping("/approvalTask")
    @Operation(summary = "审批任务")
    @SaCheckPermission(value = {"WORKFLOW:APPROVAL"}, mode = SaMode.OR)
    public R approvalTask(@Valid @RequestBody ApprovalTaskForm form){ //form{taskId, approval}

        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        approvalService.approvalTask(param);
        return R.ok();

    }

}
