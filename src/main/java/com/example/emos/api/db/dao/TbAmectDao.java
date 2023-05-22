package com.example.emos.api.db.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbAmectDao {

    public ArrayList<HashMap> searchAmectByPage(HashMap param);
    public long searchAmectCount (HashMap param);

}
