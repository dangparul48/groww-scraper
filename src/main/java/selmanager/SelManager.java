package selmanager;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class SelManager {
	
	public static void main(String args[]) {
		System.setProperty("webdriver.chrome.driver", "E://imgtotext");
		WebDriver driver = new ChromeDriver();
	}

}
