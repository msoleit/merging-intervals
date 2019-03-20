package models;

public class Interval implements Comparable<Interval> {
	public static final int MERGE_DISTANCE = Integer.parseInt(System.getProperty("MERGE_DISTANCE"));
	private int start;
	private int end;

	public Interval(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public Interval() {
	}

	public int getStart() {
		return start;
	}
	
	public int getEnd() {
		return end;
	}

	public boolean doOverlap(Interval i) {
		return this.exactOverlap(i) || this.withinMergeDistance(i);
	}

	public boolean withinMergeDistance(Interval i) {
		if (this.getStart() - i.getEnd() >= 0 && this.getStart() - i.getEnd() <= MERGE_DISTANCE
				|| i.getStart() - this.getEnd() >= 0 && i.getStart() - this.getEnd() <= MERGE_DISTANCE)
			return true;
		return false;
	}

	@Override
	public int compareTo(Interval o) {
		if (this.equals(o))
			return 0;
		if (this.getStart() != o.getStart())
			return (this.getStart() - o.getStart());
		return (this.getEnd() - o.getEnd());
	}

	public boolean equals(Interval o) {
		return (this.getStart() == o.getStart() && this.getEnd() == o.getEnd());
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(this.getStart());
		sb.append(',');
		sb.append(this.getEnd());
		sb.append(']');
		return sb.toString();
	}

	public boolean exactOverlap(Interval i) {
		return this.intersects(i);
	}

	public boolean contains(Interval i) {
		return (this.getStart() <= i.getStart() && this.getEnd() >= i.getEnd());
	}

	public boolean isContainedIn(Interval i) {
		return i.contains(this);
	}

	public boolean intersects(Interval i) {
		return (this.getStart() <= i.getEnd() && i.getStart() <= this.getEnd());
	}

	public boolean isAfter(Interval i) {
		return (this.getStart() >= i.getEnd());
	}

	public boolean isBefore(Interval i) {
		return i.isAfter(this);
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public boolean doOverlap(Interval i, boolean overlapOverDistance) {
		return overlapOverDistance ? this.doOverlap(i) : this.exactOverlap(i);
	}
	
	public int length() {
		return this.end - this.start;
	}

}
