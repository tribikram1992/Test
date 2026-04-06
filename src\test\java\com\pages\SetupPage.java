package com.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;

import com.tests.BaseTest;
import com.utils.TestData;

public class SetupPage extends BasePage {
	
	private TestData td;
	
	public SetupPage(WebDriver webDriver) {
        super(BaseTest.getDriver()); // Pass the ThreadLocal WebDriver to BasePage
        this.td = new TestData(); // Create a new instance for each thread
        PageFactory.initElements(getDriver(), this); // Initialize elements
    }
	
	@FindBy(xpath = "//frame[@name='body']")
	WebElement bodyFrame;
	
	@FindBy(xpath = "//frame[@name='head']")
	WebElement headFrame;
	
	@FindBy(xpath="//td[contains(text(),'Corporate')]")
	WebElement corporateText;
	
	@FindBy(xpath="//a[contains(text(),'Baxter Healthcare Corporation')]")
	WebElement bhcHyperlink;
	
	@FindBy(xpath="//div[@id='admin']/a[@id='customer']")
	WebElement linkCustomerSetup;
	
	@FindBy(xpath="//td[contains(text(),'Interchange Information')]")
	WebElement interchangeInformationText;
	
	@FindBy(xpath="//a[contains(text(),'Baxter Healthcare Catano')]")
	WebElement bcsHyperlink;
	
	@FindBy(xpath="//td[contains(text(),'Facility Information')]")
	WebElement facilityInformationText;
	
	public boolean validateSetupTabStatus() {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), corporateText, 30, 3);
		boolean status = corporateText.isDisplayed();
		return status;
	}
	
	public boolean validateSetupTabCustomerStatus() {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headFrame);
		waitForElementToBeDisplayed(getDriver(), linkCustomerSetup, 30, 3);
		boolean status = linkCustomerSetup.isDisplayed();
		return status;
	}
	
	public void clickOnFacilityLink(String message) {
		waitForElementToBeDisplayed(getDriver(), bcsHyperlink, 30, 3);
		scrollToWebElement(bcsHyperlink);
		click(bcsHyperlink, message);
	}
	
	public void clickOnFacilityLinkForKorea(String message) {
		waitForElementToBeDisplayed(getDriver(), bhcHyperlink, 30, 3);
		scrollToWebElement(bhcHyperlink);
		click(bhcHyperlink, message);
	}
	
	public CustomerPage clickOnCustomerLink(String message) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headFrame);
		waitForElementToBeDisplayed(getDriver(), linkCustomerSetup, 30, 3);
		scrollToWebElement(linkCustomerSetup);
		click(linkCustomerSetup, message);
		CustomerPage cp = new CustomerPage(getDriver());
		getDriver().switchTo().defaultContent();
		return cp;
	}
	
	public LoginPage validateKrggInformation() {
		waitForElementToBeDisplayed(getDriver(), interchangeInformationText, 20, 2);
		scrollToWebElement(interchangeInformationText);
		WebElement el = getDriver().findElement(By.xpath("//td[contains(text(),'KRGG')]/following-sibling::td[contains(text(),'urn:epc:id:sgln:88061015.0001.0')]"));
		String text = getAttribute(el, "class", "Checking KRGG value");
		Assert.assertTrue(el.isDisplayed(), "KRGG value is not displayed");
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}
	
	public void clickOnFacilityLinkForSwiss(String message) {
		waitForElementToBeDisplayed(getDriver(), bcsHyperlink, 30, 3);
		scrollToWebElement(bcsHyperlink);
		click(bcsHyperlink, message);
	}
	
	public LoginPage validateSwissInformation() {
		waitForElementToBeDisplayed(getDriver(), facilityInformationText, 20, 2);
		scrollToWebElement(facilityInformationText);
		WebElement el1 = getDriver().findElement(By.xpath("//td[contains(text(),'Facility Name ')]/following-sibling::td[contains(text(),'Baxter Healthcare Catano')]"));
		String text1 = getAttribute(el1, "class", "Checking facility information value for Swiss Log");
		Assert.assertTrue(el1.isDisplayed(), "Baxter Healthcare Catano value is not displayed");
		scrollToWebElement(interchangeInformationText);
		WebElement el = getDriver().findElement(By.xpath("//td[contains(text(),'SWISSLOG')]/following-sibling::td[contains(text(),'EPCIS-SWLCA')]"));
		String text = getAttribute(el, "class", "Checking EPCIS-SWLCA value");
		Assert.assertTrue(el.isDisplayed(), "EPCIS-SWLCA value is not displayed");
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

}
