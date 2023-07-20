package com.example.emos.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmosApiApplicationTests {

    @Test
    void contextLoads() {


    }


    @Test
    public void test1(){
        String SpringbootVersion = SpringBootVersion.getVersion();
        System.out.println(SpringbootVersion);
    }

}
