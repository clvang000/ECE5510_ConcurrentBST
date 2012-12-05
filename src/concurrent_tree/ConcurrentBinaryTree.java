package concurrent_tree;

/**
 * Concurrent Binary Tree Interface
 * 
 * This file describes the interface required for the concurrent binary trees
 * implemented as the final project for ECE 5510 Multiprocessor Programming.
 * 
 * All concurrent trees must implement the three main tree functions, insert,
 * remove and contains.  The function declarations (and descriptions) are
 * listed below.
 * 
 * @author Rob Lyerly <rlyerly@vt.edu>
 *
 */
public interface ConcurrentBinaryTree<T extends Comparable<? super T>> {

	/**
	 * Inserts the passed object into the tree.
	 *  
	 * @param data The data object to insert into the tree
	 * @return True if data object was successfully inserted into the tree,
	 * false otherwise
	 */
	boolean insert(T data);
	
	/**
	 * Removes the passed object from the tree.
	 * 
	 * @param data The data object to remove from the tree
	 * @return The data object if it is found in the tree and was successfully
	 * removed, null otherwise
	 */
	T remove(T data);
	
	/**
	 * Check to see if the binary tree contains the passed object.
	 * 
	 * @param data The data object to find in the tree
	 * @return True if the binary tree contains the data object, false
	 * otherwise
	 */
	boolean contains(T data);
}
