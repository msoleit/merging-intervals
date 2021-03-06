package models;

public class Interval implements Comparable<Interval> {
   private static final int MERGE_DISTANCE = 7;
   int start;
   int end;
   
   public Interval(int start, int end) {
	   this.start = start;
	   this.end = end;
   }
   
   public Interval() {
	// TODO Auto-generated constructor stub
}

public boolean doOverlap(Interval i) {
	   return this.exactOverlap(i) || this.withinMergeDistance(i);
	}
public boolean withinMergeDistance(Interval i) {
	if(this.start - i.end >= 0 && this.start - i.end <= Interval.MERGE_DISTANCE || i.start - this.end >= 0 && i.start - this.end <= Interval.MERGE_DISTANCE)
	    return true;
    return false; 
}
@Override
public int compareTo(Interval o) {
	// TODO Auto-generated method stub
    if(this.equals(o)) return 0;
    if(this.start != o.start) return (this.start - o.start);
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
	// TODO Auto-generated method stub
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
public boolean after(Interval i) {
	return (this.start > i.end);
}
public boolean before(Interval i) {
	return (this.end < i.start);
}
   
}
