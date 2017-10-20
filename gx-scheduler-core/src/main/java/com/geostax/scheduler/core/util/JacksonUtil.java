package com.geostax.scheduler.core.util;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Jackson util
 * 
 * 1、obj need private and set/get；
 * 2、do not support inner class；
 * 
 * @author xuxueli 2015-9-25 18:02:56
 */
public class JacksonUtil {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    public static ObjectMapper getInstance() {
        return objectMapper;
    }

    /**
     * bean、array、List、Map --> json
     * 
     * @param obj
     * @return json string
     * @throws Exception
     */
    public static String writeValueAsString(Object obj) {
    	try {
			return getInstance().writeValueAsString(obj);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return null;
    }

    /**
     * string --> bean、Map、List(array)
     * 
     * @param jsonStr
     * @param clazz
     * @return obj
     * @throws Exception
     */
    public static <T> T readValue(String jsonStr, Class<T> clazz) {
    	try {
			return getInstance().readValue(jsonStr, clazz);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
    }
    public static <T> T readValueRefer(String jsonStr, Class<T> clazz) {
    	try {
			return getInstance().readValue(jsonStr, new TypeReference<T>() { });
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
    }

    public static void main(String[] args) {
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("aaa", "111");
			map.put("bbb", "222");
			String json = writeValueAsString(map);
			System.out.println(json);
			System.out.println(readValue(json, Map.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
