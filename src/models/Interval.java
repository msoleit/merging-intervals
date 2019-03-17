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
	   if (this.exactOverlap(i)) 
	        return true; 
	   if(this.withinMergeDistance(i))
		    return true;
	   return false; 
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
	if (this.start <= i.end && i.start <= this.end) 
        return true; 
	return false;
}
   
}
