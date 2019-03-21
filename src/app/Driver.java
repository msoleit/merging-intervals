package app;

import models.Interval;
import models.IntervalTree;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class Driver {
	private static IntervalTree tree;
	private static Scanner scanner = new Scanner(System.in);

	private static void processInputStream(String input) {
		try {
			String[] args = input.split(" ");
			int start = Integer.parseInt(args[1]);
			int end = Integer.parseInt(args[2]);
			String action = args[3];
			switch (action) {
			case "ADDED":
				tree.add(new Interval(start, end));
				break;
			case "REMOVED":
				tree.remove(new Interval(start, end));
				break;
			case "DELETED":
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
		for (Interval i : list) {
			System.out.print(i);
		}
		System.out.print('\n');
	}

	public static void main(String[] args) {
		System.out.println("Enter the absolute input file path (Empty for the default path) :");
		String fileName = scanner.nextLine().trim();
		fileName = fileName != null && !fileName.equals("") ? fileName : "src\\resources\\intervals.txt";
		System.out.println("Enter the required merge distance (Default is 0) :");
		String MERGE_DISTANCE = scanner.nextLine().trim();
		MERGE_DISTANCE = MERGE_DISTANCE != null && MERGE_DISTANCE.matches("\\d+")
				? MERGE_DISTANCE
				: "0";
		System.setProperty("MERGE_DISTANCE", MERGE_DISTANCE);
		tree = new IntervalTree();
		// read file into stream, try-with-resources
		try (Stream<String> stream = Files.lines(Paths.get(fileName).toAbsolutePath())) {

			stream.forEach(Driver::processInputStream);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
