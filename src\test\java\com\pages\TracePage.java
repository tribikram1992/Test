package com.pages;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.datatransfer.StringSelection;
import java.io.File;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.tests.BaseTest;
import com.utils.JavaScriptExecutorUtil;
import com.utils.TestData;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class TracePage extends BasePage {

	TestData td = new TestData();

	public static String shipmentNumber;
	public static String serialNumber;

	public TracePage(WebDriver driverInstance) {
		super(BaseTest.getDriver()); // Pass the ThreadLocal WebDriver to BasePage
		this.td = new TestData(); // Create a new instance for each thread
		PageFactory.initElements(getDriver(), this); // Initialize elements
	}

	@FindBy(id = "event_extid")
	WebElement eventExtId;

	@FindBy(xpath = "//frame[@name='body']")
	WebElement bodyFrame;

	@FindBy(xpath = "//frame[@name='head']")
	WebElement headerFrame;

	@FindBy(id = "upload")
	WebElement upload;

	@FindBy(id = "import")
	WebElement importButton;

	@FindBy(id = "importFileId")
	WebElement chooseFile;

	@FindBy(id = "lotId")
	WebElement lotId;

	@FindBy(id = "btnSearch")
	WebElement searchButton;

	@FindBy(xpath = "//td[contains(text(),'Ship')]/preceding-sibling::td/a")
	WebElement eventId;

	@FindBy(id = "showEntSearch1")
	WebElement expandEventInfo;

	@FindBy(xpath = "//td[contains(text(),'File Uploaded successfully')]")
	WebElement fileUploadSuccessMessage;

	@FindBy(id = "mms_reports")
	WebElement reports;

	@FindBy(xpath = "//span[br and contains(., 'Inventory') and contains(., 'Report-Summary')]")
	WebElement inventoryReportSummary;

	@FindBy(id = "selDrugId")
	WebElement irsProductId;

	@FindBy(id = "disposition")
	WebElement disposition;

	@FindBy(id = "btnGeneratePureURI")
	WebElement runReport;

	@FindBy(id = "eventType")
	WebElement eventType;

	@FindBy(id = "itemTypeId")
	WebElement itemTypeId;

	@FindBy(id = "processedDate")
	WebElement processedDate;

	@FindBy(xpath = "//td[contains(text(),'Generate')]/preceding-sibling::td/a")
	WebElement generateId;

	@FindBy(xpath = "//td[contains(text(),'Allocate')]/preceding-sibling::td/a")
	WebElement allocateId;

	@FindBy(xpath = "//a[contains(text(),'Show Prefix')]")
	WebElement showPrefix;

	@FindBy(id = "anchor_1")
	WebElement plusButton;

	@FindBy(id = "whereIs")
	WebElement whereIs;

	@FindBy(id = "itemId")
	WebElement traceId;

	@FindBy(id = "cmb_bizTransId")
	WebElement bizTransId;

	@FindBy(id = "bizTransId")
	WebElement inputBizTransId;

	// ----

	@FindBy(xpath = "//a[@id='upload']")
	WebElement xUpload;
	@FindBy(xpath = "//a[@id='import']")
	WebElement xImport;
	@FindBy(xpath = "//input[@name='importFile']")
	WebElement xChooseFile;
	@FindBy(xpath = "//td[@class='table_text_red']")
	WebElement xFileSuccessMsg; // "File Uploaded successfully."
	@FindBy(xpath = "//a[@id='events']")
	WebElement xEventTab;
	@FindBy(xpath = "//input[@id='lotId']")
	WebElement xlotId;
	@FindBy(xpath = "//input[@name='traceId']")
	WebElement tracId;
	@FindBy(xpath = "//input[@name='itemTypeId']")
	WebElement Enter_ItemID;
	@FindBy(xpath = "//input[@name='searchByDays']")
	WebElement Enter_SearchByDaysNumber;
	@FindBy(xpath = "//a[@id='btnSearch']")
	WebElement Event_Search;
	@FindBy(xpath = "//select[@name='eventType']")
	WebElement Event_Type;
	@FindBy(xpath = "//select[@id='eventType']//option[@value='28']")
	WebElement Select_Generated;
	@FindBy(xpath = "//select[@id='eventType']//option[@value='33']")
	WebElement Select_LotSummary;
	@FindBy(xpath = "//td[@class='_Black' and text()='Unavailable']")
	WebElement Disposition_Status;
	@FindBy(xpath = "//a[@id='btnCancel']")
	WebElement EventDetails_Close;
	@FindBy(xpath = "//a[@name='uniqueEntId']")
	WebElement EventSearch_sNumberDetails;
	@FindBy(xpath = "//a[contains(@onclick, 'javascript:getTraceReport')]")
	WebElement EventDetails_GeneratedReport;
	@FindBy(xpath = "//a[@class='header_hdng']")
	WebElement ItemSUmmary_traceIDicon;
	@FindBy(xpath = "(//img[@id='omsIconId'])[1]")
	WebElement EventDetails_OMSReport;
	@FindBy(xpath = "//td[contains(text(), 'OMS order processing')]")
	WebElement OMSDetails_Process;
	@FindBy(xpath = "//table[@class='childTable']//tr[2]//td[5][1]")
	WebElement EventDetails_Quantity;
	@FindBy(xpath = "(//img[@src='/rfxcelvelocitymacros/images/view_icon.gif' and @title='Error'])[1]")
	WebElement EventSearchResult_FirstErrorIcon;
	@FindBy(xpath = "//td[@class='body_text' and @colspan='2'][contains(text(), 'Invalid Quantity')]")
	WebElement ViewNotification_InvalidQuantity;
	@FindBy(xpath = "//input[@value='back']")
	WebElement ViewNotification_Back;
	@FindBy(xpath = "//td[@id=\"buildVersion\"]")
	WebElement BuildVersion;

	@FindBy(xpath = "//a[@id='showSearchId']")
	WebElement linkShowSearch;

	@FindBy(xpath = "//a[normalize-space()='Hide Search' or normalize-space()='Show Search']")
	WebElement linkShowSearchWhereIs;

	@FindBy(xpath = "//input[@id='itemType']")
	WebElement inputItemIDWhereIs;

	@FindBy(xpath = "//select[@id='itemType2']")
	WebElement listItemTypeWhereIs;

	@FindBy(xpath = "//select[@id='itemDisposition']")
	WebElement listItemDispositionWhereIs;

	@FindBy(xpath = "//input[@id='envDate1']")
	WebElement inputExpDateWhereIs;

	@FindBy(id = "expirydateSearchFlag")
	WebElement listExpDateSearchFlagWhereIs;

	@FindBy(xpath = "//a[@id='btnSearch']")
	WebElement bittonLinkSearchWhereIs;

	@FindBy(xpath = "//a[contains(@onclick,'javascript:getTraceReport')]")
	List<WebElement> webElementListLinkTraceIDsSearchResultWhereIs;

	@FindBy(xpath = "//a[@id='showPrefix']")
	WebElement linkShowPrefix;

	@FindBy(xpath = "//a[@id='hidePrefix']")
	WebElement linkHidePrefix;

	@FindBy(xpath = "//h2[normalize-space()='Traceability Report']")
	WebElement headingTraceabilityReport;

	@FindBy(xpath = "//input[@id='parentItemId']/../td[(count(//td[text()='Disposition']/preceding-sibling::td)+1)]")
	WebElement webElementDispositionTraceabilityReport;

	@FindBy(xpath = "//td[@class='header_hdng' and normalize-space()='Event Search']")
	WebElement headingTextEventSearch;

	@FindBy(xpath = "//tr[@id='advancedSearchTable']")
	WebElement tableAdvanceSearch;

	@FindBy(xpath = "(//td[normalize-space()='Sender Custodian']/following-sibling::td)[1]")
	WebElement webElementSenderCustodianID;

	@FindBy(xpath = "(//td[normalize-space()='Sender Owner']/following-sibling::td)[1]")
	WebElement webElementSenderOwnerID;

	@FindBy(xpath = "(//td[normalize-space()='Receiver Owner']/following-sibling::td)[1]")
	WebElement webElementReceiverOwnerID;

	@FindBy(xpath = "(//td[normalize-space()='Receiver Owner']/following-sibling::td)[2]")
	WebElement webElementReceiverOwnerName;

	@FindBy(xpath = "(//td[normalize-space()='Receiver Custodian']/following-sibling::td)[1]")
	WebElement webElementReceiverCustodianID;

	@FindBy(xpath = "(//td[normalize-space()='Receiver Custodian']/following-sibling::td)[2]")
	WebElement webElementReceiverCustodianName;

	@FindBy(xpath = "//td[normalize-space()='Select Below to View Report:']")
	WebElement webElementHeadingSelectBelowToViewReport;

	@FindBy(xpath = "//span[normalize-space()='Batch Summary Report']/parent::a")
	WebElement linkBatchSummaryReport;

	@FindBy(xpath = "//input[@id='lotId']")
	WebElement inputLotIDBatchSummaryReport;

	@FindBy(xpath = "//td[contains(normalize-space(),'BATCH SUMMARY REPORT')]")
	WebElement headingBatchSummaryReport;

	@FindBy(xpath = "//div[text()='Batch Summary Report']")
	WebElement webElelemntHeaderBatchSummaryReport;

	@FindBy(id = "attributeValue")
	WebElement inputAttributeValue;

	@FindBy(xpath = "//a[contains(@id,'uniqueEntId')]")
	WebElement linkEventIDs;

	@FindBy(xpath = "//a[contains(@id,'downloadLinke')]")
	WebElement linkSourceFileDownload;

	@FindBy(xpath = "//a[@id='btnEventDetails']")
	WebElement buttonLinkEventDetails;

	@FindBy(xpath = "//div[text()='Trace Event Summary Report']")
	WebElement headerTraceEventSummaryReport;

	@FindBy(xpath = "//div[text()='Trace Event Details Report']")
	WebElement headerTraceEventDetailsReport;

	// ----

	@FindBy(id = "itemLocation")
	WebElement itemLocation;

	@FindBy(id = "itemDisposition")
	WebElement itemDisposition;

	@FindBy(xpath = "//td[contains(text(),'Lot Summary')]/following-sibling::td[contains(text(),'Success')]")
	WebElement lotSummary;

	@FindBy(xpath = "//td[@class='header_hdng' and text()='Event Details']")
	WebElement headerEventDetails;

	@FindBy(xpath = "//input[@id='traceId']")
	WebElement inputTraceIDEvents;

	public boolean validateTraceTabStatus() {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), eventExtId, 30, 3);
		boolean status = eventExtId.isDisplayed();
		return status;
	}

	public void clickOnTraceTabUpload(String message) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headerFrame);
		click(upload, message);
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), importButton, 30, 3);
	}

	public void uploadXml() throws AWTException {
		waitForElementToBeDisplayed(getDriver(), chooseFile, 30, 3);
		String path = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator
				+ "resources" + File.separator + "UAE070725.xml";
		sendKeys(chooseFile, path, "Selecting file to upload");
	}

	public void uploadXml2() throws AWTException {
		waitForElementToBeDisplayed(getDriver(), chooseFile, 30, 3);
		String path = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator
				+ "resources" + File.separator + "UAE070725-Shipment.xml";
		sendKeys(chooseFile, path, "Selecting file to upload");
	}

	public LoginPage clickOnImportButton(String message) {
		click(importButton, message);
		waitForElementToBeDisplayed(getDriver(), fileUploadSuccessMessage, 30, 2);
		Assert.assertTrue(fileUploadSuccessMessage.isDisplayed(), "Xml upload failed");
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void enterLotId(String message, SerializePage sp) {
		scrollToWebElement(lotId);
		sendKeys(lotId, sp.lotNumber, message);
	}

	public void clickOnSearchButton(String message) {
		click(searchButton, message);
		waitForElementToBeDisplayed(getDriver(),
				getDriver().findElement(By.xpath("//td[contains(text(),'Search Results')]")), 30, 3);
	}

	public void clickOnEventId(String message) {
		click(eventId, message);
		shipmentNumber = eventId.getText();
	}

	public void validateShipment() {
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String parentWindow1 = getDriver().getWindowHandle();
		switchWindow();
		getDriver().switchTo().defaultContent();
		String text = getAttribute(getDriver().findElement(By.xpath("(//td[contains(text(),'Pallet')])[2]")),
				"innerText", "validate shipment");
		System.out.println(text);
		click(expandEventInfo, "Expand Event Info");
		scrollToWebElement(getDriver().findElement(By.xpath("//td[contains(text(),'" + shipmentNumber + "')]")));
		String text1 = getAttribute(
				getDriver().findElement(By.xpath("//td[contains(text(),'" + shipmentNumber + "')]")), "innerText",
				"validate shipment");
		System.out.println(text1);
		getDriver().close();
		switchToParentWindow(parentWindow1);
	}

	public LoginPage navigateToLoginPage() {
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void uploadXmlForBahrain() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "BAHQA1_Comm_Agg.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload for bahrain");
	}

	public void uploadShipmentXmlForBahrain() throws AWTException {
		waitForElementToBeDisplayed(getDriver(), chooseFile, 30, 3);
		String path = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator
				+ "resources" + File.separator + "Bahrain_Ship.xml";
		sendKeys(chooseFile, path, "Selecting file to upload bahrain shipment");
	}

	public void uploadXmlForSaudiInternal() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SA_Comm_Agg_Event.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload for Saudi internal");
	}

	public void uploadXmlForSaudiShipmentInternal() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SA_Shipment.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload Saudi internal shipment");
	}

	public void uploadXmlForSaudiImport() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SA_IMPORT.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload Saudi import");
	}

	public void uploadXmlForSaudiShipmentImport() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SA_ImportShipment.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload Saudi import shipment");
	}

	public void uploadXmlForSaudiExport() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SA_EXPORT.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload for Saudi export");
	}

	public void uploadXmlForSaudiShipmentExport() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SA_ExportShipment.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload Saudi export shipment");
	}

	public void uploadXmlForSaudiShipmentExportCancel() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SA_ExportShipmentCancel.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload Saudi export Cancel shipment");
	}

	public void uploadXmlForSaudiNegative() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SA_Negative.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload Saudi negative");
	}

	public void uploadXmlForKoreaGG() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "KR030425.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload for Korea GG");
	}

	public void uploadXmlForKoreaGGShipment() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "KoreaGG_Shipment.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload Korea GG shipment");
	}

	public void validateShipmentKorea() {
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String parentWindow1 = getDriver().getWindowHandle();
		switchWindow();
		getDriver().switchTo().defaultContent();
		String text = getAttribute(getDriver().findElement(By.xpath("(//td[contains(text(),'Case')])[2]")), "innerText",
				"validate shipment");
		System.out.println(text);

		click(expandEventInfo, "Expand Event Info for Korea GG");
		scrollToWebElement(getDriver().findElement(By.xpath("//td[contains(text(),'" + shipmentNumber + "')]")));
		String text1 = getAttribute(
				getDriver().findElement(By.xpath("//td[contains(text(),'" + shipmentNumber + "')]")), "innerText",
				"validate shipment for Korea GG");
		System.out.println(text1);
		getDriver().close();
		switchToParentWindow(parentWindow1);
	}

	public void uploadXmlForKoreaGS() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "KRG040425.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload for Korea GS");
	}

	public void uploadXmlForKoreaGSShipment() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "KoreaGS_Shipment.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload Korea GS shipment");
	}

	public void uploadXmlForSwissLog() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SWL0704D.xml";
		sendKeys(chooseFile, filePath, "Selecting aggregate file to upload for swiss log");
	}

	public void uploadShipmentXmlForSwissLog() throws AWTException {
		waitForElementToBeDisplayed(getDriver(), chooseFile, 30, 3);
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SwisslogUS_shipment.xml";
		sendKeys(chooseFile, filePath, "Selecting shipment file to upload in swiss log test");
	}

	public void clickOnReports(String message) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headerFrame);
		click(reports, message);
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), inventoryReportSummary, 30, 3);
	}

	public void clickOnInventorySummaryReport(String message) {
		click(inventoryReportSummary, message);
		waitForElementToBeDisplayed(getDriver(), irsProductId, 30, 3);
	}

	public void enterProductIdInInventoryReportSummaryPage(String message, String data) {
		sendKeys(irsProductId, data, message);
		waitForElementToBeDisplayed(getDriver(),
				getDriver().findElement(By.xpath("//div[contains(text(),'35413760481020')]")), 20, 2);
		getDriver().findElement(By.xpath("//div[contains(text(),'35413760481020')]")).click();
	}

	public void selectDisposition() {

		WebElement selectEl = getDriver().findElement(By.id("disposition"));
		Select select = new Select(selectEl);

		if (!select.isMultiple()) {
			throw new IllegalStateException("Select is not multi-select");
		}
		select.deselectAll();
		select.selectByValue("40");

	}

	public void clickOnRunReport(String message) {
		click(runReport, message);
	}

	public LoginPage validateProductQuantity(String message) {
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String parentWindow1 = getDriver().getWindowHandle();
		switchWindow();
		getDriver().switchTo().defaultContent();
		String text = getAttribute(
				getDriver()
						.findElement(By.xpath("//div[contains(text(),'Total')]/parent::td/following-sibling::td/div")),
				"innerText", message);
		int a = Integer.parseInt(text);
		Assert.assertTrue(a >= 100000, "quanity is not matching, exiting the test case");
		getDriver().close();
		switchToParentWindow(parentWindow1);
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void selectEventType() {
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		selectElementFromDropDownByVisibleText(eventType, "Generate");
	}

	public void enterItemId(String message, String data) {
		sendKeys(itemTypeId, data, message);
		waitForElementToBeDisplayed(getDriver(),
				getDriver().findElement(By.xpath("//div[contains(text(),'" + data + "')]")), 20, 3);
		click(getDriver().findElement(By.xpath("//div[contains(text(),'" + data + "')]")),
				"Selecting item id during generation event for EU regression");
	}

	public void enterProcessedDate(String message) {
		String date = getCurrentDate();
		sendKeys(processedDate, date, message);
	}

	public LoginPage validateGenerateEvent(String message) {
		boolean status = getDriver()
				.findElement(
						By.xpath("//td[contains(text(),'Generate')]/following-sibling::td[contains(text(),'Success')]"))
				.isDisplayed();
		Assert.assertTrue(status, "Generate Event is not success");
		click(generateId, message);
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String parentWindow1 = getDriver().getWindowHandle();
		switchWindow();
		getDriver().switchTo().defaultContent();
		getDriver().findElement(By.xpath("//a[contains(text(),'Show Prefix')]")).click();
		waitForElementToBeDisplayed(getDriver(),
				getDriver().findElement(By.xpath("//td[contains(text(),'Product')]/preceding-sibling::td[2]/a")), 20,
				3);
		String text = getAttribute(
				getDriver().findElement(By.xpath("//td[contains(text(),'Product')]/preceding-sibling::td[2]/a")),
				"innerText", "Checking the serial number");
		String a = text.trim();
		String[] b = a.split(":");
		String[] c = b[4].split("\\.");
		int companyPrefixlength = String.valueOf(c[0]).length();
		Assert.assertTrue(companyPrefixlength == 7, "Company Prefix length is not matching");
		int serialNumberLength = String.valueOf(c[2]).length();
		Assert.assertTrue(serialNumberLength == 14, "Serial Number length is not matching");
		getDriver().close();
		switchToParentWindow(parentWindow1);
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void selectEventTypeAllocate() {
		selectElementFromDropDownByVisibleText(eventType, "Allocate");
	}

	public LoginPage validateAllocateEvent(String message) {
		boolean status = getDriver()
				.findElement(
						By.xpath("//td[contains(text(),'Allocate')]/following-sibling::td[contains(text(),'Success')]"))
				.isDisplayed();
		Assert.assertTrue(status, "Generate Event is not success");
		click(allocateId, message);
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String parentWindow1 = getDriver().getWindowHandle();
		switchWindow();
		getDriver().switchTo().defaultContent();
		getDriver().findElement(By.xpath("//a[contains(text(),'Show Prefix')]")).click();
		waitForElementToBeDisplayed(getDriver(),
				getDriver().findElement(By.xpath("//td[contains(text(),'Product')]/preceding-sibling::td[2]/a")), 20,
				3);
		String text = getAttribute(
				getDriver().findElement(By.xpath("//td[contains(text(),'Product')]/preceding-sibling::td[2]/a")),
				"innerText", "Checking the serial number");
		String a = text.trim();
		String[] b = a.split(":");
		String[] c = b[4].split("\\.");
		int companyPrefixlength = String.valueOf(c[0]).length();
		Assert.assertTrue(companyPrefixlength == 7, "Company Prefix length is not matching");
		int serialNumberLength = String.valueOf(c[2]).length();
		Assert.assertTrue(serialNumberLength == 14, "Serial Number length is not matching");
		getDriver().close();
		switchToParentWindow(parentWindow1);
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void selectEventTypeShip() {
		selectElementFromDropDownByVisibleText(eventType, "Ship");
	}

	public LoginPage getSerialNumber() {
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		waitForElementToBeDisplayed(getDriver(), getDriver().findElement(By.id("showPrefix")), 30, 3);
		click(getDriver().findElement(By.id("showPrefix")), "Click on show prefix for 58171 test");
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		waitForElementToBeDisplayed(getDriver(), getDriver().findElement(By.id("hidePrefix")), 30, 3);
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WebElement el = getDriver()
				.findElement(By.xpath("(//td[contains(text(),'Product')]/preceding-sibling::td[2]/a)[1]"));
		serialNumber = getAttribute(el, "innerText", "getting serial number").trim();
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void clickOnWhereIs(String message) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headerFrame);
		click(whereIs, message);
	}

	public void enterTraceId(String message) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		sendKeys(traceId, serialNumber, message);
	}

	public LoginPage validateDispositionStatus(String message) {
		boolean status = getDriver().findElement(By.xpath("//td[contains(text(),'Decommissioned')]")).isDisplayed();
		Assert.assertTrue(status, "Decommissioned status is not displayed");
		String text = getAttribute(getDriver().findElement(By.xpath("//td[contains(text(),'Decommissioned')]")),
				"innerText", message);
		System.out.println(text);
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void uploadDestroyEventXml() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "Destroyed_Event.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload for destroy event validation");
	}

	public void clickOnEventIdForDecomposition(String message) {
		WebElement el = getDriver().findElement(By.xpath(
				"//td[contains(text(),'Ship')]/following-sibling::td[contains(text(),'Success')]/following-sibling::td[2][contains(text(),'Baxter Dammam')]/preceding-sibling::td[4]/a"));
		scrollToWebElement(el);
		click(el, message);
		shipmentNumber = eventId.getText();
	}

	public void uploadManufacturingXml() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "Eng-58174.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload for manufacturing date xml");
	}

	public void uploadEngXml() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "ENGTest-Shipment.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload for Eng xml");
	}

	public void selectBizTransId() {
		selectElementFromDropDownByVisibleText(bizTransId, "Shipment Number");
	}

	public void enterShipmentNumberForEngTest(String message) {
		sendKeys(inputBizTransId, LoginPage.shipmentNumberForEng, message);
	}

	public LoginPage validateShipmentStatus() {
		WebElement el = getDriver().findElement(By.xpath("//td[contains(text(),'Ship')]/following-sibling::td[5]"));
		waitForElementToBeDisplayed(getDriver(), el, 20, 3);

		String text = el.getText();
		Matcher m = Pattern.compile("Shipment\\s*Number\\s*:\\s*(\\S+)").matcher(text);
		if (m.find()) {
			String shipmentNumber = m.group(1);
			boolean status = Integer.parseInt(LoginPage.shipmentNumberForEng) == Integer
					.parseInt(shipmentNumber.trim());
			Assert.assertTrue(status, "Didn't find ship event");
		}
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;

	}

	public void uploadEmptyPalletShipmentEngXml() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "ENGTest-EmptyShipment.xml";
		sendKeys(chooseFile, filePath, "Selecting file to upload for Eng xml");
	}

	public void uploadAggregateXmlForEuRegression() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "EURTCommissionAggregate.xml";
		sendKeys(chooseFile, filePath, "Selecting aggregate file to upload for Eu Regression Test");
	}

	public void uploadShipmentXmlForEuRegression() {
		waitForElementToBeDisplayed(getDriver(), chooseFile, 30, 3);
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "EURTShippingHalleToSA8159002.xml";
		sendKeys(chooseFile, filePath, "Selecting shipment file to upload in Eu Regression test");
	}

	public LoginPage validateShipmentForEuRegression(String message) {
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WebElement el = getDriver().findElement(By.xpath("//td[contains(text(),'Ship')]/following-sibling::td[5]"));
		String text = getAttribute(el, "textContent", message);
		System.out.println(text);
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void uploadAggregateXmlForEuSATest() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SharedCode_EUSAQA1.xml";
		sendKeys(chooseFile, filePath, "Selecting aggregate file to upload for Eu SA Regression Test");
	}

	public void uploadFirstShipXmlForEuSATest() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SharedCode_NonSAU_Ship.xml";
		sendKeys(chooseFile, filePath, "Selecting first shipment xml file to upload for Eu SA Regression Test");
	}

	public void uploadLotSummaryXmlForSwissLog() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "LotSummary.xml";
		sendKeys(chooseFile, filePath, "Selecting lot summary file to upload for swiss log");
	}

	public String GenerateLotNumber(String countryCode) {
		LocalDateTime currentDateTime = LocalDateTime.now();
		String year = String.valueOf(currentDateTime.getYear());
		String month = String.format("%02d", currentDateTime.getMonthValue());
		String day = String.format("%02d", currentDateTime.getDayOfMonth());
		String hour = String.format("%02d", currentDateTime.getHour());
		String minute = String.format("%02d", currentDateTime.getMinute());
		String second = String.format("%02d", currentDateTime.getSecond());
		String lotNumber = countryCode + month + day + year + hour + minute + second;
		System.out.println("Generate lot number is : " + lotNumber);
		return lotNumber;
	}

	public static String generateTimestamp() {
		LocalDateTime currentDateTime = LocalDateTime.now();
		ZonedDateTime zdt = ZonedDateTime.of(currentDateTime, ZoneId.systemDefault());
		zdt = zdt.minusDays(1);
		DateTimeFormatter EventTime_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		return zdt.format(EventTime_formatter);
	}

	public void Update_LSVT_EpcTagWithNewSerialNumber(String epcSGTIN, String epcShipperCase, String epcPallet)
			throws ParserConfigurationException, SAXException, IOException, TransformerException {
		try {
			String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
					+ File.separator + "resources" + File.separator + "LSVT_Commission_Aggregate.xml";
			File xmlFile = new File(filePath);
			if (!xmlFile.exists()) {
				System.out.println("XML file not found.");
				return;
			}

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();
			// Remove whitespace-only text nodes
			removeWhitespaceNodes(doc.getDocumentElement());

			NodeList epcList = doc.getElementsByTagName("epc");
			epcList.item(0).setTextContent(epcSGTIN);
			epcList.item(1).setTextContent(epcShipperCase);
			epcList.item(2).setTextContent(epcPallet);
			epcList.item(3).setTextContent(epcSGTIN);
			epcList.item(4).setTextContent(epcShipperCase);

			NodeList parentIDList = doc.getElementsByTagName("parentID");
			parentIDList.item(0).setTextContent(epcShipperCase);
			parentIDList.item(1).setTextContent(epcPallet);

			// Save changes with clean formatting
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

			DOMSource source = new DOMSource((Node) doc);
			StreamResult result = new StreamResult(xmlFile);
			transformer.transform(source, result);

			System.out.println("XML updated successfully.");
		} catch (Exception e) {
			System.out.println("Error updating XML: " + e.getMessage());
		}
	}

	public void updateEventTimeAndLotNumber(String eventTime, String lotNumber) throws ParserConfigurationException {
		try {
			String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
					+ File.separator + "resources" + File.separator + "LSVT_Commission_Aggregate.xml";
			File xmlFile = new File(filePath);
			if (!xmlFile.exists()) {
				System.out.println("XML file not found.");
				return;
			}

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			// Remove whitespace-only text nodes
			removeWhitespaceNodes(doc.getDocumentElement());

			NodeList nodeList = doc.getElementsByTagName("eventTime");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				node.setTextContent(eventTime);
			}

			NodeList lotNumberList = doc.getElementsByTagName("gs1ushc:lotNumber");
			for (int i = 0; i < lotNumberList.getLength(); i++) {
				Node node = lotNumberList.item(i);
				node.setTextContent(lotNumber);
			}

			// Save changes with clean formatting
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(xmlFile);
			transformer.transform(source, result);

			System.out.println("Event time and lot number updated successfully.");
		} catch (SAXException | IOException | TransformerException e) {
			System.out.println("Error updating event time and lot number: " + e.getMessage());
		}
	}

	public void RTSImportFile(String XmlFileName) throws AWTException {
		waitForElementToBeDisplayed(getDriver(), chooseFile, 30, 3);
		String path = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator
				+ "resources" + File.separator + XmlFileName;
		System.out.println("File name is - " + XmlFileName);
		sendKeys(chooseFile, path, "Selecting file to upload");
		click(xImport, "Import file");
		if (fileUploadSuccessMessage.isDisplayed())
			System.out.println("file Uploaded Successfully");
		else {
			System.out.println("file didnot Upload Successfully");
		}
	}

	public void ValidateLotSummaryDetails(String LotNumber, String eQuantity) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headerFrame);
		click(xEventTab,"CLick Event menu");
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		//click(Event_Type, "Open even type to select lot summary");
		//click(Select_LotSummary, "Select Lot summary ");
		scrollToWebElement(Enter_SearchByDaysNumber);
		sendKeys(xlotId, LotNumber, "Enter lotId to search");
		//Enter_SearchByDaysNumber.clear();
		sendKeys(Enter_SearchByDaysNumber, "777", "Enter 777 days to search");
		click(Event_Search,"Search the Lot details");

		String parentWindow1 = getDriver().getWindowHandle();
		WebElement aSuccess = getDriver().findElement(By.xpath("//tr[@class='trbg']//td[@class='body_text'][text()='Allocate']/following-sibling::td[1][contains(text(), 'Success')]"));
		boolean status1 = aSuccess.isDisplayed();
		if (status1 = true) {
		    System.out.println("Lot Summary successfully generated");
		    WebElement FirstEventID = getDriver().findElement(By.xpath("//tr[@class='trbg']//td[@class='body_text'][text()='Allocate']/preceding-sibling::*[1]/a[@name='uniqueEntId']"));
	click(FirstEventID,"Open Event details");
		switchWindow();
//check quantity
	String aQuantity = EventDetails_Quantity.getText();
		if (aQuantity.trim().equals(eQuantity.trim())) {
		System.out.println("Lot Summary quantity matched with expected quantity ");
		} else {
		System.out.println("Lot Summary Quanity did not match: Test failed-actual quantity is : " + aQuantity);}
		click(EventDetails_Close,"Close Event details popup window");  
		    
		}
		    else {
		   System.out.println("Lot Summary did not successfully generate");
		    }
		switchToParentWindow(parentWindow1);
		}

	public static String UpdateEpcClassIdentifier(String LotNumber) {
		String epcClassidentifier = "urn:epc:class:lgtin:0349281.042250.LSVT111125";
		String updated_epcClassidentifier = "urn:epc:class:lgtin:0349281.042250." + LotNumber;
		System.out.println("New epcClass value is - " + updated_epcClassidentifier);
		return updated_epcClassidentifier;
	}

	public void updateEpcClassAndQuantity(String epcClassValue, String quantityValue)
			throws ParserConfigurationException {
		try {
			String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
					+ File.separator + "resources" + File.separator + "LSVT_Sanofi_EOB_qty0.xml";
			File xmlFile = new File(filePath);
			if (!xmlFile.exists()) {
				System.out.println("XML file not found.");
				return;
			}

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			// Remove whitespace-only text nodes
			removeWhitespaceNodes(doc.getDocumentElement());

			NodeList nodeList = doc.getElementsByTagName("epcClass");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				node.setTextContent(epcClassValue);
			}

			nodeList = doc.getElementsByTagName("quantity");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				node.setTextContent(quantityValue);
			}

			// Save changes with clean formatting
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(xmlFile);
			transformer.transform(source, result);

			System.out.println("Epc class and quantity updated successfully.");
		} catch (SAXException | IOException | TransformerException e) {
			System.out.println("Error updating epc class and quantity: " + e.getMessage());
		}
	}

	public void ValidateLotSummaryError() {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headerFrame);
		click(xEventTab, "CLick Event menu");
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);

		click(Event_Type, "Open even type to select lot summary");
		click(Select_LotSummary, "Select Lot summary ");
		scrollToWebElement(Enter_SearchByDaysNumber);
		Enter_SearchByDaysNumber.clear();
		sendKeys(Enter_SearchByDaysNumber, "777", "Enter 777 days to search");
		click(Event_Search, "Search the Lot details");

		WebElement aSuccess = getDriver()
				.findElement(By.xpath("(//td[@class='body_text']//span[@class='body_text_red'][text()='Error'])[1]"));
		boolean status1 = aSuccess.isDisplayed();
		if (status1 = true) {
			System.out.println("Event status of Lot Summary is in ERROR state");
			// open doc and verify data
			if (EventSearchResult_FirstErrorIcon.isDisplayed()) {
				click(EventSearchResult_FirstErrorIcon, "Click Error Report icon");
				Assert.assertTrue(ViewNotification_InvalidQuantity.isDisplayed(),
						"Invalid Quantity displayed due to Zero quantity: Test PASS");
				click(ViewNotification_Back, "Back to main search page");
			} else {
				System.out
						.println(EventSearchResult_FirstErrorIcon + "EventSearchResult_FirstErrorIcon did not display");
			}
		} else {
			System.out.println("Error state did not generate");
		}
	}

	public void uploadXml(String pathToXML) throws AWTException {
		waitForElementToBeDisplayed(getDriver(), chooseFile, 30, 3);
		sendKeys(chooseFile, pathToXML, "Selecting file to upload");
	}

	public void searchEvent(String lotID, String eventType) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headerFrame);
		click(xEventTab, "Click on Event Link");

		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), headingTextEventSearch, 10, 1);
		if (linkShowSearch.getText().trim().equals("Show Search")) {
			click(linkShowSearch, "Click on Show Search");
		}
		waitForElementToBeDisplayed(getDriver(), tableAdvanceSearch, 10, 1);
		scrollToWebElement(Event_Type);
		selectElementFromDropDownByVisibleText(Event_Type, eventType);
		sendKeys(xlotId, lotID, "Enter Lot ID");
		scrollToWebElement(xlotId);
		sendKeys(xlotId, lotID, "Enter Lot ID");
		scrollToWebElement(searchButton);
		click(searchButton, "Click on Search Button");
		waitForElementToBeDisplayed(getDriver(), EventSearch_sNumberDetails, 10, 1);
		scrollToWebElement(EventSearch_sNumberDetails);
	}

	public void verifySoftDeleteEvent(String activeCustomer) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		scrollToWebElement(EventSearch_sNumberDetails);
		click(EventSearch_sNumberDetails, "Click on Event ID");
		String parentWindow = getDriver().getWindowHandle();
		switchWindow();
		getDriver().manage().window().maximize();
		scrollToWebElement(expandEventInfo);
		click(expandEventInfo, "Click on Expand Event Information");

		String receverOwnerID = webElementReceiverOwnerID.getText().replace("ID:", "").trim();
		System.out.println("receiver owner ID " + receverOwnerID);
		Assert.assertTrue(receverOwnerID.equals(activeCustomer),
				"Active Customer ID is not diaplayed in receiver owner ID");
		ern.enterLogAndCapture("Receiver Owner ID matches with Active customer ID",
				highlightElement(webElementReceiverOwnerID, "Receiver Owner ID matches with Active customer ID"));

		String receverCustodianID = webElementReceiverCustodianID.getText().replace("ID:", "").trim();
		System.out.println("receiver custodian ID " + receverCustodianID);
		Assert.assertTrue(receverCustodianID.equals(activeCustomer),
				"Active Customer ID is not diaplayed in receiver custodian ID");
		ern.enterLogAndCapture("Receiver Custodian ID matches with Active customer ID", highlightElement(
				webElementReceiverCustodianID, "Receiver Custodian ID matches with Active customer ID"));
		getDriver().close();
		switchToParentWindow(parentWindow);

	}

	public void searchBatchSummaryReport(String lotNumber) throws Exception {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headerFrame);
		click(reports, "Click on Reports Link");

		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), webElementHeadingSelectBelowToViewReport, 10, 1);

		click(linkBatchSummaryReport, "Click on Batch Summary Report");
		waitForElementToBeDisplayed(getDriver(), headingBatchSummaryReport, 10, 1);
		JavaScriptExecutorUtil.waitUntilJavaScriptCompletes(getDriver());
		new WebDriverWait(getDriver(), Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(inputLotIDBatchSummaryReport));
		click(inputLotIDBatchSummaryReport, "Click on input Field LotID of BatchSummaryReport");
		Thread.sleep(3000);
		sendKeys(inputLotIDBatchSummaryReport, lotNumber, "Enter Lot ID");

		scrollToWebElement(runReport);
		click(runReport, "Click on Run Report");

		String parentWindow = getDriver().getWindowHandle();
		switchWindow();
		getDriver().manage().window().maximize();
		waitForElementToBeDisplayed(getDriver(), webElelemntHeaderBatchSummaryReport, 10, 1);

		ern.enterLogAndCapture("Batch Summary Report Generated successfully",
				highlightElement(webElelemntHeaderBatchSummaryReport, "Batch Summary Report Generated successfully"));
		getDriver().close();
		switchToParentWindow(parentWindow);

	}

	public void Enter_ItemID(String ItemID) {
		sendKeys(Enter_ItemID, ItemID, "Enter Item ID");
	}

	public void Enter_NumberOfDaysAndSearch(String NoDays) {
		click(Event_Type, "Search Events");
		click(Select_Generated, "Search Events");
		sendKeys(Enter_SearchByDaysNumber, NoDays, "Enter Number of days");
		scrollToWebElement(Event_Search);
		click(Event_Search, "Search Events");
	}

	public void KAZ_updateXml(String palletID, String caseID, String parentID, String timestamp, String lotID)
			throws ParserConfigurationException, SAXException, IOException, TransformerException {
		try {
			String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
					+ File.separator + "resources" + File.separator + "KAZ_Comm_Agg.xml";
			File xmlFile = new File(filePath);
			if (!xmlFile.exists()) {
				System.out.println("XML file not found.");
				return;
			}

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			// Remove whitespace-only text nodes
			removeWhitespaceNodes(doc.getDocumentElement());

			NodeList epcList = doc.getElementsByTagName("epc");
			epcList.item(0).setTextContent(palletID);
			epcList.item(1).setTextContent(caseID);
			epcList.item(2).setTextContent(caseID);

			NodeList parentIDList = doc.getElementsByTagName("parentID");
			parentIDList.item(0).setTextContent(palletID);

			NodeList eventTimeList = doc.getElementsByTagName("eventTime");
			for (int i = 0; i < eventTimeList.getLength(); i++) {
				Node node = eventTimeList.item(i);
				node.setTextContent(timestamp);
			}

			NodeList lotNumberList = doc.getElementsByTagName("cbvmda:lotNumber");
			for (int i = 0; i < lotNumberList.getLength(); i++) {
				Node node = lotNumberList.item(i);
				node.setTextContent(lotID);
			}

			// Save changes with clean formatting
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

			DOMSource source = new DOMSource((Node) doc);
			StreamResult result = new StreamResult(xmlFile);
			transformer.transform(source, result);

			System.out.println("XML updated successfully.");
		} catch (Exception e) {
			System.out.println("Error updating XML: " + e.getMessage());
		}
	}

	public void Open_GenerateSerialNumberAndValidateUnavailable() {
		String parentWindow1 = getDriver().getWindowHandle();
		List<WebElement> tableRows = getDriver().findElements(By.xpath("//td[@class='body_text'][text()='Generate']"));
		for (int i = 0; i < tableRows.size(); i++) {
			WebElement linkElement = tableRows.get(i).findElement(By.xpath(
					"(//tr[@class='trbg']//td[@class='body_text'][text()='Generate']/preceding-sibling::*[1]/a[@name='uniqueEntId'])["
							+ (i + 1) + "]"));
			if (linkElement != null) {
				click(linkElement, "Event Having generate message and open to validate 'unavailable' status");
				// Validate unavailable
				switchWindow();
				// Get the window handles
				String rfxcel1 = getDriver().getWindowHandle();
				boolean status = Disposition_Status.isDisplayed();
				if (status = true) {
					System.out.println("Disposition_Status is unavailable");

					// validate OMS
					click(EventDetails_OMSReport, "Open OMS report");
					switchWindow();

					boolean status2 = OMSDetails_Process.isDisplayed();
					if (status2 = true) {
						System.out.println("OMS processed ");
						click(EventDetails_Close, "Close the OMS popup window");
					} else {
						System.out.println("OMS not processed ");
						click(EventDetails_Close, "Failed OMS - Close the popup window");
					}
					getDriver().switchTo().window(rfxcel1);
					click(EventDetails_Close, "Close the EventDetails popup window");
					break;
				} else
					System.out.println("Disposition_Status unavailable is not present ");
			} else {
				System.out.println(" Event link did not found ");
			}
		}
		switchToParentWindow(parentWindow1);
	}

	public void enterItemLocation(String message) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		sendKeys(itemLocation, "Baxter Dammam", message);
		waitForElementToBeDisplayed(getDriver(),
				getDriver().findElement(By.xpath("//div[contains(text(),'Baxter Dammam')]")), 30, 2);
		getDriver().findElement(By.xpath("//div[contains(text(),'Baxter Dammam')]")).click();
	}

	public void selectItemDisposition() {
		selectElementFromDropDownByVisibleText(itemDisposition, "In Transit");
	}

	public void validateLotSummaryEvent(String message) {
		waitForElementToBeDisplayed(getDriver(), lotSummary, 30, 2);
		boolean status = lotSummary.isDisplayed();
		Assert.assertTrue(status, "Lot summary is not displayed");
	}

	public void enterLotId(String message, String data) {
		scrollToWebElement(lotId);
		sendKeys(lotId, data, message);
	}

	public void searchEventByAttribute(String attributeValue, String days) {

		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headerFrame);
		click(xEventTab, "Click on events Link");

		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);

		waitForElementToBeDisplayed(getDriver(), headingTextEventSearch, 10, 1);

		scrollToWebElement(inputAttributeValue);
		sendKeys(inputAttributeValue, attributeValue, "Enter attribute value");

		scrollToWebElement(Enter_SearchByDaysNumber);
		sendKeys(Enter_SearchByDaysNumber, days, "Enter search By Date");

		scrollToWebElement(searchButton);
		click(searchButton, "Click On Search Button");

		waitForElementToBeDisplayed(getDriver(), linkEventIDs, 30, 1);

	}

	public void verifySourceFileDownloaded5828() throws Exception {

		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);

		scrollToWebElement(linkSourceFileDownload);
		Assert.assertTrue(linkSourceFileDownload.isDisplayed());
		ern.enterLogAndCapture("Download SuurceFile link is available",
				highlightElement(linkSourceFileDownload, "Download SuurceFile link is available"));

		ern.enterLog("Download source file Success");
	}

	public void verifyTraceEventDetailsReportGenerated() {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);

		scrollToWebElement(buttonLinkEventDetails);

		click(buttonLinkEventDetails, "Click On Event Details button link");
		String parentWindow = getDriver().getWindowHandle();
		try {
			switchWindow();
			WebElement actual = waitForElementToBeDisplayed(getDriver(), headerTraceEventDetailsReport, 30, 1);
			Assert.assertTrue(actual != null && actual.isDisplayed(),
					"Trace Event Details Report generation unsuccessfull");
			ern.enterLogAndCapture("Trace Event Details Report generated successfully", highlightElement(
					headerTraceEventDetailsReport, "Trace Event Details Report generated successfully"));
			getDriver().close();
		} catch (NoSuchElementException ne) {
			Assert.assertTrue(false, "Trace Event Details Report generation unsuccessfull");

		} catch (Exception e) {
			// TODO: handle exception
		}
		switchToParentWindow(parentWindow);

	}

	public void verifyShippingTriggerNegativeTestEvent(String SenderOwner, String SenderCustodian, String ReceiverOwner,
			String ReceiverCustodian) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		scrollToWebElement(EventSearch_sNumberDetails);
		click(EventSearch_sNumberDetails, "Click on Event ID");
		String parentWindow = getDriver().getWindowHandle();
		switchWindow();
		getDriver().manage().window().maximize();
		scrollToWebElement(expandEventInfo);
		click(expandEventInfo, "Click on Expand Event Information");

		scrollToWebElement(webElementSenderOwnerID);

		String senderOwnerID = webElementSenderOwnerID.getText().replace("ID:", "").trim();
		System.out.println("receiver owner ID " + senderOwnerID);
		Assert.assertTrue(senderOwnerID.equals(SenderOwner), "Sender owner dont match");
		ern.enterLogAndCapture("Sender Owner ID matches",
				highlightElement(webElementSenderOwnerID, "Sender Owner ID matches"));

		String senderCustodianID = webElementSenderCustodianID.getText().replace("ID:", "").trim();
		System.out.println("sender custodian ID " + senderCustodianID);
		Assert.assertTrue(senderCustodianID.equals(SenderCustodian), "sender custodian ID dont match");
		ern.enterLogAndCapture("sender Custodian ID matches",
				highlightElement(webElementSenderCustodianID, "sender Custodian ID matches"));

		String receverOwnerID = webElementReceiverOwnerID.getText().replace("ID:", "").trim();
		System.out.println("receiver owner ID " + receverOwnerID);
		Assert.assertTrue(receverOwnerID.equals(ReceiverOwner), "Receiver owner dont match");
		ern.enterLogAndCapture("Receiver Owner ID matches",
				highlightElement(webElementReceiverOwnerID, "Receiver Owner ID matches"));

		String receverCustodianID = webElementReceiverCustodianID.getText().replace("ID:", "").trim();
		System.out.println("receiver custodian ID " + receverCustodianID);
		Assert.assertTrue(receverCustodianID.equals(ReceiverCustodian), "Receiver custodian ID dont match");
		ern.enterLogAndCapture("Receiver Custodian ID matches",
				highlightElement(webElementReceiverCustodianID, "Receiver Custodian ID matches"));
		getDriver().close();
		switchToParentWindow(parentWindow);

	}

	public String serachForItemStateTransactionVerification() throws Exception {

		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);

		if (linkShowSearchWhereIs.getText().trim().equals("Show Search")) {
			click(linkShowSearchWhereIs, "Click on Show Search");
		}

		String itemID = (String) td.getTestDataByPath(BaseTest.environment,
				"TestCase.SeP_RTS_State_Transition_Verification.ItemID");
		sendKeys(inputItemIDWhereIs, itemID, "Enter Item ID");
		JavaScriptExecutorUtil.waitUntilJavaScriptCompletes(getDriver());
		Thread.sleep(2000);
		click(getDriver().findElement(By.xpath("//div[@class='optionDivSelected']")),
				"Click on the Item ID drop down value");
		selectElementFromDropDownByVisibleText(listItemTypeWhereIs, "Product");
		Thread.sleep(2000);
		selectElementFromDropDownByVisibleText(listItemDispositionWhereIs, "Packed");
		selectElementFromDropDownByVisibleText(listExpDateSearchFlagWhereIs, ">");
		sendKeys(inputExpDateWhereIs, new java.text.SimpleDateFormat("MM-dd-yyyy").format(new java.util.Date()),
				"Enter current date");
		click(bittonLinkSearchWhereIs, "Click on Search");
		JavaScriptExecutorUtil.waitUntilJavaScriptCompletes(getDriver());
		new WebDriverWait(getDriver(), Duration.ofSeconds(30)).until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//a[contains(@onclick,'javascript:getTraceReport')]")));

		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);

		// JavaScriptExecutorUtil.clickAndHeighlightWebElementUsingJS(getDriver(),
		// linkShowHidePrefix, "Show Hide Prefix");
		click(linkShowPrefix, "Click on Show prefix");
		JavaScriptExecutorUtil.waitUntilJavaScriptCompletes(getDriver());
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), linkHidePrefix, 10, 1);
		new WebDriverWait(getDriver(), Duration.ofSeconds(30)).until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//a[contains(@onclick,'javascript:getTraceReport')]")));

		WebElement traceIDElement = getDriver().findElement(
				By.xpath("//td[normalize-space()='Packed']/../td/a[contains(@onclick,'javascript:getTraceReport')]"));
		scrollToWebElement(traceIDElement);
		String traceID = traceIDElement.getText().trim();
		System.out.println("trace id : " + traceID);

		return traceID;
	}

	public void verifyDispositionPackedForTraceIDStateTransactionVerification(String traceID) {

		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);

		WebElement traceIDElement = getDriver().findElement(By
				.xpath("//a[contains(@onclick,'javascript:getTraceReport') and normalize-space()='" + traceID + "']"));

		click(traceIDElement, "Click on the first traceID found");
		String parentWindow = getDriver().getWindowHandle();
		switchWindow();
		waitForElementToBeDisplayed(getDriver(), headingTraceabilityReport, 30, 1);
		String disposition = webElementDispositionTraceabilityReport.getText().trim();
		Assert.assertTrue(disposition.equals("Packed"), "Disposition is not Packed");
		ern.enterLogAndCapture("Disposition is Packed for the selected trace ID", highlightElement(
				webElementDispositionTraceabilityReport, "Disposition is Packed for the selected trace ID"));
		getDriver().close();
		switchToParentWindow(parentWindow);
	}

	public void searchEventByTraceID(String traceID) {

		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headerFrame);
		click(xEventTab, "Click on events Link");

		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);

		waitForElementToBeDisplayed(getDriver(), headingTextEventSearch, 10, 1);

		scrollToWebElement(inputTraceIDEvents);
		sendKeys(inputTraceIDEvents, traceID, "Enter trace iD value");

		scrollToWebElement(searchButton);
		click(searchButton, "Click On Search Button");

		waitForElementToBeDisplayed(getDriver(), linkEventIDs, 30, 1);

	}

	public void verifyDispositionForTraceID(String eventType, String disposition) {

		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		WebElement eventIDLink = getDriver()
				.findElement(By.xpath("//td[text()='" + eventType + "']/../td/a[contains(@id,'uniqueEntId')]"));
		click(eventIDLink, "Click on the event link where eventType is " + eventType);
		String parentWindow = getDriver().getWindowHandle();
		switchWindow();
		getDriver().manage().window().maximize();
		waitForElementToBeDisplayed(getDriver(), headerEventDetails, 10, 1);
		WebElement dispositionElement = getDriver().findElement(By.xpath(
				"//td[text()='Disposition']/../following-sibling::tr/td[(count(//td[text()='Disposition']/preceding-sibling::td)+1)]"));
		String actualDisposition = dispositionElement.getText().trim();
		Assert.assertTrue(actualDisposition.equals(disposition), "Disposition is not updated to :" + disposition);
		ern.enterLogAndCapture("Disposition is updated to : " + disposition,
				highlightElement(dispositionElement, "Disposition is updated to : " + disposition));
		getDriver().close();
		switchToParentWindow(parentWindow);
	}

	public void searchEventByLotID(String lotID) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headerFrame);
		click(xEventTab, "Click on Event Link");

		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), headingTextEventSearch, 10, 1);
		if (linkShowSearch.getText().trim().equals("Show Search")) {
			click(linkShowSearch, "Click on Show Search");
		}
		waitForElementToBeDisplayed(getDriver(), tableAdvanceSearch, 10, 1);
		scrollToWebElement(xlotId);
		sendKeys(xlotId, lotID, "Enter Lot ID");
		scrollToWebElement(searchButton);
		click(searchButton, "Click on Search Button");
		waitForElementToBeDisplayed(getDriver(), EventSearch_sNumberDetails, 10, 1);
		scrollToWebElement(EventSearch_sNumberDetails);
	}

	public void verifyShipEventEventTime(int noOfCases) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);

		WebElement shipEventLink = getDriver().findElement(By.xpath("//td[text()='Ship']/preceding-sibling::td/a"));
		scrollToWebElement(shipEventLink);
		click(shipEventLink, "Click on Ship Event link");
		String parentWindow = getDriver().getWindowHandle();
		switchWindow();
		waitForElementToBeDisplayed(getDriver(), headerEventDetails, 10, 1);
		getDriver().manage().window().maximize();
		WebElement showItemLinkItemInfo = getDriver().findElement(By.xpath("//a[@id='anchor_1']"));
		click(showItemLinkItemInfo, "Expand Trace details");
		List<WebElement> caseList = getDriver()
				.findElements(By.xpath("//tbody[@id='tbyTraceReport_1']/tr[not(@style=\"display: none;\")]"));
		Assert.assertTrue(noOfCases == caseList.size(), "No of cases dont match with the ship event");
		ern.enterLogAndCapture("Total number of cases shipped matches",
				highlightElement(
						getDriver()
								.findElement(By.xpath("//table[@id='tblTraceReport_1']/tbody[@id='tbyTraceReport_1']")),
						"Total number of cases shipped matches"));

		WebElement epcis = getDriver().findElement(By.xpath("//img[@id='epcis1']/.."));
		scrollToWebElement(epcis);
		ern.enterLogAndCapture("Epcis link available", highlightElement(epcis, "Epcis link available"));

		getDriver().close();
		switchToParentWindow(parentWindow);
	}

	public void verifyAggregrateEventFor2ndPalletEventTime(int noOfCases) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);

		WebElement shipEventLink = getDriver()
				.findElement(By.xpath("(//td[text()='Aggregate']/preceding-sibling::td/a)[1]"));
		scrollToWebElement(shipEventLink);
		click(shipEventLink, "Click on Aggregate Event link for 2nd pallet");
		String parentWindow = getDriver().getWindowHandle();
		switchWindow();
		waitForElementToBeDisplayed(getDriver(), headerEventDetails, 10, 1);
		getDriver().manage().window().maximize();
		WebElement showItemLinkItemInfo = getDriver().findElement(By.xpath("//a[@id='anchor_1']"));
		click(showItemLinkItemInfo, "Expand Trace details");
		List<WebElement> caseList = getDriver()
				.findElements(By.xpath("//tbody[@id='tbyTraceReport_1']/tr[not(@style=\"display: none;\")]"));
		Assert.assertTrue(noOfCases == caseList.size(), "No of cases dont match with the ship event");
		ern.enterLogAndCapture("Total number of cases aggregarted for 2nd pallet matches",
				highlightElement(
						getDriver()
								.findElement(By.xpath("//table[@id='tblTraceReport_1']/tbody[@id='tbyTraceReport_1']")),
						"Total number of cases aggregate matches for 2nd pallet"));
		getDriver().close();
		switchToParentWindow(parentWindow);

	}

	public void verifyDisAggregrateEventFor1stPalletEventTime(int noOfCases) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);

		WebElement shipEventLink = getDriver()
				.findElement(By.xpath("(//td[text()='Disaggregate']/preceding-sibling::td/a)[1]"));
		scrollToWebElement(shipEventLink);
		click(shipEventLink, "Click on Disaggregate Event link for 2nd pallet");
		String parentWindow = getDriver().getWindowHandle();
		switchWindow();
		waitForElementToBeDisplayed(getDriver(), headerEventDetails, 10, 1);
		getDriver().manage().window().maximize();

		List<WebElement> caseList = getDriver().findElements(By.xpath(
				"//tbody[@id='itemsContainer']/tr//td[normalize-space()='Case']/..//a[contains(@onclick,'javascript:getTraceReport')]"));
		Assert.assertTrue(noOfCases == caseList.size(), "No of cases dont match with the ship event");
		ern.enterLogAndCapture("Total number of cases disaggregarted for 1st pallet matches",
				highlightElement(getDriver().findElement(By.xpath("//tbody[@id='itemsContainer']")),
						"Total number of cases Disaggregated matches for 1st pallet"));
		getDriver().close();
		switchToParentWindow(parentWindow);

	}

	public void verifyEventsCountByEventType(int shipEvents, String eventType) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);

		List<WebElement> shipEventLink = getDriver()
				.findElements(By.xpath("//td[text()='" + eventType + "']/preceding-sibling::td/a"));
		Assert.assertTrue(shipEventLink.size() == shipEvents, "No of " + eventType + " events dont match");
	}

	public void verifyBatchSummaryReportEventTime(String lotNumber, int noOfPackedProduct, int noOfIntransitProduct,
			int noOfPackedCases, int noOfIntransitCases, int noOfPackedPallets) throws Exception {

		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headerFrame);
		click(reports, "Click on Reports Link");

		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), webElementHeadingSelectBelowToViewReport, 10, 1);

		click(linkBatchSummaryReport, "Click on Batch Summary Report");
		waitForElementToBeDisplayed(getDriver(), headingBatchSummaryReport, 10, 1);
		JavaScriptExecutorUtil.waitUntilJavaScriptCompletes(getDriver());
		new WebDriverWait(getDriver(), Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(inputLotIDBatchSummaryReport));
		click(inputLotIDBatchSummaryReport, "Click on input Field LotID of BatchSummaryReport");
		Thread.sleep(3000);
		sendKeys(inputLotIDBatchSummaryReport, lotNumber, "Enter Lot ID");

		scrollToWebElement(runReport);
		click(runReport, "Click on Run Report");

		String parentWindow = getDriver().getWindowHandle();
		switchWindow();
		getDriver().manage().window().maximize();
		waitForElementToBeDisplayed(getDriver(), webElelemntHeaderBatchSummaryReport, 10, 1);

		ern.enterLogAndCapture("Batch Summary Report Generated successfully",
				highlightElement(webElelemntHeaderBatchSummaryReport, "Batch Summary Report Generated successfully"));

		WebElement casewithPacked = getDriver().findElement(By.xpath(
				"(//table[@id='__bookmark_1']/tbody/tr[td/div[text()='Case'] and td/div[text()='Packed']]/td/div)[7]"));
		String countCaseWithPacked = casewithPacked.getText().trim();
		Assert.assertTrue(countCaseWithPacked.equals(noOfPackedCases + ""),
				"Total count of packed cases matches with expected");
		ern.enterLogAndCapture("Total count of packed cases matches with expected",
				highlightElement(casewithPacked, "Total count of packed cases matches with expected"));

		WebElement casewithInTransit = getDriver().findElement(By.xpath(
				"(//table[@id='__bookmark_1']/tbody/tr[td/div[text()='Case'] and td/div[text()='In Transit']]/td/div)[7]"));
		String countCaseWithInTransit = casewithInTransit.getText().trim();
		Assert.assertTrue(countCaseWithInTransit.equals(noOfIntransitCases + ""),
				"Total count of in transit cases matches with expected");
		ern.enterLogAndCapture("Total count of in transit cases matches with expected",
				highlightElement(casewithInTransit, "Total count of in transit cases matches with expected"));

		WebElement productwithInTransit = getDriver().findElement(By.xpath(
				"(//table[@id='__bookmark_1']/tbody/tr[td/div[text()='Product'] and td/div[text()='In Transit']]/td/div)[7]"));
		String countProductWithInTransit = productwithInTransit.getText().trim();
		Assert.assertTrue(countProductWithInTransit.equals(noOfIntransitProduct + ""),
				"Total count of in transit products matches with expected");
		ern.enterLogAndCapture("Total count of in transit products matches with expected",
				highlightElement(productwithInTransit, "Total count of in transit products matches with expected"));

		WebElement productwithPacked = getDriver().findElement(By.xpath(
				"(//table[@id='__bookmark_1']/tbody/tr[td/div[text()='Product'] and td/div[text()='Packed']]/td/div)[7]"));
		String countProductWithPacked = productwithPacked.getText().trim();
		Assert.assertTrue(countProductWithPacked.equals(noOfPackedProduct + ""),
				"Total count of packed products matches with expected");
		ern.enterLogAndCapture("Total count of packed products matches with expected",
				highlightElement(productwithPacked, "Total count of packed products matches with expected"));

		WebElement palletwithPacked = getDriver().findElement(By.xpath(
				"(//table[@id='__bookmark_1']/tbody/tr[td/div[text()='Pallet'] and td/div[text()='Packed']]/td/div)[7]"));
		String countPalletWithPacked = palletwithPacked.getText().trim();
		Assert.assertTrue(countPalletWithPacked.equals(noOfPackedPallets + ""),
				"Total count of packed products matches with expected");
		ern.enterLogAndCapture("Total count of packed Pallet matches with expected",
				highlightElement(palletwithPacked, "Total count of packed Pallet matches with expected"));

		getDriver().close();
		switchToParentWindow(parentWindow);
	}

	public void uploadSecondShipXmlForEuSATest() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SharedCode_NonSAU_Ship2.xml";
		sendKeys(chooseFile, filePath, "Selecting second shipment xml file to upload for Eu SA Regression Test");
	}

	public void uploadAggregateXmlForBhSATest() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "BAHSAQA2_Comm_Agg.xml";
		sendKeys(chooseFile, filePath, "Selecting aggregate file to upload for Bh SA Regression Test");
	}

	public void uploadFirstShipXmlForBhSATest() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SharedCode_Bahrain_SA_Ship3.xml";
		sendKeys(chooseFile, filePath, "Selecting first shipment xml file to upload for Bh SA Regression Test");
	}

	public void uploadSecondShipXmlForBhSATest() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SharedCode_Bahrain_SA_Ship4.xml";
		sendKeys(chooseFile, filePath, "Selecting second shipment xml file to upload for Bh SA Regression Test");
	}
	
	public void uploadSwlDev01() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SWLDEV01.xml";
		sendKeys(chooseFile, filePath, "Selecting SWLDEV01 file to upload for swiss log");
	}
	
	public void uploadSwlDev02() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SWLDEV02.xml";
		sendKeys(chooseFile, filePath, "Selecting SWLDEV02 file to upload for swiss log");
	}
	
	public void uploadSwlDev03() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SWLDEV03.xml";
		sendKeys(chooseFile, filePath, "Selecting SWLDEV03 file to upload for swiss log");
	}
	
	public void uploadSwlDev04() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SWLDEV04.xml";
		sendKeys(chooseFile, filePath, "Selecting SWLDEV04 file to upload for swiss log");
	}
	
	public void uploadSwlDev06() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SWLDEV06.xml";
		sendKeys(chooseFile, filePath, "Selecting SWLDEV06 file to upload for swiss log");
	}
	
	public void uploadSwlDev07() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SWLDEV07.xml";
		sendKeys(chooseFile, filePath, "Selecting SWLDEV07 file to upload for swiss log");
	}
	
	public void uploadPalletAggregation() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "pallet_aggregation_swisslog.xml";
		sendKeys(chooseFile, filePath, "Selecting pallet_aggregation_swisslog file to upload for swiss log");
	}
	
	public void uploadSwissLogShipment() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "Swisslog_shipment_DEV.xml";
		sendKeys(chooseFile, filePath, "Selecting Swisslog_shipment_DEV file to upload for swiss log");
	}
}
