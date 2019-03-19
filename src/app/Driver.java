package app;

import models.Interval;
import models.IntervalTree;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;
public class Driver {
	static IntervalTree tree = new IntervalTree();
	static TreeSet<Interval> t = new TreeSet<>();
	public static void process(String input) {
		try {
			String[] args = input.split(" ");
			int start = Integer.parseInt(args[1]);
			int end = Integer.parseInt(args[2]);
			String action = args[3];
			switch(action) {
			case "ADDED" : 
				tree.add(new Interval(start, end));
			    break;
			case "REMOVED" :
				tree.remove(new Interval(start,end));
				break;
			case "DELETED" :
				tree.delete(new Interval(start, end));
	            break;
			}
			print(tree.getDisJointIntervals());
			System.out.print('\n');
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		}
	private static void print(List<Interval> list) {
		for(Interval i : list) {
			System.out.print(i);
	}
		System.out.print('\n');
	}
	public static void main(String[] args) {
		String fileName = "C:\\Users\\esloohm\\Documents\\workspace-sts-3.9.7.RELEASE\\merging-intervals\\src\\resources\\intervals.txt";

		//read file into stream, try-with-resources
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

			stream.forEach(Driver::process);

		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(tree.deletedBlocks);
	}

}
