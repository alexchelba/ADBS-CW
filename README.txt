EXTRACTING THE JOIN CONDITIONS
To extract all join conditions, we firstly differentiate, for each table, the conditions that include terms only from itself, from conditions that include terms from other tables as well. Once we have the lists for all tables, before calling a join between 2 tables, we establish which conditions are shared by the 2 tables. We also establish the list of common variables that the queries on these tables share, in order to be able to check for equi-joins. We call the join operator, passing these 2 lists to it.

Since we implement left-deep join tree, the results are stored always in the first table. Hence, the list of joins for it needs updated. We remove the join conditions that we just checked, and add the conditions that we didn't check from the other table. The table's schema is also updated.

You can find the code for list of join conditions creation, with comments explaining it as well, in the decideConditionType method of the class SelectStatement, starting in line 80. For list update, the code can be found in the methods getTablesJoinConds (starting at line 180), getRestOfJoinConds (starting at line 201) and generateAndExecuteQueryPlan (starting at line 258).

You can find the code for establishing the common variables between 2 tables, with comments explaining it as well, in the method generateAndExecuteQueryPlan, starting at line 243.

GETNEXTUPLE METHOD
Given that DistinctOperator needs to read all of the output from its child in order to sort and eliminate duplicates, respectively, this class will not have any call made to their getNextTuple method, which is why there is no implementation for it in there.

RESET METHOD
The reset method is used to reset the buffer reader so that a subsequent call will return the first tuple. This method
is only called by the join operator when performing the Simple Nested Loop Join, which is why operator created afterwards in the
query plan, such as DistinctOperator, will not have their reset method called. Therefore, the reset method of this class is empty and there is no implementation.

TASK 3 NOT IMPLEMENTED
I did not implement Task 3: Group-By aggregation.