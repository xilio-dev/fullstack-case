package cn.xilio.jdk.soap.simple;

import javax.xml.soap.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class StockServiceClient {
    public static void main(String[] args) {
        try {
            // 创建SOAP请求
            SOAPMessage request = createSOAPRequest("IBM");

            // 发送请求到服务端
            SOAPMessage response = sendSOAPRequest(request, "http://localhost:8080/StockService");

            // 解析响应
            double price = parseSOAPResponse(response);
            System.out.println("股票 IBM 的价格是: $" + price);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SOAPMessage createSOAPRequest(String stockName) throws Exception {
        // 创建SOAP消息
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        SOAPBody body = message.getSOAPBody();
        SOAPElement requestElement = body.addChildElement("GetStockPrice", "ns1", "http://example.com/stock");
        requestElement.addChildElement("StockName").setTextContent(stockName);
        return message;
    }

    private static SOAPMessage sendSOAPRequest(SOAPMessage request, String url) throws Exception {
        // 创建HTTP连接
        URL endpoint = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");

        // 发送SOAP请求
        try (OutputStream output = conn.getOutputStream()) {
            request.writeTo(output);
        }

        // 读取响应
        try (InputStream input = conn.getInputStream()) {
            return MessageFactory.newInstance().createMessage(null, input);
        }
    }

    private static double parseSOAPResponse(SOAPMessage response) throws Exception {
        // 解析SOAP响应
        SOAPBody body = response.getSOAPBody();
        String priceStr = body.getElementsByTagName("Price").item(0).getTextContent();
        return Double.parseDouble(priceStr);
    }
}
