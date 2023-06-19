package com.example.emos.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.common.util.R;
import com.example.emos.api.controller.form.*;
import com.example.emos.api.db.pojo.TbAmect;
import com.example.emos.api.service.impl.AmectServiceImpl;
import com.example.emos.api.websocket.webSocketService;
import com.example.emos.api.wxpay.WXPayUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/amect")
@Tag(name = "AmectController", description = "罚款功能web接口")
@Slf4j
public class AmectController {

    @Autowired
    private AmectServiceImpl amectService;

    @Value("${wx.key}")
    private String key;


    @PostMapping("/searchAmectByPage")
    @Operation(summary = "查询罚款分页记录")
    @SaCheckLogin
    public R searchAmectByPage(@Valid @RequestBody SearchAmectByPageForm form){

        // 判断startDate 和 endDate，只能同时为空或者同时不为空
        if((form.getStartDate() != null && form.getEndDate() == null) || (form.getStartDate() == null && form.getEndDate() != null)){
            return R.error("startDate和endDate只能同时为空，或者不为空");
        }

        //获取返回的值存储到 form 表单中
        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1) * length;

        // 将form中的数据转换成Hashmap数据结构，封装到 param中
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        //将start数据存储到param中
        param.put("start", start);
        //获取用户登录的ID信息
        param.put("currentUserId", StpUtil.getLoginIdAsInt());

        //用于判断是否是 含有 SELECT权限或者ROOT权限的用户，如果不含有则只能查看用户自己的信息
        if(!(StpUtil.hasPermission("AMECT:SELECT") || StpUtil.hasPermission("ROOT"))){
            param.put("userId",StpUtil.getLoginIdAsInt());
        }
        //将信息打包到 pageUtils 中返回出去
        PageUtils pageUtils = amectService.searchAmectByPage(param);
        return R.ok().put("page", pageUtils);

    }


    @PostMapping("/insert")
    @Operation(summary = "添加罚款信息")
    @SaCheckPermission(value = {"ROOT","AMECT:INSERT"}, mode = SaMode.OR)
    public R insert(@Valid @RequestBody InsertAmectForm form){

        //初始化TbAmect列表，用来存储form信息
        ArrayList<TbAmect> list = new ArrayList<>();
        for(Integer userId : form.getUserId()){
            TbAmect amect = new TbAmect();
            //BigDecimal是 Java 中的一个精确的、任意精度的十进制数值类型。它用于进行高精度的数值计算，避免了浮点数计算中的精度损失问题。
            amect.setAmount(new BigDecimal(form.getAmount()));
            amect.setTypeId(form.getTypeId());
            amect.setReason(form.getReason());
            amect.setUserId(userId);
            amect.setUuid(IdUtil.simpleUUID());
            list.add(amect);
        }

        int rows = amectService.insert(list);
        return R.ok().put("rows",rows);

    }


    /**
     * 根据用户 Id 查找罚款信息
     * @param form
     * @return
     */
    @PostMapping("/searchById")
    @Operation(summary = "根据ID 查找 罚款记录")
    @SaCheckPermission(value = {"ROOT", "AMECT:SELECT"}, mode = SaMode.OR)
    public R searchById(@Valid @RequestBody SearchAmectByIdForm form){

        HashMap map = amectService.searchById(form.getId());
        return R.ok(map);
    }


    @PostMapping("/update")
    @Operation(summary = "根据ID查找罚款记录")
    @SaCheckPermission(value = {"ROOT", "AMECT:SELECT"}, mode = SaMode.OR)
    public R update(@Valid @RequestBody UpdateAmectForm form){

        //将表单中的信息转换成哈希表信息
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        //将需要修改的表单信息传给数据库中
        int rows = amectService.update(param);
        //返回修改的表单信息
        return R.ok().put("rows",rows);
    }

    @PostMapping("/deleteAmectByIds")
    @Operation(summary = "删除罚款记录")
    @SaCheckPermission(value = {"ROOT", "AMECT:DELETE"}, mode = SaMode.OR)
    public R deleteAmectByIds(@Valid @RequestBody DeleteAmectByIdsForm form){

        //返回删除的信息
        int rows = amectService.deleteAmectByIds(form.getIds());
        return R.ok().put("rows", rows);

    }

    @PostMapping("/createNativeAmectPayOrder")
    @Operation(summary = "创建Native支付罚款订单")
    @SaCheckLogin
    public R createNativeAmectPayOrder(@Valid @RequestBody createNativeAmectPayOrderForm form){

        int userId = StpUtil.getLoginIdAsInt();
        int amectId = form.getAmectId();
        //封装 amectId 和  userId 用于查询微信支付订单
        HashMap param = new HashMap(){{
            put("amectId", amectId);
            put("userId", userId);
        }};

        //生成的支付二维码以 Base64编码返回， Base64编码将二进制数据转换为可打印字符串
        String qrCodeBase64 = amectService.createNativeAmectPayOrder(param);
        return R.ok().put("qrCodeBase64", qrCodeBase64);

    }

    /**
     * 付款成功后接收微信平台发送过来的付款通知
     * @param request
     * @param response
     */
    @Operation(summary = "接收消息通知")
    @RequestMapping("/recieveMessage")
    public void recieveMessage(HttpServletRequest request, HttpServletResponse response)throws Exception{

        request.setCharacterEncoding("utf-8");
        Reader reader = request.getReader();
        BufferedReader buffer = new BufferedReader(reader);
        String line = buffer.readLine();
        StringBuffer temp = new StringBuffer();
        while (line != null) {
            temp.append(line);
            line = buffer.readLine();
        }
        buffer.close();
        reader.close();
        String xml = temp.toString();

        //利用数字证书验证收到的响应内容，避免有人伪造付款结果发送给Web方法。
        if (WXPayUtil.isSignatureValid(xml, key)) {
            Map<String, String> map = WXPayUtil.xmlToMap(temp.toString());
            String resultCode = map.get("result_code");
            String returnCode = map.get("return_code");
            //如果微信那里返回的状态码为 SUCCESS
            if ("SUCCESS".equals(resultCode) && "SUCCESS".equals(returnCode)) {
                //out_trade_no这个为商品订单ID，也是罚款单的UUID
                String outTradeNo = map.get("out_trade_no");    //罚款单UUID
                //更新订单状态
                HashMap param = new HashMap() {{
                    put("status", 2);
                    put("uuid", outTradeNo);
                }};
                //获取修改的返回结果，rows
                int rows = amectService.updateStatus(param);
                if (rows == 1) {
                    // 向前端页面推送付款结果
                    //根据罚款单ID（uuid）查询用户ID
                    int userId = amectService.searchUserIdByUUID(outTradeNo);
                    //向用户推送结果
                    webSocketService.sendInfo("收款成功", userId + "");
                    //给微信平台返回响应，设置响应的格式为 XML格式
                    response.setCharacterEncoding("utf-8");
                    response.setContentType("application/xml");
                    Writer writer = response.getWriter();
                    BufferedWriter bufferedWriter = new BufferedWriter(writer);
                    bufferedWriter.write("<xml><return_code><![CDATA[SUCCESS]]></return_code> <return_msg><![CDATA[OK]]></return_msg></xml>");
                    bufferedWriter.close();
                    writer.close();
                } else {
                    log.error("更新订单状态失败");
                    response.sendError(500, "更新订单状态失败");
                }
            }
        } else{
            log.error("数字签名异常");
            response.sendError(500, "数字签名异常");
        }

    }

}
