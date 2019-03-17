package models;

public class IntervalTreeNode {
    
    public Interval interval;
    public IntervalTreeNode left;
    public IntervalTreeNode right;
    boolean color;     // color of parent link
    int size;          // subtree count
    
    //maximum high value in subtree rooted with this node
    int max;
	public IntervalTreeNode(Interval interval, boolean color, int size) {
		// TODO Auto-generated constructor stub
		this.interval = interval;
		this.color = color;
		this.size = size;
		this.left = null;
		this.right = null;
		this.max = interval.end;
	}
	
	
}
