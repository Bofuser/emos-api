package com.example.emos.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.common.util.R;
import com.example.emos.api.controller.form.*;
import com.example.emos.api.db.pojo.TbMeetingRoom;
import com.example.emos.api.service.MeetingRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/meeting_room")
@Tag(name = "MeetingRoomController", description = "会议管理Web接口")
public class MeetingRoomController {
    @Autowired
    private MeetingRoomService meetingRoomService;

    @GetMapping("/searchAllMeetingRoom")
    @Operation(summary = "查询所有会议室")
    @SaCheckLogin
    public R searchAllMeetingRoom() {
        ArrayList<HashMap> list = meetingRoomService.searchAllMeetingRoom();
        return R.ok().put("list", list);
    }

    @PostMapping("/searchById")
    @Operation(summary = "根据ID查找会议室")
    @SaCheckPermission(value = {"ROOT", "MEETING_ROOM:SELECT"}, mode = SaMode.OR)
    public R searchById(@Valid @RequestBody SearchMeetingRoomByIdForm form) {
        HashMap map = meetingRoomService.searchById(form.getId());
        return R.ok(map);
    }

    @PostMapping("/searchFreeMeetingRoom")
    @Operation(summary = "查询空闲会议室")
    @SaCheckLogin
    public R searchFreeMeetingRoom(@Valid @RequestBody SearchFreeMeetingRoomForm form) {
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        ArrayList<String> list = meetingRoomService.searchFreeMeetingRoom(param);
        return R.ok().put("list", list);
    }

    @PostMapping("/searchMeetingRooByPage")
    @Operation(summary = "查询会议室分页数据")
    @SaCheckLogin
    public R searchMeetingRoomByPage(@Valid @RequestBody SearchMeetingRoomByPageForm form){//form{name, canDelete, page, length}

        //将form中的length和page提取出来，计算出start
        int length = form.getLength();
        int page = form.getPage();
        int start = (page - 1) * length;

        //将JSON数据类型解析成HashMap类型
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        //将start值存储到param中
        param.put("start", start);
        //通过service层获取查询的数据返回给前端， pageUtils{list, totalCount, pageIndex, pageSize}
        PageUtils pageUtils = meetingRoomService.searchMeetingRoomByPage(param);

        return R.ok().put("page",pageUtils);

    }

    @PostMapping("/insert")
    @Operation(summary = "添加新会议室")
    @SaCheckPermission(value = {"ROOT", "MEETING_ROOM:SELECT"}, mode = SaMode.OR)
    public R insert(@Valid @RequestBody InsertMeetingRoomForm form){ //form{name, max, desc, status}

        //将其转换成TbMeetingRoom的数据类型
        TbMeetingRoom meetingRoom = JSONUtil.parse(form).toBean(TbMeetingRoom.class);
        //添加成功新数据后返回rows
        int rows = meetingRoomService.insert(meetingRoom);
        return R.ok().put("rows",rows);

    }

    @PostMapping("/update")
    @Operation(summary = "修改会议室")
    @SaCheckPermission(value = {"ROOT", "MEETING_ROOM:SELECT"}, mode = SaMode.OR)
    public R update(@Valid @RequestBody UpdateMeetingRoomForm form){    //form{id, name, max, desc, status}

        //将前端传过来的数据从form表单中提取出来，存储到TbMeetingRoom中的数据类型
        TbMeetingRoom meetingRoom = JSONUtil.parse(form).toBean(TbMeetingRoom.class);

        int rows = meetingRoomService.update(meetingRoom);

        return R.ok().put("rows", rows);

    }

    @PostMapping("/deleteMeetingRoomByIds")
    @Operation(summary = "删除会议室")
    @SaCheckPermission(value = {"ROOT", "MEETING_ROOM:SELECT"}, mode = SaMode.OR)
    public R deleteMeetingRoomByIds(@Valid @RequestBody DeleteMeetingRoomByIdsForm form){   //form{ids}

        //通过id来删除会议室的信息
        int rows = meetingRoomService.deleteMeetingRoomByIds(form.getIds());
        return R.ok().put("rows",rows);
    }


}