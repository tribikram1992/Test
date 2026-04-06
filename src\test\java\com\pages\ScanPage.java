package com.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tests.BaseTest;
import com.utils.ExcelReader;
import com.utils.JavaScriptExecutorUtil;
import com.utils.TestData;


public class ScanPage extends BasePage {

    // If TestData is not thread-safe, consider using ThreadLocal<TestData>
    @SuppressWarnings("unused")
	private TestData td;
    
    @FindBy(xpath="//a[contains(text(),'Change Disposition')]")
    WebElement changeDispostion;
    
    @FindBy(xpath = "//frame[@name='head']")
	WebElement headerFrame;
    
    @FindBy(xpath = "//frame[@name='body']")
	WebElement bodyFrame;
    
    @FindBy(id="serialNumber")
    WebElement serialNumber;
    
    @FindBy(id="btnTraceIdAdd")
    WebElement addButton;
    
    @FindBy(id="dispositionId")
    WebElement dispositionId;
    
    @FindBy(id="btnSet")
    WebElement setButton;
    
    @FindBy(xpath="//span[contains(text(),'Proceed')]")
    WebElement proceedButton;
    
    @FindBy(id="site_id")
    WebElement siteId;

    public ScanPage(WebDriver webDriver) {
        super(BaseTest.getDriver()); // Pass the ThreadLocal WebDriver to BasePage
        this.td = new TestData(); // Create a new instance for each thread
        PageFactory.initElements(getDriver(), this); // Initialize elements
    }
    
    public boolean validateScanPageStatus() {
    	boolean status = changeDispostion.isDisplayed();
    	getDriver().switchTo().defaultContent();
    	getDriver().switchTo().frame(bodyFrame);
    	return status;
    }
    
    public void clickOnChangeDisposition(String message) {
    	getDriver().switchTo().defaultContent();
    	getDriver().switchTo().frame(headerFrame);
    	click(changeDispostion, message);
    	getDriver().switchTo().defaultContent();
    	getDriver().switchTo().frame(bodyFrame);
    	waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 2);
    }
    
    public void enterSerialNumber(String message) {
    	sendKeys(serialNumber, TracePage.serialNumber, message);
    }
    
    public void clickOnAddButton(String message) {
    	click(addButton, message);
    	scrollToWebElement(getDriver().findElement(By.xpath("//td[contains(text(),'List')]")));
    	try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void selectDispositionId() {
    	selectElementFromDropDownByVisibleText(dispositionId, "Decommissioned");
    }
    
    public void clickOnSetButton(String message) {
    	click(setButton, message);
    	waitForElementToBeDisplayed(getDriver(), proceedButton, 20, 2);
    }
    
    public LoginPage clickOnProceedButton(String message) {
    	click(proceedButton, message);
    	try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	getDriver().switchTo().defaultContent();
    	LoginPage lp = new LoginPage();
    	return lp;
    	//waitForElementToBeDisplayed(getDriver(), getDriver().findElement(By.xpath("//td[contains(text(),'Decommissioned')]")), 20, 2);
    }
    
    public void selectSiteId(String siteValue) {
    	selectElementFromDropDownByVisibleText(siteId, siteValue);
    }
    
    public LoginPage changeDispositionForEuRegressionScenario() {
    	String text = td.getTestData(BaseTest.environment, "dispositionStatus");
    	String[] status = text.split("_");
    	LoginPage lp = new LoginPage();
    	for(int i =0; i<status.length;i++) {
    		selectSiteId("Baxter Lessines - Inventory");
    		sendKeys(serialNumber, SerializePage.euSerialNumber[i], "Enter serial Number status during change disposition"+i+status[i]);
    		click(addButton, "Click on add button during change disposition"+i+status[i]);
    		try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		waitForElementToBeDisplayed(getDriver(), setButton, 20, 3);
    		selectElementFromDropDownByVisibleText(dispositionId, status[i]);
    		click(setButton, "Click on set button during change disposition"+i+status[i]);
    		waitForElementToBeDisplayed(getDriver(), proceedButton, 30, 3);
    		click(proceedButton, "Click on proceed button during change disposition"+i+status[i]);  		
    		try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
    		getDriver().switchTo().defaultContent();
    		boolean status1 = lp.validateHomePageDisplayed();
    		System.out.println(status1);
    		lp.clickOnComplianceTab("Click on compliance Tab during changing disposition for EU"+i);
    		lp.clickOnScanTab("Click on scan Tab during changing disposition for EU"+i);
    		clickOnChangeDisposition("click on change disposition"+i );
    	}
    	getDriver().switchTo().defaultContent();
    	return lp;
    }
    
    public void selectDispositionId(String dispositionType) {
    	selectElementFromDropDownByVisibleText(dispositionId, dispositionType);
    }
    public void clickOnAddButton() {
    	click(addButton, "Click on Add Button");
    	scrollToWebElement(getDriver().findElement(By.xpath("//td[contains(text(),'List')]")));
    	waitForElementToBeDisplayed(getDriver(), getDriver().findElement(By.xpath("//a[@id='anchor_0']/parent::span[not (@style='display: none;')]/a")), 20, 2);
    }
    public LoginPage changeDispositionTo(String siteID, String dispositionType) throws Exception {
		
		getDriver().switchTo().defaultContent();
    	getDriver().switchTo().frame(bodyFrame);
    	waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 2);
    	enterSerialNumber("Enter the trace id to be changed");
    	selectSiteId(siteID);
    	JavaScriptExecutorUtil.waitUntilJavaScriptCompletes(getDriver());
    	selectDispositionId(dispositionType);
    	JavaScriptExecutorUtil.waitUntilJavaScriptCompletes(getDriver());
    	clickOnAddButton();
    	clickOnSetButton("Click on Set button");
    	LoginPage lp = clickOnProceedButton("Click on proceed button");
    	new WebDriverWait(getDriver(), Duration.ofSeconds(10)).until(ExpectedConditions.invisibilityOf(proceedButton));
    	Thread.sleep(5000);
    	return lp;
	}
    
}
