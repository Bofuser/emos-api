package com.example.emos.api.task;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.db.dao.TbLeaveDao;
import com.example.emos.api.db.dao.TbUserDao;
import com.example.emos.api.exception.EmosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Slf4j
public class LeaveWorkflowTask {

    @Value("${emos.code}")
    private String code;

    @Value("${emos.tcode}")
    private String tcode;

    @Value("${workflow.url}")
    private String workflow;

    @Value("${emos.recieveNotify}")
    private String recieveNotify;

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private TbLeaveDao leaveDao;

    /**
     * 执行请假申请流程
     * @param id
     * @param creatorId
     * @param days
     */
    //该注解主要用于异步执行，使用指定的异步执行器执行该方法
    @Async("AsyncTaskExecutor")
    public void startLeaveWorkflow(int id,int creatorId,String days){

        //获取用户信息
        HashMap info = userDao.searchUserInfo(creatorId);
        JSONObject json = new JSONObject();
        //json中添加各个参数的信息
        json.set("url", recieveNotify);
        json.set("creatorId", creatorId);
        json.set("creatorName", info.get("name").toString());
        json.set("code", code);
        json.set("tcode", tcode);
        json.set("title", info.get("dept").toString() + info.get("name").toString() + "的请假");
        //获取部门管理者Id
        Integer managerId = userDao.searchDeptManagerId(creatorId);
        json.set("managerId", managerId);
        //获取总经理Id
        Integer gmId = userDao.searchGmId();
        json.set("gmId", gmId);
        json.set("days", Double.parseDouble(days));

        //向工作流模块发送响应
        String url = workflow + "/workflow/startLeaveProcess";
        HttpResponse resp = HttpRequest.post(url).header("Content-Type", "application/json")
                .body(json.toString()).execute();

        //响应成功接收响应信息
        if(resp.getStatus() == 200){

            json = (JSONObject) JSONUtil.parse(resp.body());
            //获取instanceId
            String instanceId = json.getStr("instanceId");
            HashMap param = new HashMap();
            param.put("id", id);
            param.put("instanceId", instanceId);
            int row = leaveDao.updateLeaveInstanceId(param);
            if(row != 1){
                throw new EmosException("保存请假工作流实例Id失败");
            }
        }else {
            //打印响应 body 的错误
            log.error(resp.body());
        }


    }

    /**
     * 关闭审批请假工作流
     * @param instanceId
     * @param type
     * @param reason
     */
    @Async("AsyncTaskExecutor")
    public void deleteLeaveWorkflow(String instanceId, String type, String reason){

        JSONObject json = new JSONObject();
        json.set("instanceId", instanceId);
        json.set("type", type);
        json.set("reason", reason);
        json.set("code", code);
        json.set("tcode", tcode);

        //发送HttpResponse 响应
        String url = workflow + "/workflow/deleteProcessById";
        HttpResponse resp = HttpRequest.post(url).header("Content-Type", "application/json")
                .body(json.toString()).execute();

        if(resp.getStatus() != 200){
            log.error(resp.body());
            throw new EmosException("请假工作流失败");
        }

    }


}
