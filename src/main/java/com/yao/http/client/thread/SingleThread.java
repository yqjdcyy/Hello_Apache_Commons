package com.yao.http.client.thread;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2014/12/11.
 */
public class SingleThread extends Thread{
    private CloseableHttpClient httpclient;
    private String url;
    private String serverId;
    private String method;
    private CountDownLatch latch;

    public SingleThread(CloseableHttpClient httpclient,String url, String serverId, String method, CountDownLatch latch) {
        this.httpclient= httpclient;
        this.url = url;
        this.serverId = serverId;
        this.method = method;
        this.latch= latch;
    }

    @Override
    public void run() {
        try {
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("_serverid", serverId);
            httpget.setHeader("_method", method);
//            System.out.println("executing request " + httpget.getURI());
            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                // 打印响应状态
//                System.out.println(response.getStatusLine());
                if (entity != null) {
                    // 打印响应内容长度
//                    System.out.println("\tResponse content length: " + entity.getContentLength() + "\tResponse content: " + EntityUtils.toString(entity));
                }
            } finally {
                response.close();
                latch.countDown();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
