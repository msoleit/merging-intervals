package models;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import utilities.IntervalsUtilities;

public class IntervalTree {

	List<Interval> disjointIntervals;
	public List<Interval> deletedBlocks;
	public TreeSet<Interval> intervals;

	public IntervalTree() {
		this.disjointIntervals = new ArrayList<>();
		this.deletedBlocks = new ArrayList<>();
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
		updateDisjointIntervals();
	}

	// Delete an interval block from the disjoint intervals set
	public void delete(Interval i) {
		this.deletedBlocks = IntervalsUtilities.insertInSortedDisjointIntervals(this.deletedBlocks, i, false);
		this.disjointIntervals = IntervalsUtilities.deleteBlockFromDisjointIntervals(this.disjointIntervals, i);
	}

	private void updateDeletedBlocks(Interval i) {
		// if deleted blocks can be overwritten do it here
		this.deletedBlocks = IntervalsUtilities.deleteBlockFromDisjointIntervals(this.deletedBlocks, i);
	}

	private void addToDisjointIntervals(Interval i) {
		this.disjointIntervals = IntervalsUtilities.insertInSortedDisjointIntervals(this.disjointIntervals, i, true);
	}

	private void updateDisjointIntervals() {
		this.disjointIntervals = IntervalsUtilities.deleteBlocksFromDisjointIntervals(
				IntervalsUtilities.mergeOverlappingIntervals(new ArrayList<>(this.intervals)),
				this.deletedBlocks);
	}

}
