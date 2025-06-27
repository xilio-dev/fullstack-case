package soap.simple;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;


import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class StockServiceServer {
    private static final Map<String, Double> stockPrices = new HashMap<>();

    static {
        stockPrices.put("IBM", 145.50);
        stockPrices.put("GOOGL", 2750.00);
        stockPrices.put("AAPL", 175.25);
    }

    public static void main(String[] args) throws Exception {
        // 创建HTTP服务器，监听8080端口
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/StockService", new StockHandler());
        server.start();
        System.out.println("SOAP服务已启动在 http://localhost:8080/StockService");
    }

    static class StockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                // 读取请求的SOAP消息
                InputStream input = exchange.getRequestBody();
                SOAPMessage request = MessageFactory.newInstance().createMessage(null, input);
                SOAPBody body = request.getSOAPBody();

                // 获取请求中的股票名称
                String stockName = body.getElementsByTagName("StockName").item(0).getTextContent();

                // 查询股票价格
                double price = stockPrices.getOrDefault(stockName, 0.0);

                // 创建SOAP响应
                SOAPMessage response = MessageFactory.newInstance().createMessage();
                SOAPBody responseBody = response.getSOAPBody();
                SOAPElement priceElement = responseBody.addChildElement("GetStockPriceResponse", "ns1", "http://example.com/stock");
                priceElement.addChildElement("Price").setTextContent(String.valueOf(price));

                // 发送响应
                exchange.getResponseHeaders().set("Content-Type", "text/xml; charset=utf-8");
                exchange.sendResponseHeaders(200, 0);
                OutputStream output = exchange.getResponseBody();
                response.writeTo(output);
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
            }
        }
    }
}
