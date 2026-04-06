package com.pages;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;

import com.tests.BaseTest;
import com.utils.JavaScriptExecutorUtil;
import com.utils.TestData;

public class CustomerPage extends BasePage {

	TestData td = new TestData();
	
	public CustomerPage(WebDriver driverInstance) {
		super(BaseTest.getDriver()); // Pass the ThreadLocal WebDriver to BasePage
		this.td = new TestData(); // Create a new instance for each thread
		PageFactory.initElements(getDriver(), this); // Initialize elements
	}

	@FindBy(xpath = "//frame[@name='body']")
	WebElement bodyFrame;
	
	@FindBy(xpath = "//frame[@name='head']")
	WebElement headFrame;
	
	@FindBy(xpath = "//td[@class='header_hdng' and normalize-space()='Customer List']")
	WebElement headingTextCustomerList;
	
	@FindBy(xpath = "//a[@id='entAddNew']")
	WebElement linkAddNewCustomer;
	
	@FindBy(xpath = "//a[@id='showEntSearchId']")
	WebElement linkShowSearch;
	
	@FindBy(xpath = "//select[@id='cmbCusId']")
	WebElement listCustomerIDSearchType;
	
	@FindBy(xpath = "//select[@id='cmbEnterprise']")
	WebElement listCustomerNameSearchType;
	
	@FindBy(xpath = "//select[@id='cmbState']")
	WebElement listStateSearchType;
	
	@FindBy(xpath = "//input[@id='schEntCusId']")
	WebElement inputCustomerIDSearch;
	
	@FindBy(xpath = "//input[@id='schEnterprise']")
	WebElement inputCustomerNameSearch;
	
	@FindBy(xpath = "//input[@id='schEntState']")
	WebElement inputStateSearch;
	
	
	@FindBy(xpath = "//select[@id='cmbCity']")
	WebElement listCitySearchType;
	
	@FindBy(xpath = "//input[@id='schEntCity']")
	WebElement inputCitySearch;

	@FindBy(xpath = "//input[@id='btnSearch']")
	WebElement buttonSearch;
	
	@FindBy(xpath = "//td[@class='header_hdng' and normalize-space()='Customer']")
	WebElement headingTextCustomer;
	
	@FindBy(xpath = "//td[@class='header_hdng' and normalize-space()='View Facility']")
	WebElement headingTextViewFacility;
	
	@FindBy(xpath = "//td[@class='header_hdng' and normalize-space()='Modify Facility']")
	WebElement headingTextModifyFacility;
	
	@FindBy(xpath = "//td[normalize-space()='Facility ID']/following-sibling::td")
	WebElement webElementFacilityID;
	
	@FindBy(xpath = "//td[normalize-space()='Interchange ID Qualifier']/following-sibling::td")
	WebElement webElementInterchangeIDQualifier;
	
	@FindBy(xpath = "//td[normalize-space()='Interchange ID']/following-sibling::td")
	WebElement webElementInterchangeID;
	
	@FindBy(xpath = "//td[text()='Interchange Information']/../../following-sibling::tbody[@id='tb1']/tr/td[count(//td[text()='Interchange Information']/../following-sibling::tr/td[normalize-space()='Interchange ID']/preceding-sibling::td)+1]")
	WebElement webElementInterchangeIDInterchangeInformation;
	
	@FindBy(xpath = "//td[text()='Interchange Information']/../../following-sibling::tbody[@id='tb1']/tr/td[count(//td[text()='Interchange Information']/../following-sibling::tr/td[normalize-space()='Interchange ID Qualifier']/preceding-sibling::td)+1]")
	WebElement webElementInterchangeIDQualifierInterchangeInformation;
	
	@FindBy(xpath = "//input[@id='cancel']")
	WebElement buttonBackFacility;
	
	@FindBy(xpath = "//input[@id='btn_entSave2']")
	WebElement buttonBackCustomer;
	
	@FindBy(xpath = "//td[normalize-space()='Found No Customer!']")
	WebElement webElementNoCustomerFound;
	
	@FindBy(xpath = "//a[@id='lnkAddInterchangeId']")
	WebElement linkAddInterchangeID;
	
	@FindBy(xpath = "//input[@id='locSave4']")
	WebElement buttonSaveModifyFacility;
	

