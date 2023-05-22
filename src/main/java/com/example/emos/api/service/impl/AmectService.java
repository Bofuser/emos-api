package com.example.emos.api.service.impl;

import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbAmectDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
public class AmectService implements com.example.emos.api.service.AmectService {

    @Autowired
    private TbAmectDao amectDao;

    @Override
    public PageUtils searchAmectByPage(HashMap param) {
        return null;
    }




}
