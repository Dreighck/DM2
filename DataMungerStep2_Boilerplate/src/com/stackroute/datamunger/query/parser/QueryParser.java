package com.stackroute.datamunger.query.parser;

/*There are total 4 DataMungerTest file:
 * 
 * 1)DataMungerTestTask1.java file is for testing following 4 methods
 * a)getBaseQuery()  b)getFileName()  c)getOrderByClause()  d)getGroupByFields()
 * 
 * Once you implement the above 4 methods,run DataMungerTestTask1.java
 * 
 * 2)DataMungerTestTask2.java file is for testing following 2 methods
 * a)getFields() b) getAggregateFunctions()
 * 
 * Once you implement the above 2 methods,run DataMungerTestTask2.java
 * 
 * 3)DataMungerTestTask3.java file is for testing following 2 methods
 * a)getRestrictions()  b)getLogicalOperators()
 * 
 * Once you implement the above 2 methods,run DataMungerTestTask3.java
 * 
 * Once you implement all the methods run DataMungerTest.java.This test case consist of all
 * the test cases together.
 */

import java.util.*;


public class QueryParser {

	private final QueryParameter queryParameter = new QueryParameter();

	/*
	 * This method will parse the queryString and will return the object of
	 * QueryParameter class
	 */
	public QueryParameter parseQuery(String queryString) {
		queryString = queryString.toLowerCase();
		queryParameter.setFileName(getFileName(queryString));
		queryParameter.setBaseQuery(getBaseQuery(queryString));
		queryParameter.setOrderByFields(getOrderByClause(queryString));
		queryParameter.setGroupByFields(getGroupByFields(queryString));
		queryParameter.setFields(getFields(queryString));
		queryParameter.setAggregateFunctions(getAggregateFunctions(queryString));
		queryParameter.setRestrictions(getRestriction(queryString));
		queryParameter.setLogicalOperators(getLogicalOperators(queryString));
		return queryParameter;
	}


	/*
	 * Extract the name of the file from the query. File name can be found after the
	 * "from" clause.
	 */
	public String getFileName(String queryString){
		return queryString.split("from")[1].split(" ")[1];
	}
	/*
	 * 
	 * Extract the baseQuery from the query.This method is used to extract the
	 * baseQuery from the query string. BaseQuery contains from the beginning of the
	 * query till the where clause
	 */
	public String getBaseQuery(String queryString){
		return queryString.split("order by | group by | where")[0].trim();
	}

	/*
	 * extract the order by fields from the query string. Please note that we will
	 * need to extract the field(s) after "order by" clause in the query, if at all
	 * the order by clause exists.
	 * For eg: select city,winner,team1,team2 from data/ipl.csv order by city
	 * from the query mentioned above, we need to extract "city". Please note that we can have more than one order by fields.
	 */
	public List<String> getOrderByClause(String queryString) {

		List<String> list =null;
		if (queryString.contains(" order by ")) {
			list = new ArrayList<>();
			String[] separate = queryString.split(" from ")[1].split(" order by ");
			for(String s : separate){
				if (s.contains(" group by ")){
					s = s.split(" group by ")[0];
				}
				if (!s.contains(getFileName(queryString))){
					list.add(s);
				}
			}
		}
		return list;
	}

	/*
	 * Extract the group by fields from the query string. Please note that we will
	 * need to extract the field(s) after "group by" clause in the query, if at all
	 * the group by clause exists. For eg: select city,max(win_by_runs) from
	 * data/ipl.csv group by city from the query mentioned above, we need to extract
	 * "city". Please note that we can have more than one group by fields.
	 */
	public List<String> getGroupByFields(String queryString) {
		List<String> list=null;
		if (queryString.contains(" group by ")) {
			list = new ArrayList<>();
			String[] separate = queryString.split(" from ")[1].split(" group by ");
			for(String s : separate){
				if (s.contains(" order by ")){
					s = s.split(" order by ")[0];
				}
				if (!s.contains(getFileName(queryString))){
					list.add(s);
				}
			}
		}
		return list;
	}

