/**
 * Copyright (C) 1998-2012 enStratus Networks Inc
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

/* $Id: LifoStack.java,v 1.2 2008/09/05 02:06:55 greese Exp $ */
/* Copyright (c) 2008 Valtira LLC, All Rights Reserved */
package org.dasein.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Provides a last-in, first-out stack. Useful for reversing the order of lists.
 * @param <E> the type of items in the stack
 */
public class LifoStack<E> implements List<E> {
    private ArrayList<E> elements;
    
    public LifoStack() {
        elements = new ArrayList<E>();
    }
    
    public LifoStack(Collection<E> list) {
        elements = new ArrayList<E>();
        addAll(list);
    }

    public LifoStack(int size) {
        elements = new ArrayList<E>(size);
    }
    
    public boolean add(E element) {
        Object ob = push(element);
        
        if( element == null ) {
            return (ob == null);
        }
        return element.equals(ob);
    }

    public void add(int index, E element) {
        elements.add(index, element);
    }

    public boolean addAll(Collection<? extends E> list) {
        for( E element : list ) {
            push(element);
        }
        return true;
    }

    public boolean addAll(int index, Collection<? extends E> list) {
        if( index == 0 ) {
            addAll(list);
        }
        return false;
    }

    public void clear() {
        elements.clear();
    }

    public boolean contains(Object element) {
        return elements.contains(element);
    }

    public boolean containsAll(Collection<?> list) {
        return elements.containsAll(list);
    }

    public int indexOf(Object element) {
        return elements.indexOf(element);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public Iterator<E> iterator() {
        return elements.iterator();
    }

    public int lastIndexOf(Object element) {
        return elements.lastIndexOf(element);
    }

    public ListIterator<E> listIterator() {
        return elements.listIterator();
    }

    public ListIterator<E> listIterator(int index) {
        return elements.listIterator(index);
    }

    public E peek() {
        return elements.get(0);
    }
    
    public E pop() {
        if( isEmpty() ) {
            throw new EmptyStackException();
        }
        return remove(0);
    }
    
    public E push(E element) {
        elements.add(0, element);
        return elements.get(0);
    }
    
    public boolean remove(Object element) {
        return elements.remove(element);
    }

    public E remove(int index) {
        return elements.remove(index);
    }

    public boolean removeAll(Collection<?> list) {
        return elements.removeAll(list);
    }

    public boolean retainAll(Collection<?> list) {
        return elements.retainAll(list);
    }

    public E set(int index, E element) {
        return elements.set(index, element);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return elements.subList(fromIndex, toIndex);
    }

    public Object[] toArray() {
        return elements.toArray();
    }

    public <T> T[] toArray(T[] array) {
        return elements.toArray(array);
    }

    public E get(int index) {
        return elements.get(index);
    }

    public int size() {
        return elements.size();
    }
    
    static public void main(String ... args) {
        LifoStack<String> strings = new LifoStack<String>();
        
        for( String arg : args ) {
            strings.push(arg);
        }
        while( !strings.isEmpty() ) {
            System.out.println("Popping: " + strings.pop());
            if( !strings.isEmpty() ) {
                System.out.println("Now on the top: " + strings.peek());
            }
        }
    }
}
