package com.example.loginapi.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

@Controller
public class ExchangeRateController {

    @RequestMapping("/getExchangeRateDetailsByCurrency")
    public @ResponseBody
    JsonObject getExchangeRateDetails(String  currency) throws IOException {

        JsonObject jsonObject = new JsonObject();
        String data = getExchangeRateData(currency);
        data = data.replaceAll("^\"|\"$", "");
        StringTokenizer jsonTokenizer = new StringTokenizer(data,",");
        String internalData[];
        String expectedExchangeRateOutput = null;
        ArrayList otherCurrencies = new ArrayList();


        if (currency.equalsIgnoreCase("BRL")) {
            otherCurrencies.add("GBP");
            otherCurrencies.add("EUR");
        }


        if (currency.equalsIgnoreCase("GBP")) {
            otherCurrencies.add("BRL");
            otherCurrencies.add("EUR");
        }


        if (currency.equalsIgnoreCase("EUR")) {
            otherCurrencies.add("BRL");
            otherCurrencies.add("GBP");
        }
        while (jsonTokenizer.hasMoreTokens()) {
            expectedExchangeRateOutput = jsonTokenizer.nextToken();
            internalData = StringUtils.split(expectedExchangeRateOutput,":");
            System.out.println(internalData[0]+internalData[1]);
            if (internalData[0].substring(2,internalData[0].length()-1).equalsIgnoreCase(currency)) {
                jsonObject.addProperty(currency, internalData[1]);
            }
            if (internalData[0].substring(1,internalData[0].length()-1).equalsIgnoreCase(otherCurrencies.get(0).toString())) {
                jsonObject.addProperty(otherCurrencies.get(0).toString(), internalData[1]);
            }
            if (internalData[0].substring(1,internalData[0].length()-1).equalsIgnoreCase(otherCurrencies.get(1).toString())) {
                jsonObject.addProperty(otherCurrencies.get(1).toString(), internalData[1]);
            }
        }

        return jsonObject;
    }

    private String getExchangeRateData(String currency) throws IOException {
        String data = null;
        StringBuilder responseData = new StringBuilder();
        JsonObject jsonObject = null;
        URL url = null;
        url = new URL("https://api.exchangerate-api.com/v4/latest/" + currency);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                responseData.append(line);
            }
            jsonObject = new Gson().fromJson(responseData.toString(), JsonObject.class);

            data = jsonObject.get("rates").toString();

        }
        // System.out.println(data);
        return data;
    }


}
