package com.example.emos.api.db.dao;

import com.example.emos.api.db.pojo.TbAmect;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbAmectDao {

    /**
     * 查询罚款人数页面
     * @param param
     * @return
     */
    public ArrayList<HashMap> searchAmectByPage(HashMap param);

    public long searchAmectCount (HashMap param);

    /**
     * 添加罚款信息
     * @param amect
     * @return
     */
    public int insert(TbAmect amect);

    public HashMap searchById(int id);

    public int update (HashMap param);

    public int deleteAmectByIds (Integer[] ids);

    /**
     * 查询罚款记录（一般为一条记录）
     * @param param
     * @return
     */
    public HashMap searchAmectByCondition(HashMap param);

    /**
     * 创建了微信支付订单后，给它设置微信支付订单Id
     * @param param
     * @return
     */
    public int updatePrepayId(HashMap param);

    /**
     *  付款成功后修改罚款状态
     * @param param
     * @return
     */
    public int updateStatus(HashMap param);

    /**
     * 查询用户的uuid
     * @param uuid
     * @return
     */
    public int searchUserIdByUUID(String uuid);

    /**
     * 查询图表，有4个类型图表
     * @param param
     * @return
     */
    /**
     * 这个用来查询罚款类型的，将罚款类型作为一个分类
     * @param param
     * @return
     */
    public ArrayList<HashMap> searchChart_1(HashMap param);

    /**
     * 这个函数是用来查询罚款金额的区间
     * @param param
     * @return
     */
    public ArrayList<HashMap> searchChart_2(HashMap param);

    /**
     * 这个函数是查询已付款和未付款得记录数
     * @param param
     * @return
     */
    public ArrayList<HashMap> searchChart_3(HashMap param);

    /**
     * 这个函数是查询特定年份和状态下的每个月份的记录数
     * @param param
     * @return
     */
    public ArrayList<HashMap> searchChart_4(HashMap param);


}
