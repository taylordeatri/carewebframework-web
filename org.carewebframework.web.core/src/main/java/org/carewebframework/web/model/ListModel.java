/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.web.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.IteratorUtils;

public class ListModel<T> implements IListModel<T> {
    
    private final List<T> list = new LinkedList<T>();
    
    private final List<IListModelListener> listeners = new ArrayList<>();
    
    private Comparator<T> ascendingComparator;
    
    private final Comparator<T> descendingComparator = new Comparator<T>() {
        
        @Override
        public int compare(T o1, T o2) {
            return -ascendingComparator.compare(o1, o2);
        }
        
    };
    
    public ListModel() {
    }
    
    public ListModel(Collection<T> list) {
        this.list.addAll(list);
    }
    
    @Override
    public void add(int index, T value) {
        list.add(index, value);
        fireEvent(ListEventType.ADD, index, index);
    }
    
    @Override
    public boolean add(T value) {
        if (list.add(value)) {
            int i = list.size() - 1;
            fireEvent(ListEventType.ADD, i, i);
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean addAll(Collection<? extends T> c) {
        return addAll(list.size(), c);
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        int i = list.size();
        
        if (list.addAll(c)) {
            int delta = list.size() - i;
            fireEvent(ListEventType.ADD, index, index + delta - 1);
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean addEventListener(IListModelListener listener) {
        return listeners.add(listener);
    }
    
    @Override
    public void clear() {
        int i = list.size();
        
        if (i > 0) {
            list.clear();
            fireEvent(ListEventType.DELETE, 0, i - 1);
        }
    }
    
    @Override
    public boolean contains(Object value) {
        return list.contains(value);
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }
    
    private void fireEvent(ListEventType type, int startIndex, int endIndex) {
        for (IListModelListener listener : listeners) {
            listener.onListChange(type, startIndex, endIndex);
        }
    }
    
    @Override
    public T get(int index) {
        return list.get(index);
    }
    
    public Comparator<T> getComparator() {
        return ascendingComparator;
    }
    
    @Override
    public int indexOf(Object value) {
        return list.indexOf(value);
    }
    
    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<T> iterator() {
        return IteratorUtils.unmodifiableIterator(list.iterator());
    }
    
    @Override
    public int lastIndexOf(Object value) {
        return list.lastIndexOf(value);
    }
    
    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ListIterator<T> listIterator(int index) {
        return IteratorUtils.unmodifiableListIterator(list.listIterator(index));
    }
    
    @Override
    public T remove(int index) {
        T value = list.remove(index);
        fireEvent(ListEventType.DELETE, index, index);
        return value;
    }
    
    @Override
    public boolean remove(Object value) {
        int i = list.indexOf(value);
        
        if (i >= 0) {
            remove(i);
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        if (list.removeAll(c)) {
            fireEvent(ListEventType.CHANGE, -1, -1);
            return true;
        }
        
        return false;
    }
    
    @Override
    public void removeAllListeners() {
        listeners.clear();
    }
    
    @Override
    public boolean removeEventListener(IListModelListener listener) {
        return listeners.remove(listener);
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        if (list.retainAll(c)) {
            fireEvent(ListEventType.CHANGE, -1, -1);
            return true;
        }
        
        return false;
    }
    
    @Override
    public T set(int index, T value) {
        T result = list.set(index, value);
        
        if (result != value) {
            fireEvent(ListEventType.REPLACE, index, index);
        }
        
        return result;
    }
    
    public void setComparator(Comparator<T> comparator) {
        this.ascendingComparator = comparator;
    }
    
    @Override
    public int size() {
        return list.size();
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void sort(boolean ascending) {
        if (ascendingComparator != null) {
            Object[] a = list.toArray();
            Arrays.sort(a, (Comparator) (ascending ? ascendingComparator : descendingComparator));
            
            for (int newIndex = 0; newIndex < a.length; newIndex++) {
                int oldIndex = list.indexOf(a[newIndex]);
                swap(newIndex, oldIndex);
            }
        }
    }
    
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void swap(int index1, int index2) {
        if (index1 != index2) {
            T item1 = list.get(index1);
            T item2 = list.get(index2);
            list.set(index1, item2);
            list.set(index2, item1);
            fireEvent(ListEventType.SWAP, index1, index2);
        }
    }
    
    @Override
    public void swap(T item1, T item2) {
        swap(list.indexOf(item1), list.indexOf(item2));
    }
    
    @Override
    public Object[] toArray() {
        return list.toArray();
    }
    
    @SuppressWarnings("hiding")
    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }
}
