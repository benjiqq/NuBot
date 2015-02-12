package com.nubits.nubot.webui;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nubits.nubot.options.OptionsJSON;
import com.nubits.nubot.trading.keys.ApiKeys;

public class JsonRead {

    private static String exchangeFile = "config.json";

    public static HashMap<String, ApiKeys> getKeys(){
        HashMap<String, ApiKeys> map = new HashMap<String, ApiKeys>();
        
        JSONParser parser = new JSONParser();

        JSONArray a;
        try {
            a = (JSONArray) parser.parse(new FileReader(exchangeFile));

//            for (Object o : a) {
//                JSONObject person = (JSONObject) o;
//
//                String exc = (String) person.get("exchange");
//                String k = (String) person.get("apikey");
//                String s = (String) person.get("apisecret");
//                ApiKeys key = new ApiKeys(s,k);
//                map.put(exc, key);
//
//            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        return map;
    }
    
    public static HashMap<String, String> getOptions(){
        HashMap<String, String> map = new HashMap<String, String>();
        
        JSONParser parser = new JSONParser();

        JSONArray a;
        try {
            a = (JSONArray) parser.parse(new FileReader(exchangeFile));

//            for (Object o : a) {
//                JSONObject person = (JSONObject) o;
//
//                String exc = (String) person.get("exchange");
//                String k = (String) person.get("apikey");
//                String s = (String) person.get("apisecret");
//                ApiKeys key = new ApiKeys(s,k);
//                map.put(exc, key);
//
//            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        return map;
    }
    
    public static void print(){
        HashMap<String, ApiKeys> m = getKeys();
        Iterator<String> it = m.keySet().iterator();
        while(it.hasNext()){
            String k = it.next();
            ApiKeys key = m.get(k);
            System.out.println(key);
        }
    }
    
    public static void printMap(Map m){
        
        Iterator<String> it = m.keySet().iterator();
        while(it.hasNext()){
            String k = it.next();
            Object o = m.get(k);
            System.out.println(k + ": " + o);
        }
    }
    
    public static void main(final String[] args) {
        Map setMap = new HashMap(); 
        setMap = OptionsJSON.getOptionsFromSingleFile(exchangeFile);
        printMap(setMap);

    }
}

// Map<String, Object> data = new HashMap<String, Object>();
// data.put("name", "Mars");
// data.put("age", 32);
// data.put("city", "NY");
// JSONObject json = new JSONObject();
// json.putAll(data);
// System.out.printf("JSON: %s", json.toString());
