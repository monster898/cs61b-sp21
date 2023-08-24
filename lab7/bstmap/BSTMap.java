package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private class BSTNode {
        K key;
        V value;
        BSTNode left;
        BSTNode right;
        public BSTNode(K key, V value) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
        }

    }

    private BSTNode root;
    private int size;

    public BSTMap() {
        root = null;
        size = 0;
    }
    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        BSTNode node = get(key, root);
        return node != null;
    }

    @Override
    public V get(K key) {
        BSTNode node = get(key, root);
        if (node == null) {
            return null;
        }
        return node.value;
    }

    private BSTNode get(K key, BSTNode root) {
        if (root == null) {
            return null;
        }

        if (key.equals(root.key)) {
            return root;
        }

        BSTNode leftResult = get(key, root.left);
        BSTNode rightResult =  get(key, root.right);

        return leftResult != null ? leftResult : rightResult;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        size += 1;
        BSTNode node = new BSTNode(key, value);
        root = put(node, root);
    }

    private BSTNode put(BSTNode node, BSTNode root) {
        if (root == null) {
            return node;
        }

        int cmp = node.key.compareTo(root.key);
        if (cmp == 0) {
            root.value = node.value;
        } else if (cmp > 0) {
            root.right =  put(node, root.right);
        } else {
            root.left = put(node, root.left);
        }
        return root;
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        dfs(root, set);
        return set;
    }

    private void dfs(BSTNode root, Set<K> set) {
        if (root == null) {
            return;
        }

        set.add(root.key);

        dfs(root.left, set);
        dfs(root.right, set);
    }

    @Override
    public V remove(K key) {
        BSTNode node = get(key, root);
        if (node == null) {
            return null;
        }
        size -= 1;
        root = delete(node, root);
        return node.value;
    }

    private BSTNode delete(BSTNode node, BSTNode root) {
        int cmp = node.key.compareTo(root.key);
        if (cmp > 0) {
            root.right = delete(node, root.right);
        } else if (cmp < 0) {
            root.left = delete(node, root.left);
        } else {
            // no child
            if (node.left == null && node.right == null) {
                return null;
            }

            // one child
            if (node.left != null && node.right == null) {
                return node.left;
            }

            if (node.left == null && node.right != null) {
                return node.right;
            }

            // two child
            // find left side rightmost node
            BSTNode newRoot = findRightMostNode(node.left);
            // delete it from tree
            node.left = delete(newRoot, node.left);
            // insert is as new root
            newRoot.left = node.left;
            newRoot.right = node.right;
            return newRoot;
        }
        return root;
    }

    private BSTNode findRightMostNode(BSTNode root) {
        if (root.right == null) {
            return root;
        }
        return findRightMostNode(root.right);
    }

    @Override
    public V remove(K key, V value) {
        BSTNode node = get(key, root);
        if (node == null || node.value != value) {
            return null;
        }
        size -= 1;
        return remove(key);
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    /*prints out BSTMap in order of increasing Key.*/
    public void printInOrder() {
        printInOrderHelper(root);
    }

    private void printInOrderHelper(BSTNode root) {
        if (root == null) {
            return;
        }
        printInOrderHelper(root.left);
        System.out.print(root.key + " ");
        printInOrderHelper(root.right);
    }

}
