package com.example.emos.api.service.impl;

import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbAmectTypeDao;
import com.example.emos.api.db.pojo.TbAmectType;
import com.example.emos.api.service.AmectTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class AmectTypeServiceImpl implements AmectTypeService {
    @Autowired
    private TbAmectTypeDao amectTypeDao;

    @Override
    public ArrayList<TbAmectType> searchAllAmectType() {
        ArrayList<TbAmectType> list = amectTypeDao.searchAllAmectType();
        return list;
    }


    /**
     * 查询罚款分页
     * @param param
     * @return
     */
    @Override
    public PageUtils searchAmectTypeByPage(HashMap param) {

        //获取数据库中的罚款类型数据
        ArrayList<HashMap> list = amectTypeDao.searchAmectTypeByPage(param);
        //获取总条数
        long count = amectTypeDao.searchAmectTypeCount();
        //获取当前页数和查询的长度
        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");

        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }


    /**
     * 插入新的罚款类型
     * @param tbAmectType
     * @return
     */
    @Override
    public int insert(TbAmectType tbAmectType) {

        int rows = amectTypeDao.insert(tbAmectType);
        return rows;
    }

    /**
     * 根据 id 查询 罚款类型，返回给前端
     * @param id
     * @return
     */
    @Override
    public HashMap searchById(int id) {
        HashMap map = amectTypeDao.searchById(id);
        return map;
    }

    /**
     * 修改罚款类型
     * @param param
     * @return
     */
    @Override
    public int update(HashMap param) {

        int rows = amectTypeDao.update(param);
        return rows;
    }

    @Override
    public int deleteAmectTypeByIds(Integer[] ids) {
        int rows = amectTypeDao.deleteAmectTypeByIds(ids);
        return rows;
    }
}