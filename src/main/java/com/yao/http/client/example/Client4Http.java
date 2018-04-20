package com.yao.http.client.example;

import com.yao.http.client.thread.PoolThread;
import com.yao.http.client.thread.SingleThread;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2014/12/11.
 */
public class Client4Http {

    private static Integer THREAD_CNT= 1000;

    public static void main(String[] args) {
        Client4Http client= new Client4Http();
        client.testPool();
        client.testSingle();
    }

    /**
     * HttpClient连接SSL
     */
    public void ssl() {
        CloseableHttpClient httpclient = null;
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream instream = new FileInputStream(new File("d:\\tomcat.keystore"));
            try {
                // 加载keyStore d:\\tomcat.keystore
                trustStore.load(instream, "123456".toCharArray());
            } catch (CertificateException e) {
                e.printStackTrace();
            } finally {
                try {
                    instream.close();
                } catch (Exception ignore) {
                }
            }
            // 相信自己的CA和所有自签名的证书
            SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy()).build();
            // 只允许使用TLSv1协议
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[]{"TLSv1"}, null,
                    SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
            httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
            // 创建http请求(get方式)
            HttpGet httpget = new HttpGet("https://localhost:8443/myDemo/Ajax/serivceJ.action");
            System.out.println("executing request" + httpget.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                HttpEntity entity = response.getEntity();
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                if (entity != null) {
                    System.out.println("Response content length: " + entity.getContentLength());
                    System.out.println(EntityUtils.toString(entity));
                    EntityUtils.consume(entity);
                }
            } finally {
                response.close();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } finally {
            if (httpclient != null) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * post方式提交表单（模拟用户登录请求）
     */
    public void postForm() {
        // 创建默认的httpClient实例.
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建httppost
        HttpPost httppost = new HttpPost("http://localhost:8080/myDemo/Ajax/serivceJ.action");
        // 创建参数队列
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("username", "admin"));
        formparams.add(new BasicNameValuePair("password", "123456"));
        UrlEncodedFormEntity uefEntity;
        try {
            uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httppost.setEntity(uefEntity);
            System.out.println("executing request " + httppost.getURI());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    System.out.println("--------------------------------------");
                    System.out.println("Response content: " + EntityUtils.toString(entity, "UTF-8"));
                    System.out.println("--------------------------------------");
                }
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
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

    /**
     * 发送 post请求访问本地应用并根据传递参数不同返回不同结果
     */
    public void post() {
        // 创建默认的httpClient实例.
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建httppost
        HttpPost httppost = new HttpPost("http://192.168.70.76:9091/invoke");
        httppost.setHeader("_serverid", "server-demo");
        httppost.setHeader("_method", "postArgs");
        httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        // 创建参数队列
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("type", "yao"));
        UrlEncodedFormEntity uefEntity;
        try {
            uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httppost.setEntity(uefEntity);
            System.out.println("executing request " + httppost.getURI());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    System.out.println("--------------------------------------");
                    System.out.println("Response content: " + EntityUtils.toString(entity, "UTF-8"));
                    System.out.println("--------------------------------------");
                }
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
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

    /**
     * 发送 get请求
     */
    public void get() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            // 创建httpget.
            HttpGet httpget = new HttpGet("http://192.168.70.76:9091/invoke?tenantCode=ksf");
            httpget.setHeader("_serverid", "server-demo");
            httpget.setHeader("_method", "getArgs");
            System.out.println("executing request " + httpget.getURI());
            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                System.out.println("--------------------------------------");
                // 打印响应状态
                System.out.println(response.getStatusLine());
                if (entity != null) {
                    // 打印响应内容长度
                    System.out.println("Response content length: " + entity.getContentLength());
                    // 打印响应内容
                    System.out.println("Response content: " + EntityUtils.toString(entity));
                }
                System.out.println("------------------------------------");
            } finally {
                response.close();
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

    /**
     * 上传文件
     */
    public void upload() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httppost = new HttpPost("http://localhost:8080/myDemo/Ajax/serivceFile.action");

            FileBody bin = new FileBody(new File("F:\\image\\sendpix0.jpg"));
            StringBody comment = new StringBody("A binary file of some kind", ContentType.TEXT_PLAIN);

            HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("bin", bin).addPart("comment", comment).build();

            httppost.setEntity(reqEntity);

            System.out.println("executing request " + httppost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    System.out.println("Response content length: " + resEntity.getContentLength());
                }
                EntityUtils.consume(resEntity);
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //BasicClientConnectionManager
    public static void basicClientConnectionManager() throws ClientProtocolException, IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://m.weather.com.cn/data/101010100.html");
        HttpResponse response = httpClient.execute(httpGet);
        String result = EntityUtils.toString(response.getEntity(), Charset.forName("utf-8"));
        System.out.println(result);
        httpClient.getConnectionManager().shutdown();
    }

