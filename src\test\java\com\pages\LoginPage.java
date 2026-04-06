package com.pages;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.tests.BaseTest;
import com.utils.ExcelReader;
import com.utils.TestData;


public class LoginPage extends BasePage {

    // If TestData is not thread-safe, consider using ThreadLocal<TestData>
    @SuppressWarnings("unused")
	private TestData td;
    public static String shipmentNumberForEng;

    public LoginPage() {
        super(BaseTest.getDriver()); // Pass the ThreadLocal WebDriver to BasePage
        this.td = new TestData(); // Create a new instance for each thread
        PageFactory.initElements(getDriver(), this); // Initialize elements
    }
    
    @FindBy(xpath="//frame[@name='head']")
    WebElement headerFrame;
    
    @FindBy(id="complianceTab")
    WebElement complianceTab;
    
    @FindBy(id="productTab")
    WebElement productTab;
    
    @FindBy(id="serializeTab")
    WebElement serializeTab;
    
    @FindBy(id="traceTab")
    WebElement traceTab;
    
    @FindBy(xpath="//a/img[@title='Edit']")
	WebElement clickEditImg;
    
    @FindBy(id="setup")
    WebElement setupTab;
    
    @FindBy(id="username")
    WebElement aipUsername;
    
    @FindBy(id="password")
    WebElement aipPassword;
    
    @FindBy(id="submit")
    WebElement aipLoginButton;
    
    @FindBy(id="_ic_auditlog_search.html")
    WebElement searchAuditLog;
    
    @FindBy(id="inputapplicationId")
    WebElement aipInputApplicationId;
    
    @FindBy(id="timeZone")
    WebElement aipTimeZone;
    
    @FindBy(xpath="//a[contains(text(),'Submit')]")
    WebElement aipSubmitButton;
    
    @FindBy(id="scanTab")
    WebElement scanTab;
    
    @FindBy(id="notificationsTab")
    WebElement notificationsTab;
    
