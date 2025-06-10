package cn.xilio.netty.demo14_tls;

import java.security.NoSuchAlgorithmException;

public class CheckTls {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        System.out.println(javax.net.ssl.SSLContext.getDefault().getSupportedSSLParameters().getProtocols());
    }
}
