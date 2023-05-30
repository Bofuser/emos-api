package com.example.emos.api.common.util;

import lombok.Data;
import java.io.Serializable;
import java.util.List;


/**
 * 这个工具类是用来处理分页查询的结果，封装查询的数据，将其展示到页面中
 */
@Data
public class PageUtils implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 总记录数
     */
    private long totalCount;
    /**
     * 每页记录数
     */
    private int pageSize;
    /**
     * 总页数
     */
    private int totalPage;
    /**
     * 当前页数
     */
    private int pageIndex;
    /**
     * 列表数据
     */
    private List list;

    /**
     * 用来封装页面查询数据的类，可以不用重复写代码，直接调用这个页面展示类，一般用于业务的“查询”功能
     * @param list  用来存储查询的数据到列表中
     * @param totalCount    数据总数
     * @param pageIndex     当前页数
     * @param pageSize      每页显示的数据条数
     *
     *  这四个参数对应 Service层中的 list, count, start, length
     */
    public PageUtils(List list, long totalCount, int pageIndex, int pageSize) {
        this.list = list;
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.pageIndex = pageIndex;
        this.totalPage = (int) Math.ceil((double) totalCount / pageSize);
    }

}
