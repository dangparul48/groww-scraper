package com.groww.datamodel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.groww.scrapper.DataComparator;

public class Analyser {
	
	private final static String SELECT_FUND_IDS = "SELECT ID FROM MUTUALFUNDS";
	private final static String SELECT_FUND_NAME = "SELECT NAME FROM MUTUALFUNDS WHERE ID = ?";
	private final static String SELECT_HOLDINGS_SHARES = "SELECT ASSETNAME, SHARE FROM HOLDINGS WHERE ID = ?";
	private static Map<String, Float> fund1, fund2;
	
	public static void main(String args[]) throws InterruptedException {
		List<Integer> fundIds = getAllFundIds();
		for(Integer fundId: fundIds) {
			for(Integer nestedFundId: fundIds) {
				if(fundId == nestedFundId) {
					continue;
				}
				
				System.out.println(String.format("\nComparison between %s vs %s \n", getMutualFundName(fundId), getMutualFundName(nestedFundId)));
				
				Thread t1 = new Thread(() -> {
					fund1 = getAllHoldingsByMutualFundId(fundId);
				});
				
				Thread t2 = new Thread(() -> {
					fund2 = getAllHoldingsByMutualFundId(nestedFundId);
				});
				
				t1.start(); t2.start();
				t1.join(); t2.join();
				System.out.println("\nTotal assets universe - " + (fund1.size() + fund2.size()) + "\n");
				DataComparator dc = new DataComparator();
				dc.compareTwoHoldingMaps(fund1, fund2);
				
			}
			break;
		}
		
	}
	
	private static Connection getConnectionToDB() {
		Connection con = null;
		try {
			con = DriverManager.getConnection("jdbc:sqlite:db/mutualfunds.db");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return con;
	}
	
	private static List<Integer> getAllFundIds() {
		List<Integer> ids = new ArrayList<Integer>();
		try {
			Connection con = getConnectionToDB();
			PreparedStatement stat = con.prepareStatement(SELECT_FUND_IDS);
			ResultSet rs = stat.executeQuery();
			while(rs.next()) {
				ids.add(rs.getInt(1));
			}
			con.close();
		} catch(SQLException sqle) {
			sqle.printStackTrace();
		}
		return ids;
	}
	
	 private static String getMutualFundName(int fundId) {
	    	Connection con = getConnectionToDB();
	    	String fundName = "";
	    	try {
	    		PreparedStatement stat = con.prepareStatement(SELECT_FUND_NAME);
	    		stat.setInt(1, fundId);
	    		ResultSet rs = stat.executeQuery();
	    		while(rs.next()) {
	    			fundName = rs.getString(1);
	    		}
	    		con.close();
	    	} catch(SQLException sqle) {
	    		sqle.printStackTrace();
	    	}
	    	finally {
	    		con = null;
	    	}
	    	return fundName;
	 }
	
	private static Map<String, Float> getAllHoldingsByMutualFundId(int id) {
		Map<String, Float> holdingsMap = new HashMap<String, Float>();
		try {
			Connection con = getConnectionToDB();
			PreparedStatement stat = con.prepareStatement(SELECT_HOLDINGS_SHARES);
			stat.setInt(1, id);
			ResultSet rs = stat.executeQuery();
			while(rs.next()) {
				String share = rs.getString(2);
				holdingsMap.put(rs.getString(1), Float.parseFloat(share.substring(0, share.length()-1)));
			}
			con.close();
		} catch(SQLException sqle) {
			sqle.printStackTrace();
		}
		return holdingsMap;
	}
	
	
	

}
