package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IntervalTree {
	private static final boolean RED = true;
	private static final boolean BLACK = false;

	public IntervalTreeNode root;
	List<Interval> disjointIntervals;
	List<Interval> deletedBlocks;

	public IntervalTree() {
		// TODO Auto-generated constructor stub
		root = null;
		this.disjointIntervals = new ArrayList<>();
		this.deletedBlocks = new ArrayList<>();
	}
	
	public List<Interval> getDisJointIntervals() {
		return this.disjointIntervals;
	}

	/**
	 * Initializes an empty symbol table.
	 */

	/***************************************************************************
	 * Node helper methods.
	 ***************************************************************************/
	// is node x red; false if x is null ?
	private boolean isRed(IntervalTreeNode x) {
		if (x == null)
			return false;
		return x.color == RED;
	}

	// number of node in subtree rooted at x; 0 if x is null
	private int size(IntervalTreeNode x) {
		if (x == null)
			return 0;
		return x.size;
	}

	/**
	 * Returns the number of key-value pairs in this symbol table.
	 * 
	 * @return the number of key-value pairs in this symbol table
	 */
	public int size() {
		return size(root);
	}

	/**
	 * Is this symbol table empty?
	 * 
	 * @return {@code true} if this symbol table is empty and {@code false}
	 *         otherwise
	 */
	public boolean isEmpty() {
		return root == null;
	}

	/***************************************************************************
	 * Standard BST search.
	 ***************************************************************************/

	/**
	 * Returns the value associated with the given key.
	 * 
	 * @param key the key
	 * @return the value associated with the given key if the key is in the symbol
	 *         table and {@code null} if the key is not in the symbol table
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	public IntervalTreeNode get(Interval i) {
		if (i == null)
			throw new IllegalArgumentException("argument to get() is null");
		return get(root, i);
	}

	// value associated with the given key in subtree rooted at x; null if no such
	// key
	private IntervalTreeNode get(IntervalTreeNode x, Interval key) {
		while (x != null) {
			int cmp = key.compareTo(x.interval);
			if (cmp < 0)
				x = x.left;
			else if (cmp > 0)
				x = x.right;
			else
				return x;
		}
		return null;
	}

	/**
	 * Does this symbol table contain the given key?
	 * 
	 * @param key the key
	 * @return {@code true} if this symbol table contains {@code key} and
	 *         {@code false} otherwise
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	public boolean contains(Interval key) {
		return get(key) != null;
	}

	/***************************************************************************
	 * Red-black tree insertion.
	 ***************************************************************************/

	/**
	 * Inserts the specified key-value pair into the symbol table, overwriting the
	 * old value with the new value if the symbol table already contains the
	 * specified key. Deletes the specified key (and its associated value) from this
	 * symbol table if the specified value is {@code null}.
	 *
	 * @param key the key
	 * @param val the value
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	public void put(Interval i) {
		if (i == null)
			throw new IllegalArgumentException("first argument to put() is null");
		root = put(root, i);
		root.color = BLACK;
		// assert check();
	}

	// insert the key-value pair in the subtree rooted at h
	private IntervalTreeNode put(IntervalTreeNode h, Interval i) {
		addToDisjointIntervals(i);
		updateDeletedBlocks(i);
		if (h == null)
			return new IntervalTreeNode(i, RED, 1);

		int cmp = i.compareTo(h.interval);
		if (cmp < 0)
			h.left = put(h.left, i);
		else if (cmp > 0)
			h.right = put(h.right, i);
		else
			h.interval = i;

		// fix-up any right-leaning links
		if (isRed(h.right) && !isRed(h.left))
			h = rotateLeft(h);
		if (isRed(h.left) && isRed(h.left.left))
			h = rotateRight(h);
		if (isRed(h.left) && isRed(h.right))
			flipColors(h);
		h.size = size(h.left) + size(h.right) + 1;
		if (h.max < i.end)
			h.max = i.end;
		return h;
	}

	private void updateDeletedBlocks(Interval i) {
		// if deleted blocks can be overwritten do it here
	}

	private List<Interval> insert(List<Interval> disjointIntervals2, Interval newInterval) {
		ArrayList<Interval> ans = new ArrayList<>();
		int n = disjointIntervals2.size();
		if (n == 0) {
			ans.add(newInterval);
			return deleteBlocksIfNeeded(ans);
		}
		if (newInterval.end < disjointIntervals2.get(0).start && !newInterval.doOverlap(disjointIntervals2.get(0)) || newInterval.start > disjointIntervals2.get(n - 1).end && !newInterval.doOverlap(disjointIntervals2.get(n-1))) {
			if (newInterval.end < disjointIntervals2.get(0).start)
				ans.add(newInterval);

			for (int i = 0; i < n; i++)
				ans.add(disjointIntervals2.get(i));

			if (newInterval.start > disjointIntervals2.get(n - 1).end)
				ans.add(newInterval);

			return deleteBlocksIfNeeded(ans);
		}
		
		// covers all existing
		if (newInterval.start <= disjointIntervals2.get(0).start && newInterval.end >= disjointIntervals2.get(n - 1).end) {
			ans.add(newInterval);
			return deleteBlocksIfNeeded(ans);
		}
		boolean overlap = true;
		for (int i = 0; i < n; i++) {
			overlap = disjointIntervals2.get(i).doOverlap(newInterval);
			if (!overlap) {
				ans.add(disjointIntervals2.get(i));

				// Case 4 : To check if given interval
				// lies between two intervals.
				if (i < n && newInterval.start > disjointIntervals2.get(i).end && newInterval.end < disjointIntervals2.get(i + 1).start)
					ans.add(newInterval);

				continue;
			}
			Interval temp = new Interval();
			temp.start = Math.min(newInterval.start, disjointIntervals2.get(i).start);

			// Traverse the set until intervals are
			// overlapping
			while (i < n && overlap) {

				// Ending time of new merged interval
				// is maximum of ending time both
				// overlapping intervals.
				temp.end = Math.max(newInterval.end, disjointIntervals2.get(i).end);
				if (i == n - 1)
					overlap = false;
				else
					overlap = disjointIntervals2.get(i + 1).doOverlap(newInterval);
				i++;
			}

			i--;
			ans.add(temp);
		}

		return deleteBlocksIfNeeded(ans);
	}

	private List<Interval> deleteBlocksIfNeeded(List<Interval> intervals) {
		return intervals.parallelStream().flatMap(interval -> { 
			return splitIntervals(interval, this.deletedBlocks).stream();
		}).collect(Collectors.toList());
		}
	

	private List<Interval> splitIntervals(Interval interval, List<Interval> deletedBlocks) {
		// TODO Auto-generated method stub
		List<Interval> splittedIntervals = new ArrayList<>();
		for(Interval block : deletedBlocks) {
			if(block.exactOverlap(interval)) {
				if(interval.start >= block.start) splittedIntervals.add(new Interval(block.end, interval.end));
				else if (interval.end <= block.end) splittedIntervals.add(new Interval(interval.start, block.start));
				else {
					splittedIntervals.add(new Interval(interval.start, block.start));
					splittedIntervals.add(new Interval(block.end, interval.end));
				}
			}
		}
		if (splittedIntervals.isEmpty()) splittedIntervals.add(interval);
		return splittedIntervals;
	}

	private void addToDisjointIntervals(Interval i) {
		// TODO Auto-generated method stub
		this.disjointIntervals = insert(this.disjointIntervals, i);
	}
	
	

	/***************************************************************************
	 * Red-black tree deletion.
	 ***************************************************************************/

	/**
	 * Removes the smallest key and associated value from the symbol table.
	 * 
	 * @throws NoSuchElementException if the symbol table is empty
	 */
	public void deleteMin() {
		if (isEmpty())
			throw new NoSuchElementException("BST underflow");

		// if both children of root are black, set root to red
		if (!isRed(root.left) && !isRed(root.right))
			root.color = RED;

		root = deleteMin(root);
		if (!isEmpty())
			root.color = BLACK;
		// assert check();
	}

	// delete the key-value pair with the minimum key rooted at h
	private IntervalTreeNode deleteMin(IntervalTreeNode h) {
		if (h.left == null)
			return null;

		if (!isRed(h.left) && !isRed(h.left.left))
			h = moveRedLeft(h);

		h.left = deleteMin(h.left);
		return balance(h);
	}

	/**
	 * Removes the largest key and associated value from the symbol table.
	 * 
	 * @throws NoSuchElementException if the symbol table is empty
	 */
	public void deleteMax() {
		if (isEmpty())
			throw new NoSuchElementException("BST underflow");

		// if both children of root are black, set root to red
		if (!isRed(root.left) && !isRed(root.right))
			root.color = RED;

		root = deleteMax(root);
		if (!isEmpty())
			root.color = BLACK;
		// assert check();
	}

	// delete the key-value pair with the maximum key rooted at h
	private IntervalTreeNode deleteMax(IntervalTreeNode h) {
		if (isRed(h.left))
			h = rotateRight(h);

		if (h.right == null)
			return null;

		if (!isRed(h.right) && !isRed(h.right.left))
			h = moveRedRight(h);

		h.right = deleteMax(h.right);

		return balance(h);
	}

	/**
	 * Removes the specified key and its associated value from this symbol table (if
	 * the key is in this symbol table).
	 *
	 * @param key the key
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	public void remove(Interval key) {
		if (key == null)
			throw new IllegalArgumentException("argument to delete() is null");
		if (!contains(key))
			return;

		// if both children of root are black, set root to red
		if (!isRed(root.left) && !isRed(root.right))
			root.color = RED;

		root = remove(root, key);
		if (!isEmpty())
			root.color = BLACK;
		updateDisjointIntervals();
		// assert check();
	}

	private void updateDisjointIntervals() {
		// TODO Auto-generated method stub
		this.disjointIntervals = mergeOverlappingIntervals(root);
	}
	

	private List<Interval> mergeOverlappingIntervals(IntervalTreeNode r) {
		// TODO Auto-generated method stub
		List<Interval> mergedIntervals = new ArrayList<>();
		if(r == null) return mergedIntervals;
		reverseOrder(r, mergedIntervals);
		//System.out.println(mergedIntervals);
		int index = 0;
	    for (int i=0; i<mergedIntervals.size(); i++) 
	    { 
	        // If this is not first Interval and overlaps 
	        // with the previous one 
	        if (index != 0 && mergedIntervals.get(index-1).doOverlap(mergedIntervals.get(i))) 
	        { 
	            while (index != 0 && mergedIntervals.get(index-1).doOverlap(mergedIntervals.get(i))) 
	            { 
	                // Merge previous and current Intervals 
	                mergedIntervals.get(index - 1).end = Math.max(mergedIntervals.get(index - 1).end, mergedIntervals.get(i).end); 
	                mergedIntervals.get(index - 1).start = Math.min(mergedIntervals.get(index - 1).start, mergedIntervals.get(i).start); 
	                index--; 
	            } 
	        } 
	        else // Doesn't overlap with previous, add to 
	            // solution 
	            mergedIntervals.add(index, mergedIntervals.get(i));
	  
	        index++; 
	    } 
//	    System.out.println(index);
//	    System.out.println(mergedIntervals.subList(0,index));
	    mergedIntervals =  mergedIntervals.subList(0,index);
	    Collections.reverse(mergedIntervals);
	    return deleteBlocksIfNeeded(mergedIntervals);
	}

	private void reverseOrder(IntervalTreeNode r, List<Interval> reverse) {
		// TODO Auto-generated method stub
		if(r == null) return;
		reverseOrder(r.right, reverse);
		reverse.add(r.interval);
		reverseOrder(r.left, reverse);
	}

	// delete the key-value pair with the given key rooted at h
	private IntervalTreeNode remove(IntervalTreeNode h, Interval key) {
		// assert get(h, key) != null;

		if (key.compareTo(h.interval) < 0) {
			if (!isRed(h.left) && !isRed(h.left.left))
				h = moveRedLeft(h);
			h.left = remove(h.left, key);
		} else {
			if (isRed(h.left))
				h = rotateRight(h);
			if (key.compareTo(h.interval) == 0 && (h.right == null))
				return null;
			if (!isRed(h.right) && !isRed(h.right.left))
				h = moveRedRight(h);
			if (key.compareTo(h.interval) == 0) {
				IntervalTreeNode x = min(h.right);
				h.interval = x.interval;
				// h.val = get(h.right, min(h.right).key);
				// h.key = min(h.right).key;
				h.right = deleteMin(h.right);
			} else
				h.right = remove(h.right, key);
		}
		return balance(h);
	}

	/***************************************************************************
	 * Red-black tree helper functions.
	 ***************************************************************************/

	// make a left-leaning link lean to the right
	private IntervalTreeNode rotateRight(IntervalTreeNode h) {
		// assert (h != null) && isRed(h.left);
		IntervalTreeNode x = h.left;
		h.left = x.right;
		x.right = h;
		x.color = x.right.color;
		x.right.color = RED;
		x.size = h.size;
		h.size = size(h.left) + size(h.right) + 1;
		return x;
	}

	// make a right-leaning link lean to the left
	private IntervalTreeNode rotateLeft(IntervalTreeNode h) {
		// assert (h != null) && isRed(h.right);
		IntervalTreeNode x = h.right;
		h.right = x.left;
		x.left = h;
		x.color = x.left.color;
		x.left.color = RED;
		x.size = h.size;
		h.size = size(h.left) + size(h.right) + 1;
		return x;
	}

	// flip the colors of a node and its two children
	private void flipColors(IntervalTreeNode h) {
		// h must have opposite color of its two children
		// assert (h != null) && (h.left != null) && (h.right != null);
		// assert (!isRed(h) && isRed(h.left) && isRed(h.right))
		// || (isRed(h) && !isRed(h.left) && !isRed(h.right));
		h.color = !h.color;
		h.left.color = !h.left.color;
		h.right.color = !h.right.color;
	}

	// Assuming that h is red and both h.left and h.left.left
	// are black, make h.left or one of its children red.
	private IntervalTreeNode moveRedLeft(IntervalTreeNode h) {
		// assert (h != null);
		// assert isRed(h) && !isRed(h.left) && !isRed(h.left.left);

		flipColors(h);
		if (isRed(h.right.left)) {
			h.right = rotateRight(h.right);
			h = rotateLeft(h);
			flipColors(h);
		}
		return h;
	}

	// Assuming that h is red and both h.right and h.right.left
	// are black, make h.right or one of its children red.
	private IntervalTreeNode moveRedRight(IntervalTreeNode h) {
		// assert (h != null);
		// assert isRed(h) && !isRed(h.right) && !isRed(h.right.left);
		flipColors(h);
		if (isRed(h.left.left)) {
			h = rotateRight(h);
			flipColors(h);
		}
		return h;
	}

	// restore red-black tree invariant
	private IntervalTreeNode balance(IntervalTreeNode h) {
		// assert (h != null);

		if (isRed(h.right))
			h = rotateLeft(h);
		if (isRed(h.left) && isRed(h.left.left))
			h = rotateRight(h);
		if (isRed(h.left) && isRed(h.right))
			flipColors(h);

		h.size = size(h.left) + size(h.right) + 1;
		return h;
	}

	/***************************************************************************
	 * Utility functions.
	 ***************************************************************************/

	/**
	 * Returns the height of the BST (for debugging).
	 * 
	 * @return the height of the BST (a 1-node tree has height 0)
	 */
	public int height() {
		return height(root);
	}

	private int height(IntervalTreeNode x) {
		if (x == null)
			return -1;
		return 1 + Math.max(height(x.left), height(x.right));
	}

	/***************************************************************************
	 * Ordered symbol table methods.
	 ***************************************************************************/

	/**
	 * Returns the smallest key in the symbol table.
	 * 
	 * @return the smallest key in the symbol table
	 * @throws NoSuchElementException if the symbol table is empty
	 */
	public Interval min() {
		if (isEmpty())
			throw new NoSuchElementException("calls min() with empty symbol table");
		return min(root).interval;
	}

	// the smallest key in subtree rooted at x; null if no such key
	private IntervalTreeNode min(IntervalTreeNode x) {
		// assert x != null;
		if (x.left == null)
			return x;
		else
			return min(x.left);
	}

	/**
	 * Returns the largest key in the symbol table.
	 * 
	 * @return the largest key in the symbol table
	 * @throws NoSuchElementException if the symbol table is empty
	 */
	public Interval max() {
		if (isEmpty())
			throw new NoSuchElementException("calls max() with empty symbol table");
		return max(root).interval;
	}

	// the largest key in the subtree rooted at x; null if no such key
	private IntervalTreeNode max(IntervalTreeNode x) {
		// assert x != null;
		if (x.right == null)
			return x;
		else
			return max(x.right);
	}

	/**
	 * Returns the largest key in the symbol table less than or equal to
	 * {@code key}.
	 * 
	 * @param key the key
	 * @return the largest key in the symbol table less than or equal to {@code key}
	 * @throws NoSuchElementException   if there is no such key
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	public Interval floor(Interval key) {
		if (key == null)
			throw new IllegalArgumentException("argument to floor() is null");
		if (isEmpty())
			throw new NoSuchElementException("calls floor() with empty symbol table");
		IntervalTreeNode x = floor(root, key);
		if (x == null)
			return null;
		else
			return x.interval;
	}

	// the largest key in the subtree rooted at x less than or equal to the given
	// key
	private IntervalTreeNode floor(IntervalTreeNode x, Interval key) {
		if (x == null)
			return null;
		int cmp = key.compareTo(x.interval);
		if (cmp == 0)
			return x;
		if (cmp < 0)
			return floor(x.left, key);
		IntervalTreeNode t = floor(x.right, key);
		if (t != null)
			return t;
		else
			return x;
	}

	/**
	 * Returns the smallest key in the symbol table greater than or equal to
	 * {@code key}.
	 * 
	 * @param key the key
	 * @return the smallest key in the symbol table greater than or equal to
	 *         {@code key}
	 * @throws NoSuchElementException   if there is no such key
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	public Interval ceiling(Interval key) {
		if (key == null)
			throw new IllegalArgumentException("argument to ceiling() is null");
		if (isEmpty())
			throw new NoSuchElementException("calls ceiling() with empty symbol table");
		IntervalTreeNode x = ceiling(root, key);
		if (x == null)
			return null;
		else
			return x.interval;
	}

	// the smallest key in the subtree rooted at x greater than or equal to the
	// given key
	private IntervalTreeNode ceiling(IntervalTreeNode x, Interval key) {
		if (x == null)
			return null;
		int cmp = key.compareTo(x.interval);
		if (cmp == 0)
			return x;
		if (cmp > 0)
			return ceiling(x.right, key);
		IntervalTreeNode t = ceiling(x.left, key);
		if (t != null)
			return t;
		else
			return x;
	}

	/**
	 * Return the key in the symbol table whose rank is {@code k}. This is the
	 * (k+1)st smallest key in the symbol table.
	 *
	 * @param k the order statistic
	 * @return the key in the symbol table of rank {@code k}
	 * @throws IllegalArgumentException unless {@code k} is between 0 and
	 *                                  <em>n</em>–1
	 */
	public Interval select(int k) {
		if (k < 0 || k >= size()) {
			throw new IllegalArgumentException("argument to select() is invalid: " + k);
		}
		IntervalTreeNode x = select(root, k);
		return x.interval;
	}

	// the key of rank k in the subtree rooted at x
	private IntervalTreeNode select(IntervalTreeNode x, int k) {
		// assert x != null;
		// assert k >= 0 && k < size(x);
		int t = size(x.left);
		if (t > k)
			return select(x.left, k);
		else if (t < k)
			return select(x.right, k - t - 1);
		else
			return x;
	}

	/**
	 * Return the number of keys in the symbol table strictly less than {@code key}.
	 * 
	 * @param key the key
	 * @return the number of keys in the symbol table strictly less than {@code key}
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	public int rank(Interval key) {
		if (key == null)
			throw new IllegalArgumentException("argument to rank() is null");
		return rank(key, root);
	}

	// number of keys less than key in the subtree rooted at x
	private int rank(Interval key, IntervalTreeNode x) {
		if (x == null)
			return 0;
		int cmp = key.compareTo(x.interval);
		if (cmp < 0)
			return rank(key, x.left);
		else if (cmp > 0)
			return 1 + size(x.left) + rank(key, x.right);
		else
			return size(x.left);
	}

	/***************************************************************************
	 * Range count and range search.
	 ***************************************************************************/

	/**
	 * Returns all keys in the symbol table as an {@code Iterable}. To iterate over
	 * all of the keys in the symbol table named {@code st}, use the foreach
	 * notation: {@code for (Key key : st.keys())}.
	 * 
	 * @return all keys in the symbol table as an {@code Iterable}
	 */
	public ArrayList<Interval> keys() {
		if (isEmpty())
			return new ArrayList<Interval>();
		return keys(min(), max());
	}

	/**
	 * Returns all keys in the symbol table in the given range, as an
	 * {@code Iterable}.
	 *
	 * @param lo minimum endpoint
	 * @param hi maximum endpoint
	 * @return all keys in the sybol table between {@code lo} (inclusive) and
	 *         {@code hi} (inclusive) as an {@code Iterable}
	 * @throws IllegalArgumentException if either {@code lo} or {@code hi} is
	 *                                  {@code null}
	 */
	public ArrayList<Interval> keys(Interval lo, Interval hi) {
		if (lo == null)
			throw new IllegalArgumentException("first argument to keys() is null");
		if (hi == null)
			throw new IllegalArgumentException("second argument to keys() is null");

		ArrayList<Interval> queue = new ArrayList<Interval>();
		// if (isEmpty() || lo.compareTo(hi) > 0) return queue;
		keys(root, queue, lo, hi);
		return queue;
	}

	// add the keys between lo and hi in the subtree rooted at x
	// to the queue
	private void keys(IntervalTreeNode x, ArrayList<Interval> queue, Interval lo, Interval hi) {
		if (x == null)
			return;
		int cmplo = lo.compareTo(x.interval);
		int cmphi = hi.compareTo(x.interval);
		if (cmplo < 0)
			keys(x.left, queue, lo, hi);
		if (cmplo <= 0 && cmphi >= 0)
			queue.add(x.interval);
		if (cmphi > 0)
			keys(x.right, queue, lo, hi);
	}

	/**
	 * Returns the number of keys in the symbol table in the given range.
	 *
	 * @param lo minimum endpoint
	 * @param hi maximum endpoint
	 * @return the number of keys in the sybol table between {@code lo} (inclusive)
	 *         and {@code hi} (inclusive)
	 * @throws IllegalArgumentException if either {@code lo} or {@code hi} is
	 *                                  {@code null}
	 */
	public int size(Interval lo, Interval hi) {
		if (lo == null)
			throw new IllegalArgumentException("first argument to size() is null");
		if (hi == null)
			throw new IllegalArgumentException("second argument to size() is null");

		if (lo.compareTo(hi) > 0)
			return 0;
		if (contains(hi))
			return rank(hi) - rank(lo) + 1;
		else
			return rank(hi) - rank(lo);
	}

	/***************************************************************************
	 * Check integrity of red-black tree data structure.
	 ***************************************************************************/
	private boolean check() {
		if (!isBST())
			System.out.println("Not in symmetric order");
		if (!isSizeConsistent())
			System.out.println("Subtree counts not consistent");
		if (!isRankConsistent())
			System.out.println("Ranks not consistent");
		if (!is23())
			System.out.println("Not a 2-3 tree");
		if (!isBalanced())
			System.out.println("Not balanced");
		return isBST() && isSizeConsistent() && isRankConsistent() && is23() && isBalanced();
	}

	// does this binary tree satisfy symmetric order?
	// Note: this test also ensures that data structure is a binary tree since order
	// is strict
	private boolean isBST() {
		return isBST(root, null, null);
	}

	// is the tree rooted at x a BST with all keys strictly between min and max
	// (if min or max is null, treat as empty constraint)
	// Credit: Bob Dondero's elegant solution
	private boolean isBST(IntervalTreeNode x, Interval min, Interval max) {
		if (x == null)
			return true;
		if (min != null && x.interval.compareTo(min) <= 0)
			return false;
		if (max != null && x.interval.compareTo(max) >= 0)
			return false;
		return isBST(x.left, min, x.interval) && isBST(x.right, x.interval, max);
	}

	// are the size fields correct?
	private boolean isSizeConsistent() {
		return isSizeConsistent(root);
	}

	private boolean isSizeConsistent(IntervalTreeNode x) {
		if (x == null)
			return true;
		if (x.size != size(x.left) + size(x.right) + 1)
			return false;
		return isSizeConsistent(x.left) && isSizeConsistent(x.right);
	}

	// check that ranks are consistent
	private boolean isRankConsistent() {
		for (int i = 0; i < size(); i++)
			if (i != rank(select(i)))
				return false;
		for (Interval key : keys())
			if (key.compareTo(select(rank(key))) != 0)
				return false;
		return true;
	}

	// Does the tree have no red right links, and at most one (left)
	// red links in a row on any path?
	private boolean is23() {
		return is23(root);
	}

	private boolean is23(IntervalTreeNode x) {
		if (x == null)
			return true;
		if (isRed(x.right))
			return false;
		if (x != root && isRed(x) && isRed(x.left))
			return false;
		return is23(x.left) && is23(x.right);
	}

	// do all paths from root to leaf have same number of black edges?
	private boolean isBalanced() {
		int black = 0; // number of black links on path from root to min
		IntervalTreeNode x = root;
		while (x != null) {
			if (!isRed(x))
				black++;
			x = x.left;
		}
		return isBalanced(root, black);
	}

	// does every path from the root to a leaf have the given number of black links?
	private boolean isBalanced(IntervalTreeNode x, int black) {
		if (x == null)
			return black == 0;
		if (!isRed(x))
			black--;
		return isBalanced(x.left, black) && isBalanced(x.right, black);
	}

	public void delete(Interval interval) {
		// TODO Auto-generated method stub
		this.deletedBlocks.add(interval);
		this.disjointIntervals = deleteBlocksIfNeeded(this.disjointIntervals);
	}

}
