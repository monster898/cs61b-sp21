package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {

    private static class Node<T> {
        private final T value;
        private Node<T> prev;
        private Node<T> next;
        Node(T value) {
            this.value = value;
        }

    }

    private final Node<T> sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel = new Node<>(null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
        size = 0;
    }
    public void addFirst(T item) {
        Node<T> newNode = new Node<>(item);
        Node<T> previousFirst = sentinel.next;
        previousFirst.prev = newNode;
        sentinel.next = newNode;
        newNode.prev = sentinel;
        newNode.next = previousFirst;
        size += 1;
    }

    public void addLast(T item) {
        Node<T> newNode = new Node<>(item);
        Node<T> previousLast = sentinel.prev;
        previousLast.next = newNode;
        sentinel.prev = newNode;
        newNode.prev = previousLast;
        newNode.next = sentinel;
        size += 1;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        Node<T> current = sentinel.next;
        while (current != sentinel) {
            System.out.print(current.value);
            current = current.next;
            if (current != sentinel) {
                System.out.print(" ");
            }
        }
        System.out.print("\n");
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        Node<T> first = sentinel.next;
        Node<T> second = first.next;
        sentinel.next = second;
        second.prev = sentinel;
        first.prev = null;
        first.next = null;
        size -= 1;
        return first.value;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }
        Node<T> last = sentinel.prev;
        Node<T> newLast = last.prev;
        sentinel.prev = newLast;
        newLast.next = sentinel;
        last.prev = null;
        last.next = null;
        size -= 1;
        return last.value;
    }

    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        Node<T> current = sentinel;
        int i = 0;
        while (i <= index) {
            current = current.next;
            i += 1;
        }
        return current.value;
    }

    private T getHelper(int current, int index, Node<T> p) {
        if (current == index) {
            return p.value;
        }
        return getHelper(current + 1, index, p.next);
    }
    public T getRecursive(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        return getHelper(0, index, sentinel.next);
    }

    private class MyIterator implements Iterator<T> {
        int visitedCount;
        Node<T> current;
        MyIterator() {
            current = sentinel;
            visitedCount = 0;
        }
        @Override
        public boolean hasNext() {
            return visitedCount < size;
        }

        @Override
        public T next() {
            if (hasNext()) {
                visitedCount += 1;
                T result =  current.next.value;
                current = current.next;
                return result;
            }
            return null;
        }
    }
    public Iterator<T> iterator() {
        return new MyIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Deque)) {
            return false;
        }

        Deque<?> oAsDeque = (Deque<?>) o;

        if (oAsDeque.size() != size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (!get(i).equals(oAsDeque.get(i))) {
                return false;
            }
        }
        return true;
    }


}
