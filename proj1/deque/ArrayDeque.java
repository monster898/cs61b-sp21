package deque;

import java.util.Iterator;

public class ArrayDeque<T> {
    private  T[] arr;
    private int front;
    private int end;
    private int size;
    private final double SHRINK_RADIO = 0.25;
    private final double ARRAY_MINIMUM_LENGTH = 8;
    public ArrayDeque() {
        arr = (T[]) new Object[8];
        front = 3;
        end = 4;
        size = 0;
    }

    public void addFirst(T item) {
        if (size == arr.length) {
            resize(arr.length * 2);
        }
        arr[front] = item;
        front = minusOne(front);
        size += 1;
    }

    public void addLast(T item) {
        if (size == arr.length) {
            resize(arr.length * 2);
        }
        arr[end] = item;
        end = plusOne(end);
        size += 1;
    }

    public T removeFirst() {
        if ((double) arr.length / size < SHRINK_RADIO) {
            resize(arr.length / 2);
        }
        front = plusOne(front);
        T result = arr[front];
        arr[front] = null;
        size -= 1;
        return result;
    }

    public T removeLast() {
        if ((double) arr.length / size < SHRINK_RADIO) {
            resize(arr.length / 2);
        }
        end = minusOne(end);
        T result = arr[end];
        arr[end] = null;
        size -= 1;
        return result;
    }

    private void resize(int capacity) {
        T[] newArray = (T[]) new Object[capacity];
        int index = 0;
        while (plusOne(front) != end) {
            front = plusOne(front);
            T current = arr[front];
            newArray[index] = current;
            arr[front] = null;
            index += 1;
        }
        front = newArray.length - 1;
        end = size;
        arr = newArray;
    }


    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void printDeque() {
        while (plusOne(front) != end) {
            front = plusOne(front);
            T value = arr[front];
            System.out.print(value);
            if (plusOne(front) != end) {
                System.out.print(" ");
            }
        }
        System.out.print("\n");
    }

    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        return arr[(plusOne(front) + index) % arr.length];
    }

    private class MyIterator implements Iterator<T> {
        private int index;
        public MyIterator() {
            index = front;
        }
        @Override
        public boolean hasNext() {
            return plusOne(index) < end;
        }

        @Override
        public T next() {
            if (hasNext()) {
                index = plusOne(index);
                return arr[index];
            }
            return null;
        }
    }
    public Iterator<T> iterator() {
        return new MyIterator();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ArrayDeque)) {
            return false;
        }

        ArrayDeque<?> objAsArrayDeque = (ArrayDeque<?>) obj;

        if (objAsArrayDeque.size() != size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (objAsArrayDeque.get(i) != get(i)) {
                return false;
            }
        }
        return true;
    }

    private int minusOne(int index) {
        if (index == 0) {
            return arr.length - 1;
        }
        return index - 1;
    }

    private int plusOne(int index) {
        return (index + 1) % arr.length;
    }
}
