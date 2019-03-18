package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class IntervalTree {
	private static final boolean RED = true;
	private static final boolean BLACK = false;

	public IntervalTreeNode root;
	List<Interval> disjointIntervals;
	public TreeSet<Interval> deletedBlocks;

	public IntervalTree() {
		// TODO Auto-generated constructor stub
		root = null;
		this.disjointIntervals = new ArrayList<>();
		this.deletedBlocks = new TreeSet<>();
	}

	public List<Interval> getDisJointIntervals() {
		return this.disjointIntervals;
	}

	// is node x red; false if x is null ?
	private boolean isRed(IntervalTreeNode x) {
		if (x == null)
			return false;
		return x.color == RED;
	}

	private int size(IntervalTreeNode x) {
		if (x == null)
			return 0;
		return x.size;
	}

	public int size() {
		return size(root);
	}

	public boolean isEmpty() {
		return root == null;
	}

	public IntervalTreeNode get(Interval i) {
		if (i == null)
			throw new IllegalArgumentException("argument to get() is null");
		return get(root, i);
	}

	private IntervalTreeNode get(IntervalTreeNode x, Interval i) {
		while (x != null) {
			int cmp = i.compareTo(x.interval);
			if (cmp < 0)
				x = x.left;
			else if (cmp > 0)
				x = x.right;
			else
				return x;
		}
		return null;
	}

	public boolean contains(Interval i) {
		return get(i) != null;
	}

	public void put(Interval i) {
		if (i == null)
			throw new IllegalArgumentException("argument to put() is null");
		root = put(root, i);
		updateDeletedBlocks(i);
		addToDisjointIntervals(i);
		root.color = BLACK;
		assert check();
	}

	private IntervalTreeNode put(IntervalTreeNode h, Interval i) {
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
		this.deletedBlocks = this.deletedBlocks.stream().filter(block -> !i.contains(block)).flatMap(block -> {
			return block.exactOverlap(i) ? splitInterval(block, i).stream()
					: new ArrayList<>(Arrays.asList(block)).stream();
		}).collect(Collectors.toCollection(TreeSet::new));
	}

	private List<Interval> insert(List<Interval> disjointIntervals, Interval newInterval) {
		ArrayList<Interval> ans = new ArrayList<>();
		int n = disjointIntervals.size();
		if (n == 0) {
			ans.add(newInterval);
			return deleteBlocksIfNeeded(ans);
		}
		// Case 1 & 2 : new interval is before all intervals or after all intervals
		// without any overlap
		if (newInterval.end < disjointIntervals.get(0).start && !newInterval.doOverlap(disjointIntervals.get(0))
				|| newInterval.start > disjointIntervals.get(n - 1).end
						&& !newInterval.doOverlap(disjointIntervals.get(n - 1))) {
			if (newInterval.end < disjointIntervals.get(0).start)
				ans.add(newInterval);

			for (int i = 0; i < n; i++)
				ans.add(disjointIntervals.get(i));

			if (newInterval.start > disjointIntervals.get(n - 1).end)
				ans.add(newInterval);

			return deleteBlocksIfNeeded(ans);
		}

		// Case 3 : new interval covers all existing intervals
		if (newInterval.start <= disjointIntervals.get(0).start
				&& newInterval.end >= disjointIntervals.get(n - 1).end) {
			ans.add(newInterval);
			return deleteBlocksIfNeeded(ans);
		}
		boolean overlap = true;
		for (int i = 0; i < n; i++) {
			overlap = disjointIntervals.get(i).doOverlap(newInterval);
			if (!overlap) {
				ans.add(disjointIntervals.get(i));

				// Case 4 : To check if given interval
				// lies between two intervals.
				if (i < n && newInterval.after(disjointIntervals.get(i))
						&& newInterval.before(disjointIntervals.get(i + 1)))
					ans.add(newInterval);

				continue;
			}
			Interval temp = new Interval();
			temp.start = Math.min(newInterval.start, disjointIntervals.get(i).start);
			// Traverse the set until intervals are not overlapping
			while (i < n && overlap) {

				// Ending time of new merged interval
				// is maximum of ending time both
				// overlapping intervals.
				temp.end = Math.max(newInterval.end, disjointIntervals.get(i).end);
				if (i == n - 1)
					overlap = false;
				else
					overlap = disjointIntervals.get(i + 1).doOverlap(newInterval);
				i++;
			}

			i--;
			ans.add(temp);
		}
//		System.out.println("insert:");
//		System.out.println(ans);
		return deleteBlocksIfNeeded(ans);
	}

	private List<Interval> deleteBlocksIfNeeded(List<Interval> intervals) {
		return intervals.parallelStream().flatMap(interval -> {
			return splitIntervals(interval, this.deletedBlocks).stream();
		}).collect(Collectors.toList());
	}

	private List<Interval> splitIntervals(Interval interval, Set<Interval> deletedBlocks) {
		// TODO Auto-generated method stub
		Stack<Interval> splitted = new Stack<>();
		splitted.push(interval);
		for (Interval block : deletedBlocks) {
			if (splitted.peek().exactOverlap(block)) {
				splitted.addAll(splitInterval(splitted.pop(), block));
//		        System.out.println("split: ");
//		        System.out.println(splitted);
			}
		}
		return new ArrayList<>(splitted);
	}

	private List<Interval> splitInterval(Interval interval, Interval block) {
		List<Interval> splittedIntervals = new ArrayList<>();
		if (interval.start >= block.start)
			splittedIntervals.add(new Interval(block.end, interval.end));
		else if (interval.end <= block.end)
			splittedIntervals.add(new Interval(interval.start, block.start));
		else {
			splittedIntervals.add(new Interval(interval.start, block.start));
			splittedIntervals.add(new Interval(block.end, interval.end));
		}
		return splittedIntervals;
	}

	private void addToDisjointIntervals(Interval i) {
		this.disjointIntervals = insert(this.disjointIntervals, i);
	}

	/***************************************************************************
	 * Red-black tree deletion.
	 ***************************************************************************/
	public void deleteMin() {
		if (isEmpty())
			throw new NoSuchElementException("BST underflow");

		// if both children of root are black, set root to red
		if (!isRed(root.left) && !isRed(root.right))
			root.color = RED;

		root = deleteMin(root);
		if (!isEmpty())
			root.color = BLACK;
		assert check();
	}

	private IntervalTreeNode deleteMin(IntervalTreeNode h) {
		if (h.left == null)
			return null;

		if (!isRed(h.left) && !isRed(h.left.left))
			h = moveRedLeft(h);

		h.left = deleteMin(h.left);
		return balance(h);
	}

	public void deleteMax() {
		if (isEmpty())
			throw new NoSuchElementException("BST underflow");

		// if both children of root are black, set root to red
		if (!isRed(root.left) && !isRed(root.right))
			root.color = RED;

		root = deleteMax(root);
		if (!isEmpty())
			root.color = BLACK;
		assert check();
	}

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

	public void remove(Interval i) {
		if (i == null)
			throw new IllegalArgumentException("argument to remove() is null");
		if (!contains(i))
			return;

		// if both children of root are black, set root to red
		if (!isRed(root.left) && !isRed(root.right))
			root.color = RED;

		root = remove(root, i);
		if (!isEmpty())
			root.color = BLACK;
		this.updateDisjointIntervals();
		assert check();
	}

	private void updateDisjointIntervals() {
		this.disjointIntervals = mergeOverlappingIntervals();
	}

	private List<Interval> mergeOverlappingIntervals() {
		List<Interval> mergedIntervals = new ArrayList<>();
		if (this.root == null)
			return mergedIntervals;
		reverseOrder(this.root, mergedIntervals);
		int index = 0;
		for (int i = 0; i < mergedIntervals.size(); i++) {
			// If this is not first Interval and overlaps
			// with the previous one
			if (index != 0 && mergedIntervals.get(index - 1).doOverlap(mergedIntervals.get(i))) {
				while (index != 0 && mergedIntervals.get(index - 1).doOverlap(mergedIntervals.get(i))) {
					// Merge previous and current Intervals
					Interval temp = new Interval();
					temp.end = Math.max(mergedIntervals.get(index - 1).end, mergedIntervals.get(i).end);
					temp.start = Math.min(mergedIntervals.get(index - 1).start, mergedIntervals.get(i).start);
					mergedIntervals.set(index - 1, temp);
					index--;
				}
			} else // Doesn't overlap with previous, add to
					// solution
				mergedIntervals.add(index, mergedIntervals.get(i));

			index++;
		}
		mergedIntervals = mergedIntervals.subList(0, index);
		Collections.reverse(mergedIntervals);
		return deleteBlocksIfNeeded(mergedIntervals);
	}

	private void reverseOrder(IntervalTreeNode r, List<Interval> reverse) {
		if (r == null)
			return;
		reverseOrder(r.right, reverse);
		reverse.add(r.interval);
		reverseOrder(r.left, reverse);
	}

	private IntervalTreeNode remove(IntervalTreeNode r, Interval i) {
		assert get(r, i) != null;

		if (i.compareTo(r.interval) < 0) {
			if (!isRed(r.left) && !isRed(r.left.left))
				r = moveRedLeft(r);
			r.left = remove(r.left, i);
		} else {
			if (isRed(r.left))
				r = rotateRight(r);
			if (i.compareTo(r.interval) == 0 && (r.right == null))
				return null;
			if (!isRed(r.right) && !isRed(r.right.left))
				r = moveRedRight(r);
			if (i.compareTo(r.interval) == 0) {
				IntervalTreeNode x = min(r.right);
				r.interval = x.interval;
				r.right = deleteMin(r.right);
			} else
				r.right = remove(r.right, i);
		}
		return balance(r);
	}

	/***************************************************************************
	 * Red-black tree helper functions.
	 ***************************************************************************/

	// make a left-leaning link lean to the right
	private IntervalTreeNode rotateRight(IntervalTreeNode r) {
		assert (r != null) && isRed(r.left);
		IntervalTreeNode x = r.left;
		r.left = x.right;
		x.right = r;
		x.color = x.right.color;
		x.right.color = RED;
		x.size = r.size;
		r.size = size(r.left) + size(r.right) + 1;
		return x;
	}

	// make a right-leaning link lean to the left
	private IntervalTreeNode rotateLeft(IntervalTreeNode h) {
		assert (h != null) && isRed(h.right);
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
		assert (h != null);

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

	public Interval min() {
		if (isEmpty())
			throw new NoSuchElementException("calls min() with empty tree");
		return min(root).interval;
	}

	// the smallest interval in subtree rooted at r; null if no such key
	private IntervalTreeNode min(IntervalTreeNode r) {
		assert r != null;
		if (r.left == null)
			return r;
		else
			return min(r.left);
	}

	public Interval max() {
		if (isEmpty())
			throw new NoSuchElementException("calls max() with empty symbol table");
		return max(root).interval;
	}

	// the largest interval in the subtree rooted at r; null if no such key
	private IntervalTreeNode max(IntervalTreeNode r) {
		assert r != null;
		if (r.right == null)
			return r;
		else
			return max(r.right);
	}

	public Interval select(int k) {
		if (k < 0 || k >= size()) {
			throw new IllegalArgumentException("argument to select() is invalid: " + k);
		}
		IntervalTreeNode x = select(root, k);
		return x.interval;
	}

	// the key of rank k in the subtree rooted at x
	private IntervalTreeNode select(IntervalTreeNode r, int k) {
		assert r != null;
		assert k >= 0 && k < size(r);
		int t = size(r.left);
		if (t > k)
			return select(r.left, k);
		else if (t < k)
			return select(r.right, k - t - 1);
		else
			return r;
	}

	public int rank(Interval i) {
		if (i == null)
			throw new IllegalArgumentException("argument to rank() is null");
		return rank(i, root);
	}

	// number of keys less than key in the subtree rooted at x
	private int rank(Interval i, IntervalTreeNode r) {
		if (r == null)
			return 0;
		int cmp = i.compareTo(r.interval);
		if (cmp < 0)
			return rank(i, r.left);
		else if (cmp > 0)
			return 1 + size(r.left) + rank(i, r.right);
		else
			return size(r.left);
	}

	/***************************************************************************
	 * Range count and range search.
	 ***************************************************************************/
	public ArrayList<Interval> intervals() {
		if (isEmpty())
			return new ArrayList<Interval>();
		return intervals(min(), max());
	}

	public ArrayList<Interval> intervals(Interval lo, Interval hi) {
		if (lo == null)
			throw new IllegalArgumentException("first argument to keys() is null");
		if (hi == null)
			throw new IllegalArgumentException("second argument to keys() is null");

		ArrayList<Interval> queue = new ArrayList<Interval>();
		// if (isEmpty() || lo.compareTo(hi) > 0) return queue;
		intervals(root, queue, lo, hi);
		return queue;
	}

	// add the keys between lo and hi in the subtree rooted at x
	// to the queue
	private void intervals(IntervalTreeNode x, ArrayList<Interval> queue, Interval lo, Interval hi) {
		if (x == null)
			return;
		int cmplo = lo.compareTo(x.interval);
		int cmphi = hi.compareTo(x.interval);
		if (cmplo < 0)
			intervals(x.left, queue, lo, hi);
		if (cmplo <= 0 && cmphi >= 0)
			queue.add(x.interval);
		if (cmphi > 0)
			intervals(x.right, queue, lo, hi);
	}

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
		for (Interval i : intervals())
			if (i.compareTo(select(rank(i))) != 0)
				return false;
		return true;
	}

	// Does the tree have no red right links, and at most one (left)
	// red links in a row on any path?
	private boolean is23() {
		return is23(root);
	}

	private boolean is23(IntervalTreeNode r) {
		if (r == null)
			return true;
		if (isRed(r.right))
			return false;
		if (r != root && isRed(r) && isRed(r.left))
			return false;
		return is23(r.left) && is23(r.right);
	}

	// do all paths from root to leaf have same number of black edges?
	private boolean isBalanced() {
		int black = 0; // number of black links on path from root to min
		IntervalTreeNode r = root;
		while (r != null) {
			if (!isRed(r))
				black++;
			r = r.left;
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
