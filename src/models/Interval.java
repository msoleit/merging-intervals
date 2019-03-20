package models;

public class Interval implements Comparable<Interval> {
	private static final int MERGE_DISTANCE = Integer.parseInt(System.getProperty("MERGE_DISTANCE"));
	int start;
	int end;

	public Interval(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public Interval() {
	}

	public boolean doOverlap(Interval i) {
		return this.exactOverlap(i) || this.withinMergeDistance(i);
	}

	public boolean withinMergeDistance(Interval i) {
		if (this.start - i.end >= 0 && this.start - i.end <= MERGE_DISTANCE
				|| i.start - this.end >= 0 && i.start - this.end <= MERGE_DISTANCE)
			return true;
		return false;
	}

	@Override
	public int compareTo(Interval o) {
		if (this.equals(o))
			return 0;
		if (this.start != o.start)
			return (this.start - o.start);
		return (this.end - o.end);
	}

	public boolean equals(Interval o) {
		return (this.start == o.start && this.end == o.end);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(this.start);
		sb.append(',');
		sb.append(this.end);
		sb.append(']');
		return sb.toString();
	}

	public boolean exactOverlap(Interval i) {
		return this.intersects(i);
	}

	public boolean contains(Interval i) {
		return (this.start <= i.start && this.end >= i.end);
	}

	public boolean isContainedIn(Interval i) {
		return i.contains(this);
	}

	public boolean intersects(Interval i) {
		return (this.start <= i.end && i.start <= this.end);
	}

	public boolean isAfter(Interval i) {
		return (this.start > i.end);
	}

	public boolean isBefore(Interval i) {
		return i.isAfter(this);
	}

}
