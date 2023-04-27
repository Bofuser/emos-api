package com.example.emos.api.service.impl;

import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbDeptDao;
import com.example.emos.api.db.pojo.TbDept;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class DeptServiceImpl implements DeptService {
    @Autowired
    private TbDeptDao deptDao;

    @Override
    public ArrayList<HashMap> searchAllDept() {
        ArrayList<HashMap> list = deptDao.searchAllDept();
        return list;
    }

    /**
     * 通过id查询将数据渲染到添加或修改表单中
     * @param id
     * @return
     */
    @Override
    public HashMap searchById(int id) {
        HashMap map = deptDao.searchById(id);
        return map;
    }

    /**
     * 查询用户部门并分页
     * @param param
     * @return
     */
    @Override
    public PageUtils searchDeptByPage(HashMap param) {

        //获取后端返回的查询列表
        ArrayList<HashMap> list = deptDao.searchDeptByPage(param);
        //获取后端查询的部门数count
        long count = deptDao.searchDeptCount(param);
        //将param中包含的length和start提取出来保存到pageUtils中
        int length = (Integer) param.get("length");
        int start = (Integer) param.get("start");

        PageUtils pageUtils = new PageUtils(list, count, start, length);

        return pageUtils;
    }


    /**
     * 新增部门
     * @param dept
     * @return rows
     */
    @Override
    public int insert(TbDept dept) {
        int rows = deptDao.insert(dept);
        return rows;
    }

    /**
     * 通过id修改部门信息,原理和新增差不多，只不过多了个id将他们进行区别
     * @param dept
     * @return rows
     */
    @Override
    public int update(TbDept dept) {

        int rows = deptDao.update(dept);
        return rows;
    }

    /**
     * 删除部门，通过传入的id值来进行删除还是批量删除
     * @param ids
     * @return rows
     */
    @Override
    public int deleteDeptByIds(Integer[] ids) {

        if(!deptDao.searchCanDelete(ids)){

            throw new EmosException("无法删除关联用户的部门");
        }

        int rows = deptDao.deleteDeptByIds(ids);
        return rows;
    }
}