	public void verifyCustomerListPage(String ProductName) {
		getDriver().switchTo().defaultContent();
		waitUntilPageLoad();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), headingTextCustomerList, 30, 3);
		scrollToWebElement(headingTextCustomerList);
		Assert.assertTrue(headingTextCustomerList.isDisplayed());
		ern.enterLog("Customer List page launched");
		
	}
	
	public AddCustomerPage clickOnAddCustomer() {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), linkAddNewCustomer, 30, 3);
		scrollToWebElement(linkAddNewCustomer);
		click(linkAddNewCustomer, "Clicked on Add Customer Link");
		getDriver().switchTo().defaultContent();
		AddCustomerPage acp = new AddCustomerPage(getDriver());
		return acp;
	}
	
	
	
	public void verifyCustomerAdded(String customerID, String searchType) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		if(linkShowSearch.getText().trim().equals("Show Search")) {
			click(linkShowSearch, "Click on Show Search");
		}
		waitForElementToBeDisplayed(getDriver(), buttonSearch, 10, 1);
		scrollToWebElement(inputCustomerIDSearch);
		selectElementFromDropDownByVisibleText(listCustomerIDSearchType, searchType);
		sendKeys(inputCustomerIDSearch, customerID, "Enter Customer ID");
		click(buttonSearch, "Click on button Search");
		List<WebElement> trs = getDriver().findElements(By.xpath("//a[contains(@id,'entName')]/../../../tr"));
		Assert.assertTrue(trs.size()>0,"No Customer exist");
		Boolean check = false;
		WebElement customerActual=null;
		for(WebElement tr : trs) {
			WebElement customer = tr.findElement(By.xpath("./td[(count(//thead//a[normalize-space()='Customer ID']/../preceding-sibling::td)+1)]"));
			if(customer.getText().trim().equals(customerID)) {
				check= true;
				customerActual =customer; 
				break;
			}
		}
		Assert.assertTrue(check, "No Customer found with the provided customer ID ");
		scrollToWebElement(customerActual);
		ern.enterLogAndCapture("Customer is found", highlightElement(customerActual, "Customer with ID "+customerID));
		
	}

	public void deleteCustomer(String customerID) {
		String searchType = "=";
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		if(linkShowSearch.getText().trim().equals("Show Search")) {
			click(linkShowSearch, "Click on Show Search");
		}
		waitForElementToBeDisplayed(getDriver(), buttonSearch, 10, 1);
		scrollToWebElement(inputCustomerIDSearch);
		selectElementFromDropDownByVisibleText(listCustomerIDSearchType, searchType);
		sendKeys(inputCustomerIDSearch, customerID, "Enter Customer ID");
		click(buttonSearch, "Click on button Search");
		List<WebElement> trs = getDriver().findElements(By.xpath("//a[contains(@id,'entName')]/../../../tr"));
		Assert.assertTrue(trs.size()>0,"No Customer exist");
		Boolean check = false;
		WebElement customerActual=null;
		for(int i=0;i<trs.size();i++) {
			WebElement customer = getDriver().findElement(By.xpath("//a[contains(@id,'entName')]/../../../tr["+(i+1)+"]/td[(count(//thead//a[normalize-space()='Customer ID']/../preceding-sibling::td)+1)]"));
			if(customer.getText().trim().equals(customerID)) {
				check= true;
				customerActual =customer; 
				break;
			}
		}
		Assert.assertTrue(check, "No Customer found with the provided customer ID ");
		scrollToWebElement(customerActual);
		
		WebElement deleteButton = getDriver().findElement(By.xpath("//a[contains(@id,'entName')]/../../../tr/td[(count(//thead//a[normalize-space()='Customer ID']/../preceding-sibling::td)+1) and text()='"+customerID+"']/../td/a[@title='Delete']"));
		click(deleteButton, "Click on Delete Customer");
		Alert alert = getDriver().switchTo().alert();
		alert.accept();
		
		
	}

	public void verifyCustomerAndInterChangeID(String customerID) throws Exception {
		String searchType = "=";
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		if(linkShowSearch.getText().trim().equals("Show Search")) {
			click(linkShowSearch, "Click on Show Search");
		}
		waitForElementToBeDisplayed(getDriver(), buttonSearch, 10, 1);
		scrollToWebElement(inputCustomerIDSearch);
		selectElementFromDropDownByVisibleText(listCustomerIDSearchType, searchType);
		sendKeys(inputCustomerIDSearch, customerID, "Enter Customer ID");
		click(buttonSearch, "Click on button Search");
		JavaScriptExecutorUtil.waitUntilJavaScriptCompletesByDuration(getDriver(),10);
		List<WebElement> trs = getDriver().findElements(By.xpath("//a[contains(@id,'entName')]/../../../tr"));
		Assert.assertTrue(trs.size()>0,"No Customer exist");
		Boolean check = false;
		WebElement customerActual=null;
		for(WebElement tr : trs) {
			WebElement customer = tr.findElement(By.xpath("./td[(count(//thead//a[normalize-space()='Customer ID']/../preceding-sibling::td)+1)]"));
			if(customer.getText().trim().equals(customerID)) {
				check= true;
				customerActual =customer; 
				break;
			}
		}
		Assert.assertTrue(check, "No Customer found with the provided customer ID ");
		scrollToWebElement(customerActual);
		ern.enterLogAndCapture("Customer is found", highlightElement(customerActual, "Customer with ID "+customerID));
		
		WebElement customerLink = getDriver().findElement(By.xpath("//a[contains(@id,'entName')]/../../../tr/td[(count(//thead//a[normalize-space()='Customer Name']/../preceding-sibling::td)+1) and text()='"+customerID+"']/../td/a[contains(@id,'entName')]"));
		click(customerLink, "Click on Customer Link");
		waitForElementToBeDisplayed(getDriver(), headingTextCustomer, 10, 1);
		
		List<WebElement> facilityLink = getDriver().findElements(By.xpath("//a[contains(@id,'entFac')]/../../../tr/td[(count(//thead//a[normalize-space()='Facility Name']/../preceding-sibling::td)+1) and text()='"+customerID+"']/../td/a[contains(@id,'entFac')]"));
		if(facilityLink.size()==0) {
			Assert.assertTrue(false, "Facility is present for the Customer");
		}
		click(facilityLink.get(0), "CLick on customer facility");
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), headingTextViewFacility, 10, 1);
		
		/*
		 * String facilityID = webElementFacilityID.getText().trim(); String
		 * interchangeID=
		 * webElementInterchangeIDInterchangeInformation.getText().trim(); String
		 * InterchangeIDQualifier=
		 * webElementInterchangeIDQualifierInterchangeInformation.getText().trim();
		 */
		
		
		for (int i = 0; i < 2; i++) {
			JavascriptExecutor js = (JavascriptExecutor) getDriver();
			js.executeScript("arguments[0].style.border='3px solid orange'", webElementFacilityID);
		}
		for (int i = 0; i < 2; i++) {
			JavascriptExecutor js = (JavascriptExecutor) getDriver();
			js.executeScript("arguments[0].style.border='3px solid orange'", webElementInterchangeIDInterchangeInformation);
		}
		
		((JavascriptExecutor)getDriver()).executeScript("document.body.style.zoom = '50%';");
		scrollToWebElement(headingTextViewFacility);
		ern.enterLogAndCapture("Facility Details with Interchange ID", highlightElement(webElementInterchangeIDQualifierInterchangeInformation, "Facility Details with Interchange ID"));
		
		((JavascriptExecutor)getDriver()).executeScript("document.body.style.zoom = '100%';");
		
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		scrollToWebElement(buttonBackFacility);
		click(buttonBackFacility, "Click on Back");
		waitForElementToBeDisplayed(getDriver(), headingTextCustomer, 10, 1);
		
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		scrollToWebElement(buttonBackCustomer);
		click(buttonBackCustomer, "Click on Back");
		waitForElementToBeDisplayed(getDriver(), headingTextCustomerList, 10, 1);
		
	}

	public void verifyCustomerDeleted(String customerID) throws Exception {
		
		String searchType = "=";
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		if(linkShowSearch.getText().trim().equals("Show Search")) {
			click(linkShowSearch, "Click on Show Search");
		}
		waitForElementToBeDisplayed(getDriver(), buttonSearch, 10, 1);
		scrollToWebElement(inputCustomerIDSearch);
		selectElementFromDropDownByVisibleText(listCustomerIDSearchType, searchType);
		sendKeys(inputCustomerIDSearch, customerID, "Enter Customer ID");
		click(buttonSearch, "Click on button Search");
		JavaScriptExecutorUtil.waitUntilJavaScriptCompletesByDuration(getDriver(), 5);
		Assert.assertTrue(waitForVisibility(webElementNoCustomerFound), "Customer is not deleted");
		ern.enterLogAndCapture("Customer is deleted", highlightElement(webElementNoCustomerFound, "Customer is deleted"));
	}

	public void addDeletedCustomerIDInActiveCustomerInterChangeID(String activeCustomer, String deletedCustomer) throws Exception {
		
		String searchType = "=";
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		if(linkShowSearch.getText().trim().equals("Show Search")) {
			click(linkShowSearch, "Click on Show Search");
		}
		waitForElementToBeDisplayed(getDriver(), buttonSearch, 10, 1);
		scrollToWebElement(inputCustomerIDSearch);
		selectElementFromDropDownByVisibleText(listCustomerIDSearchType, searchType);
		sendKeys(inputCustomerIDSearch, activeCustomer, "Enter Customer ID");
		click(buttonSearch, "Click on button Search");
		JavaScriptExecutorUtil.waitUntilJavaScriptCompletesByDuration(getDriver(),10);
		List<WebElement> trs = getDriver().findElements(By.xpath("//a[contains(@id,'entName')]/../../../tr"));
		Assert.assertTrue(trs.size()>0,"No Customer exist");
		Boolean check = false;
		WebElement customerActual=null;
		for(WebElement tr : trs) {
			WebElement customer = tr.findElement(By.xpath("./td[(count(//thead//a[normalize-space()='Customer ID']/../preceding-sibling::td)+1)]"));
			if(customer.getText().trim().equals(activeCustomer)) {
				check= true;
				customerActual =customer; 
				break;
			}
		}
		Assert.assertTrue(check, "No Customer found with the provided customer ID ");
		scrollToWebElement(customerActual);
		
		
		WebElement customerLink = getDriver().findElement(By.xpath("//a[contains(@id,'entName')]/../../../tr/td[(count(//thead//a[normalize-space()='Customer Name']/../preceding-sibling::td)+1) and text()='"+activeCustomer+"']/../td/a[contains(@id,'entName')]"));
		click(customerLink, "Click on Customer Link");
		waitForElementToBeDisplayed(getDriver(), headingTextCustomer, 10, 1);
		
		List<WebElement> facilityEditActionLink = getDriver().findElements(By.xpath("//a[contains(@id,'entFac')]/../../../tr/td[(count(//thead//a[normalize-space()='Action']/../preceding-sibling::td)+1) and text()='"+activeCustomer+"']/../td/a[contains(@id,'entFacModify')]"));
		if(facilityEditActionLink.size()==0) {
			Assert.assertTrue(false, "Facility is present for the Customer");
		}
		click(facilityEditActionLink.get(0), "CLick on customer Edit facility");
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), headingTextModifyFacility, 10, 1);
		
		scrollToWebElement(linkAddInterchangeID);
		click(linkAddInterchangeID, "Click on Link Add Interchange ID");
		JavaScriptExecutorUtil.waitUntilJavaScriptCompletesByDuration(getDriver(), 10);
		
		WebElement inputIntercChangeIDQualifier = getDriver().findElement(By.xpath("(//input[@name='interChangeIDQualifier'])[2]"));
		WebElement inputIntercChangeID = getDriver().findElement(By.xpath("(//input[@name='interChangeID'])[2]"));
		
		scrollToWebElement(inputIntercChangeIDQualifier);
		sendKeys(inputIntercChangeIDQualifier, "ZZ1", "Enter IntercChange ID Qualifier");
		sendKeys(inputIntercChangeID, deletedCustomer, "Enter IntercChange ID as Deleted Customer ID");
		
		scrollToWebElement(buttonSaveModifyFacility);
		click(buttonSaveModifyFacility, "Click on Save button");
		
		JavaScriptExecutorUtil.waitUntilJavaScriptCompletesByDuration(getDriver(), 10);
		
		waitForElementToBeDisplayed(getDriver(), headingTextCustomer, 10, 1);
		
		List<WebElement> facilityLink = getDriver().findElements(By.xpath("//a[contains(@id,'entFac')]/../../../tr/td[(count(//thead//a[normalize-space()='Facility Name']/../preceding-sibling::td)+1) and text()='"+activeCustomer+"']/../td/a[contains(@id,'entFac')]"));
		if(facilityLink.size()==0) {
			Assert.assertTrue(false, "Facility is present for the Customer");
		}
		click(facilityLink.get(0), "CLick on customer facility");
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), headingTextViewFacility, 10, 1);
		List<WebElement> interchangeInfoWithDeletedCustomer = getDriver().findElements(By.xpath("//td[normalize-space()='Interchange Information']/../../following-sibling::tbody/tr/td[normalize-space()='"+deletedCustomer+"']/.."));
		Assert.assertTrue(interchangeInfoWithDeletedCustomer.size()>0,"Deleted Customer ID is not added to the Intercgange Information");
		scrollToWebElement(getDriver().findElement(By.xpath("//td[normalize-space()='Interchange Information']")));
		ern.enterLogAndCapture("Interchange ID updated as the Deleted Customer ID", highlightElement(interchangeInfoWithDeletedCustomer.get(0), "Interchange ID updated as the Deleted Customer ID"));
		
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		scrollToWebElement(buttonBackFacility);
		click(buttonBackFacility, "Click on Back");
		waitForElementToBeDisplayed(getDriver(), headingTextCustomer, 10, 1);
		
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		scrollToWebElement(buttonBackCustomer);
		click(buttonBackCustomer, "Click on Back");
		waitForElementToBeDisplayed(getDriver(), headingTextCustomerList, 10, 1);
		
	}
	
	
	

}
