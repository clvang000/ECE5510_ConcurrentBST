ECE5510 Multiprocessor Programming Final Project
================================================
Analysis of Concurrent Binary Search Trees
------------------------------------------------

Final project for ECE 5510 Multiprocessor Programming.  A set of different concurrent binary search trees using
various approaches for synchronization and correctness.  The implemented binary trees are:

1. A sequential binary search tree.  This is the classic binary search tree data structure with no rebalancing, and is
only appropriate in a sequential setting.  This is implemented as a baseline to measure the concurrent trees.

2. A fine-grained locking binary search tree.  This tree uses a hand-over-hand locking approach to ensure correctness;
each node contains a mutex that is locked before any accesses or modifications are performed.  This ensures that all
accesses and modifications to a node (and additionally, to the tree) are atomic and correct.

3. A lock-free binary search tree.  This tree implements a leaf-based set to ensure correctness; by using a leaf-based
set (where all the data is stored in the leaves), we avoid a correctness issue of replacing a node during deletion.
When deleting a node from the classic binary search tree, a replacement must be found.  This replacement can either be
the direct predecessor (the "biggest" node in the left subtree) or the direct successor (the "smallest" node in the
right subtree) to maintain a total ordering within the tree.  In a lock-free setting, we can run into the scenario where
one thread is searching for a node that is selected as a replacement for a node to be deleted.  The replacement node
gets removed from the subtree before the first thread can find it, resulting in that thread returning a false negative.
Based on the tree presented by Faith et. al. in "Non-blocking Binary Search Trees" (in the repository).

4. A binary search tree that uses flat-combining.
  