package com.safziy.commom.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.safziy.commom.service.log.LogUtil;

public class JSONUtils {

	/**
	 * list to JSasONString
	 * 
	 * @param list
	 * @return
	 */
	public static String collection2String(Collection<?> collection) {
		JSONArray jSArray = new JSONArray(collection);
		return jSArray.toString();
	}

	public static String list2String(List<?> list) {
		JSONArray jSArray = new JSONArray(list);
		return jSArray.toString();
	}

	/**
	 * object to JSONString
	 * 
	 * @param object
	 * @return
	 */
	public static String object2tring(Object object) {
		JSONObject jSObject = new JSONObject(object);
		return jSObject.toString();
	}

	/**
	 * map to JSONString
	 * 
	 * @param map
	 * @return
	 */

	public static <K, V> String map2String(Map<K, V> map) {
		JSONObject jSObject = new JSONObject(map);
		return jSObject.toString();
	}

	public static String mapToString(Map<Integer, Integer> map) {
		JSONObject jSObject = new JSONObject(map);
		return jSObject.toString();
	}

	public static String maptoString(Map<Byte, Integer> map) {
		JSONObject jSObject = new JSONObject(map);
		return jSObject.toString();
	}

	public static String multimap2String(Multimap<Object, Object> map) {
		JSONObject jSObject = new JSONObject(map);
		return jSObject.toString();
	}

	@SuppressWarnings("unchecked")
	public static Map<Integer, Multiset<Integer>> string2MultiSet(String value) {
		value = value.trim();
		Map<Integer, Multiset<Integer>> map = new HashMap<Integer, Multiset<Integer>>();
		if (ValidateUtils.isFalse(ValidateUtils.isNotNullAndMoreThanZero(value))) {
			return map;
		}
		try {
			JSONObject jSObject = new JSONObject(value);
			Iterator<String> params = jSObject.keys();
			while (params.hasNext()) {
				String param = params.next();
				if (ValidateUtils.isFalse(map.containsKey(Integer.parseInt(param)))) {
					Multiset<Integer> set = HashMultiset.create();
					map.put(Integer.valueOf(param), set);
				}
				JSONObject tempJsObject = new JSONObject(jSObject.get(param).toString());
				Iterator<String> keys = tempJsObject.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					int tempValue = tempJsObject.getInt(key);
					map.get(Integer.valueOf(param)).add(Integer.parseInt(key), tempValue);
				}
			}
		} catch (JSONException e) {
			LogUtil.error(e);
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, String>> string2ListMap(String value) {
		List<Map<String, String>> list = new LinkedList<Map<String, String>>();
		if(ValidateUtils.isNull(value)){
			return list;
		}
		value = value.trim();
		if (ValidateUtils.isFalse(ValidateUtils.isNotNullAndMoreThanZero(value))) {
			return list;
		}
		try {
			JSONArray jSArray = new JSONArray(value);
			for (int i = 0; i < jSArray.length(); i++) {
				Map<String, String> map = new HashMap<String, String>();
				JSONObject jSObject = jSArray.getJSONObject(i);
				for (Iterator<String> it = jSObject.keys(); it.hasNext();) {
					String key = it.next();
					map.put(key, jSObject.get(key).toString());
				}
				if (map.containsKey("beginTime")) {
					map.put("beginTimeStr", DateUtils.commomFormatDate(Integer.parseInt(map.get("beginTime"))));
				}
				if (map.containsKey("endTime")) {
					map.put("endTimeStr", DateUtils.commomFormatDate(Integer.parseInt(map.get("endTime"))));
				}
				if (map.containsKey("addTime")) {
					map.put("addTimeStr", DateUtils.commomFormatDate(Integer.parseInt(map.get("addTime"))));
				}
				list.add(map);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> string2Map(String value) {
		Map<String, String> map = new HashMap<String, String>();
		if (ValidateUtils.isNotNullAndMoreThanZero(value)) {
			try {
				JSONObject jsb = new JSONObject(value);
				for (Iterator<String> it = jsb.keys(); it.hasNext();) {
					String key = it.next();
					map.put(key, jsb.get(key).toString());
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return map;
	}

}
