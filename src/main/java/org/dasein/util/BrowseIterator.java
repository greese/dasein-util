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

/* $Id: BrowseIterator.java,v 1.4 2005/09/26 14:15:05 greese Exp $ */
/* Copyright © 2004-2005 Valtira Corporation, All Rights Reserved */
package org.dasein.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;

/**
 * <p>
 *   Iterates through a multi-page list of items. The iterator
 *   makes a small violation of the {@link java.util.Iterator}
 *   contract by returning false for {@link #next()} even when
 *   more elements exist. The list is specifically exhausted 
 *   when both {@link #next()} and {@link #hasMorePages()}
 *   return false.
 * </p>
 * <p>
 *   This iterator divides a list into pages of a specified
 *   page size. When no page size is specified, the value
 *   from {@link #DEFAULT_PAGE_SIZE} is used. You can specify
 *   a {@link java.util.Comparator} to perform sorts on the
 *   list prior to pagination.
 * </p>
 * <p>
 *   This class also will store an ID field to help you create
 *   unique identifiers for the list for temporary storage and
 *   reference. You can ignore this value if you have no use for
 *   it.
 * </p>
 * <p>
 *   Typical use of this iterator will be to display a list of
 *   items one page at a time via a web interface. When you
 *   first get the list and display the first page, you might
 *   have the following code (functions in italics are dependent
 *   on your implementation environment):
 * </p>
 * <p>
 *   <code>
 *     Collection&lt;MyItem&gt; items = <i>someItemListing();</i><br/>
 *     int page_size = 20;<br/>
 *     BrowseIterator&lt;MyItem&gt; it;<br/>
 *     <br/>
 *     it = new BrowseIterator&lt;MyItem&gt;(1, items, 20, null);<br/>
 *     <i>storeList(1, it);</i><br/>
 *     while( it.hasNext() ) {<br/>
 *       MyItem ob = it.next();<br/>
 *       <br/>
 *       // display object in your list<br/>
 *     }
 *   </code>
 * </p>
 * <p>
 *   When the user clicks the next button, your UI can respond:
 * </p>
 * <p>
 *   <code>
 *     long id = <i>readValueFromParameters();</i><br/>
 *     BrowseIterator&lt;MyItem&gt; it = <i>lookupList(id);</i><br/>
 *     <br/>
 *     it.nextPage();<br/>
 *     while( it.hasNext() ) {<br/>
 *       MyItem ob = it.next();<br/>
 *       <br/>
 *       // display object in your list<br/>
 *     }
 *   </code>
 * </p>
 * <p>
 * Last modified: $Date: 2005/09/26 14:15:05 $
 * </p>
 * @version $Revision: 1.4 $
 * @author George Reese
 * @param <T> the type of object being iterated over
 */
public class BrowseIterator<T> implements Serializable, Iterator<T> {
    private static final long serialVersionUID = -6480287782521593109L;

    /**
	 * Default page size when none is specified.
	 */
	static public final int  DEFAULT_PAGE_SIZE = 20;
	
	/**
	 * Iterator for the current page in the list.
	 */
	private Iterator<T>         currentPage = null;
	/**
	 * An optional unique ID to identify this list.
	 */
	private long                listId      = -1L;
	/**
	 * The index of the current page.
	 */
	private int                 pageIndex   = 0;
	/**
	 * The number of items allowed in each page.
	 */
	private int                 pageSize    = DEFAULT_PAGE_SIZE;
	/**
	 * A list of all the pages in the iterator.
	 */
	private List<Collection<T>> pages       = null;
	/**
	 * The sorting rules for the list.
	 */
	private Comparator<T>       sorter      = null;

    /**
     * Constructs a new, unsorted browse iterator with the 
     * default page size having the specified items.
     * @param items The items to be iterated over.
     */
	public BrowseIterator(Collection<T> items) {
		this(-1L, items, DEFAULT_PAGE_SIZE, null);
	}
	
    /**
     * Constructs a new, unsorted browse iterator with the
     * specified page size having the specified items.
     * @param items the items to iterate over
     * @param ps the page size for each page in the iterator
     */
	public BrowseIterator(Collection<T> items, int ps) {
		this(-1L, items, ps, null);
	}
	
    /**
     * Constructs a new iterator with items sorted by the
     * specified sorter.
     * @param items the items to be iterated over
     * @param ps the page size for each page in the iterator
     * @param sort the rules for sorting the items
     */
	public BrowseIterator(Collection<T> items, int ps, Comparator<T> sort) {
		this(-1L, items, ps, sort);
	}

    /**
     * Constructs an iterator having the specified unique ID.
     * @param lid the unique ID to be used to reference this list
     * @param items the items to iterate over
     * @param ps the page size for each page in the iterator
     */
	public BrowseIterator(long lid, Collection<T> items, int ps) {
		this(lid, items, ps, null);
	}
	
    /**
     * Constructs an iterator having the specified unique ID with
     * specific sorting rules and page size.
     * @param lid the unique ID for this iterator
     * @param items the list of items to iterate over
     * @param ps the page size for each page in the iterator
     * @param sort the rules for sorting the items
     */
	public BrowseIterator(long lid, Collection<T> items, int ps, Comparator<T> sort) {
		super();
		listId = lid;		
		setup(items, ps, sort);
	}

    /**
     * Sets the iterator to the first page in the iterator.
     */
	public void firstPage() {
		if( pageIndex != 0 ) {
			pageIndex = 0;
			currentPage = null;
		}
	}

