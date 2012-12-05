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
		LockFreeNode<T> curNode = null;
		LockFreeNode<T> parentNode = null;
		int compare = 0;
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
				if(curNode.isMarked()) {
					//Handle the special situation where the root is logically
					//deleted
					LockFreeNode<T> replacement = findReplacement(curNode);
					if(replacement != null) {
						//Set the replacement's pointers
						replacement.parent = null;
						replacement.insertChild(ChildPointer.LEFT,
								curNode.getChild(ChildPointer.LEFT,
								marked));
						replacement.insertChild(ChildPointer.RIGHT,
								curNode.getChild(ChildPointer.RIGHT,
								marked));
					}
					
					//Set the root to point to the replacement.  If it
					//fails we need to re-insert the replacement node into the
					//tree.  No matter what, continue from retry
					if(!root.compareAndSet(curNode, replacement) &&
							replacement != null)
						this.insert(replacement.data);
					continue retry;
				}
				
				while(true) {
					parentNode = curNode;
					compare = curNode.data.compareTo(data);
					if(compare > 0) {
						//curNode is "bigger" than the passed data, iterate
						//into the left subtree
						curNode = curNode.getChild(ChildPointer.LEFT, marked);
					} else if(compare < 0) {
						//curNode is "smaller" than the passed data, iterate
						//into the right subtree
						curNode = curNode.getChild(ChildPointer.RIGHT, marked);
					} else {
						//Data is already in the tree, make sure it isn't
						//isn't marked for deletion
						if(!curNode.isMarked())
							return false;
						else {
							//TODO deletion
							continue retry;
						}
					}
					if(curNode == null)
						break;
					else if(marked[0]) {
						//curNode is marked for deletion, remove it from the
						//tree
						//TODO deletion
					}
				}
				//Found an appropriate location, attempt to insert
				newNode.parent = parentNode;
				if(compare > 0) {
					if(parentNode.insertChild(ChildPointer.LEFT, newNode))
						return true;
					else
						continue retry;
				} else {
					if(parentNode.insertChild(ChildPointer.RIGHT, newNode))
						return true;
					else
						continue retry;
				}
			}
		}
	}

	@Override
	public T remove(T data) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Checks to see if the specified data is in the lock-free binary tree.
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
				curNode = curNode.getChild(ChildPointer.LEFT, marked);
			} else if(compare < 0) {
				//curNode is "smaller" than the passed data, search the
				//right subtree
				curNode = curNode.getChild(ChildPointer.RIGHT, marked);
			} else {
				//Found the data, make sure it isn't marked
				return !curNode.isMarked();
			}
		}			
		//Tree is empty or data is not in the tree
		return false;
	}
	
	/**
	 * Physically removes a node from the tree.
	 * @param curNode The node to be physically deleted (it is already
	 * logically deleted)
	 * @return The node replacing the current node, or null if no replacement
	 * is needed 
	 */
	private LockFreeNode<T> physicalRemove(LockFreeNode<T> curNode) {
		//TODO implement
		return null;
	}
	
	/**
	 * Finds a replacement node for the specified sub-root.
	 * @param subRoot The sub-root for which a replacement is to be found
	 * @return A replacement node for the sub-root, or null if none exists
	 */
	private LockFreeNode<T> findReplacement(LockFreeNode<T> subRoot) {
		//TODO implement
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
