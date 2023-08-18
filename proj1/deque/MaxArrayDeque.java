package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private final Comparator<T> defaultComparator;
    public MaxArrayDeque(Comparator<T> c) {
        super();
        defaultComparator = c;
    }

    public T max() {
        return max(defaultComparator);
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }
        T maxValue = get(0);
        for (T currentValue : this) {
            if (c.compare(currentValue, maxValue) > 0) {
                maxValue = currentValue;
            }
        }
        return maxValue;
    }
}
