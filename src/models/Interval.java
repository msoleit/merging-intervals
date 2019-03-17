package models;

public class Interval implements Comparable<Interval> {
   private static final int MERGE_DISTANCE = 5;
   int start;
   int end;
   
   public Interval(int start, int end) {
	   this.start = start;
	   this.end = end;
   }
   
   public boolean doOverlap(Interval i) {
	   if (this.start <= i.end && i.start <= this.end) 
	        return true; 
	   if(this.start - i.end <= Interval.MERGE_DISTANCE || i.start - this.end <= Interval.MERGE_DISTANCE)
		    return true;
	   return false; 
	}

@Override
public int compareTo(Interval o) {
	// TODO Auto-generated method stub
//	if(this.equals(o)) return 0;
	return this.start - o.start;
}

public boolean equals(Interval o) {
	return (this.start == o.start && this.end == o.end);
}
   
}
