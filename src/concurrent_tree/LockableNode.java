package concurrent_tree;

import java.util.concurrent.locks.ReentrantLock;

/**
 * LockableNode Class
 * 
 * Extends the basic Node class (used in the sequential binary tree) to add
 * an external lock used for fine-grained locking.
 * 
 * @author Rob Lyerly <rlyerly@vt.edu>
 * @param <T> The generic data type being wrapped by the LockableNode class. 
 */
public class LockableNode<T> {
	
	public T data;
	public ReentrantLock lock;
	public LockableNode<T> left;
	public LockableNode<T> right;
	
	/**
	 * Instantiates a LockableNode object.  Stores a reference to the data,
	 * instantiates the lock and sets up the LockableNode pointers.
	 * @param data The data object wrapped by the new LockableNode
	 */
	public LockableNode(T data) {
		this.data = data;
		left = null;
		right = null;
		lock = new ReentrantLock();
	}
	
	/**
	 * Convenience method to lock the LockableNode's lock.
	 */
	public void lock() {
		lock.lock();
	}
	
	/**
	 * Convenience method to unlock the LockableNode's lock.
	 */
	public void unlock() {
		lock.unlock();
	}
}
