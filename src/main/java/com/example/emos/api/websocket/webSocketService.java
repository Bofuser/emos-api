package com.example.emos.api.websocket;


import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint(value = "/socket")
@Component
public class webSocketService {

    //用于保存WebSocket连接对象
    public static ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
    }

    /**
     * 链接关闭调用的方法
     * @param session
     */
    public void onClose(Session session){
        Map map = session.getUserProperties();
        if(map.containsKey("userId")){
            String userId = MapUtil.getStr(map, "userId");
            sessionMap.remove(userId);
        }
    }

    /**
     * 接收消息
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session){

        //把字符串转换成JSON
        JSONObject json = JSONUtil.parseObj(message);
        String opt = json.getStr("opt");
        //判断ping是否正确
        if("ping".equals(opt)){
            return;
        }

        //从JSON中取出token
        String token = json.getStr("token");
        //从token中取出userId
        String userId = StpUtil.stpLogic.getLoginIdByToken(token).toString();

        //取出Session绑定的属性
        Map map = session.getUserProperties();
        //判断是否由userId 的属性，如果没有属性就1给其赋值属性，关闭链接的时候需要用到
        if(!map.containsKey("userId")){
            map.put("userId",userId);
        }

        //把Session缓存起来
        if(sessionMap.containsKey(userId)){
            //替换缓存中的session
            sessionMap.replace(userId,session);
        }else {
            //向缓存中添加session
            sessionMap.put(userId,session);
        }

        sendInfo("ok", userId);
    }

    /**
     * 发送消息给客户端
     * @param message
     * @param userId
     */
    public static void sendInfo(String message, String userId) {

        if(StrUtil.isNotBlank(userId) && sessionMap.containsKey(userId)){
            //从缓存中查找对象并发送消息
            Session session = sessionMap.get(userId);
            //发送消息
            sendMessage(message, session);
        }

    }

    /**
     * 封装发送消息给客户端
     * @param message
     * @param session
     */
    private static void sendMessage(String message, Session session) {

        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e){
            log.error("执行异常", e);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误", error);
    }
}
