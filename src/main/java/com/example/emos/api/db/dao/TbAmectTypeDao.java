package com.example.emos.api.db.dao;

import com.example.emos.api.db.pojo.TbAmectType;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbAmectTypeDao {
    public ArrayList<TbAmectType> searchAllAmectType();

    /**
     * 查询罚款类型分类
     * @return
     */
    public ArrayList<HashMap> searchAmectTypeByPage(HashMap param);

    /**
     * 查询数据总数
     * @return
     */
    public long searchAmectTypeCount();

    /**
     * 新增用户数
     * @param tbAmectType
     * @return
     */
    public int insert(TbAmectType tbAmectType);

    /**
     * 通过id查询罚款类型，并返回给前端页面
     * @param id
     * @return
     */
    public HashMap searchById(int id);

    /**
     * 修改罚款类型
     * @param param
     * @return
     */
    public int update(HashMap param);

    /**
     * 删除罚款类型
     * @param ids
     * @return
     */
    public int deleteAmectTypeByIds(Integer[] ids);

}
