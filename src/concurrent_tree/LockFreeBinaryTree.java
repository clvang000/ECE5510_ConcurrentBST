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
		LockFreeNode<T> ggparentNode = null;
		int compare = 0, oldCompare = 0, reallyOldCompare = 0;
		boolean[] marked = {false};
		
		retry: while(true) {
			curNode = root.get();
			if(curNode == null) {
				//Tree is empty, try to insert newNode as the root
				if(root.compareAndSet(null, newNode))
					return true;
				else
					continue retry;
			} else {
				//Tree is not empty, iterate into the tree
				while(curNode != null) {
					ggparentNode = gparentNode;
					gparentNode = parentNode;
					parentNode = curNode;
					reallyOldCompare = oldCompare;
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
				
				//Check edge cases
				if(gparentNode == null) {
					//Edge case 1: inserting at 1st level (tree only has 1 element)
					if(parentNode.isMarked()) {
						root.compareAndSet(parentNode, null);
						continue retry;
					}
					newParent = createSubtree(parentNode, newNode, compare);
					if(root.compareAndSet(parentNode, newParent))
						return true;
					else
						continue retry;
				} else if(ggparentNode == null) {
					//Edge case 2: Inserting at 2nd level (tree has 2 elements)
					if(parentNode.isMarked()) {
						if(oldCompare > 0)
							parentNode = gparentNode.getChild(Child.RIGHT,
									marked);
						else
							parentNode = gparentNode.getChild(Child.LEFT,
									marked);
						if(!root.compareAndSet(gparentNode, parentNode))
							continue retry;
						newParent = createSubtree(parentNode, newNode,
								oldCompare);
						if(root.compareAndSet(parentNode, newParent))
							return true;
						else
							continue retry;
					}
				} else if(parentNode.isMarked()) {
					//Edge case 3: Attempt to delete parentNode if it is marked
					if(oldCompare > 0)
						newParent = gparentNode.getChild(Child.RIGHT, marked);
					else
						newParent = gparentNode.getChild(Child.LEFT, marked);
					if(reallyOldCompare > 0) {
						if(!ggparentNode.insertChild(Child.LEFT, gparentNode,
								newParent))
							continue retry;
					} else {
						if(!ggparentNode.insertChild(Child.RIGHT, gparentNode,
								newParent))
							continue retry;
					}
					parentNode = newParent;
					gparentNode = ggparentNode;
					compare = oldCompare;
					oldCompare = reallyOldCompare;
				}	

				//Attempt insertion
				newParent = createSubtree(parentNode, newNode, compare);
				if(oldCompare > 0) {
					if(gparentNode.insertChild(Child.LEFT, parentNode,
							newParent))							
						return true;
					else
						continue retry;
				} else {
					if(gparentNode.insertChild(Child.RIGHT, parentNode,
							newParent))
						return true;
					else
						continue retry;
				}
			}
		}
	}
	
	/**
	 * Helper method that creates a subtree of parentNode and curNode based on
	 * their ordering.
	 * 
	 * @param parentNode The parent node which will be replaced by this subtree
	 * @param newNode The new data being inserted into the tree
	 * @param compare The ordering of the two nodes (set via a call to
	 * compareTo (this is parentNode's data compared to newNode)
	 * @return The new subtree of elements
	 */
	private LockFreeNode<T> createSubtree(LockFreeNode<T> parentNode,
			LockFreeNode<T> newNode, int compare) {
		LockFreeNode<T> newParent;
		if(compare > 0) {
			newParent = new LockFreeNode<T>(parentNode.data, newNode,
					parentNode);
		} else {
			newParent = new LockFreeNode<T>(newNode.data, parentNode,
					newNode);
		}
		return newParent;
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
			curNode = root.get();
			if(curNode == null)
				return null;
			else {
				//The tree isn't empty, iterate into the tree
				parentNode = curNode;
				while(curNode != null) {
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
						//If this is a leaf node, then the data is in the tree
						//and can be removed.  Otherwise, we keep traversing
						if(curNode.isLeaf()) {
							//Attempt to mark the current node.  Note that this
							//is the linearization point.  Even if we can't
							//physically remove the node, if this call succeeds
							//then we were successful
							if(!curNode.mark())
								continue retry;
							
							if(parentNode == null) {
								//Edge case 1: deletion of root
								root.compareAndSet(curNode, null);
							} else if(gparentNode == null) {
								//Edge case 2: deletion at 1st level
								if(compare > 0)
									newParent = parentNode.getChild(
											Child.RIGHT, marked);
								else
									newParent = parentNode.getChild(
											Child.LEFT, marked);
								root.compareAndSet(parentNode, newParent);
									
							} else {
								//Normal deletion.  Attempt to physically
								//remove the node
								if(compare > 0)
									newParent =
										parentNode.getChild(Child.RIGHT,
												marked);
								else
									newParent =
										parentNode.getChild(Child.LEFT,
												marked);
								if(oldCompare > 0) {
									gparentNode.insertChild(Child.LEFT,
											parentNode, newParent);
									
								} else {
									gparentNode.insertChild(Child.RIGHT,
											parentNode, newParent);
								}								
							}
							return curNode.data;
						} else
							curNode = curNode.getChild(Child.RIGHT, marked);
					}
					gparentNode = parentNode;
					parentNode = curNode;
					oldCompare = compare;
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