   //----    
    @FindBy(xpath="//a[@id='serializeTab']")
    WebElement serializeTab2;
   //---
    
    
    public void navigateToRtsApplication() {
    	getDriver().get(td.getTestData(BaseTest.environment,"url"));
    	try {
			Thread.sleep(40000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	waitForElementToBeDisplayed(getDriver(), headerFrame, 50, 3);
    }
    
    public boolean validateHomePageDisplayed() {
    	getDriver().switchTo().defaultContent();
    	getDriver().switchTo().frame(headerFrame);
    	boolean status = complianceTab.isDisplayed();
    	return status;
    }
    
    public CompliancePage clickOnComplianceTab(String message) {
    	click(complianceTab, message);
    	CompliancePage cp = new CompliancePage(getDriver());
    	return cp;
    }
    
    public ProductPage clickOnProductTab(String message) {
    	getDriver().switchTo().defaultContent();
    	getDriver().switchTo().frame(headerFrame);
    	click(productTab, message);
    	ProductPage pp = new ProductPage(getDriver());
    	return pp;
    }
    
    public SerializePage clickOnSerializeTab(String message) {
    	getDriver().switchTo().defaultContent();
    	getDriver().switchTo().frame(headerFrame);
    	click(serializeTab, message);
    	SerializePage sp = new SerializePage(getDriver());
    	return sp;
    }
    
    public TracePage clickOnTraceTab(String message) {
    	getDriver().switchTo().defaultContent();
    	getDriver().switchTo().frame(headerFrame);
    	click(traceTab, message);
    	TracePage tp = new TracePage(getDriver());
    	return tp;
    }
    
    public SetupPage clickOnSetupTab(String message) {
    	click(setupTab, message);
    	SetupPage stp = new SetupPage(getDriver());
    	return stp;
    }
    
    public void switchToDefaultContent() {
    	getDriver().switchTo().defaultContent();
    }
    
    public void navigateToAip() {
    	getDriver().get(td.getTestData(BaseTest.environment, "aipUrl"));
    	waitForElementToBeDisplayed(getDriver(), aipUsername, 50, 3);    	
    }
    
    public void enterUsernameForAip(String message) {
    	sendKeys(aipUsername, td.getTestData(BaseTest.environment, "testUser"), message);
    }
    
    public void enterPasswordForAip(String message) {
    	String data = decryptString(td.getTestData(BaseTest.environment, "validPassword"));
    	System.out.println(data);
    	sendKeys(aipPassword, data.trim(), message);
    }
    
    public void clickOnLoginButton(String message) {
    	click(aipLoginButton, message);
    	waitForElementToBeDisplayed(getDriver(), searchAuditLog, 30, 3);
    }
    
    public void clickOnSearchAuditLog(String message) {
    	click(searchAuditLog,message);
    	waitForElementToBeDisplayed(getDriver(), aipInputApplicationId, 30, 2);
    }
    
    public void enterApplicationName(String message) {
    	sendKeys(aipInputApplicationId, td.getTestData(BaseTest.environment, "aipApplicationName"), message);
    }
    
    public void selectAipTimeZone() {
    	selectElementFromDropDownByVisibleText(aipTimeZone, "MST");
    }
    
    public void clickOnSubmitButton(String message) {
    	click(aipSubmitButton, message);
    	waitForElementToBeDisplayed(getDriver(), getDriver().findElement(By.xpath("//h1[contains(text(),'Audit Log List')]")), 20, 3);
    }
    
    public ScanPage clickOnScanTab(String message) {
    	getDriver().switchTo().defaultContent();
        getDriver().switchTo().frame(headerFrame);
    	click(scanTab, message);
    	ScanPage scp = new ScanPage(getDriver());
    	return scp;
    }
    
    public void updateDestroyEventXml() {
    	String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "Destroyed_Event.xml";
    	String time = getFixedTimestamp();
    	try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	List<String> eventTimeStamp = new ArrayList<>();
    	eventTimeStamp.add(time);
    	updateTagWithValues(filePath, "eventTime", eventTimeStamp);
    }
    
    public NotificationPage clickOnNotificationTab(String message) {
    	try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	click(notificationsTab, message);
    	NotificationPage np = new NotificationPage(getDriver());
    	return np;
    }
    
    public void updateShipmentFor58163Test() {
    	String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "ENGTest-Shipment.xml";
    	String[] a = new String[4];
    	Long n = randomNumber(4);
		a[0]= Long.toString(n);
		Long n1 = randomNumber(4);
		a[1]= Long.toString(n1);
		shipmentNumberForEng=a[1];
		a[2]="urn:epcglobal:cbv:bt::PO1-202511050026";
		Long n2 = randomNumber(6);
		String text = "ENG"+ Long.toString(n2);
		a[3]=text;
		List<String> bizValues = new ArrayList<>();
		for(int i=0;i<a.length;i++) {
			bizValues.add(a[i]);
		}
		updateTagWithValues(filePath, "bizTransaction", bizValues);
		String time = getFixedTimestamp();
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> timeStamp = new ArrayList<>();
		timeStamp.add(time);
		updateTagWithValues(filePath, "eventTime", timeStamp);
    }
    
    public void updateEmptyPalletShipmentFor58163Test() {
    	String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "ENGTest-EmptyShipment.xml";
		String time = getFixedTimestamp();
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> timeStamp = new ArrayList<>();
		timeStamp.add(time);
		updateTagWithValues(filePath, "eventTime", timeStamp);
    }
    
    //KAZ & Lot
    
    
    public SerializePage clickOnSerializeTab_withoutHeader(String message) {
    	getDriver().switchTo().defaultContent();
    	getDriver().switchTo().frame(headerFrame);
    	click(serializeTab2, message);
    	SerializePage sp = new SerializePage(getDriver());
    	return sp;
    }
    
    
    public TracePage clickOnTraceTab2(String message) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headerFrame);
    	click(traceTab, message);
    	TracePage tp = new TracePage(getDriver());
    	return tp;
    }   
   
    
    
}
