package se.eit.db_package;


import java.util.Iterator;
import java.util.NoSuchElementException;


public class DbIterator <T> implements Iterator<T> {

	DbList<T> dbList;
	int index=-1;
	int nextIndex=0;
	
	public DbIterator(DbList<T> dbList)
	{
		this.dbList=dbList;
	}
	
	
    public boolean hasNext() {
    	final int m = dbList.getCapacity();
    	nextIndex = index + 1;
    	while (nextIndex<m)
    	{
    		T n=dbList.get(nextIndex);
    		if (n!=null)
    		{
    			return true;
    		}
    		++nextIndex;
    	}
    	return false;
    }

    public T next() {
    	final int m = dbList.getCapacity();

    	index = nextIndex;
    	while (index<m)
    	{
    		T n=dbList.get(index);
    		if (n!=null)
    		{
    			nextIndex=index+1;
    			return n;
    		}
    		++index;
    	}
    	throw(new NoSuchElementException());
    }

    public void remove() {
		dbList.remove(index);
    }
}