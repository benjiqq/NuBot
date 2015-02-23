package com.nubits.nubot.options;


import com.google.gson.*;
import org.json.simple.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OptionsSerializer implements JsonSerializer<NuBotOptions> {


    //TODO: pretty pring
    //Gson gson = new GsonBuilder().setPrettyPrinting().create();
    //JsonParser jp = new JsonParser();
    //JsonElement je = jp.parse(uglyJSONString);
    //String prettyJsonString = gson.toJson(je);

    @Override
    public JsonElement serialize(NuBotOptions opt, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = new JsonObject();


        /*JsonObject opt = new JsonObject();

        Map setMap = new HashMap();
        for (Object o : tempSet) {
            Map.Entry entry = (Map.Entry) o;
            setMap.put(entry.getKey(), entry.getValue());
        }

        JSONObject oldObject = (JSONObject) optionsJSON.get("secondary-peg-options");

        Set tempSet = oldObject.entrySet();

        pegOptionsJSON = new org.json.JSONObject(setMap);
        */

        //Map setMap = new HashMap();
        //setMap.put("dualside", opt.isDualSide());



        //Gson gson=new Gson();

        //Map<String,String> map=new HashMap<String,String>();
        //map=(Map<String,String>) gson.fromJson("" + opt, map.getClass());

        //JSONObject newopt = new JSONObject();
        //newopt.put("dualside", opt.isDualSide());

        //newopt = (JSONObject)new JSONObject(setMap);

        //newopt.add("dualside",opt.is)

        root.addProperty("exchangename", opt.getExchangeName());
        root.addProperty("apikey", opt.getApiKey());
        root.addProperty("apisecret", opt.getApiSecret());
        root.addProperty("dualside", opt.getApiSecret());

        root.addProperty("submitliquidity", opt.isSendRPC());




        root.addProperty("pair", opt.getPair().toString());

        return root;
    }
}