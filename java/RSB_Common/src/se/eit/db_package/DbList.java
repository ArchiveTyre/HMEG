package se.eit.db_package;

import java.util.Iterator;

public class DbList<E> implements java.lang.Iterable<E> {
	
    //private Queue<E> queue = new LinkedList<E>();
    //private ArrayList<E> array = new ArrayList<E>();

	private Object[] a=new Object[4];	
	private int lastest=-1;
    private int size=0;
	
    public DbList()
    {
    }
    
    
	private int incWrap(int i)
	{
		if (++i==a.length)
		{
			i=0;
		}
		return i;
	}
	
	// Make the list larger
	private void makeArrayLarger()
	{
		final int n=a.length;

		// Just a warning message in case something gets very big
		if (n>0x10000)
		{
			System.out.println("DbList: many objects, more than "+n);
		}

		// make a larger array
		Object newArray[] = new Object[n*2];

		// copy data over to the new array
		for(int i=0;i<n;i++)
		{
			newArray[i]=a[i];
		}
		
		// use the new array
		a=newArray;
	}


	// Find empty slot, create more slots if needed.
	private int findUnusedId()
	{
        final int m =  a.length;
		// find an unused position in existing list
		for(int i=0;i<m;i++)
		{
			lastest=incWrap(lastest);
			
			if (a[lastest]==null)
			{
				// An unused slot was found
				return lastest;
			}
		}

		// The array is full, make it bigger

		lastest=a.length;

		makeArrayLarger();
				
		return lastest;
	}

    
    // Returns the index at which the element was added
    public int add(E e)
    {        
    	// First try to find a free slot
        final int i = findUnusedId();

        a[i] = e;
        size++;
        
		return i;
    }
    
    // Store an object at a given position in the list. 
    // It will not move objects if there are objects at desired index position.
    // If that position is not free an exception is thrown.
    public void add(int index, E e)
    {        	
    	// First make sure the array is large enough
    	while(index>=a.length)
    	{
    		makeArrayLarger();
    	}
    	
		// Check that the position in list is free.
		if (a[index]==null)
		{
			// position is free
			a[index] = e;
	        size++;
		}
		else
		{
			// that position is not free
			
			// Perhaps this object is already registered there
			if (a[index]==e)
			{
				//debug("already registered");
				// ok
			}
			else
			{
				// If not its an error.
				//error("index already used "+index);
				throw(new ArrayStoreException("that position is not free, index " + index + " capacity " + getCapacity()));
			}
		}
    }

    // Remove the object at position index
    public void remove(int index)
    {
        --size;
    	a[index] = null;
    }

    @SuppressWarnings("unchecked")
	public E get(int index)
    {                
		return (E)a[index];
    }


	@Override
	public Iterator<E> iterator() {
		return new DbIterator<E>(this);
	}

	public int getCapacity()
	{
		return a.length;
	}

    // Gives the number of objects stored here
    public int size()
    {
    	return size;
    }
    
    public void clear()
    {
    	a=new Object[4];	
    	lastest=-1;
        size=0;
    }

    public boolean isEmpty()
    {
    	return size==0;
    }
}
