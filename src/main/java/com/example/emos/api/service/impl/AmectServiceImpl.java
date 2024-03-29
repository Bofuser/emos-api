package com.example.emos.api.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbAmectDao;
import com.example.emos.api.db.pojo.TbAmect;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.AmectService;
import com.example.emos.api.wxpay.MyWXPayConfig;
import com.example.emos.api.wxpay.WXPay;
import com.example.emos.api.wxpay.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AmectServiceImpl implements AmectService {

    @Autowired
    private TbAmectDao amectDao;

    /**
     * 导入微信支付Config
     */
    @Autowired
    private MyWXPayConfig myWXPayConfig;

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


    /**
     * 用于创建微信支付订单和生成支付二维码
     * @param param
     * @return
     */
    @Override
    public String createNativeAmectPayOrder(HashMap param) {

        int userId = MapUtil.getInt(param, "userId");
        int amectId = MapUtil.getInt(param, "amectId");

        //根据罚款单ID和用户ID查询罚款单记录, param{ userId, amectId }
        HashMap map = amectDao.searchAmectByCondition(param);
        if(map != null && map.size() > 0){
            //次代码的作用是将Map中的字符串类型的金额转换为整型，并将其乘以100。这种操作通常用于将金额从元转换为分，或者从分转换为元。
            String amount = new BigDecimal(MapUtil.getStr(map, "amount")).multiply(new BigDecimal("100")).intValue() + "";

            //用于获取微信支付订单ID和生成支付二维码
            try{
                WXPay wxPay = new WXPay(myWXPayConfig);
                param.clear();
                // 下面为请求参数
                //	随机字符串:5K8264ILTKCH16CQ2502SI8ZNMTM67VS
                param.put("nonce_str", WXPayUtil.generateNonceStr());
                //商品订单描述：罚款订单
                param.put("body","缴纳罚款");
                // 商品订单号：20150806125346  UUID
                param.put("out_trade_no", MapUtil.getStr(map, "uuid"));
                //订单金额
                param.put("total_fee", amount);
                //终端IP
                param.put("spbill_create_ip", "127.0.0.1");
                //通知地址: 这里设置了内网渗透，穿透地址为：http://s10.s100.vip:17357
                param.put("notify_url", "http://s10.s100.vip:17357/emos-api/amect/recieveMessage");
                //交易类型
                param.put("trade_type", "NATIVE");
                //生成数字签名字符串
                String sign = WXPayUtil.generateSignature(param, myWXPayConfig.getKey());
                param.put("sign", sign);
                //创建支付订单, 用于扫码支付
                Map<String,String> result = wxPay.unifiedOrder(param);
                //微信支付订单ID，由微信支付平台返回的 prepayId
                String prepayId = result.get("prepay_id");
                // 支付链接，需要生成二维码让手机扫码
                String codeUrl = result.get("code_url");
                //判断获取的微信订单ID是否存在,在DAO层中有个status变量用来判断订单是否已支付，支付过的（status=2）则不在生成 prepayId
                if(prepayId != null){
                    param.clear();
                    param.put("prepayId", prepayId);
                    param.put("amectId", amectId);
                    int rows = amectDao.updatePrepayId(param);
                    if (rows != 1) {
                        throw new EmosException("更新罚款单的支付订单ID失败");
                    }

                    //把支付订单的URL生成二维码  QrConfig 为hutool框架中专门用于生成二维码的工具类
                    QrConfig qrConfig = new QrConfig();
                    qrConfig.setWidth(255);
                    qrConfig.setHeight(255);
                    //该函数是用于生成二维码中空白边框的边距
                    qrConfig.setMargin(2);

                    String qrCodeBase64 = QrCodeUtil.generateAsBase64(codeUrl,qrConfig,"jpg");
                    return qrCodeBase64;

                }else{
                    log.error("创建支付订单失败", result);
                    throw new EmosException("创建支付订单失败");
                }

            }catch (Exception e){
                log.error("创建支付订单失败", e);
                throw new EmosException("创建支付订单失败 ");
            }

        }else{
            throw new EmosException("没有找到罚款单");
        }


    }

    /**
     * 付完款项之后将状态进行修改，结果返回给用户
     * @param param
     * @return
     */
    @Override
    public int updateStatus(HashMap param) {
        int rows = amectDao.updateStatus(param);
        return rows;
    }

    /**
     * 通过用户UUID查询 userId
     * @param uuid
     * @return
     */
    @Override
    public int searchUserIdByUUID(String uuid) {

        int userId = amectDao.searchUserIdByUUID(uuid);
        return userId;
    }


    /**
     * 用户付款成功后，查询微信平台返回的支付结果信息
     * @param param
     */
    @Override
    public void searchNativeAmectPayResult(HashMap param) {
        //获取数据库中订单信息：uuid、prepayId等
        HashMap map = amectDao.searchAmectByCondition(param);
        //如果map包含订单信息
        if(MapUtil.isNotEmpty(map)){

            //获取uuid
            String uuid = MapUtil.getStr(map,"uuid");
            //清空上传的参数信息，重新添加返回给微信平台的响应数据
            param.clear();
            param.put("appid", myWXPayConfig.getAppID());
            param.put("mch_id", myWXPayConfig.getMchID());
            param.put("out_trade_no", uuid);
            param.put("nonce_str", WXPayUtil.generateNonceStr());
            try {
                //获取数字签名
                String sign = WXPayUtil.generateSignature(param,myWXPayConfig.getKey());
                param.put("sign", sign);
                WXPay wxPay = new WXPay(myWXPayConfig);
                // 发送请求参数 param 给微信平台
                Map<String, String> result = wxPay.orderQuery(param);
                String returnCode = result.get("return_code");
                String resultCode = result.get("result_code");
                //当请求响应成功时，更新订单状态
                if("SUCCESS".equals(resultCode) && "SUCCESS".equals(returnCode)){

                    //获取微信平台返回的交易状态码
                    String tradeState = result.get("trade_state");
                    //查询订单支付成功，修改数据状态
                    if("SUCCESS".equals(tradeState)){
                        //更新订单状态
                        amectDao.updateStatus(new HashMap(){{
                            put("uuid",uuid);
                            put("status",2);
                        }});
                    }
                }
            }catch (Exception e){
                log.error("执行异常",e);
                throw new EmosException("执行异常");
            }

        }



    }

    /**
     * 用于查询罚款表单，图视化
     * @param param
     * @return
     */
    @Override
    public HashMap searchChart(HashMap param) {

        //查询罚款类型
        ArrayList<HashMap> chart_1 = amectDao.searchChart_1(param);
        //查询罚款金额区间
        ArrayList<HashMap> chart_2 = amectDao.searchChart_2(param);
        //查询罚款付款人数和未付款人数
        ArrayList<HashMap> chart_3 = amectDao.searchChart_3(param);
        param.clear();
        int year = DateUtil.year(new Date());
        param.put("year", year);
        // 获取未付款名单
        param.put("status", 1);
        ArrayList<HashMap> list_1 = amectDao.searchChart_4(param);
        // 获取已付款人员名单
        param.replace("status", 2);
        ArrayList<HashMap> list_2 = amectDao.searchChart_4(param);

        //下面代码中（for循环这里），主要作用为：预先创建一个包含所有月份的 HashMap 列表，并初始化记录数为 0，
        // 以便在后续查询中将数据填充到这个列表中
        ArrayList<HashMap> chart_4_1 = new ArrayList<>();
        ArrayList<HashMap> chart_4_2 = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            HashMap map = new HashMap();
            map.put("month", i);
            map.put("ct", 0);
            chart_4_1.add(map);
            //map.clone 是为了保证每个列表中的 HashMap 对象是独立的拷贝，而不是共享同一个对象
            chart_4_2.add((HashMap) map.clone());
        }
        //将list_1（未付款）的记录数按照月份逐个拷贝过去
        list_1.forEach(one -> {
            chart_4_1.forEach(temp -> {
                if (MapUtil.getInt(one, "month") == MapUtil.getInt(temp, "month")) {
                    temp.replace("ct", MapUtil.getInt(one, "ct"));
                }
            });
        });
        //将list_2（已付款）的记录数按照月份逐个拷贝过去
        list_2.forEach(one -> {
            chart_4_2.forEach(temp -> {
                if (MapUtil.getInt(one, "month") == MapUtil.getInt(temp, "month")) {
                    temp.replace("ct", MapUtil.getInt(one, "ct"));
                }
            });
        });

        /**
         * 将各个图表封装成HashMap返回到控制层中
         */
        HashMap map = new HashMap() {{
            put("chart_1", chart_1);
            put("chart_2", chart_2);
            put("chart_3", chart_3);
            put("chart_4_1", chart_4_1);
            put("chart_4_2", chart_4_2);
        }};
        return map;
    }
}
