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
	public class childNodes {
		LockFreeNode<T> left;
		LockFreeNode<T> right;
		
		/**
		 * Creates a childNodes object with no child pointers.
		 */
		public childNodes() {
			left = null;
			right = null;
		}
	}
	
	/**
	 * Object variables.
	 */
	public T data;
	public LockFreeNode<T> parent;
	private AtomicMarkableReference<childNodes> children;
	
	/**
	 * Instantiates a LockFreeNode object.  Stores a reference to the data
	 * and instantiates the AtomicMarkableReferences for the parent and
	 * children nodes.
	 * @param data The data object stored by the LockFreeNode
	 */
	public LockFreeNode(T data) {
		this.data = data;
		parent = new LockFreeNode<T>(null);
		children = new AtomicMarkableReference<childNodes>(
				new childNodes(), false);
	}
	
	/**
	 * Attempt to set the child pointer of the LockFreeNode.
	 * @param cp Which child pointer to attempt to set (LEFT or RIGHT)
	 * @param child The new child pointer
	 * @return True if the child pointer was set, false otherwise
	 */
	public boolean insertChild(ChildPointer cp, LockFreeNode<T> child) {
		
		//Create a new child node object to try and replace the current one
		childNodes curCN = children.getReference();
		childNodes newCN = new childNodes();
		switch(cp) {
		case RIGHT:
			newCN.left = curCN.left;
			newCN.right = child;
			break;
		case LEFT:
			newCN.left = child;
			newCN.right = curCN.right;
			break;
		default:
			return false;
		}
		
		//Attempt to replace the old childNodes object with the new one
		if(children.compareAndSet(curCN, newCN, false, false))
			return true;
		else		
			return false;
	}
	
	/**
	 * Getter shorthand method to grab a child pointer.
	 * @param cp Which child pointer to access
	 * @return A pointer to the child node, or null if no child exists for that
	 * subtree
	 */
	public LockFreeNode<T> getChild(ChildPointer cp, boolean[] marked) {
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
	 * Getter method that returns whether or not the current node is marked.
	 * @return True if the node is marked for deletion, false otherwise
	 */
	public boolean isMarked() {
		return children.isMarked();
	}
}
