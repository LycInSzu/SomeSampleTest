package com.cydroid.note.common;

public class IntSortedSet {
    private int[] items = null;
    private int endPos = 0;

    public IntSortedSet() {
        this(10);
    }

    public IntSortedSet(int capacity) {
        items = new int[capacity];
    }

    public void add(int item) {
        int index = findInsertIndex(item);
        items = insert(items, endPos, index, item);
        assert endPos <= items.length;
        ++endPos;
    }

    public int findInsertIndex(int item) {
        return ~ContainerHelpers.binarySearch(items, endPos, item);
    }

    public boolean findAndInsert(int item) {
        int index = indexOf(item);
        if (index >= 0) {
            return true;
        }
        insertAtIndex(item, ~index);
        return false;
    }

    public int indexOf(int item) {
        return ContainerHelpers.binarySearch(items, endPos, item);
    }

    public boolean contains(int item) {
        int index = ContainerHelpers.binarySearch(items, endPos, item);
        return index >= 0;
    }

    public boolean delete(int item) {
        int index = ContainerHelpers.binarySearch(items, endPos, item);
        if (index < 0) {
            return false;
        }
        System.arraycopy(items, index + 1, items, index, endPos - (index + 1));
        --endPos;
        return true;
    }

    public String toString() {
        if (endPos == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder(endPos * 6);
        sb.append('[');
        sb.append(items[0]);
        for (int i = 1; i < endPos; i++) {
            sb.append(", ");
            sb.append(items[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    private void insertAtIndex(int item, int index) {
        items = insert(items, endPos, index, item);
        ++endPos;
    }


    /**
     * Given the current getCount of an array, returns an ideal getCount to which the array should grow.
     * This is typically double the given getCount, but should not be relied upon to do so in the
     * future.
     */
    private static int growSize(int currentSize) {
        return currentSize <= 4 ? 8 : currentSize * 2;
    }


    /**
     *
     */
    private static int[] insert(int[] array, int currentSize, int index, int element) {
        assert currentSize <= array.length;
        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        }

        int[] newArray = new int[growSize(currentSize)];
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }
}
