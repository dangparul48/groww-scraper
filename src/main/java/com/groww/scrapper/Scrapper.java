package com.groww.scrapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.groww.customds.Table;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Hello world!
 *
 */
public class Scrapper 
{
	private static String defaultWindow = "";
	private final static String INSERT_MUTUALFUND = "INSERT INTO MUTUALFUNDS (NAME, TYPE, RATING, FUNDSIZE, EXPENSERATIO) VALUES (?, ?, ?, ?, ?)";
	private final static String GET_MUTUALFUND = "SELECT * FROM MUTUALFUNDS WHERE NAME = ?";
	private final static String INSERT_MUTUALFUND_HOLDINGS = "INSERT INTO HOLDINGS (ID, ASSETNAME, SECTOR, SHARE) VALUES (?, ?, ?, ?)";
    public static void main( String[] args ) throws InterruptedException
    {
    	WebDriverManager.chromedriver().setup();
    	ChromeDriver driver = new ChromeDriver();
    	driver.get("https://groww.in/mutual-funds/filter");
    	driver.manage().window().maximize();
    	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    	Actions actions = new Actions(driver);
    	driver.findElement(By.xpath("//span[text()='Other']")).click();
    	driver.findElement(By.xpath("//span[text()='Other']/following::*[local-name()='svg' and @class='st16SmallIcon'][1]")).click();
    	//actions.click(driver.findElement(By.xpath("//span[text()='Equity']/following::*[local-name()='svg' and @class='st16SmallIcon'][1]"))).perform();
    	defaultWindow = driver.getWindowHandle();
    	wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[text()='Index']")))
    	.click();
    	Thread.sleep(1000);
    	int totalPages = Integer.parseInt(driver.findElement(By.xpath("//div[@class='pg1231Box fs15'][last()]")).getText());
    	System.out.println("Total pages: " + totalPages);
    	for(int i=0;i<totalPages;i++) {
//    		driver.findElement(By.xpath("//div[@class='pg1231Icon pg1231Box pg1231Enable'][2]")).click();
//    		Thread.sleep(1000);
    		List<WebElement> mutualFundLinks = driver.findElements(By.xpath("//tr[@class='f22Card']/a"));
    		for(int j=0;j<mutualFundLinks.size();j++) {
    			mutualFundLinks.get(j).click();
    			switchToNewWindow(driver);
    			String fundName = wait.until(ExpectedConditions
    					.visibilityOfElementLocated(By.xpath("//h1[contains(@class, 'SchemeName')]")))
    					.getText();
    			String rating = driver.findElement(By.xpath("//td[text()='Rating']//following::td[1]")).getText();
    			String fundSize = driver.findElement(By.xpath("//td[text()='Fund size']//following::td[1]")).getText();
    			String expenseRatio = driver.findElement(By.xpath("//h3[contains(text(), 'Expense ratio:')]")).getText();
    			System.out.println(String.format("Fund name: %s, Rating: %s, Fund size: %s, %s", fundName, rating, fundSize, expenseRatio));
    			insertMutualFundRecord(fundName, "Index Fund", rating, fundSize, expenseRatio);
    			int fundId = getMutualFundId(fundName);
    			
    			driver.findElement(By.xpath("//*[text()='See All']")).click();
    			List<WebElement> holdingRows = driver.findElements(By.xpath("//table[contains(@class, 'holdings101Table')]//tr"));
    			Table table = new Table(4);
    			for(int k=1;k<holdingRows.size();k++) {
    				WebElement holdingRow = holdingRows.get(k);
    				String assetName = holdingRow.findElement(By.xpath(".//td[contains(@class, 'holdings101CompanyName')]")).getText();
    				String sector = holdingRow.findElement(By.xpath(".//td[2]")).getText();
    				String share = holdingRow.findElement(By.xpath(".//td[4]")).getText();
    				System.out.println(String.format("Holding name: %s, Sector: %s, Assets: %s", assetName, sector, share));
    				table.putData(fundId, assetName, sector, share);
    			}
    			insertMutualFundHoldings(table);
    			driver.close();
    			switchToDefaultWindow(driver);
    		}
    		//if(totalPages - i >= 1) {
    			
    		//}
    		
    	}
    	driver.quit();
    }
    
    private static void switchToNewWindow(WebDriver driver) {
    	String currentWindow = driver.getWindowHandle();
    	for(String windowHandle: driver.getWindowHandles()) {
    		if(!windowHandle.equals(currentWindow)) {
    			driver.switchTo().window(windowHandle);
    		}
    	}
    }
    
    private static void switchToDefaultWindow(WebDriver driver) {
    	driver.switchTo().window(defaultWindow);
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
    
    private static int getMutualFundId(String fundName) {
    	Connection con = getConnectionToDB();
    	int id = -1;
    	try {
    		PreparedStatement stat = con.prepareStatement(GET_MUTUALFUND);
    		stat.setString(1, fundName);
    		ResultSet rs = stat.executeQuery();
    		while(rs.next()) {
    			id = rs.getInt(1);
    		}
    		con.close();
    	} catch(SQLException sqle) {
    		sqle.printStackTrace();
    	}
    	finally {
    		con = null;
    	}
    	return id;
    }
    
    private static void insertMutualFundRecord(String fundName, String type, String rating, String fundSize, String expenseRatio) {
    	Connection con = getConnectionToDB();
    	int id = -1;
    	try {
    		PreparedStatement stat = con.prepareStatement(INSERT_MUTUALFUND);
    		stat.setString(1, fundName);
    		stat.setString(2, type);
    		stat.setString(3, rating);
    		stat.setString(4, fundSize);
    		stat.setString(5, expenseRatio);
    		stat.execute();
    		con.close();
    	} catch(SQLException sqle) {
    		sqle.printStackTrace();
    	}
    	finally {
    		con = null;
    	}
    }
    
    private static void insertMutualFundHoldings(Table table) {
    	Connection con = getConnectionToDB();
    	int id = -1;
    	try {
    		PreparedStatement stat = con.prepareStatement(INSERT_MUTUALFUND_HOLDINGS);
    		for(int i=0;i<table.getSize();i++) {
    			List data = table.getData(i);
    			stat.setInt(1, (Integer) data.get(0));
        		stat.setString(2, data.get(1).toString());
        		stat.setString(3, data.get(2).toString());
        		stat.setString(4, data.get(3).toString());
        		stat.addBatch();
    		}
    		
    		stat.executeBatch();
    		con.close();
    	} catch(SQLException sqle) {
    		sqle.printStackTrace();
    	}
    	finally {
    		con = null;
    	}
    }
    
    
    
}

