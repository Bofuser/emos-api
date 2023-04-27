package com.example.emos.api.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbMeetingDao;
import com.example.emos.api.db.pojo.TbMeeting;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.MeetingService;
import com.example.emos.api.task.MeetingWorkflowTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
@Slf4j
public class MeetingServiceImpl implements MeetingService {

    @Autowired
    private TbMeetingDao meetingDao;

    //引入工作流项目接口
    @Autowired
    private MeetingWorkflowTask meetingWorkflowTask;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 用来查询线下会议
     * @param param
     * @return
     */
    @Override
    public PageUtils searchOfflineMeetingByPage(HashMap param) {

        //获取查询的线下会议信息
        ArrayList<HashMap> list = meetingDao.searchOfflineMeetingByPage(param);
        long count = meetingDao.searchOfflineMeetingCount(param);
        //获取start和length
        int start = (Integer)param.get("start");
        int length = (Integer)param.get("length");

        //把meeting字段转换为JSON数组对象  meeting{{"end": "15:30", "time": 5 , "start": "13:00", "status": 3},{"end": "11:00", "time": 4, "start": "09:00", "status": 3}}
        for (HashMap map: list) {//list存储的是name和meeting,如果没有会议通知，则没有meeting list{"name", "meeting" }
            String meeting = (String) map.get("meeting");
            //如果meeting是有效字符串，就转换成JSON数组对象
            if(meeting != null && meeting.length() > 0){
                map.replace("meeting", JSONUtil.parseArray(meeting));
            }

        }

        //将数据传到pageUtils中
        PageUtils pageUtils = new PageUtils(list,count, start, length);
        return pageUtils;
    }

    /**
     * 申请会议功能（添加）
     * @param meeting
     * @return
     */
    @Override
    public int insert(TbMeeting meeting) {

        int rows = meetingDao.insert(meeting);

        //当rows不等于1时提示申请失败
        if (rows != 1){

            throw new EmosException("会议添加失败");
        }

        /**
         * 调用异步线程方法startMeetingWorkflow（），将meeting中存储的参数发送到工作流中，
         * 进而获取instanceId的值。这一步为异步线程，可同时进行，不相互影响
         */
        meetingWorkflowTask.startMeetingWorkflow(meeting.getUuid(), meeting.getCreatorId(), meeting.getTitle(), meeting.getDate(), meeting.getStart() + ":00",
                meeting.getType() == 1 ? "线上会议" : "线下会议");
        return rows;
    }

    @Override
    public ArrayList<HashMap> searchOfflineMeetingInWeek(HashMap param) {

        ArrayList<HashMap> list = meetingDao.searchOfflineMeetingInWeek(param);
        return list;
    }

    @Override
    public HashMap searchMeetingInfo(short status, long id) {

        //判断正在进行中的会议
        HashMap map;
        /**
         * 正在进行和已经结束的会议都可以查询present和unpresent字段
         * 当status ==4或者5时 表示正在进行中的会议。
         */
        if(status ==4 || status==5){
            map = meetingDao.searchCurrentMeetingInfo(id);
        }else {
            map = meetingDao.searchMeetingInfo(id);
        }

        return map;
    }

    /**
     * 删除会议信息
     * @param param
     * @return rows
     */
    @Override
    public int deleteMeetingApplication(HashMap param) {

        Long id = MapUtil.getLong(param, "id");
        String uuid = MapUtil.getStr(param, "uuid");
        String instanceId = MapUtil.getStr(param, "instanceId");

        //查询会议详情，一会儿要判断是否距离会议开始不足20分钟,web层中的userId传入到这里
        HashMap meeting = meetingDao.searchMeetingById(param);
        String date = MapUtil.getStr(meeting, "date");
        String start = MapUtil.getStr(meeting, "start");
        int status = MapUtil.getInt(meeting, "status");
        boolean isCreator = Boolean.parseBoolean(MapUtil.getStr(meeting, "isCreator"));
        DateTime dateTime = DateUtil.parse(date + " " + start);
        DateTime now = DateUtil.date();

        //距离会议开始不足20分钟，不能删除会议
        if (now.isAfterOrEquals(dateTime.offset(DateField.MINUTE, -20))) {
            throw new EmosException("距离会议开始不足20分钟，不能删除会议");
        }

        //只能申请人删除该会议
        if (!isCreator) {
            throw new EmosException("只能申请人删除该会议");
        }

        //待审批和未开始的会议可以删除
        if (status == 1 || status == 3) {
            //删除会议
            int rows = meetingDao.deleteMeetingApplication(param);
            //判断删除成功,将uuid, instanceId, 和reason传到工作流中才可以真正的删除掉会议。
            if (rows == 1) {
                String reason = param.get("reason").toString();
                meetingWorkflowTask.deleteMeetingApplication(uuid, instanceId, reason);
            }
            return rows;
        } else {
            throw new EmosException("只能删除待审批和未开始的会议");
        }

    }

    @Override
    public PageUtils searchOnlineMeetingByPage(HashMap param) {

        //查询结果存储到list和count中
        ArrayList<HashMap> list = meetingDao.searchOnlineMeetingByPage(param);
        long count = meetingDao.searchOnlineMeetingCount(param);

        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");

        //将数据存储到pageUtils中
        PageUtils pageUtils = new PageUtils(list, count, start, length);

        return pageUtils;
    }

    /**
     * 通过UUID查询存储在缓存中的roomId,但是只有在会议开始前20分钟，
     * 工作流项目的定时器才会生成这个RoomID
     * 用户在前端页面进入到meeting_video.vue页面之后，需要通过Ajax查询这个会议室的RoomID，
     * 然后让TrtcClient连接到视频会议室
     *
     * @param uuid
     * @return
     */
    @Override
    public Long searchRoomIdByUUID(String uuid) {


        if (redisTemplate.hasKey(uuid)) {
            Object temp = redisTemplate.opsForValue().get(uuid);
            long roomId = Long.parseLong(temp.toString());
            return roomId;
        }
        return null;
    }

    /**
     * 查询参会人员信息，将参会人员的信息保存到ArrayList中返回过去
     * @param param
     * @return
     */
    @Override
    public ArrayList<HashMap> searchOnlineMeetingMembers(HashMap param) {

        ArrayList<HashMap> list = meetingDao.searchOnlineMeetingMembers(param);
        return list;
    }

    @Override
    public boolean searchCanCheckinMeeting(HashMap param) {

        long count = meetingDao.searchCanCheckinMeeting(param);
        return count == 1 ? true : false;
    }

    /**
     * 更新签到人数，签到后人数进行更新，返回已经更新的条数rows
     * @param param
     * @return rows
     */
    @Override
    public int updateMeetingPresent(HashMap param) {

        int rows = meetingDao.updateMeetingPresent(param);
        return rows;
    }


}
