package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class IntervalTree {

	List<Interval> disjointIntervals;
	public TreeSet<Interval> deletedBlocks;
	public TreeSet<Interval> intervals;

	public IntervalTree() {
		this.disjointIntervals = new ArrayList<>();
		this.deletedBlocks = new TreeSet<>();
		this.intervals = new TreeSet<>();
	}

	public List<Interval> getDisJointIntervals() {
		return this.disjointIntervals;
	}

	// Add an interval to the tree
	public void add(Interval i) {
		intervals.add(i);
		updateDeletedBlocks(i);
		addToDisjointIntervals(i);
	}

	// Remove an interval from the original stream
	public void remove(Interval i) {
		intervals.remove(i);
		this.updateDisjointIntervals();
	}

	// Delete an interval block from the disjoint intervals set
	public void delete(Interval interval) {
		this.deletedBlocks.add(interval);
		this.disjointIntervals = deleteBlocksIfNeeded(this.disjointIntervals);
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
		return deleteBlocksIfNeeded(ans);
	}

	private List<Interval> deleteBlocksIfNeeded(List<Interval> intervals) {
		return intervals.parallelStream().flatMap(interval -> {
			return splitIntervals(interval, this.deletedBlocks).stream();
		}).collect(Collectors.toList());
	}

	private List<Interval> splitIntervals(Interval interval, Set<Interval> deletedBlocks) {
		Stack<Interval> splitted = new Stack<>();
		splitted.push(interval);
		for (Interval block : deletedBlocks) {
			if (splitted.peek().exactOverlap(block)) {
				splitted.addAll(splitInterval(splitted.pop(), block));
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

	private void updateDisjointIntervals() {
		List<Interval> reverse = new ArrayList<>(this.intervals.descendingSet());
		this.disjointIntervals = mergeOverlappingIntervals(reverse);
	}

	private List<Interval> mergeOverlappingIntervals(List<Interval> mergedIntervals) {

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

}
