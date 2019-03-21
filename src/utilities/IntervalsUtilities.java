package utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import models.Interval;

public final class IntervalsUtilities {

	public IntervalsUtilities() {

	}

	// insert an interval in a set of sorted disjoint intervals, merging if
	// necessary
	public static List<Interval> insertInSortedDisjointIntervals(List<Interval> disjointIntervals, Interval newInterval,
			boolean overlapOverDistance) {
		ArrayList<Interval> updated = new ArrayList<>();
		int n = disjointIntervals.size();
		if (n == 0) {
			updated.add(newInterval);
			return updated;
		}
		// Case 1 & 2 : new interval is before all intervals or after all intervals
		// without any overlap
		if (newInterval.isBefore(disjointIntervals.get(0))
				&& !newInterval.doOverlap(disjointIntervals.get(0), overlapOverDistance)
				|| newInterval.isAfter(disjointIntervals.get(n - 1))
						&& !newInterval.doOverlap(disjointIntervals.get(n - 1), overlapOverDistance)) {
			if (newInterval.isBefore(disjointIntervals.get(0)))
				updated.add(newInterval);

			for (int i = 0; i < n; i++)
				updated.add(disjointIntervals.get(i));

			if (newInterval.isAfter(disjointIntervals.get(n - 1)))
				updated.add(newInterval);

			return updated;
		}

		// Case 3 : new interval covers all existing intervals
		if (newInterval.contains(disjointIntervals.get(0)) && newInterval.contains(disjointIntervals.get(n - 1))) {
			updated.add(newInterval);
			return updated;
		}
		boolean overlap = true;
		for (int i = 0; i < n; i++) {
			overlap = disjointIntervals.get(i).doOverlap(newInterval, overlapOverDistance);
			if (!overlap) {
				updated.add(disjointIntervals.get(i));

				// Case 4 : To check if given interval
				// lies between two intervals.
				if (i < n && newInterval.isAfter(disjointIntervals.get(i))
						&& newInterval.isBefore(disjointIntervals.get(i + 1)))
					updated.add(newInterval);
				continue;
			}
			Interval temp = new Interval();
			temp.setStart(Math.min(newInterval.getStart(), disjointIntervals.get(i).getStart()));
			// Traverse the set until intervals are not overlapping
			while (i < n && overlap) {

				// Ending time of new merged interval
				// is maximum of ending time both
				// overlapping intervals.
				temp.setEnd(Math.max(newInterval.getEnd(), disjointIntervals.get(i).getEnd()));
				if (i == n - 1)
					overlap = false;
				else
					overlap = disjointIntervals.get(i + 1).doOverlap(newInterval, overlapOverDistance);
				i++;
			}

			i--;
			updated.add(temp);
		}
		return updated;
	}

