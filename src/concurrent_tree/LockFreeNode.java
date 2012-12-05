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
	public class childNodes<T> {
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
	 * Specifies which child node is being accessed.
	 * 
	 * @author Rob Lyerly <rlyerly@vt.edu>
	 */
	public enum childPointer {
		LEFT,
		RIGHT
	}
	
	/**
	 * Object variables.
	 */
	public T data;
	public LockFreeNode<T> parent;
	public AtomicMarkableReference<childNodes<T>> children;
	
	/**
	 * Instantiates a LockFreeNode object.  Stores a reference to the data
	 * and instantiates the AtomicMarkableReferences for the parent and
	 * children nodes.
	 * @param data The data object stored by the LockFreeNode
	 */
	public LockFreeNode(T data) {
		this.data = data;
		parent = new LockFreeNode<T>(null);
		children = new AtomicMarkableReference<childNodes<T>>(
				new childNodes<T>(), false);
	}
	
	/**
	 * Attempt to set the child pointer of the LockFreeNode.
	 * @param cp Which child pointer to attempt to set (LEFT or RIGHT)
	 * @param child The new child pointer
	 * @return True if the child pointer was set, false otherwise
	 */
	public boolean insertChild(childPointer cp, LockFreeNode<T> child) {
		
		//Create a new child node object to try and replace the current one
		childNodes<T> curCN = children.getReference();
		childNodes<T> newCN = new childNodes<T>();
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
}
