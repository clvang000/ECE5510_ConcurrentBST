package test_bench;

import concurrent_tree.*;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;

public class TestBench {
	
	public static final int maxThreads = 8;
	private static final int numOps = 1000000;

	private static class TreeTestRunnable implements Runnable {

		ConcurrentBinaryTree<Integer> tree = null;
		LinkedList<Integer> list = null;
		Random rand = null;
		
		/**
		 * Creates a new runnable object to test the trees.
		 * @param tree The particular tree implementation to test
		 */
		public TreeTestRunnable(ConcurrentBinaryTree<Integer> tree) {
			this.tree = tree;
			list = new LinkedList<Integer>();
			rand = new Random();
		}
		
		/**
		 * Runs the concurrent binary tree throughput test.
		 */
		@Override
		public void run() {
			for(int i = 0; i < numOps; i++) {
				int random = rand.nextInt();
				if((random % 2) == 0) {
					if(tree.insert(random)) {
						list.addLast(random);
					}
				} else {
					try{
						Integer data = list.removeFirst();
						data = tree.remove(data);
						if(data == null) {
							//Couldn't remove the data from the tree
						}
					} catch (NoSuchElementException nsee) {
						//Empty list, no problem
					}
				}
			}
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConcurrentBinaryTree<Integer> tree;
		Thread thread[];
		int i = 0;
		int j = 0;
		long before = 0;
		long after = 0;
		
		//Test the sequential tree
		System.out.println("Testing the sequential binary tree");
		tree = new SequentialBinaryTree<Integer>();
		thread = new Thread[1];
		thread[0] = new Thread(new TreeTestRunnable(tree));
		before = System.nanoTime();
		thread[0].start();
		try {
			thread[0].join();
			after = System.nanoTime();
		} catch (InterruptedException e) {
			//Interrupted, stop testing
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Finished testing the sequential binary tree - " 
				+ (after - before) + " ns");
		
		//Test the fine-grained locking tree
		System.out.println("Testing the fine-grained locking binary tree");
		tree = new FineGrainedLockingBinaryTree<Integer>();
		for(i = 1; i <= maxThreads; i++) {
			thread = new Thread[i];
			for(j = 0; j < i; j++)
				thread[j] = new Thread(new TreeTestRunnable(tree));
			before = System.nanoTime();
			for(j = 0; j < i; j++)
				thread[j].start();
			try {
				for(j = 0; j < i; j++)
					thread[j].join();
				after = System.nanoTime();
			} catch(InterruptedException e) {
				//Interrupted, stop testing
				e.printStackTrace();
				System.exit(1);
			}
			System.out.println(i + " threads, " + (after - before) + " ns");
		}
		System.out.println("Finished testing the fine-grained locking binary" +
				" tree");
	}
}