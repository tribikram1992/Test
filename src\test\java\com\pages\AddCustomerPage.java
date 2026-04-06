package com.pages;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;

import com.fasterxml.jackson.databind.deser.Deserializers.Base;
import com.tests.BaseTest;
import com.utils.TestData;

public class AddCustomerPage extends BasePage {

	TestData td = new TestData();

	public AddCustomerPage(WebDriver driverInstance) {
		super(BaseTest.getDriver()); // Pass the ThreadLocal WebDriver to BasePage
		this.td = new TestData(); // Create a new instance for each thread
		PageFactory.initElements(getDriver(), this); // Initialize elements
	}

	

	@FindBy(xpath = "//frame[@name='body']")
	WebElement bodyFrame;

	@FindBy(xpath = "//frame[@name='head']")
	WebElement headFrame;

	@FindBy(xpath = "//td[@class='header_hdng' and normalize-space()='Add Customer']")
	WebElement headingTextAddCustomer;
	
	@FindBy(xpath = "//input[@id='entName']")
	WebElement inputCustomerName;
	
	@FindBy(xpath = "//select[@id='partnerTypeCode']")
	WebElement listIdentificationType;
	
	@FindBy(xpath = "//input[@id='entAddrLine12']")
	WebElement inputAddressLine1;
	
	@FindBy(xpath = "//input[@id='entAddrLine22']")
	WebElement inputAddressLine2;
	
	@FindBy(xpath = "//input[@id='entCityName']")
	WebElement inputCity;
	
	@FindBy(xpath = "//select[@id='entState']")
	WebElement selectState;
	
	@FindBy(xpath = "//input[@id='entZip']")
	WebElement inputZipCode;
	
	@FindBy(xpath = "//select[@id='entCountry']")
	WebElement listCountry;
	
	@FindBy(xpath = "//input[@type='radio' and @id='authorizedTradingPartner1']")
	WebElement radioIsAuthorizedPartnerYes;
	
	@FindBy(xpath = "//input[@type='radio' and @id='authorizedTradingPartner2']")
	WebElement radioIsAuthorizedPartnerNo;
	
	@FindBy(xpath = "//input[@id='extEntId']")
	WebElement inputID;
	
	@FindBy(xpath = "//input[@id='entSave']")
	WebElement buttonSave;
	
	@FindBy(xpath = "//input[@id='cancel']")
	WebElement buttonCancel;

