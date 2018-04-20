package com.yao.http.client.main;

import com.yao.http.client.thread.PoolThread;
import com.yao.utils.ReadUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yao on 2014/12/21.
 */
public class httpClientMain {

    /***
     *  content.properties
     *      content.get.url -> 维护获取可乐云的链接地址
     *      content.get.ids
     *
     * @param args
     */
    public static void main(String[] args){

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(20);
        cm.setMaxTotal(1000);
        cm.setMaxPerRoute(new HttpRoute(new HttpHost("106.187.50.222", 88)), 300);     //设置指定服务，如SSO以更高配置
        cm.closeExpiredConnections();                       //关闭失效连接
        cm.closeIdleConnections(30, TimeUnit.SECONDS);      //关闭30秒内不活动的连接
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();

        try {
            String[] values={"content.get.url", "content.get.ids"};
            String[] params= ReadUtils.readProp("content.properties", values);

            //获取资源文件总数
            String url= params[0];
            String[] props= params[1].split(" ");
            for(int i=0; i< props.length; i++){
                if(props[i].indexOf("/")>= 0)
                    props[i]= props[i].substring(props[i].lastIndexOf("/")+ 1);
            }

            //创建总线程和相关的语句获取
            PoolThread[] threads = new PoolThread[props.length];
            CountDownLatch latch = new CountDownLatch(props.length);
            for (int i = 0; i < props.length; i++) {
                HttpGet httpget = new HttpGet(url+ props[i]);
                threads[i] = new PoolThread(httpClient, httpget, latch);
            }

            // 启动线程
            for (int j = 0; j < props.length; j++) {
                threads[j].start();
            }
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
