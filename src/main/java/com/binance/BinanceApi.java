package com.binance;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * Created by stephen on 10/17/17.
 */
public class BinanceApi {
    
    private static final String API_KEY = "";
    private static final String API_SECRET = "";
    
    private static final String BASE_URL = "https://www.binance.com/api/";
    
    private final HttpClient httpClient = HttpClientBuilder.create().build();
    
    public double getPriceForSymbol(String symbol) throws IOException {
        String str = publicRequest(BASE_URL + "v1/ticker/allPrices", Collections.emptyList());
        
        List<SymbolPrice> prices = new Gson().fromJson(str, new TypeToken<List<SymbolPrice>>() {}.getType());
        
        Map<String, Double> symbolPriceMap =
                prices.stream().collect(Collectors.toMap(v -> v.getSymbol(), v -> v.getPrice()));
        
        return symbolPriceMap.get(symbol);
    }
    
    public OrderBook getOrderBook(String symbol) throws IOException {
        final String responseStr =
                publicRequest(BASE_URL + "v1/depth", Arrays.asList(new BasicNameValuePair("symbol", symbol)));
        
        final OrderBookResponse response = new Gson().fromJson(
                responseStr,
                OrderBookResponse.class);
        
        return getOrderBookFromResponse(response);
    }
    
    private OrderBook getOrderBookFromResponse(OrderBookResponse orderBookResponse) {
        return new OrderBook(
                orderBookResponse.getBids()
                        .stream()
                        .map(v -> new Order(Double.valueOf((String)v.get(0)), Double.valueOf((String)v.get(1))))
                        .collect(Collectors.toList()),
                orderBookResponse.getAsks()
                        .stream()
                        .map(v -> new Order(Double.valueOf((String)v.get(0)), Double.valueOf((String)v.get(1))))
                        .collect(Collectors.toList()));
    }
    
    public NewOrderResponse limitOrder(String symbol, Side side, TimeInForce timeInForce, double quantity, double price)
            throws Exception {
        
        final String responseStr = signedRequest(
                BASE_URL + "v3/order",
                Arrays.asList(
                        new BasicNameValuePair("symbol", symbol),
                        new BasicNameValuePair("side", side.toString()),
                        new BasicNameValuePair("timeInForce", timeInForce.toString()),
                        new BasicNameValuePair("type", Type.LIMIT.toString()),
                        new BasicNameValuePair("quantity", String.valueOf(quantity)),
                        new BasicNameValuePair("price", String.valueOf(price))), HttpMethod.POST);
        
        return new Gson().fromJson(responseStr, NewOrderResponse.class);
        
    }
    
    public NewOrderResponse marketOrder(String symbol, Side side, double quantity)
            throws Exception {
        
        final String responseStr = signedRequest(
                BASE_URL + "v3/order",
                Arrays.asList(
                        new BasicNameValuePair("symbol", symbol),
                        new BasicNameValuePair("side", side.toString()),
                        new BasicNameValuePair("type", Type.MARKET.toString()),
                        new BasicNameValuePair("quantity", String.valueOf(quantity))), HttpMethod.POST);
        
        return new Gson().fromJson(responseStr, NewOrderResponse.class);
        
    }
    
    public OrderDetails getOrder(String symbol, String orderId) throws Exception {
        
        final String responseStr = signedRequest(
                BASE_URL + "v3/order",
                Arrays.asList(
                        new BasicNameValuePair("orderId", orderId),
                        new BasicNameValuePair("symbol", symbol)),
                HttpMethod.GET);
        
        return new Gson().fromJson(responseStr, OrderDetails.class);
        
    }
    
    public String cancelOrder(String symbol, String orderId) throws Exception {
        
        return signedRequest(
                BASE_URL + "v3/order",
                Arrays.asList(
                        new BasicNameValuePair("orderId", orderId),
                        new BasicNameValuePair("symbol", symbol)),
                HttpMethod.DELETE);
        
    }
    
