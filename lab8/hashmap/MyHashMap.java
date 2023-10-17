package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private final double MAX_LOAD;

    private int size = 0;

    private final static int INITIAL_SIZE = 16;

    private final static double DEFAULT_MAX_LOAD = 0.75;


    /** Constructors */
    public MyHashMap() {
        this(INITIAL_SIZE, DEFAULT_MAX_LOAD);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, DEFAULT_MAX_LOAD);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = createTable(initialSize);
        MAX_LOAD = maxLoad;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }
    private int computeIndex(K key) {
        return Math.floorMod(key.hashCode(), buckets.length);
    }

    private Node findNodeByKey(K key, Collection<Node> bucket) {
        if (bucket == null) {
            return null;
        }

        for (Node node: bucket) {
            if (node.key.equals(key)) {
                return node;
            }
        }
        return null;
    }

    private void resize(int newSize) {
        LinkedList<Node> allNodes = new LinkedList<>();
        for (K key: this) {
            V value = get(key);
            Node node = createNode(key, value);
            allNodes.add(node);
        }

        clear();

        Collection<Node>[] newBuckets = createTable(newSize);
        buckets = newBuckets;
        for (Node node: allNodes) {
            put(node.key, node.value);
        }
    }

    @Override
    public void clear() {
        Arrays.fill(buckets, null);
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        V value = get(key);
        return value != null;
    }

    @Override
    public V get(K key) {
        int index = computeIndex(key);
        Collection<Node> bucket = buckets[index];
        Node node = findNodeByKey(key, bucket);

        if (node != null) {
            return node.value;
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    private double loadFactor() {
        return (double) size / buckets.length;
    }

    @Override
    public void put(K key, V value) {
        if (loadFactor() >= MAX_LOAD) {
            resize(size * 2);
        }

        int index = computeIndex(key);
        if (buckets[index] == null) {
            buckets[index] = createBucket();
        }
        Collection<Node> bucket = buckets[index];
        Node node = createNode(key, value);

        if (containsKey(key)) {
            remove(key);
        }
        bucket.add(node);
        size += 1;
    }

    @Override
    public Set<K> keySet() {
        HashSet<K> set = new HashSet<>();
        for (Collection<Node> bucket : buckets) {
            if (bucket == null) continue;
            for (Node node : bucket) {
                set.add(node.key);
            }
        }
        return set;
    }

    @Override
    public V remove(K key) {
        if (!containsKey(key)) {
            return null;
        }
        int index = computeIndex(key);
        Collection<Node> bucket = buckets[index];
        Node node = findNodeByKey(key, bucket);
        bucket.remove(node);
        size -= 1;
        return node.value;
    }

    @Override
    public V remove(K key, V value) {
        V v = get(key);
        if (value.equals(v)) {
            return remove(key);
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }
}
