package com.example.emos.api.service.impl;

import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbMeetingRoomDao;
import com.example.emos.api.db.pojo.TbMeetingRoom;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.MeetingRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class MeetingRoomServiceImpl implements MeetingRoomService {
    @Autowired
    private TbMeetingRoomDao meetingRoomDao;

    @Override
    public ArrayList<HashMap> searchAllMeetingRoom() {
        ArrayList<HashMap> list = meetingRoomDao.searchAllMeetingRoom();
        return list;
    }

    /**
     * 根据id查找会议室，用于修改函数中，将id所绑定的数据信息渲染到前端
     * @param id
     * @return
     */
    @Override
    public HashMap searchById(int id) {
        HashMap map = meetingRoomDao.searchById(id);
        return map;
    }

    @Override
    public ArrayList<String> searchFreeMeetingRoom(HashMap param) {
        ArrayList<String> list = meetingRoomDao.searchFreeMeetingRoom(param);
        return list;
    }

    /**
     * 查询用户部门并分页
     * @param param
     * @return pageUtils(list, count, start, length)
     */
    @Override
    public PageUtils searchMeetingRoomByPage(HashMap param) {

        //查询出来的list内容
        ArrayList<HashMap> list = meetingRoomDao.searchMeetingRoomByPage(param);
        //查询出来的总会议室数
        long count =  meetingRoomDao.searchMeetingRoomCount(param);
        //获取param中的start和length值
        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");


        PageUtils pageUtils = new PageUtils(list, count, start, length);

        return pageUtils;
    }

    /**
     * 新增会议室信息
     * @param meetingRoom
     * @return rows
     */
    @Override
    public int insert(TbMeetingRoom meetingRoom) {

        int rows = meetingRoomDao.insert(meetingRoom);
        return rows;
    }

    /**
     * 修改会议室信息
     * @param meetingRoom
     * @return  rows
     */
    @Override
    public int update(TbMeetingRoom meetingRoom) {

        int rows = meetingRoomDao.update(meetingRoom);
        return rows;
    }

    /**
     * 删除会议室
     * @param ids
     * @return rows
     */
    @Override
    public int deleteMeetingRoomByIds(Integer[] ids) {

        //只有没有关联会议的会议室才能被删除。
        if(!meetingRoomDao.searchCanDelete(ids)){

            throw new EmosException("无法删除关联会议的会议室");
        }
        //删除成功后返回rows
        int rows = meetingRoomDao.deleteMeetingRoomByIds(ids);
        return rows;
    }


}