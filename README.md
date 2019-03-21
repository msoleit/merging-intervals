# merging-intervals

## To run : 
The Driver class is the entry point to the application. 
When run, it will prompt the user to enter the path of the input file (default is under src/resources folder in the project directory) and the merging distance (default is zero).

## Assumptions : 
1. A deleted block can be overwritten when adding new intervals which overlaps with it.
2. A deleted block will be neglected if it is smaller than or equal the merge distance between the intervals on its right and left which will be merged in that case. 


## Complexity of operations: 
Since it is desired to output the list of merged intervals after each operation, O(size of merged intervals) is the best we can do. This was taken into consideration while choosing the solution approach.

### 1. Adding an interval : 
 - Add to the interval tree is O(logn) where n is the number of intervals in the original tree.
 - Add to the disjoint set of merged intervals is O(size of merged intervals).
 - Update the deleted blocks is O(size of merged deleted).
 - Complexity of Add operation is the bigger of the second and third steps.
 ### 2. Removal of an interval : 
 - Remove an interval from the tree is O(logn) where n is the number of intervals in the original tree.
 - Re-construct the set of merged disjoint intervals is O(n) where n is the number of intervals in the original tree.
 - Delete the deleted blocks is O(size of merged intervals + size of merged deleted blocks).
 - Complexity of Remove operation is the bigger of the second and third steps.
 
 ### 3. Deletion of a block :
 - Add the block to the set of disjoint merged deleted blocks is O(size of merged deleted blocks).
 - Delete the block from the set of merged disjoint intervals is O(size of merged intervals).
 - Complexity of Deletion is the bigger of the first and second steps.