	public void verifyAddCuctomerPage(String ProductName) {
		getDriver().switchTo().defaultContent();
		waitUntilPageLoad();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), headingTextAddCustomer, 30, 3);
		scrollToWebElement(headingTextAddCustomer);
		Assert.assertTrue(headingTextAddCustomer.isDisplayed());
		ern.enterLog("Add Customer page launched");

	}

	public Map<String, String> getCustomer(String CustomerNumber) throws FileNotFoundException {
		
		String customerName = (String) td.getTestDataByPath(BaseTest.environment, "TestCase.SeP_RTS_OQ_SoftDeleteTest."+CustomerNumber+".CustomerName"); 
		String identificationType = (String) td.getTestDataByPath(BaseTest.environment, "TestCase.SeP_RTS_OQ_SoftDeleteTest."+CustomerNumber+".IdentificationType");;
		String addressLine1 = (String) td.getTestDataByPath(BaseTest.environment, "TestCase.SeP_RTS_OQ_SoftDeleteTest."+CustomerNumber+".AddressLine1");;
		String addressLine2 = (String) td.getTestDataByPath(BaseTest.environment, "TestCase.SeP_RTS_OQ_SoftDeleteTest."+CustomerNumber+".AddressLine2");;
		String city = (String) td.getTestDataByPath(BaseTest.environment, "TestCase.SeP_RTS_OQ_SoftDeleteTest."+CustomerNumber+".City");;
		String state = (String) td.getTestDataByPath(BaseTest.environment, "TestCase.SeP_RTS_OQ_SoftDeleteTest."+CustomerNumber+".State");;
		String zipCode = (String) td.getTestDataByPath(BaseTest.environment, "TestCase.SeP_RTS_OQ_SoftDeleteTest."+CustomerNumber+".ZipCode");;
		String country = (String) td.getTestDataByPath(BaseTest.environment, "TestCase.SeP_RTS_OQ_SoftDeleteTest."+CustomerNumber+".Country");;
		String isAuthorizedTradingPartner = (String) td.getTestDataByPath(BaseTest.environment, "TestCase.SeP_RTS_OQ_SoftDeleteTest."+CustomerNumber+".IsAuthorizedTradingPartner");;
		String id = ((long) (Math.random() * (999 - 550 + 1)) + 550)+RandomStringUtils.randomNumeric(7);
		
		Map<String, String> customer = new HashMap<String, String>();
		customer.put("CustomerName", customerName);
		customer.put("IdentificationType", identificationType);
		customer.put("AddressLine1", addressLine1);
		customer.put("AddressLine2", addressLine2);
		customer.put("City", city);
		customer.put("State", state);
		customer.put("ZipCode", zipCode);
		customer.put("Country", country);
		customer.put("IsAuthorizedTradingPartner", isAuthorizedTradingPartner);
		customer.put("ID", id);
		
		return customer;
		
	}

	public String addCuctomerDetailsAndSave(Map<String, String> customer) throws Exception {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		scrollToWebElement(inputCustomerName);
		if(customer.get("CustomerName").equals(null)) {
			throw new Exception ("Customer Name is Mandatory");
		}
		else {
			sendKeys(inputCustomerName, customer.get("CustomerName"), "Enter Customer Name");
		}
		
		
		scrollToWebElement(listIdentificationType);
		if(customer.get("IdentificationType").equals(null)) {
			throw new Exception ("Identification Type is Mandatory");
		}
		else {
			selectElementFromDropDownByVisibleText(listIdentificationType, customer.get("IdentificationType"));
		}
		
		scrollToWebElement(inputAddressLine1);
		if(customer.get("AddressLine1").equals(null)) {
			throw new Exception ("Address Line 1 is Mandatory");
		}
		else {
			sendKeys(inputAddressLine1, customer.get("AddressLine1"), "Enter Address Line1");
			if(customer.get("AddressLine2")!=null) {
				sendKeys(inputAddressLine2, customer.get("AddressLine2"), "Enter Address Line2");
			}
		}

		scrollToWebElement(inputCity);
		if(customer.get("City").equals(null)) {
			throw new Exception ("City is Mandatory");
		}
		else {
			sendKeys(inputCity, customer.get("City"), "Enter City");
		}
		
		scrollToWebElement(selectState);
		if(customer.get("State").equals(null)) {
			throw new Exception ("State is Mandatory");
		}
		else {
			selectElementFromDropDownByVisibleText(selectState, customer.get("State"));
		}
		
		scrollToWebElement(inputZipCode);
		if(customer.get("ZipCode").equals(null)) {
			throw new Exception ("Customer Name is Mandatory");
		}
		else {
			sendKeys(inputZipCode, customer.get("ZipCode"), "Enter Zip Code");
		}
		
		scrollToWebElement(listCountry);
		if(customer.get("Country").equals(null)) {
			throw new Exception ("Country is Mandatory");
		}
		else {
			selectElementFromDropDownByVisibleText(listCountry, customer.get("Country"));
		}
		
		scrollToWebElement(radioIsAuthorizedPartnerYes);
		if(customer.get("IsAuthorizedTradingPartner").equals(null)) {
			throw new Exception ("Is Authorized Partner is Mandatory");
		}
		else {
			if(customer.get("IsAuthorizedTradingPartner").equalsIgnoreCase("yes")) {
				click(radioIsAuthorizedPartnerYes, "Click on radio button Authorized Partner Yes");
			}
			else if(customer.get("IsAuthorizedTradingPartner").equalsIgnoreCase("no")) {
				click(radioIsAuthorizedPartnerNo, "Click on radio button Authorized Partner No");
			}
		}
		
		scrollToWebElement(inputID);
		if(customer.get("ID").equals(null)) {
			throw new Exception ("ID is Mandatory");
		}
		else {
			sendKeys(inputID, customer.get("ID"), "Enter ID");
		}
		
		scrollToWebElement(buttonSave);
		click(buttonSave, "Click on button Save");
		
		try {
			Alert alert = getDriver().switchTo().alert();
			if(alert.getText().equals("External Enterprise Id already exists")) {
				alert.accept();
				ern.enterLog("External Enterprise Id already exists, updating the customer ID and adding the customer again");
				click(getDriver().findElement(By.xpath("//input[@name='cancel']")), "Click on Cancel");
				customer.put("ID", ((long) (Math.random() * (999 - 550 + 1)) + 550)+RandomStringUtils.randomNumeric(7));
				addCuctomerDetailsAndSave(customer);
			}
		} catch (Exception e) {
			
		}
		
		return customer.get("ID");

	}
	

}
