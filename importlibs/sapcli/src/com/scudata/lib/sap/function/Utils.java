package com.scudata.lib.sap.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.scudata.common.RQException;
import com.scudata.dm.DataStruct;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.Table;

public class Utils {
	public static String[] objectArray2StringArray(Object[] objs) {
		return Arrays.asList(objs).toArray(new String[0]);
	}

	// 是否合法的sql语句.
	public static boolean isLegalSql(String strSql) {
		String span = strSql.toUpperCase();// 测试用sql语句
		System.out.println(span);
		String column = "(\\w+\\s*(\\w+\\s*){0,1})";// 一列的正则表达式 匹配如 product p
		String columns = column + "(,\\s*" + column + ")*"; // 多列正则表达式 匹配如
															// product
															// p,category
															// c,warehouse w
		// 一列的正则表达式匹配如a.product　p
		String ownerenable = "((\\w+\\.){0,1}\\w+\\s*(\\w+\\s*){0,1})";
		// 多列正则表达式匹配如a.product p,a.category c,b.warehouse w              
		String ownerenables = ownerenable + "(,\\s*" + ownerenable + ")*";
		String from = "FROM\\s+" + columns;
		// 条件的正则表达式匹配如a=b或a　is　b..
		String condition = "(\\w+\\.){0,1}\\w+\\s*(=|LIKE|IS)\\s*'?(\\w+\\.){0,1}[\\w%]+'?";
		String conditions = condition + "(\\s+(AND|OR)\\s*" + condition
				+ "\\s*)*";// 多个条件 匹配如 a=b and c like 'r%' or d is null
		String where = "(WHERE\\s+" + conditions + "){0,1}";
		String pattern = "SELECT\\s+(\\*|" + ownerenables + "\\s+" + from
				+ ")\\s+" + where + "\\s*"; // 匹配最终sql的正则表达式
		System.out.println(pattern);// 输出正则表达式

		boolean bRet = span.matches(pattern);// 是否比配

		return bRet;
	}

	// 通过Url获取主机名，port, warehouse
	public static boolean isMatch(String strUrl, String regExp,
			Matcher[] retMatch) {
		// 1.通过Url获取主机名，port, warehouse
		// String regex="hdfs:\\/\\/(.*?):(\\d+)(\\/.*)";
		// 2.通过Url获取主机名，port
		// String regex="hdfs:\\/\\/(.*?):(\\d+)";
		if (strUrl == null || strUrl.isEmpty()) {
			throw new RQException("hive isMatch strUrl is empty");
		}

		if (regExp == null || regExp.isEmpty()) {
			throw new RQException("hive isMatch regExp is empty");
		}

		Pattern p = Pattern.compile(regExp);
		retMatch[0] = p.matcher(strUrl);

		return retMatch[0].find();		
	}

	public static List<List<Object>> resultsConvertDList(List<Object> result) {
		if (result == null ||result.size() == 0){
			return null;
		}
		
		List<List<Object>> lls = new ArrayList<List<Object>>();
		for (Object row : result) {
			String[] sourceStrArray = row.toString().split("\t");
			List list = Arrays.asList(sourceStrArray);
			lls.add(list);
		}

		return lls;
	}

	public static void testPrintTable(Table table) {
		if (table == null)
			return;
		System.out.println("size = " + table.length());

		DataStruct ds = table.dataStruct();
		String[] fields = ds.getFieldNames();
		int i = 0;
		// print colNames;
		for (i = 0; i < fields.length; i++) {
			System.out.print(fields[i] + "\t");
		}
		System.out.println();
		// print tableData
		for (i = 0; i < table.length(); i++) {
			BaseRecord rc = table.getRecord(i + 1);
			Object[] objs = rc.getFieldValues();
			for (Object o : objs) {
				System.out.printf(o + "\t");
			}
			System.out.println();
		}
	}
}