	// merge overlapping intervals in a set of sorted intervals
	public static List<Interval> mergeOverlappingIntervals(List<Interval> mergedIntervals) {

		Collections.reverse(mergedIntervals);
		int index = 0;
		for (int i = 0; i < mergedIntervals.size(); i++) {
			// If this is not first Interval and overlaps
			// with the previous one
			if (index != 0 && mergedIntervals.get(index - 1).doOverlap(mergedIntervals.get(i))) {
				while (index != 0 && mergedIntervals.get(index - 1).doOverlap(mergedIntervals.get(i))) {
					// Merge previous and current Intervals
					Interval temp = new Interval();
					temp.setEnd(Math.max(mergedIntervals.get(index - 1).getEnd(), mergedIntervals.get(i).getEnd()));
					temp.setStart(
							Math.min(mergedIntervals.get(index - 1).getStart(), mergedIntervals.get(i).getStart()));
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
		return (mergedIntervals);
	}

	public static Interval searchOverlap(List<Interval> disjointIntervals, Interval i) {
		int low = 0;
		int high = disjointIntervals.size() - 1;
		while (low <= high) {
			int middleIndex = (low + high) / 2;
			Interval current = disjointIntervals.get(middleIndex);
			if (current.exactOverlap(i))
				return current;
			else if (current.isBefore(i))
				low = middleIndex + 1;
			else
				high = middleIndex - 1;
		}
		return null;
	}

	// delete blocks from a set of sorted disjoint intervals
	public static List<Interval> deleteBlocksFromDisjointIntervals(List<Interval> sortedDisjointIntervals,
			List<Interval> sortedDeletedBlocks) {
		if (sortedDeletedBlocks.isEmpty())
			return sortedDisjointIntervals;
		Stack<Interval> updated = new Stack<>();
		int deletedIndex = 0;
		int intervalIndex = 0;
		while (deletedIndex < sortedDeletedBlocks.size() && intervalIndex < sortedDisjointIntervals.size()) {
			if (sortedDeletedBlocks.get(deletedIndex).isBefore(sortedDisjointIntervals.get(intervalIndex))) {
				deletedIndex++;
			}
			if (deletedIndex < sortedDeletedBlocks.size()
					&& sortedDeletedBlocks.get(deletedIndex).isAfter(sortedDisjointIntervals.get(intervalIndex))) {
				updated.push(sortedDisjointIntervals.get(intervalIndex));
				intervalIndex++;
			}
			if (deletedIndex < sortedDeletedBlocks.size() && intervalIndex < sortedDisjointIntervals.size()
					&& sortedDeletedBlocks.get(deletedIndex).contains(sortedDisjointIntervals.get(intervalIndex))) {
				intervalIndex++;
				continue;
			}
			if (deletedIndex < sortedDeletedBlocks.size() && intervalIndex < sortedDisjointIntervals.size()
					&& sortedDeletedBlocks.get(deletedIndex).exactOverlap(sortedDisjointIntervals.get(intervalIndex))) {
				updated.addAll(splitInterval(sortedDisjointIntervals.get(intervalIndex),
						sortedDeletedBlocks.get(deletedIndex)));
				deletedIndex++;
				intervalIndex++;
				while (deletedIndex < sortedDeletedBlocks.size() && !updated.empty()
						&& updated.peek().exactOverlap(sortedDeletedBlocks.get(deletedIndex))) {
					updated.addAll(splitInterval(updated.pop(), sortedDeletedBlocks.get(deletedIndex)));
					deletedIndex++;
				}
				deletedIndex--;
			}
		}
		while (intervalIndex < sortedDisjointIntervals.size()) {
			updated.push(sortedDisjointIntervals.get(intervalIndex++));
		}
		return updated;
	}

	// cut a deleted block from an intersecting interval
	public static List<Interval> splitInterval(Interval interval, Interval block) {
		List<Interval> splittedIntervals = new ArrayList<>();
		if (interval.equals(block))
			return splittedIntervals;
		if (interval.getStart() >= block.getStart())
			splittedIntervals.add(new Interval(block.getEnd(), interval.getEnd()));
		else if (interval.getEnd() <= block.getEnd())
			splittedIntervals.add(new Interval(interval.getStart(), block.getStart()));
		else if (block.length() <= Interval.MERGE_DISTANCE)
			splittedIntervals.add(interval);
		else {
			splittedIntervals.add(new Interval(interval.getStart(), block.getStart()));
			splittedIntervals.add(new Interval(block.getEnd(), interval.getEnd()));
		}
		return splittedIntervals;
	}

	// delete a block from a set of sorted disjoint intervals
	public static List<Interval> deleteBlockFromDisjointIntervals(List<Interval> disjointIntervals, Interval block) {
		return disjointIntervals.stream().filter(i -> !block.contains(i)).flatMap(i -> {
			return i.exactOverlap(block) ? IntervalsUtilities.splitInterval(i, block).stream()
					: new ArrayList<>(Arrays.asList(i)).stream();
		}).collect(Collectors.toList());
	}
}
