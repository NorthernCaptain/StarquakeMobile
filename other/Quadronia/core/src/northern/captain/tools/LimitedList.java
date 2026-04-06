package northern.captain.tools;

import java.util.ArrayList;

/**
 * Limited capacity list backed by ArrayList
 */
public class LimitedList<E> extends ArrayList<E>
{
    private int maxQty;
    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list
     * @throws IllegalArgumentException
     *          if the specified initial capacity
     *          is negative
     */
    public LimitedList(int initialCapacity)
    {
        super(initialCapacity);
        maxQty = initialCapacity;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link java.util.Collection#add})
     */
    @Override
    public boolean add(E e)
    {
        boolean ret = super.add(e);
        if(size() > maxQty)
        {
            remove(0);
        }

        return ret;
    }

    /**
     * Pop last record and return it
     * @param ifEmpty
     * @return
     */
    public E popBack(E ifEmpty)
    {
        if(isEmpty())
            return ifEmpty;

        return remove(size()-1);
    }

    /**
     * Just peek last record, don't pop it
     * @param ifEmpty
     * @return last record or 'ifEmpty'
     */
    public E peekBack(E ifEmpty)
    {
        if(isEmpty())
            return ifEmpty;

        return get(size()-1);
    }
}
