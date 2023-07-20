package com.example.emos.api.service.impl;

import cn.hutool.core.map.MapUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbLeaveDao;
import com.example.emos.api.db.pojo.TbLeave;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.LeaveService;
import com.example.emos.api.task.LeaveWorkflowTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class LeaveServiceImpl implements LeaveService {

    @Autowired
    private LeaveWorkflowTask leaveWorkflowTask;

    @Autowired
    private TbLeaveDao tbLeaveDao;

    /**
     * 页面数据查询
     * @param param
     * @return
     */
    @Override
    public PageUtils searchLeaveByPage(HashMap param) {

        //查询的请假列表数据
        ArrayList<HashMap> list = tbLeaveDao.searchLeaveByPage(param);
        //查询的请假列表总条数
        long count = tbLeaveDao.searchLeaveCount(param);
        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    @Override
    public boolean searchContradiction(HashMap param) {

        long count = tbLeaveDao.searchContradiction(param);
        //bool的值为true表示count大于0，为false表示count小于等于0。
        boolean bool = count > 0;
        return bool;
    }

    @Override
    public int insert(TbLeave leave) {

        int rows = tbLeaveDao.insert(leave);
        //开启工作流
        if(rows == 1){
            leaveWorkflowTask.startLeaveWorkflow(leave.getId(), leave.getUserId(), leave.getDays());
        }else {
            throw new EmosException("会议添加失败");
        }
        return rows;
    }

    @Override
    public int deleteLeaveById(HashMap param) {

        //获取对象param的Map对象中的“id”键对应的值
        int id = MapUtil.getInt(param,"id");
        String instanceId = tbLeaveDao.searchInstanceIdById(id);
        int rows = tbLeaveDao.deleteLeaveById(param);

        if(rows == 1){
            //删除工作流
            leaveWorkflowTask.deleteLeaveWorkflow(instanceId, "员工请假", "删除请假申请");

        }else{
            throw new EmosException("删除请假失败");
        }

        return rows;
    }

    @Override
    public HashMap searchLeaveById(HashMap param) {

        HashMap map = tbLeaveDao.searchLeaveById(param);
        return map;
    }
}
