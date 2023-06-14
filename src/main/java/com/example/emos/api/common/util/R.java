package com.example.emos.api.common.util;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;


public class R extends HashMap<String, Object> {

    /**
     * R 类的构造方法，在构造 R 对象时自动添加 状态码 和 消息
     * 一般响应成功后返回这个函数的信息
     */
    public R() {
        // 响应状态码
        put("code", HttpStatus.SC_OK);
        // 返回 success 消息
        put("msg", "success");
    }

    /**
     * 向R对象中添加键值对，并返回R对象本身。
     * 这个函数返回 业务的数据内容，如 pageUtils、rows、list表单等业务信息
     *
     * @param key
     * @param value
     * @return
     */
    public R put(String key, Object value) {
        //一般调用这个 put 函数注意要用到键值对信息，如 return R.ok().put("rows", rows);
        super.put(key, value);
        return this;
    }

    /**
     *  返回一个状态码为200，消息为“success”的R对象。
     * @return
     */
    public static R ok() {
        return new R();
    }

    /**
     * 返回一个状态码为200，消息为传入参数 msg 的R对象。
     * @param msg
     * @return
     */
    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    /**
     * 返回一个状态码为200，消息为“success”，并将传入的map中的键值对添加到R对象中的R对象。
     * @param map
     * @return
     */
    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    /**
     * 返回一个状态码为传入参数code，消息为传入参数msg的R对象。
     * 一般用于输出错误信息
     * @param code
     * @param msg
     * @return
     */
    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    /**
     * 返回一个状态码为500，消息为传入参数msg的R对象。
     * @param msg
     * @return
     */
    public static R error(String msg) {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    /**
     * 返回一个状态码为500，消息为“未知异常，请联系管理员”的R对象。
     * @return
     */
    public static R error() {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
    }

}