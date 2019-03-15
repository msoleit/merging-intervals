package models;

public class Interval {
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
   
}
