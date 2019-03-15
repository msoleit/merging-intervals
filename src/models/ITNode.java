package models;

public class ITNode {
    Interval interval;
    Interval left;
    Interval right;
    
    //maximum high value in subtree rooted with this node
    int max;
	public ITNode(Interval interval) {
		// TODO Auto-generated constructor stub
		this.interval = interval;
		this.left = null;
		this.right = null;
		this.max = interval.end;
	}
	
	
}