    //PoolingClientConnectionManager
    public static void poolingClientConnectionManager() throws ClientProtocolException, IOException {
        SchemeRegistry registry = new SchemeRegistry();//创建schema
        SSLContext sslContext = null;//https类型的消息访问
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SSLSocketFactory sslFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
        registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));//http 80 端口
        registry.register(new Scheme("https", 443, sslFactory));//https 443端口

        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(registry);//创建connectionManager

        cm.setDefaultMaxPerRoute(20);//对每个指定连接的服务器（指定的ip）可以创建并发20 socket进行访问
        cm.setMaxTotal(200);//创建socket的上线是200
        HttpHost localhost = new HttpHost("locahost", 80);
        cm.setMaxPerRoute(new HttpRoute(localhost), 80);//对本机80端口的socket连接上限是80

        HttpClient httpClient = new DefaultHttpClient(cm);//使用连接池创建连接
        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setSoTimeout(params, 60 * 1000);//设定连接等待时间
        HttpConnectionParams.setConnectionTimeout(params, 60 * 1000);//设定超时时间

        try {
            HttpGet httpGet = new HttpGet("http://m.weather.com.cn/data/101010100.html");
            HttpResponse response = httpClient.execute(httpGet);
            String result = EntityUtils.toString(response.getEntity(), Charset.forName("utf-8"));
            System.out.println(result);
        } finally {
            httpClient.getConnectionManager().shutdown();//用完了释放连接
        }
    }

    public static void multiThreadedHttpConnectionManager() {
        HttpClientConnectionManager connMrg = new BasicHttpClientConnectionManager();
        HttpRoute route = new HttpRoute(new HttpHost("www.yeetrack.com", 80));
        // 获取新的连接. 这里可能耗费很多时间
        ConnectionRequest connRequest = connMrg.requestConnection(route, null);
        HttpClientConnection conn = null;

        try {
            conn = connRequest.get(10, TimeUnit.SECONDS);   // 10秒超时
            // 如果创建连接失败
            if (!conn.isOpen()) {
                HttpClientContext context = HttpClientContext.create();
                // establish connection based on its route basic.info
                connMrg.connect(conn, route, 1000, context);
                // and mark it as route complete
                connMrg.routeComplete(conn, route, context);
            }

            //获取连接后进行相关操作
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            connMrg.releaseConnection(conn, null, 1, TimeUnit.MINUTES);
        }
    }

    public static void PoolingHttpClientConnectionMapper() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        // 将最大连接数增加到200
        cm.setMaxTotal(200);
        // 将每个路由基础的连接增加到20
        cm.setDefaultMaxPerRoute(20);
        //将目标主机的最大连接数增加到50
        HttpHost localhost = new HttpHost("www.yeetrack.com", 80);
        cm.setMaxPerRoute(new HttpRoute(localhost), 50);

        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }

    public static void testPool() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(20);
        cm.setMaxTotal(1000);
        cm.setMaxPerRoute(new HttpRoute(new HttpHost("192.168.70.76", 7070)), 300);     //设置指定服务，如SSO以更高配置
        cm.closeExpiredConnections();                       //关闭失效连接
        cm.closeIdleConnections(30, TimeUnit.SECONDS);      //关闭30秒内不活动的连接
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();

        try {
            Long start = System.currentTimeMillis();
            // URL列表数组
            String[] urisToGet = {
                    "http://192.168.70.76:7070/ops/server/batchQueryServerStatus?ids=6,10,11,",
                    "http://192.168.70.76:7070/ops/mule/registerList?rmi=service:jmx:rmi:///jndi/rmi://192.168.70.76:1098/server&curPage=1&pageSize=15&filter=",
                    "http://192.168.70.76:7070/ops/server/queryRemoteMuleAppStatus?rmi=service:jmx:rmi:///jndi/rmi://192.168.70.76:1098/server&appName=Mule.mln-bus",
                    "http://192.168.70.76:7070/ops/mule/serverList?rmi=service:jmx:rmi:///jndi/rmi://192.168.70.76:1098/server&curPage=1&pageSize=15&filter="
            };

            // 为每个url创建一个线程，GetThread是自定义的类
            PoolThread[] threads = new PoolThread[THREAD_CNT];
            CountDownLatch latch = new CountDownLatch(THREAD_CNT);
            for (int i = 0; i < THREAD_CNT; i++) {
                HttpGet httpget = new HttpGet(urisToGet[THREAD_CNT % urisToGet.length]);
                threads[i] = new PoolThread(httpClient, httpget, latch);
            }
            // 启动线程
            for (int j = 0; j < THREAD_CNT; j++) {
                threads[j].start();
            }
            latch.await();
            Long end = System.currentTimeMillis();

            System.out.println("POOL: " + (end - start));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void testSingle() {
        Long start= System.currentTimeMillis();

        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            // 为每个url创建一个线程，GetThread是自定义的类
            SingleThread[] threads = new SingleThread[THREAD_CNT];
            CountDownLatch latch = new CountDownLatch(THREAD_CNT);
            for (int i = 0; i < THREAD_CNT; i++) {
                threads[i] = new SingleThread(httpclient, "http://192.168.70.76:9091/invoke?tenantCode=ksf", "server-demo",  "getArgs", latch);
            }

            // 启动线程
            for (int j = 0; j < THREAD_CNT; j++) {
                threads[j].start();
            }
            latch.await();
            Long end= System.currentTimeMillis();

            System.out.println("SINGLE: "+ (end- start));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pool() {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        Integer CONNECTION_TIMEOUT = 2 * 1000; //设置请求超时2秒钟 根据业务调整
        Integer SO_TIMEOUT = 2 * 1000; //设置等待数据超时时间2秒钟 根据业务调整
        //定义了当从ClientConnectionManager中检索ManagedClientConnection实例时使用的毫秒级的超时时间
        //这个参数期望得到一个java.lang.Long类型的值。如果这个参数没有被设置，默认等于CONNECTION_TIMEOUT，因此一定要设置
        Long CONN_MANAGER_TIMEOUT = 500L; //该值就是连接不够用的时候等待超时时间，一定要设置，而且不能太大 ()

        HttpParams params = new BasicHttpParams();
        params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);
        params.setLongParameter(ClientPNames.CONN_MANAGER_TIMEOUT, CONN_MANAGER_TIMEOUT);
        //在提交请求之前 测试连接是否可用
        params.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);

        PoolingClientConnectionManager conMgr = new PoolingClientConnectionManager();
        conMgr.setMaxTotal(200); //设置整个连接池最大连接数 根据自己的场景决定
        //是路由的默认最大连接（该值默认为2），限制数量实际使用DefaultMaxPerRoute并非MaxTotal。
        //设置过小无法支持大并发(ConnectionPoolTimeoutException: Timeout waiting for connection from pool)，路由是对maxTotal的细分。
        conMgr.setDefaultMaxPerRoute(conMgr.getMaxTotal());//（目前只有一个路由，因此让他等于最大值）

        //另外设置http client的重试次数，默认是3次；当前是禁用掉（如果项目量不到，这个默认即可）
        httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
    }

}
