package concurrent_tree;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * LockFreeNode Class
 * 
 * Implementation of a node (data wrapper) to be used in conjunction with the
 * LockFreeBinaryTree class.  This implementation uses AtomicMarkableReferences
 * to store pointers (and a logical removal flag) to the parent and children
 * nodes.
 * 
 * @author Rob Lyerly <rlyerly@vt.edu>
 *
 * @param <T> The generic data type being wrapped by the LockFreeNode class
 */
public class LockFreeNode<T> {
	
	
	/**
	 * Private class that allows coupling both child pointers to a single
	 * reference.  This is useful when testing for logical removal because
	 * we can represent both child pointers with a single
	 * AtomicMarkableReference, ensuring atomicity when checking/setting a node
	 * as logically deleted.
	 *  
	 * @author Rob Lyerly <rlyerly@vt.edu>
	 *
	 * @param <T> The generic data type being wrapped by the LockFreeNode class
	 */
	public class ChildNodes {
		
		LockFreeNode<T> left;
		LockFreeNode<T> right;
		
		/**
		 * Creates a ChildNodes object with no child pointers.
		 */
		public ChildNodes() {
			left = null;
			right = null;
		}
		
		/**
		 * Creates a ChildNodes object with the passed child nodes.
		 * @param left The new left child of the current node.
		 * @param right The new right child of the current node.
		 */
		public ChildNodes(LockFreeNode<T> left, LockFreeNode<T> right) {
			this.left = left;
			this.right = right;
		}
	}
	
	public T data;
	public AtomicMarkableReference<ChildNodes> children;
	
	/**
	 * Instantiates a LockFreeNode object.  Stores a reference to the data
	 * and instantiates the AtomicMarkableReferences for the parent and
	 * children nodes.
	 * @param data The data object stored in the LockFreeNode
	 */
	public LockFreeNode(T data) {
		this.data = data;
		children = new AtomicMarkableReference<ChildNodes>(
				new ChildNodes(), false);
	}
	
	/**
	 * Overloaded constructor.  Instantiates a LockFreeNode object with the
	 * specified data and child references.
	 * 
	 * @param data The data object stored in the LockFreeNode
	 * @param leftChild The left child of this new node
	 * @param rightChild The right child of this new node
	 */
	public LockFreeNode(T data, LockFreeNode<T> leftChild,
			LockFreeNode<T> rightChild) {
		this.data = data;
		children = new AtomicMarkableReference<ChildNodes>(
				new ChildNodes(leftChild, rightChild), false);
	}
	
	/**
	 * Attempt to set the child pointer of the LockFreeNode.
	 * @param cp Which child pointer to attempt to set (LEFT or RIGHT)
	 * @param child The new child pointer
	 * @return True if the child pointer was set, false otherwise
	 */
	public boolean insertChild(Child cp, LockFreeNode<T> oldChild,
			LockFreeNode<T> newChild) {
		
		//Create a new child node object to try and replace the current one
		ChildNodes curCN = children.getReference();
		ChildNodes newCN;
		switch(cp) {
		case RIGHT:
			if(curCN.right != oldChild)
				return false;
			newCN = new ChildNodes(curCN.left, newChild);
			break;
		case LEFT:
			if(curCN.left != oldChild)
				return false;
			newCN = new ChildNodes(newChild, curCN.right);
			break;
		default:
			return false;
		}
		
		//Attempt to replace the old childNodes object with the new one
		return children.compareAndSet(curCN, newCN, false, false);
	}
	
	/**
	 * Getter shorthand method to grab a child pointer.
	 * @param cp Which child pointer to access
	 * @return A pointer to the child node, or null if no child exists for that
	 * subtree
	 */
	public LockFreeNode<T> getChild(Child cp, boolean[] marked) {
		switch(cp) {
		case LEFT:
			return this.children.get(marked).left;
		case RIGHT:
			return this.children.get(marked).right;
		default:
			return null;
		}
	}
	
	/**
	 * Attempts to mark the node as logically deleted.
	 * @return True if the node was marked, false otherwise.
	 */
	public boolean mark() {
		return children.attemptMark(children.getReference(), true);
	}
	
	/**
	 * Getter method that returns whether or not the current node is marked.
	 * @return True if the node is marked for deletion, false otherwise
	 */
	public boolean isMarked() {
		return children.isMarked();
	}
	
	/**
	 * Returns whether or not the current node is a leaf node by checking the
	 * child references.
	 * @return True if the node is a leaf node, false otherwise
	 */
	public boolean isLeaf() {
		//TODO less checking? Shouldn't they both be the same, and so we should
		//only need to check one of them?
		return (children.getReference().left == null &&
				children.getReference().right == null);
	}
}
