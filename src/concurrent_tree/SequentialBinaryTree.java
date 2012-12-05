package concurrent_tree;

import java.util.LinkedList;
import java.util.Random;

/**
 * Sequential Binary Tree
 * 
 * This class implements a sequential version of a binary tree.  It is intended
 * for single-threaded use only and serves as a baseline against the concurrent
 * binary trees implemented as part of the final project for ECE 5510. 
 * 
 * @author Rob Lyerly <rlyerly>
 *
 * @param <T> Generic data type that the tree stores.  The data type must
 * implement the Comparable interface so that an ordering can be determined.
 */
public class SequentialBinaryTree<T extends Comparable<? super T>>
		implements ConcurrentBinaryTree<T> {

	/**
	 * Local variables and definitions.
	 */
	Node<T> root;
	
	/**
	 * Instantiates an empty sequential binary tree for use.
	 */
	public SequentialBinaryTree() {
		root = null;
	}
	
	/**
	 * Inserts new data into the tree.
	 * 
	 * @param data The data to be inserted into the tree
	 * @return True if the data was successfully inserted, false otherwise
	 */
	@Override
	public boolean insert(T data) {
	
		Node<T> newNode = new Node<T>(data);
		Node<T> curNode = null;
		Node<T> parentNode = null;
		int compare = 0;
		
		if(root == null) {
			//The tree is empty, insert the new node as the root
			root = newNode;
		} else {
			//The tree is not empty, find a location to insert the new node
			curNode = root;
			while(curNode != null) {
				compare = curNode.data.compareTo(data);
				parentNode = curNode;
				if(compare > 0) {
					//curNode is "bigger" than newNode, enter left subtree
					curNode = curNode.left;
				} else if(compare < 0) {
					//curNode is "smaller" than newNode, enter right subtree
					curNode = curNode.right;
				} else {
					//The data is already in the tree
					return false;
				}
			}
			
			//Insert the node into the tree
			if(compare > 0)
				parentNode.left = newNode;
			else
				parentNode.right = newNode;
		}
		return true;
	}

	/**
	 * Removes the specified data from the tree.
	 * 
	 * @param data The data object to remove from the tree
	 * @return The removed data element if it is in the tree, false otherwise
	 */
	@Override
	public T remove(T data) {
		
		Node<T> curNode = null;
		Node<T> parentNode = null;
		int compare = 0;
		int oldCompare = 0;
		
		if(root != null) {
			//The tree is not empty, search the tree for the passed data
			curNode = root;
			while(curNode != null) {
				compare = curNode.data.compareTo(data);
				if(compare > 0) {
					//curNode is "bigger" than the passed data, search the
					//left subtree
					parentNode = curNode;
					curNode = curNode.left;
					oldCompare = compare;
				} else if(compare < 0) {
					//curNode is "smaller" than the passed data, search the
					//right subtree
					parentNode = curNode;
					curNode = curNode.right;
					oldCompare = compare;
				} else {
					//Found the data - find a replacement node
					Node<T> replacement = findReplacement(curNode);
					
					//Set parentNode's child pointer to the replacement, or if
					//we are removing the root set the root pointer
					if(parentNode != null) {
						if(oldCompare > 0)
							parentNode.left = replacement;
						else
							parentNode.right = replacement;
					} else {
						root = replacement;
					}
					
					//Replace curNode with the replacement node
					if(replacement != null) {
						replacement.left = curNode.left;
						replacement.right = curNode.right;
					}
					return curNode.data;
				}
			}
		}
		
		//The tree was empty or the passed data was not in the tree
		return null;
	}
	
	/**
	 * Finds a replacement node to put in place of the node being deleted.
	 * Automatically deletes the replacement node from the tree so that it can
	 * be inserted in place of the removed node.
	 * 
	 * @param subRoot The node being deleted
	 * @return A replacement node or null if no replacement exists
	 */
	private Node<T> findReplacement(Node<T> subRoot) {
		
		Node<T> curNode = null;
		Node<T> parentNode = null;
		
		if(subRoot.left != null) {
			//Find the "biggest" node in the left subtree as the replacement
			parentNode = subRoot;
			curNode = subRoot.left;
			while(curNode.right != null) {
				parentNode = curNode;
				curNode = curNode.right;
			}
			if(parentNode == subRoot)
				parentNode.left = curNode.left;
			else
				parentNode.right = curNode.left;
		} else if(subRoot.right != null) {
			//Find the "smallest" node in the right subtree as the replacement
			parentNode = subRoot;
			curNode = subRoot.right;
			while(curNode.left != null) {
				parentNode = curNode;
				curNode = curNode.left;
			}
			if(parentNode == subRoot)
				parentNode.right = curNode.right;
			else
				parentNode.left = curNode.right;
		} else {
			//No children, no replacement needed
			return null;
		}
		return curNode;
	}

	/**
	 * Searches the tree for the specified data.
	 * 
	 * @param data The data object to search for in the tree
	 * @return True if the data is in the tree, false otherwise
	 */
	@Override
	public boolean contains(T data) {
		
		Node<T> curNode = null;
		int compare = 0;
		
		if(root != null) {
			//The tree is not empty, search the tree for the passed data
			curNode = root;
			while(curNode != null) {
				compare = curNode.data.compareTo(data);
				if(compare > 0) {
					//curNode is "bigger" than the passed data, search the
					//left subtree
					curNode = curNode.left;
				} else if(compare < 0) {
					//curNode is "smaller" than the passed data, search the
					//right subtree
					curNode = curNode.right;
				} else {
					//We found the data
					return true;
				}
			}
		}
		
		//The tree was empty or the passed data was not in the tree
		return false;
	}
	
	/**
	 * Performs a depth-first search of the tree, printing out the data of each
	 * node.
	 */
	public void printTree() {
		printTree(root);
	}
	
	/**
	 * Private method to perform a depth-first search of the tree and print
	 * every node's data.
	 * 
	 * @param curNode The current node being printed.  
	 */
	private void printTree(Node<T> curNode) {
		
		//Check to make sure curNode isn't null
		if(curNode == null)
			return;
		
		//Print the left subtree
		printTree(curNode.left);
		
		//Print the current node
		System.out.println(curNode.data.toString());
		
		//Print the right subtree
		printTree(curNode.right);
	}

	/**
	 * Driver program to test the sequential binary tree.
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		//Test the tree
		SequentialBinaryTree<Integer> tree =
				new SequentialBinaryTree<Integer>();
		LinkedList<Integer> randomNums = new LinkedList<Integer>();
		Random rand = new Random();
		int random = 0;
		int i = 0;
		
		for(i = 0; i < 10; i++) {
			random = rand.nextInt(500);
			randomNums.addLast(random);
			tree.insert(random);
			System.out.println("Number: " + random);
		}
		
		System.out.println("----------\nTree contains:");
		tree.printTree();
		System.out.println("----------");
		
		for(i = 0; i < 10; i++) {
			random = randomNums.removeFirst();
			System.out.println("Number [" + i + "]: " + random +
					" -> removed? " + tree.remove(random));
		}
	}
}