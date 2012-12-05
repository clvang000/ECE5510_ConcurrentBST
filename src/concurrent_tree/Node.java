package concurrent_tree;

/**
 * Node Class
 * 
 * Implements the basic node needed to wrap data objects for manipulation in
 * the sequential binary tree.
 * 
 * @author Rob Lyerly
 * @param <T> The generic data type being wrapped by the Node class
 */
public class Node<T> {

	public T data;
	public Node<T> left;
	public Node<T> right;
	
	/**
	 * Instantiate a new node object with the passed data object.
	 * @param data The data object wrapped by the new Node
	 */
	public Node(T data) {
		this.data = data;
		left = null;
		right = null;
	}
}
