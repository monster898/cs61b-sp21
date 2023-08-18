package deque;

import java.util.Comparator;
import java.util.Iterator;

public class MaxArrayDeque<T> extends ArrayDeque<T>{
    private final Comparator<T> defaultComparator;
    public MaxArrayDeque(Comparator<T> c) {
        super();
        defaultComparator = c;
    }

    public T max() {
        return getMax(defaultComparator);
    }

    public T max(Comparator<T> c) {
        return getMax(c);
    }

    private T getMax(Comparator<T> c) {
        T max = null;
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            T currentValue = it.next();
            int cmp = c.compare(currentValue, max);
            if (cmp > 0) {
                max = currentValue;
            }
        }
        return max;
    }
}
