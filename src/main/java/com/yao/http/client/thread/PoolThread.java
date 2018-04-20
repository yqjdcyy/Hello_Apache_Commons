package com.yao.http.client.thread;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2014/12/11.
 */
public class PoolThread extends Thread{
    private final CloseableHttpClient httpClient;
    private final HttpClientContext context;
    private final HttpGet httpget;
    private CountDownLatch latch;

    public PoolThread(CloseableHttpClient httpClient, HttpGet httpget, CountDownLatch latch) {
        this.httpClient = httpClient;
        //多线程时，建议为每个httpClient创建各自的context
        this.context = HttpClientContext.create();
        this.httpget = httpget;
        this.latch= latch;
    }

    @Override
    public void run() {
        try {
            CloseableHttpResponse response = httpClient.execute(httpget, context);
            try {
                HttpEntity entity = response.getEntity();
                String out= EntityUtils.toString(entity);
                System.out.println("\t"+ out.substring(out.indexOf("\"")+ 1, out.lastIndexOf("\"")));
            } finally {
                response.close();
                latch.countDown();
            }
        } catch (ClientProtocolException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
