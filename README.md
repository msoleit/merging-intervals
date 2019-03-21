# merging-intervals

## Assumptions : 
1. A deleted block can not be overwritten even by adding new intervals which overlaps with it.
2. A deleted block will be neglected if it is smaller than or equal the merge distance between the intervals on its right and left which will be merged in that case. 


## Complexity of operations: 
Since it is desired to output the list of merged intervals after each operation, O(size of merged intervals) is the best we can do. This was taken into consideration while choosing the solution approach.

### 1. Adding an interval : 
 - Add to the interval tree is O(logn) where n is the number of intervals in the original tree.
 - Add to the disjoint set of merged intervals is O(size of merged intervals).
 - Delete the deleted blocks is O(size of merged intervals + size of merged deleted blocks) which marks the complexity of the add operation.
 
 ### 2. Removal of an interval : 
 - Remove an interval from the tree is O(logn) where n is the number of intervals in the original tree.
 - Re-construct the set of merged disjoint intervals is O(n) where n is the number of intervals in the original tree.
 - Delete the deleted blocks is O(size of merged intervals + size of merged deleted blocks).
 - Complexity of Remove operation is the bigger of the second and third steps.
 
 ### 3. Deletion of a block :
 - Add the block to the set of disjoint merged deleted blocks is O(size of merged deleted blocks).
 - Delete the block from the set of merged disjoint intervals is O(size of merged intervals).
 - Complexity of Deletion is the bigger of the first and second steps.
