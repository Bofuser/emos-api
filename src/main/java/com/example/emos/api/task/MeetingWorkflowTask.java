package com.example.emos.api.task;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.db.dao.TbMeetingDao;
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
public class MeetingWorkflowTask {

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private TbMeetingDao meetingDao;

    @Value("${emos.recieveNotify}")
    private String recieveNotify;

    @Value("${emos.code}")
    private String code;

    @Value("${emos.tcode}")
    private String tcode;

    @Value("${workflow.url}")
    private String workflow;

    /**
     * 异步线程方法，通过这个方法获取工作流中的instanceId
     * @param uuid
     * @param creatorId
     * @param title
     * @param date
     * @param start
     * @param meetingType
     */
    @Async("AsyncTaskExecutor")
    public void startMeetingWorkflow(String uuid, int creatorId,String title, String date, String start,String meetingType){

        //查询申请人的基本信息
        HashMap info = userDao.searchUserInfo(creatorId);

        JSONObject json = new JSONObject();
        json.set("url", recieveNotify);     //审批结果的信息
        json.set("uuid", uuid);             //uuid为随机字符串，用于设置定时器的GroupName
        json.set("creatorId",creatorId);    //会议发起者的UserId
        json.set("creatorName",info.get("name").toString());    //会议发起者的姓名
        json.set("code", code);             //慕课网码
        json.set("tcode", tcode);           //小程序码
        json.set("title",title);            //会议主题
        json.set("date", date);             //会议日期
        json.set("start", start);           //开始时间
        json.set("meetingType",meetingType);    //会议类型

        String[] roles = info.get("roles").toString().split(",");
        //判断用户角色是不是总经理，总经理创建的会议不需要审批，所以不需要查询总经理userId和部门经理userId
        if(!ArrayUtil.contains(roles, "总经理")){

            //查询部门经理userId,并存储managerId
            Integer managerId = userDao.searchDeptManagerId(creatorId);
            json.set("managerId", managerId);

            //查询总经理userId,并存储gmId
            Integer gmId = userDao.searchGmId();
            json.set("gmId",gmId);

            //查询参会人是否率属于同一部门
            boolean bool = meetingDao.searchMeetingMembersInSameDept(uuid);
            json.set("sameDept", bool);

        }

        //设置访问路径
        String url = workflow + "/workflow/startMeetingProcess";
        //发起http请求,向工作流获取数据{uuid, instanceId}
        HttpResponse resp = HttpRequest.post(url).header("Content-Type", "application/json")
                .body(json.toString()).execute();

        if(resp.getStatus() == 200){
            json = JSONUtil.parseObj(resp.body());
            String instanceId = json.getStr("instanceId");
            HashMap param = new HashMap();
            param.put("uuid",uuid);
            param.put("instanceId", instanceId);

            //更新会议记录的instance_id字段,将instanceId存储到数据库中
            int row = meetingDao.updateMeetingInstanceId(param);
            //当后端存储instanceId失败时则抛出语句
            if(row != 1){
                throw new EmosException("保存会议工作流实例Id失败");
            }
        }else {     //当状态码不是200时则发出错误日志提示

            log.error(resp.body());
        }

    }

    @Async("AsyncTaskExecutor")
    public void deleteMeetingApplication(String uuid, String instanceId, String reason){

        JSONObject json = new JSONObject();
        json.set("uuid", uuid);
        json.set("instanceId", instanceId);
        json.set("code", code);
        json.set("tcode", tcode);
        json.set("type", "会议申请");
        json.set("reason", reason);

        //发送http请求响应
        String url = workflow + "/workflow/deleteProcessById";
        HttpResponse resp = HttpRequest.post(url).header("Content-Type", "application/json")
                .body(json.toString()).execute();

        if(resp.getStatus() == 200){
            log.debug("删除了会议申请");
        }else {
            //打印错处消息
            log.error(resp.body());
        }


    }




}
