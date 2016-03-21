package com.safziy.collection;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import com.safziy.collection.temp.IntConcurrentHashMap;
import com.safziy.collection.temp.IntHashMap;

public class CollectionTest {

	@Test
	public void testHashMap() {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		long benginTime = System.currentTimeMillis();
		for (int i = 0; i < 10000; ++i) {
			for (int j = 0; j < 10000; ++j) {
				if (map.get(j) == null) {
					map.put(j, 1);
				}
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - benginTime);
	}

	/**
	 * 使用IntHashMap 效率极大地提升
	 */
	@Test
	public void testIntHashMap() {
		IntHashMap<Integer> map = new IntHashMap<Integer>();
		long benginTime = System.currentTimeMillis();
		for (int i = 0; i < 10000; ++i) {
			for (int j = 0; j < 10000; ++j) {
				if (map.get(j) == null) {
					map.put(j, 1);
				}
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - benginTime);
	}
	
	@Test
	public void testConcurrentHashMap() {
		ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<Integer, Integer>();
		long benginTime = System.currentTimeMillis();
		for (int i = 0; i < 10000; ++i) {
			for (int j = 0; j < 10000; ++j) {
				if (map.get(j) == null) {
					map.put(j, 1);
				}
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - benginTime);
	}

	/**
	 * 使用IntConcurrentHashMap 效率极大地提升
	 */
	@Test
	public void testIntConcurrentHashMap() {
		IntConcurrentHashMap<Integer> map = new IntConcurrentHashMap<Integer>();
		long benginTime = System.currentTimeMillis();
		for (int i = 0; i < 10000; ++i) {
			for (int j = 0; j < 10000; ++j) {
				if (map.get(j) == null) {
					map.put(j, 1);
				}
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - benginTime);
	}
	
	@Test
	public void testInitSizeHashMap(){
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>(20000);
		long benginTime = System.currentTimeMillis();
		for (int i = 0; i < 10000; ++i) {
			for (int j = 0; j < 10000; ++j) {
				if (map.get(j) == null) {
					map.put(j, 1);
				}
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - benginTime);
	}
}
