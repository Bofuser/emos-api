package com.example.emos.api.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.ApprovalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
@Slf4j
public class ApprovalServiceImpl implements ApprovalService {

    /**
     * @Value("${xxx}") 表示为变量注入配置外部的属性，
     * 这个外部的属性来自application.properties. springboot启动时默认加载此文件
     */
    @Value("${workflow.url}")
    private String workflow;

    @Value("${emos.code}")
    private String code;

    @Value("${emos.tcode}")
    private String tcode;

    /**
     * 查询审批页面
     * @param param
     * @return
     */
    public PageUtils searchTaskByPage(HashMap param){

        param.put("code", code);
        param.put("tcode", tcode);
        String url = workflow + "/workflow/searchTaskByPage";
        HttpResponse resp = HttpRequest.post(url).header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(param)).execute();

        //如果响应状态码为200的话
        if(resp.getStatus() == 200){
            //将响应体中的内容提取出来  json {page: {list, totalCount, pageIndex, pageSize}}
            JSONObject json = JSONUtil.parseObj(resp.body());
            JSONObject page = json.getJSONObject("page");
            ArrayList list = page.get("list", ArrayList.class);
            Long totalCount = page.getLong("totalCount");
            Integer pageIndex = page.getInt("pageIndex");
            Integer pageSize = page.getInt("pageSize");
            //提取出来后存储到PageUtils中
            PageUtils pageUtils = new PageUtils(list, totalCount, pageIndex, pageSize);
            return pageUtils;

        }else { //响应失败则发送log日志

            log.error(resp.body());
            throw new EmosException("获取工作流数据异常");

        }

    }

    /**
     * 查询审批会议中的详细内容（包括bpmn图）
     * @param param
     * @return
     */
    @Override
    public HashMap searchApprovalContent(HashMap param) {

        //给param添加code和tcode参数
        param.put("code", code);
        param.put("tcode", tcode);
        String url = workflow + "/workflow/searchApprovalContent";
        HttpResponse resp = HttpRequest.post(url).header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(param)).execute();

        //如果相应状态码为200的话
        if(resp.getStatus() == 200){
            //提取响应体中的json,将json中的content内容提取出来,并返回出去
            JSONObject json = JSONUtil.parseObj(resp.body());
            HashMap content = json.get("content",HashMap.class);
            return content;

        } else {
            log.error(resp.body());
            throw new EmosException("获取工作流异常数据");
        }

    }

    /**
     * 审批工作
     * @param param
     */
    @Override
    public void approvalTask(HashMap param) {

        param.put("code", code);
        param.put("tcode", tcode);
        String url = workflow + "/workflow/approvalTask";
        HttpResponse resp = HttpRequest.post(url).header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(param)).execute();

        //如果响应状态码不等于200的话,打印工作流报错信息
        if (resp.getStatus() != 200) {
            log.error(resp.body());
            throw new EmosException("调用工作流审批异常");
        }
    }

}
