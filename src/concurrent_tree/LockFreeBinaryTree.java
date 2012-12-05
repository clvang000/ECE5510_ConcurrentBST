package concurrent_tree;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Lock-Free Binary Tree
 * 
 * This class implements a lock-free concurrent binary tree.  Uses
 * AtomicMarkableReferences to store references to parent and children pointers
 * and ensure atomicity when editing the tree.
 * 
 * @author Rob Lyerly <rlyerly@vt.edu>
 *
 * @param <T> The generic data type stored by this binary tree
 */
public class LockFreeBinaryTree<T extends Comparable<? super T>>
		implements ConcurrentBinaryTree<T> {

	/**
	 * Local variables and definitions.
	 */
	AtomicReference<LockFreeNode<T>> root;
	
	/**
	 * Instantiates an empty lock-free binary tree for use.
	 */
	public LockFreeBinaryTree() {
		root = new AtomicReference<LockFreeNode<T>>(null);
	}
	
	/**
	 * Inserts new data into the tree.
	 * 
	 * @param data The data to be inserted into the tree
	 * @return True if the data was successfully inserted, false otherwise
	 */
	@Override
	public boolean insert(T data) {
		
		LockFreeNode<T> newNode = new LockFreeNode<T>(data);
		LockFreeNode<T> newParent = null;
		LockFreeNode<T> curNode = null;
		LockFreeNode<T> parentNode = null;
		LockFreeNode<T> gparentNode = null;
		int compare = 0, oldCompare = 0;
		boolean[] marked = {false};
		
		retry: while(true) {
			if(root.get() == null) {
				//Tree is empty, try to insert newNode as the root
				if(root.compareAndSet(null, newNode))
					return true;
				else
					continue retry;
			} else {
				//Tree is not empty, iterate into the tree
				curNode = root.get();
				while(curNode != null) {
					gparentNode = parentNode;
					parentNode = curNode;
					oldCompare = compare;
					compare = curNode.data.compareTo(data);
					if(compare > 0) {
						//curNode is "bigger" than the passed data, iterate
						//into the left subtree
						curNode = curNode.getChild(Child.LEFT, marked);
					} else if(compare < 0) {
						//curNode is "smaller" than the passed data, iterate
						//into the right subtree
						curNode = curNode.getChild(Child.RIGHT, marked);
					} else {
						//If this is a leaf node, then the data is already in
						//the tree.  Otherwise, we can keep traversing
						if(curNode.isLeaf())
							return false; //TODO if its marked, can this thread
										  //try and remove it then restart the
										  //insertion?
						else
							curNode = curNode.getChild(Child.RIGHT, marked);
					}
				}
				
				//TODO handle case where root is being deleted (parentNode == null) and case where
				//2nd level node is being deleted (gparentNode == null)
				
				if(parentNode.isMarked()) {
					//If parent is marked, try to perform the deletion
				}

				//Create a subtree of parentNode & curNode, then attempt to
				//insert it
				if(compare > 0) {
					newParent = new LockFreeNode<T>(parentNode.data);
					newParent.insertChild(Child.LEFT, newNode);
					newParent.insertChild(Child.RIGHT, curNode);
				} else {
					newParent = new LockFreeNode<T>(data);
					newParent.insertChild(Child.LEFT, curNode);
					newParent.insertChild(Child.RIGHT, newNode);
				}	

				if(oldCompare > 0) {
					if(gparentNode.insertChild(Child.LEFT, newParent))							
						return true;
					else
						continue retry;
				} else {
					if(gparentNode.insertChild(Child.RIGHT, newParent))
						return true;
					else
						continue retry;
				}
			}
		}
	}

	/**
	 * Removes the specified data from the tree.
	 * 
	 * @param data The data to remove from the tree
	 * @return The data element that was removed, or null otherwise
	 */
	@Override
	public T remove(T data) {
		
		LockFreeNode<T> newParent = null;
		LockFreeNode<T> curNode = null;
		LockFreeNode<T> parentNode = null;
		LockFreeNode<T> gparentNode = null;
		int compare = 0, oldCompare = 0;
		boolean[] marked = {false};
		
		retry: while(true) {
			//Check to see if the tree is empty
			if(root.get() == null)
				return null;
			else {
				//The tree isn't empty, iterate into the tree
				curNode = root.get();
				while(curNode != null) {
					gparentNode = parentNode;
					parentNode = curNode;
					oldCompare = compare;
					compare = curNode.data.compareTo(data);
					if(compare > 0) {
						//curNode is "bigger" than the passed data, iterate
						//into the left subtree
						curNode = curNode.getChild(Child.LEFT, marked);
					} else if(compare < 0) {
						//curNode is "smaller" than the passed data, iterate
						//into the right subtree
						curNode = curNode.getChild(Child.RIGHT, marked);
					} else {
						//If this is a leaf node, then the data is already in
						//the tree.  Otherwise, we can keep traversing
						if(curNode.isLeaf()) {
							//Attempt to mark the parent node
							if(!curNode.mark())
								continue retry;
							//Logically marked the node, now attempt to
							//physically remove it.  The linearization point
							//was the logical removal, so even if we can't
							//physically remove it we were successful.
							if(compare > 0)
								newParent = parentNode.getChild(Child.RIGHT,
										marked);
							else
								newParent = parentNode.getChild(Child.LEFT,
										marked);
							if(oldCompare > 0) {
								gparentNode.insertChild(Child.LEFT, newParent);
								return curNode.data;
								
							} else {
								gparentNode.insertChild(Child.RIGHT,
										newParent);
								return curNode.data;
							}
						} else
							curNode = curNode.getChild(Child.RIGHT, marked);
					}
				}
				//The data wasn't in the tree
				return null;
			}
		}
	}

	/**
	 * Checks to see if the specified data is in the tree.
	 * 
	 * @param data The data being searched for in the tree
	 * @return True if the data is in the tree, false otherwise
	 */
	@Override
	public boolean contains(T data) {
		
		LockFreeNode<T> curNode = root.get();
		int compare = 0;
		boolean[] marked = {false};
		
		//Tree is not empty, search the tree
		while(curNode != null) {
			compare = curNode.data.compareTo(data);
			if(compare > 0) {
				//curNode is "bigger" than the passed data, search the left
				//subtree
				curNode = curNode.getChild(Child.LEFT, marked);
			} else if(compare < 0) {
				//curNode is "smaller" than the passed data, search the
				//right subtree
				curNode = curNode.getChild(Child.RIGHT, marked);
			} else {
				//Found the data, make sure that it isn't an internal node and
				//that is isn't marked.
				if(curNode.isLeaf())
					return !curNode.isMarked();
				else
					curNode = curNode.getChild(Child.RIGHT, marked);
			}
		}			
		//Tree is empty or data is not in the tree
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
