package com.example.emos.api.service.impl;

import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbAmectDao;
import com.example.emos.api.db.pojo.TbAmect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;

@Service
@Slf4j
public class AmectService implements com.example.emos.api.service.AmectService {

    @Autowired
    private TbAmectDao amectDao;

    /**
     *  将查询的用户信息返回出来
     * @param param
     * @return
     */
    @Override
    public PageUtils searchAmectByPage(HashMap param) {

        // 获取数据库种根据条件param参数查询的员工数据保存在 list中
        ArrayList<HashMap> list = amectDao.searchAmectByPage(param);
        // 获取总条数
        long count = amectDao.searchAmectCount(param);
        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");

        //将所有的查询数据信息封装到 pageUtils 返回出去
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    /**
     * 新增罚款信息
     * @param list
     * @return
     */
    @Override
    @Transactional
    public int insert(ArrayList<TbAmect> list) {

        //list.forEach 是一个lambda 表达式，对列表中的每个元素执行插入操作。对于列表中的每一个元素 “one”，将
        //amectDao.insert（one）将其插入到数据库中。
        list.forEach(one ->{
            amectDao.insert(one);
        });

        //返回插入的记录数，即列表的大小。
        return list.size();
    }

    /**
     * 通过 id 去查找数据，将数据填充到弹窗中
     * @param id
     * @return
     */
    @Override
    public HashMap searchById(int id) {

        HashMap map = amectDao.searchById(id);
        return map;
    }

    @Override
    public int update(HashMap param) {

        int rows = amectDao.update(param);
        return rows;
    }

    @Override
    public int deleteAmectByIds(Integer[] ids) {

        int rows = amectDao.deleteAmectByIds(ids);
        return rows;
    }


}
