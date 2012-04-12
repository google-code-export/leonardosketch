package org.joshy.sketch.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: Jun 18, 2010
* Time: 2:05:12 PM
* To change this template use File | Settings | File Templates.
*/
public class BiList<K,V> {
    private ArrayList<K> l1;
    private ArrayList<V> l2;
    private Map<K,V> forwardMap;
    private Map<V,K> reverseMap;

    public BiList() {
        l1 = new ArrayList<K>();
        l2 = new ArrayList<V>();
        forwardMap = new HashMap<K,V>();
        reverseMap = new HashMap<V,K>();
    }

    public void add(K key, V value) {
        l1.add(key);
        l2.add(value);
        forwardMap.put(key,value);
        reverseMap.put(value,key);
    }

    public Iterable<K> keys() {
        return l1;
    }

    public Iterable<V> values() {
        return l2;
    }

    public V getValue(K key) {
        return forwardMap.get(key);
    }

    public K getKey(V value) {
        return reverseMap.get(value);
    }
}