    public List<OrderDetails> getOpenOrders(String symbol) throws Exception {
        
        final String responseStr = signedRequest(
                BASE_URL + "v3/openOrders",
                Arrays.asList(new BasicNameValuePair("symbol", symbol)),
                HttpMethod.GET);
        
        return new Gson().fromJson(responseStr, new TypeToken<List<OrderDetails>>() {}.getType());
        
    }
    
    public AccountDetails getAccountDetails() throws Exception {
        return new Gson().fromJson(
                signedRequest(BASE_URL + "v3/account", Collections.emptyList(), HttpMethod.GET),
                AccountDetails.class);
    }
    
    public String publicRequest(String apiUrl, List<NameValuePair> params) throws IOException {
        
        final String queryString = URLEncodedUtils.format(params, "UTF-8");
        
        apiUrl += "?" + queryString;
        
        final HttpUriRequest httpUriRequest = new HttpGet(apiUrl);
        
        return httpClient.execute(httpUriRequest, new HttpResponseHandler());
    }
    
    public String apiRequest(String apiUrl, List<NameValuePair> params, HttpMethod httpMethod) throws IOException {
        
        final String queryString = URLEncodedUtils.format(params, "UTF-8");
        
        apiUrl += "?" + queryString;
        
        final HttpUriRequest httpUriRequest = httpUriRequestForMethod(httpMethod, apiUrl);
        
        httpUriRequest.setHeader("X-MBX-APIKEY", API_KEY);
        
        return httpClient.execute(httpUriRequest, new HttpResponseHandler());
    }
    
    public String signedRequest(String apiUrl, List<NameValuePair> params, HttpMethod httpMethod)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException {
        
        List<NameValuePair> allParams = Stream.concat(
                params.stream(),
                Stream.of(new BasicNameValuePair("timestamp", String.valueOf(System.currentTimeMillis()))))
                .collect(Collectors.toList());
        
        final String queryString = URLEncodedUtils.format(allParams, "UTF-8");
        
        final String hmac = calculateHMAC(queryString, API_SECRET);
        
        apiUrl += "?" + queryString + "&signature=" + hmac;
        
        final HttpUriRequest httpUriRequest = httpUriRequestForMethod(httpMethod, apiUrl);
        
        httpUriRequest.setHeader("X-MBX-APIKEY", API_KEY);
        
        return httpClient.execute(httpUriRequest, new HttpResponseHandler());
    }
    
    private HttpUriRequest httpUriRequestForMethod(HttpMethod httpMethod, String apiUrl) {
        switch (httpMethod) {
            case GET:
                return new HttpGet(apiUrl);
            case POST:
                return new HttpPost(apiUrl);
            case DELETE:
                return new HttpDelete(apiUrl);
        }
        return null;
    }
    
    private String calculateHMAC(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        final String algorithm = "HmacSHA256";
        
        Mac mac = Mac.getInstance(algorithm);
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), algorithm);
        mac.init(secretKey);
        
        return Hex.encodeHexString(mac.doFinal(data.getBytes()));
    }
    
    public static void main(String[] args) throws Exception {
        BinanceApi binanceApi = new BinanceApi();
        
        // Getting latest price of a symbol
        System.out.println(binanceApi.getPriceForSymbol("ETHBTC"));
        
        // Getting depth of a symbol
        System.out.println(binanceApi.getOrderBook("ETHBTC"));
        
        // Placing a LIMIT order
        final NewOrderResponse limitOrder = binanceApi.limitOrder("ETHBTC", Side.BUY, TimeInForce.GTC, 0.02d, 0.055d);
        System.out.println(limitOrder);
        
        // Placing a MARKET order
        System.out.println(binanceApi.marketOrder("ETHBTC", Side.BUY, 0.02d));
        
        // Checking an order’s status
        System.out.println(binanceApi.getOrder("ETHBTC", limitOrder.getOrderId()));
        
        // Cancelling an order
        System.out.println(binanceApi.cancelOrder("ETHBTC", limitOrder.getOrderId()));
        
        // Getting list of open orders
        System.out.println(binanceApi.getOpenOrders("ETHBTC"));
        
        // Getting list of current position
        System.out.println(binanceApi.getAccountDetails());
    }
}