    /**
     * @return the number of items in the list on all pages
     */
    public int getItemCount() {
        int count = 0;
        
        for( Collection<T> page : pages ) {
            count += page.size();
        }
        return count;
    }
    
    /**
     * Provides an application-defined unique identifier for this
     * iterator. Some applications may need to store browse
     * iterators for asynchronous access at a later time. The
     * application can create a unique ID value to associate with
     * this list and use the unique ID to store the iterator.
     * If no application-specified value is set, this method
     * will return -1L.
     * @return the unique list identifier
     */
	public long getListId() {
		return listId;
	}

    /**
     * @return the current page number
     */
	public int getPage() {
		if( pageIndex == 0 && pages.size() < 1 ) {
			return 0;
		}
		return pageIndex+1;
	}
	
    /**
     * @return the number of pages in this browse iterator
     */
	public int getPageCount() {
		return pages.size();
	}
	
    /**
     * @return the size of each page
     */
	public int getPageSize() {
		return pageSize;
	}
	
    /**
     * @return the sorter being used to sort elements in the iterator
     */
	public Comparator<T> getSorter() {
		return sorter;
	}
	
    /**
     * @return true if there are more pages to navigate
     */
	public boolean hasMorePages() {
		int sz = pages.size();
		
		if( (sz-1) > pageIndex ) {
			return true;
		}
		return false;
	}
	
	/**
     * @return true if there are more elements in this page 
	 */
	public boolean hasNext() {
		if( currentPage == null ) {
			if( !loadPage() ) {
				return false;
			}
		}
		return currentPage.hasNext();
	}

    /**
     * Navigates to the last page in the iterator.
     */
	public void lastPage() {
		int idx = pages.size()-1;
		
		if( idx < 0 ) {
			idx = 0;
		}
		if( pageIndex != idx ) {
			pageIndex = idx;
			currentPage = null;
		}
	}

    /**
     * Loads the next page
     * @return false if there are no more pages to load
     */
	private boolean loadPage() {
		Collection<T> list;
		
		if( pages.size() <= pageIndex ) {
			return false;
		}
		list = pages.get(pageIndex);
		currentPage = list.iterator();
		return true;
	}
	
	/**
     * @return the next element in the iterator 
	 */
	public T next() {
		if( currentPage == null ) {
			if( !loadPage() ) {
				throw new NoSuchElementException();
			}
		}
		return currentPage.next();
	}

    /**
     * Navigates to the next page in the iterator.
     * @return true if it navigated to a new page, false if there are no more pages
     */
	public boolean nextPage() {
		pageIndex++;
		if( pageIndex >= pages.size() ) {
			pageIndex--;
			return false;
		}
		loadPage();
		return true;
	}
	
    /**
     * Navigates to the previous page in the iterator.
     * @return true if it navigated to a new page, false if there are no previous pages
     */
	public boolean previousPage() {
		if( pageIndex == 0 ) {
			return false;
		}
		pageIndex--;
		loadPage();
		return true;
	}
	
	/**
     * Throws an {@link UnsupportedOperationException} always. 
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

    /**
     * Resets the list to the beginning of the iterator.
     *
     */
    public void reset() {
        currentPage = null;
    }

    /**
     * Navigates to the specified page number with 0 being the first page.
     * @param p the page number to navigate to
     * @return true if the page was successfully navigated to
     */
	public boolean setPage(int p) {
		p--;
		if( p < 0 ) {
			return false;
		}
		if( p >= pages.size() ) {
			return false;
		}
		pageIndex = p;
		currentPage = null;
		return true;
	}

    /**
     * Changes the page size of the list and reforms the list. May cause unexpected
     * behavior in terms of what is considered the "current page" after resizing.
     * @param ps the new page size
     */
	public void setPageSize(int ps) {
		ArrayList<T> tmp = new ArrayList<T>();
        
        for( Collection<T> page : pages ) {
            for( T item : page ) {
			    tmp.add(item);
            }
		}
        setup(tmp, ps, sorter);
	}
	
    /**
     * Sets the collection up using the specified parameters.
     * @param items the items in the iterator
     * @param ps the page size for the structure
     * @param sort the sorter, if any, to use for sorting
     */
	private void setup(Collection<T> items, int ps, Comparator<T> sort) {
		Collection<T> list;
		Iterator<T> it;

		if( sort == null ) {
			list = items;
		}
		else {
			list = new TreeSet<T>(sort);
			list.addAll(items);			
		}
		sorter = sort;
		pageSize = ps;
        pages = new ArrayList<Collection<T>>();
		it = list.iterator();		
		while( it.hasNext() ) {
			ArrayList<T> page = new ArrayList<T>();
			
			pages.add(page);
			for(int i=0; i<ps; i++ ) {
                T ob;
                
				if( !it.hasNext() ) {
					break;
				}
                ob = it.next();
				page.add(ob);
			}
		}
	}
	
    /**
     * Re-sorts the list according to the specified sorter.
     * @param sort the sorter to sort the list with
     */
	public void sort(Comparator<T> sort) {
		ArrayList<T> tmp = new ArrayList<T>();
		
        for( Collection<T> page : pages ) {
			tmp.addAll(page);
		}
		setup(tmp, pageSize, sort);
	}
    
    /**
     * @return a string depiction of the list
     */
    public String toString() {
        StringBuffer str = new StringBuffer();
        
        str.append("[" + listId);
        str.append(": ");
        str.append("Page " + (pageIndex+1));
        str.append(" of " + getPageCount());
        str.append(" (" + getItemCount());
        str.append(" items)]");
        return str.toString();
    }
}
