package concurrent_tree;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Fine-Grained Locking Binary Tree
 * 
 * This class implements a concurrent version of a binary tree using a
 * fine-grained locking approach for correctness and synchronization. 
 * 
 * @author Rob Lyerly <rlyerly>
 *
 * @param <T> Generic data type that the tree stores.  The data type must
 * implement the Comparable interface so that an ordering can be determined.
 */
public class FineGrainedLockingBinaryTree<T extends Comparable<? super T>>
		implements ConcurrentBinaryTree<T> {
	
	/**
	 * Local variables and definitions.
	 */
	LockableNode<T> root;
	ReentrantLock headLock;
	
	/**
	 * Instantiates an empty fine-grained locking binary tree for use.
	 */
	public FineGrainedLockingBinaryTree() {
		root = null;
		headLock = new ReentrantLock();
	}
	
	/**
	 * Inserts new data into the tree.  Traverses the tree using a
	 * hand-over-hand locking approach to make sure that the insertion doesn't
	 * interfere with other operations being performed on the tree.
	 * 
	 * @param data The data to be inserted into the tree
	 * @return True if the data was successfully inserted, false otherwise
	 */
	@Override
	public boolean insert(T data) {
	
		LockableNode<T> newNode = new LockableNode<T>(data);
		LockableNode<T> curNode = null;
		LockableNode<T> parentNode = null;
		int compare = 0;
		
		headLock.lock();
		if(root == null) {
			//The tree is empty, insert the new node as the root
			root = newNode;
			headLock.unlock();
		} else {
			//The tree is not empty, find a location to insert the new node
			curNode = root;
			curNode.lock();
			headLock.unlock();
			while(true) {
				parentNode = curNode;
				compare = curNode.data.compareTo(data);
				if(compare > 0) {
					//curNode is "bigger" than newNode, enter left subtree
					curNode = curNode.left;
				} else if(compare < 0) {
					//curNode is "smaller" than newNode, enter right subtree
					curNode = curNode.right;
				} else {
					//The data is already in the tree
					curNode.unlock();
					return false;
				}
				
				//Check to see if we've found our location.  If not, continue
				//traversing the tree; else, break out of the loop
				if(curNode == null) {
					break;
				} else {
					curNode.lock();
					parentNode.unlock();
				}
			}
			
			//Insert the node into the tree
			if(compare > 0)
				parentNode.left = newNode;
			else
				parentNode.right = newNode;
			parentNode.unlock();
		}
		return true;
	}

	/**
	 * Removes the specified data from the tree.  Traverses the tree using a
	 * hand-over-hand locking approach to make sure that the deletion doesn't
	 * interfere with other operations being performed on the tree.
	 * 
	 * @param data The data object to remove from the tree
	 * @return The removed data element if it is in the tree, false otherwise
	 */
	@Override
	public T remove(T data) {
		
		LockableNode<T> curNode = null;
		LockableNode<T> parentNode = null;
		int compare = 0;
		int oldCompare = 0;
		
		headLock.lock();
		if(root != null) {
			//Tree is not empty, search for the passed data.  Start by checking
			//the root separately.
			curNode = root;
			parentNode = curNode;
			curNode.lock();
			compare = curNode.data.compareTo(data);
			if(compare > 0) {
				//root is "bigger" than passed data, search the left subtree
				curNode = curNode.left;
				oldCompare = compare;
			} else if(compare < 0) {
				//root is "smaller" than passed data, search the right subtree
				curNode = curNode.right;
				oldCompare = compare;
			} else {
				//Found the specified data, remove it from the tree
				LockableNode<T> replacement = findReplacement(curNode);
				
				root = replacement;
				
				if(replacement != null) {
					replacement.left = curNode.left;
					replacement.right = curNode.right;
				}
				
				curNode.unlock();
				headLock.unlock();
				return curNode.data;
			}
			curNode.lock();
			headLock.unlock();
			
			while(true) {
				compare = curNode.data.compareTo(data);
				if(compare != 0) {
					parentNode.unlock();
					parentNode = curNode;
					if(compare > 0) {
						//curNode is "bigger" than passed data, search the left
						//subtree
						curNode = curNode.left;
						oldCompare = compare;
					} else if(compare < 0) {
						//curNode is "smaller" than passed data, search the right
						//subtree
						curNode = curNode.right;
						oldCompare = compare;
					}
				} else {
					//Found the specified data, remove it from the tree
					LockableNode<T> replacement = findReplacement(curNode);
					
					//Set the parent pointer to the new child
					if(oldCompare > 0)
						parentNode.left = replacement;
					else
						parentNode.right = replacement;
					
					//Replace curNode with replacement
					if(replacement != null) {
						replacement.left = curNode.left;
						replacement.right = curNode.right;
					}
					
					curNode.unlock();
					parentNode.unlock();
					return curNode.data;
				}
				
				if(curNode == null) {
					break;
				} else {
					curNode.lock();
				}
			}
		} else {
			//Tree is empty
			headLock.unlock();
			return null;
		}
		
		//The specified data was not in the tree
		parentNode.unlock();
		return null;
	}
	
	/**
	 * Finds a replacement node to put in place of the node being deleted.
	 * Automatically deletes the replacement node from the tree so that it can
	 * be inserted in place of the removed node.  Performs the same
	 * hand-over-hand locking approach used in other methods to ensure correct
	 * concurrent operation.
	 * 
	 * @param subRoot The node being deleted
	 * @return A replacement node or null if no replacement exists
	 */
	private LockableNode<T> findReplacement(LockableNode<T> subRoot) {
		
		LockableNode<T> curNode = null;
		LockableNode<T> parentNode = null;
		
		if(subRoot.left != null) {
			//Find the "biggest" node in the left subtree as the replacement
			parentNode = subRoot;
			curNode = subRoot.left;
			curNode.lock();
			while(curNode.right != null) {
				if(parentNode != subRoot)
					parentNode.unlock();
				parentNode = curNode;
				curNode = curNode.right;
				curNode.lock();
			}
			if(curNode.left != null)
				curNode.left.lock();
			if(parentNode == subRoot)
				parentNode.left = curNode.left;
			else {
				parentNode.right = curNode.left;
				parentNode.unlock();
			}
			if(curNode.left != null)
				curNode.left.unlock();
			curNode.unlock();
		} else if(subRoot.right != null) {
			//Find the "smallest" node in the right subtree as the replacement
			parentNode = subRoot;
			curNode = subRoot.right;
			curNode.lock();
			while(curNode.left != null) {
				if(parentNode != subRoot)
					parentNode.unlock();
				parentNode = curNode;
				curNode = curNode.left;
				curNode.lock();
			}
			if(curNode.right != null)
				curNode.right.lock();
			if(parentNode == subRoot)
				parentNode.right = curNode.right;
			else {
				parentNode.left = curNode.right;
				parentNode.unlock();
			}
			if(curNode.right != null)
				curNode.right.unlock();
			curNode.unlock();
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
		
		LockableNode<T> curNode = null;
		LockableNode<T> parentNode = null;
		int compare = 0;
		
		headLock.lock();
		if(root != null) {
			//The tree is not empty, search the tree for the passed data
			curNode = root;
			curNode.lock();
			headLock.unlock();
			while(curNode != null) {
				compare = curNode.data.compareTo(data);
				parentNode = curNode;
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
					curNode.unlock();
					return true;
				}
				
				if(curNode == null) {
					break;
				} else {
					curNode.lock();
					parentNode.unlock();
				}
			}
		} else {
			//The tree is empty
			headLock.unlock();
			return false;
		}
		
		//The passed data is not in the tree
		parentNode.unlock();
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
	private void printTree(LockableNode<T> curNode) {
		
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
		FineGrainedLockingBinaryTree<Integer> tree =
				new FineGrainedLockingBinaryTree<Integer>();
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