	/*
	 * Extract the selected fields from the query string. Please note that we will
	 * need to extract the field(s) after "select" clause followed by a space from
	 * the query string. For eg: select city,win_by_runs from data/ipl.csv from the
	 * query mentioned above, we need to extract "city" and "win_by_runs". Please
	 * note that we might have a field containing name "from_date" or "from_hrs".
	 * Hence, consider this while parsing.
	 */
	public List<String> getFields(String queryString){
		return Arrays.asList(queryString.split("select ")[1].split(" from ")[0].split(","));
	}

	/*
	 * Extract the conditions from the query string(if exists). for each condition,
	 * we need to capture the following:
	 * 1. Name of field 2. condition 3. value
	 * 
	 * For eg: select city,winner,team1,team2,player_of_match from data/ipl.csv
	 * where season >= 2008 or toss_decision != bat
	 * 
	 * here, for the first condition, "season>=2008" we need to capture: 1. Name of
	 * field: season 2. condition: >= 3. value: 2008
	 * 
	 * the query might contain multiple conditions separated by OR/AND operators.
	 * Please consider this while parsing the conditions.
	 * 
	 */
	 public List<Restriction> getRestriction(String queryString){
		 List<Restriction> a = null;
		 String query = queryString;
		 if(queryString.contains(" where ")){
		 	query = queryString.split(" where ")[1].split(" group by | order by ")[0];
		 	a = new ArrayList<>();}
		 String[] conditions = query.split(" or | and");
		 String[] operations = {"<","<=","=",">",">=","!="};
		 for(String s : conditions){
			for(String operation : operations) {
				if(s.contains(operation)) {
					String name = s.split(operation)[0].trim();
					String value = capitalize(s.split(operation)[1].trim().replace("'",""));
					if(name.length()>0&&value.length()>0) {
						Restriction r =
								new Restriction(name, value, operation);
						a.add(r);
					}
				}
			}
		 }
		return a;
	 }

	public static String capitalize(String str) {
		if (str == null || str.length() == 0) return str;
		return str.substring(0, 1).toUpperCase() + str.substring(1);

	}


	/*
	 * Extract the logical operators(AND/OR) from the query, if at all it is
	 * present. For eg: select city,winner,team1,team2,player_of_match from
	 * data/ipl.csv where season >= 2008 or toss_decision != bat and city =
	 * bangalore
	 * 
	 * The query mentioned above in the example should return a List of Strings
	 * containing [or,and]
	 */
	 public List<String> getLogicalOperators(String queryString){

		 List<String> list = null;
		 if (queryString.contains(" or ")||queryString.contains(" and "))
			 list = new ArrayList<>();
		 String[] pieces = queryString.split(" ");
		 String[] operators = {"and","or"};
		 for(String piece : pieces){
			 for(String operator : operators){
				 if(piece.equals(operator)) list.add(operator);
			 }
		 }
		 return list;
	 }



	/*
	 * Extract the aggregate functions from the query. The presence of the aggregate
	 * functions can determined if we have either "min" or "max" or "sum" or "count"
	 * or "avg" followed by opening braces"(" after "select" clause in the query
	 * string. in case it is present, then we will have to extract the same. For
	 * each aggregate functions, we need to know the following:
	 * 1. type of aggregate function(min/max/count/sum/avg)
	 * 2. field on which the aggregate function is being applied.
	 * 
	 * Please note that more than one aggregate function can be present in a query.
	 */
	public List<AggregateFunction> getAggregateFunctions(String queryString) {
		String[] Aggs = {"min(", "max(", "sum(", "count(", "avg("};
		List<AggregateFunction> type = new ArrayList<>();
		String[] query = queryString.split("select ")[1].split(" from ")[0].split(",");
		for(String functions : query) {
			for(String s : Aggs) {
				if (functions.contains(s)) {
					String function = s.substring(0, s.length() - 1);
					String field = functions.replace(s, "").replace(")", "").trim();
					AggregateFunction a = new AggregateFunction(field,function);
					type.add(a);
				}
			}
		}
		return type;
	}
}