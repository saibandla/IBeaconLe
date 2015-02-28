package com.example.bhargavbandla.lebeacon;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by BhargavBandla on 28/02/15.
 */
public class JSONParser {
    JSONObject jsonObject;
    public JSONObject excecuteGetTypeResquestFromUrl(String url)
    {
        DefaultHttpClient httpClient=new DefaultHttpClient();
        HttpGet httpGet=new HttpGet(url);
        try {
            HttpResponse response=httpClient.execute(httpGet);
            String jsonString=  EntityUtils.toString(response.getEntity());
            jsonObject= new JSONObject(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    public JSONObject executePostTypeResquestFromParameters(String url,List<NameValuePair> valuePairs)
    {
        DefaultHttpClient httpClient=new DefaultHttpClient();
        HttpPost httpPost=new HttpPost(url);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(valuePairs));
            HttpResponse httpResponse=httpClient.execute(httpPost);
            String jsonString=EntityUtils.toString(httpResponse.getEntity());
            jsonObject=new JSONObject(jsonString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (JSONException e){
            e.printStackTrace();
        }
        return jsonObject;
    }
}
