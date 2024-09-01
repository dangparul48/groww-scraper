package com.groww.customds;

import java.util.ArrayList;

public class Table {
	
	ArrayList<Object>[] columns;
	public Table(int colsize)
	{
		columns=new ArrayList[colsize];
		for(int i=0;i<columns.length;i++)
		{
			columns[i]=new ArrayList<Object>();
		}
	}
	
	public void putData(Object... objects) //Using varargs
	{
		if(columns.length!=objects.length)
		{
			throw new IllegalArgumentException("Number of argumnets mismatch to the number of columns in the table");
		}
		for(int i=0;i<columns.length;i++)
		{
			columns[i].add(objects[i]);
		}
	}
	
	public ArrayList<Object> getData(int row)
	{
		ArrayList<Object> data=new ArrayList<Object>();
		for(int i=0;i<columns.length;i++)
		{
			data.add(columns[i].get(row));
		}
		return data;
	}
	public int getSize()
	{
		return columns[0].size();
	}
	
}
