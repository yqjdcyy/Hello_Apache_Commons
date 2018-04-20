package com.yao.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Yao on 2014/12/21.
 */
public class ReadUtils {

    public static void main(String[] args){
        System.out.println(readProp("content.properties", "content.get.url"));
        System.out.println(readProp("content.properties", "content.get.ids"));
    }

    /***
     * 使用java自带方法进行文件信息读取
     * @param fileName
     */
    public static String readProp(String fileName, String property){
        Properties prop= new Properties();
        String outStr= "";
        InputStream input = null;

        try {
            input= ReadUtils.class.getClassLoader().getResourceAsStream(fileName);
            prop.load(input);
            input.close();
            outStr= prop.getProperty(property).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outStr;
    }

    /***
     * 批量对Property文件的多个参数值进行获取
     * @param fileName
     * @param props
     * @return
     */
    public static String[] readProp(String fileName, String... props){
        Properties prop= new Properties();
        InputStream input = null;
        String[] retStrs= new String[props.length];

        try {
            input= ReadUtils.class.getClassLoader().getResourceAsStream(fileName);
            prop.load(input);
            input.close();
            for(int i= 0; i< props.length; i++){
                retStrs[i]= prop.getProperty(props[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return retStrs;
    }
}
