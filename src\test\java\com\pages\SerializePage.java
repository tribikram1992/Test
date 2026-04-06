package com.pages;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.testng.Assert;

import com.tests.BaseTest;
import com.utils.JavaScriptExecutorUtil;
import com.utils.TestData;

public class SerializePage extends BasePage {

	private static final Logger LOGGER = Logger.getLogger(SerializePage.class.getName());

	// ==================== CONSTANTS ====================
	TestData td = new TestData();
	// Wait timeouts
	private static final int DEFAULT_WAIT_TIMEOUT = 30;
	private static final int DEFAULT_POLL_INTERVAL = 3;
	private static final int SHORT_WAIT_TIMEOUT = 20;
	private static final int LONG_WAIT_TIMEOUT = 40;
	private static final int LONG_POLL_INTERVAL = 4;
	
	// Sleep durations (in milliseconds)
	private static final int LONG_SLEEP_MS = 15000;
	private static final int MEDIUM_SLEEP_MS = 10000;
	
	// Site IDs
	private static final String SITE_ID_DEFAULT = "22798";
	private static final String SITE_ID_EU_REGRESSION = "122387";
	private static final String SITE_ID_SWL_DEV04 = "122392";
	
	// Product Codes
	private String PRODUCT_CODE_DEFAULT = td.getTestData(BaseTest.environment, "palletCode");
	private static final String PRODUCT_CODE_SWISSLOG_TEST = "0085412-3";
	private String PRODUCT_CODE_BAHRAIN_CASE = (String) td.getTestDataByPath(BaseTest.environment,
			"TestCase.BahrainRegressionTest.caseCode");
	private static final String PRODUCT_CODE_KOREA_GG_CASE = "58806553002549";
	private static final String PRODUCT_CODE_SWISSLOG_CASE = "50303380102049";
	private static final String PRODUCT_CODE_EU_PALLET = "5413760-3";
	private static final String PRODUCT_CODE_SWL_DEV04_CASE = "50303380116206";
	
	// Lot Number Prefixes
	private static final String LOT_PREFIX_UAE = "UAE";
	private static final String LOT_PREFIX_BAHRAIN = "BAH";
	private static final String LOT_PREFIX_SAUDI = "SAD";
	private static final String LOT_PREFIX_KOREA = "KR";
	private static final String LOT_PREFIX_KOREA_GS = "KRG";
	private static final String LOT_PREFIX_SWISSLOG = "SWL";
	private static final String LOT_PREFIX_EU = "EUR";
	private static final String LOT_PREFIX_EU_SA = "EUSA";
	private static final String LOT_PREFIX_BH_SA = "BHSA";
	
	// XML File Names - Aggregation
	private static final String XML_UAE = "UAE070725.xml";
	private static final String XML_BAHRAIN_COMM_AGG = "BAHQA1_Comm_Agg.xml";
	private static final String XML_SA_COMM_AGG = "SA_Comm_Agg_Event.xml";
	private static final String XML_SA_IMPORT = "SA_IMPORT.xml";
	private static final String XML_SA_EXPORT = "SA_EXPORT.xml";
	private static final String XML_SA_NEGATIVE = "SA_Negative.xml";
	private static final String XML_KOREA_GG = "KR030425.xml";
	private static final String XML_KOREA_GS = "KRG040425.xml";
	private static final String XML_SWISSLOG_AGG = "SWL0704D.xml";
	private static final String XML_EU_COMM_AGG = "EURTCommissionAggregate.xml";
	private static final String XML_SHARED_EU_SA = "SharedCode_EUSAQA1.xml";
	private static final String XML_SHARED_BH_SA = "BAHSAQA2_Comm_Agg.xml";
	
	// XML File Names - Shipment
	private static final String XML_UAE_SHIPMENT = "UAE070725-Shipment.xml";
	private static final String XML_BAHRAIN_SHIP = "Bahrain_Ship.xml";
	private static final String XML_SA_SHIPMENT = "SA_Shipment.xml";
	private static final String XML_SA_IMPORT_SHIPMENT = "SA_ImportShipment.xml";
	private static final String XML_SA_EXPORT_SHIPMENT = "SA_ExportShipment.xml";
	private static final String XML_SA_EXPORT_CANCEL = "SA_ExportShipmentCancel.xml";
	private static final String XML_KOREA_GG_SHIPMENT = "KoreaGG_Shipment.xml";
	private static final String XML_KOREA_GS_SHIPMENT = "KoreaGS_Shipment.xml";
	private static final String XML_SWISSLOG_SHIPMENT = "SwisslogUS_shipment.xml";
	private static final String XML_EU_SHIPMENT = "EURTShippingHalleToSA8159002.xml";
	private static final String XML_SHARED_NON_SAU_SHIP = "SharedCode_NonSAU_Ship.xml";
	private static final String XML_SHARED_NON_SAU_SHIP2 = "SharedCode_NonSAU_Ship2.xml";
	private static final String XML_SHARED_BH_SA_SHIP3 = "SharedCode_Bahrain_SA_Ship3.xml";
	private static final String XML_SHARED_BH_SA_SHIP4 = "SharedCode_Bahrain_SA_Ship4.xml";
	private static final String XML_EVENT_TIME_SHIPPING = "5829ETT_Shipping_Testing.xml";
	
	// XML Tag Names
	private static final String TAG_EVENT_TIME = "eventTime";
	private static final String TAG_EPC = "epc";
	private static final String TAG_PARENT_ID = "parentID";
	private static final String TAG_LOT_NUMBER_GS1 = "gs1ushc:lotNumber";
	private static final String TAG_LOT_NUMBER_CBVMDA = "cbvmda:lotNumber";
	private static final String TAG_BIZ_TRANSACTION = "bizTransaction";
	private static final String TAG_DESTINATION = "destination";
	private static final String TAG_SOURCE = "source";
	
	// ==================== INSTANCE VARIABLES ====================

	public String lotNumber;
	public String bizTransNumber;
	public String lotNumberOneSwissLog;
	public String lotNumberTwoSwissLog;
	public String lotNumberThreeSwissLog;
	public String lotNumberFourSwissLog;
	public String lotNumberFiveSwissLog;
	public String lotNumberSixSwissLog;
	public static String[] abc = new String[5];
	public static String[] palletBahrain = new String[2];
	public static String[] palletSwissTwo = new String[2];
	public static String[] palletSwissThree = new String[2];
	public static String[] palletSwissFour = new String[2];
	public static String[] caseBahrain = new String[2];
	public static String[] products = new String[1];
	public static String[] cases = new String[1];
	public static String[] pallets = new String[1];
	public static String[] caseSwissTwo = new String[2];
	public static String[] caseSwissThree = new String[2];
	public static String[] def = new String[5];
	public static String[] euSerialNumber = new String[15];
	public static String[] partialPallets = new String[2];
	public static String[] caseSwissSix = new String[1];
	public static String[] caseSwissSeven = new String[2];

	public SerializePage(WebDriver driverInstance) {
		super(BaseTest.getDriver()); // Pass the ThreadLocal WebDriver to BasePage
		this.td = new TestData(); // Create a new instance for each thread
		PageFactory.initElements(getDriver(), this); // Initialize elements
	}

	// ==================== HELPER METHODS ====================
	
	/**
	 * Constructs the full path to a resource file in the test resources directory.
	 * @param fileName The name of the file
	 * @return The full path to the file
	 */
	private String getResourcePath(String fileName) {
		return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", fileName).toString();
	}
	
	/**
	 * Sleeps for the specified duration, handling InterruptedException.
	 * @param milliseconds The duration to sleep in milliseconds
	 */
	private void sleepSafely(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, "Sleep interrupted", e);
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Generates a lot number with the specified prefix and random digits.
	 * @param prefix The prefix for the lot number
	 * @param digits The number of random digits to append
	 * @return The generated lot number
	 */
	private String generateLotNumber(String prefix, int digits) {
		return prefix + RandomStringUtils.randomNumeric(digits);
	}
	
	/**
	 * Switches to body frame and waits for an element.
	 * @param element The element to wait for
	 * @param timeout Timeout in seconds
	 * @param pollInterval Poll interval in seconds
	 */
	private void switchToBodyFrameAndWait(WebElement element, int timeout, int pollInterval) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), element, timeout, pollInterval);
	}
	
	/**
	 * Enters product code and selects from dropdown.
	 * @param pCode The product code to enter
	 * @param message Log message
	 */
	private void enterProductCodeAndSelect(String pCode, String message) {
		switchToBodyFrameAndWait(productCode, DEFAULT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		sendKeys(productCode, pCode, message);
		sleepSafely(3000);
		click(Select_ProductDropdown_Opt1, "Selecting product from dropdown");
	}
	
	/**
	 * Clicks allocate button and fetches serial numbers into specified array.
	 * @param targetArray The array to store serial numbers
	 * @param count Number of serial numbers to fetch
	 * @param message Log message
	 */
	private void clickAllocateAndFetch(String[] targetArray, int count, String message) {
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, DEFAULT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		for (int i = 1; i <= count; i++) {
			WebElement ele = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			targetArray[i - 1] = ele.getText();
		}
		getDriver().switchTo().defaultContent();
	}

	// ==================== PAGE ELEMENTS ====================

	@FindBy(id = "prodCode2")
	WebElement productCode;

	@FindBy(xpath = "//frame[@name='body']")
	WebElement bodyFrame;

	@FindBy(id = "txtNumberOfSerialNumber_1")
	WebElement serialNumber;

	@FindBy(id = "site_id")
	WebElement siteId;

	@FindBy(id = "btnGenerate")
	WebElement allocateButton;

	@FindBy(id = "showExcelIcon_1")
	WebElement excelIcon;

	// -----

	@FindBy(xpath = "//frame[@name='head']")
	WebElement headerFrame;
	@FindBy(xpath = "//input[@name='txtNumberOfSerialNumber']")
	WebElement xSerialNumber;
	@FindBy(xpath = "//input[@name='prodCode1']")
	WebElement xCode;
	@FindBy(xpath = "(//div[@id='ajax_listOfOptions']//div[@class='optionDivSelected'])[1]")
	// @FindBy(xpath = "//div[@id='1688662827657216']")
	WebElement Select_ProductDropdown_Opt1;
	@FindBy(xpath = "//a[@id='btnClear']")
	WebElement Clear_allocateSerialdata;
	@FindBy(xpath = "//select[@name='site_id']")
	WebElement xSiteId;
	@FindBy(xpath = "//option[@value='122391']")
	WebElement xSiteId_BXLessines;
	@FindBy(xpath = "//option[@value='122373']")
	WebElement xSiteId_BXHealthCareLessines;
	@FindBy(xpath = "//a[@id='btnPreview']")
	WebElement PreviewButton;
	@FindBy(xpath = "//input[@name='prodCode1']")
	WebElement pCode;
	@FindBy(xpath = "//input[@name='btnProductSearch']")
	WebElement pCode_Selector;
	@FindBy(xpath = "//input[@name='productCode']")
	WebElement SearchWindow_ProductCode;
	@FindBy(xpath = "//input[@name='btnGO']")
	WebElement SearchWindow_GO;
	@FindBy(xpath = "//input[@name='productId' and @type='radio']")
	WebElement SearchWindow_SelectOpt1;
	@FindBy(xpath = "//input[@name='Submit1']")
	WebElement SearchWindow_Submit;
	@FindBy(xpath = "//table/tbody/tr[2]/td[@class='body_text' and starts-with(text(), 'urn')]")
	WebElement Capture_sgtinSnumber;

	// -----

	public boolean validateSerializePageDisplayed() {
		switchToBodyFrameAndWait(productCode, DEFAULT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		return productCode.isDisplayed();
	}

	public void enterSerialNumber(String message, int count) {
		waitForElementToBeDisplayed(getDriver(), serialNumber, DEFAULT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		sendKeys(serialNumber, Integer.toString(count), message);
	}

	public void selectSiteId() {
		waitForElementToBeDisplayed(getDriver(), siteId, DEFAULT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		selectElementFromDropDownByValue(siteId, SITE_ID_DEFAULT);
	}

	public LoginPage clickAllocateButton(String message, int count) {
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, DEFAULT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		Assert.assertTrue(excelIcon.isDisplayed(), "After clicking on allocation excel files are not generated");
		// Resize array if needed
		if (count != abc.length) {
			abc = new String[count];
		}
		for (int i = 1; i <= count; i++) {
			WebElement ele = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			abc[i - 1] = ele.getText();
		}
		getDriver().switchTo().defaultContent();
		return new LoginPage();
	}

	public LoginPage massAllocate() {
		switchToBodyFrameAndWait(productCode, DEFAULT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		sendKeys(productCode, PRODUCT_CODE_DEFAULT, "New product entering in serialization tab");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+PRODUCT_CODE_DEFAULT+"_"+PRODUCT_CODE_DEFAULT+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, SHORT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		click(ele, "Click on the new product code");
		waitForElementToBeDisplayed(getDriver(), serialNumber, DEFAULT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		sendKeys(serialNumber, "1", "Enter lot number");
		selectSiteId();
		click(allocateButton, "Click on allocate button during lot id");
		waitForElementToBeDisplayed(getDriver(), excelIcon, DEFAULT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		Assert.assertTrue(excelIcon.isDisplayed(), "After clicking on allocation excel files are not generated");
		WebElement ship = getDriver().findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[2]/td[1]"));
		String text = ship.getText();
		String path = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator
				+ "resources" + File.separator + "UAE070725-Shipment.xml";

		Long n = randomNumber(6);
		String lotNum = "UAE" + Long.toString(n);
		lotNumber = lotNum;
		List<String> sl = new ArrayList<>();
		sl.add(text);
		updateTagWithValues(path, "epc", sl);
		updateLastBizTransaction(path, lotNumber);
		String path1 = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "UAE070725.xml";

		for (int i = 0; i < 5; i++) {
			System.out.println(abc[i]);
		}

		List<String> slNum = new ArrayList<>();
		slNum.add(text);
		for (int i = 0; i <= 1; i++) {
			for (int j = 0; j < 5; j++) {
				slNum.add(abc[j]);
			}
		}
		String time = getFixedTimestamp();
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String newTime = getFixedTimestamp();
		List<String> timeStamp = new ArrayList<>();
		timeStamp.add(time);
		timeStamp.add(time);
		timeStamp.add(newTime);

		List<String> lNum = new ArrayList<>();
		lNum.add(lotNum);
		lNum.add(lotNum);
		updateTagWithValues(path1, "eventTime", timeStamp);
		updateTagWithValues(path1, "gs1ushc:lotNumber", lNum);
		updateTagWithValues(path1, "epc", slNum);
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String time1 = getFixedTimestamp();
		List<String> times = new ArrayList<>();
		times.add(time1);
		updateTagWithValues(path, "eventTime", times);
		updateTagWithValues(path1, "parentID", sl);

		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void enterProductCode(String message, String data, String country) {
		waitForElementToBeDisplayed(getDriver(), productCode, SHORT_WAIT_TIMEOUT, 2);
		sendKeys(productCode, data, message);
		WebElement ele = getDriver()
				.findElement(By.xpath("//div[contains(text(),'" + data + "_" + data + "')]"));
		waitForElementToBeDisplayed(getDriver(), ele, SHORT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		click(ele, "Click on the product code for "+country);
	}

	public void enterSerialNumberForBahrain(String message) {
		waitForElementToBeDisplayed(getDriver(), serialNumber, DEFAULT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		sendKeys(serialNumber, "10", message);
	}

	public LoginPage enterPalletNumberForBahrain(String message, int count) {
		switchToBodyFrameAndWait(productCode, DEFAULT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		sendKeys(productCode, PRODUCT_CODE_DEFAULT, "Pallet Number entering in serialization tab for Bahrain");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+PRODUCT_CODE_DEFAULT+"_"+PRODUCT_CODE_DEFAULT+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, SHORT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		click(ele, "Click on the new product code for Bahrain");
		selectSiteId();
		waitForElementToBeDisplayed(getDriver(), serialNumber, DEFAULT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, DEFAULT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			palletBahrain[i - 1] = el.getText();
		}
		getDriver().switchTo().defaultContent();
		return new LoginPage();
	}

	public void enterCaseInformationForBahrain(String message, int count) {
		switchToBodyFrameAndWait(productCode, DEFAULT_WAIT_TIMEOUT, DEFAULT_POLL_INTERVAL);
		sendKeys(productCode, PRODUCT_CODE_BAHRAIN_CASE, "Pallet Number entering in serialization tab for Bahrain");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+PRODUCT_CODE_BAHRAIN_CASE+"_"+PRODUCT_CODE_BAHRAIN_CASE+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new product code for Bahrain");
		selectSiteId();
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			caseBahrain[i - 1] = el.getText();

		}
		getDriver().switchTo().defaultContent();
	}

	public void updateAggregrateXmlForBahrain() {
		String filePath = getResourcePath(XML_BAHRAIN_COMM_AGG);
		List<String> timestamps = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			timestamps.add(getFixedTimestamp());
			if (i < 6) sleepSafely(LONG_SLEEP_MS);
		}
		updateTagWithValues(filePath, TAG_EVENT_TIME, timestamps);
		
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			epcData.add(abc[i]);
		}
		for (int i = 0; i < 2; i++) {
			epcData.add(caseBahrain[i]);
		}
		for (int i = 0; i < 2; i++) {
			epcData.add(palletBahrain[i]);
		}
		for (int i = 0; i < 10; i++) {
			epcData.add(abc[i]);
		}
		for (int i = 0; i < 2; i++) {
			epcData.add(caseBahrain[i]);
		}
		updateTagWithValues(filePath, TAG_EPC, epcData);
		
		List<String> parentData = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			parentData.add(caseBahrain[i]);
		}
		for (int i = 0; i < 2; i++) {
			parentData.add(palletBahrain[i]);
		}
		updateTagWithValues(filePath, TAG_PARENT_ID, parentData);
		
		lotNumber = generateLotNumber(LOT_PREFIX_BAHRAIN, 6);
		List<String> lotNum = Arrays.asList(lotNumber, lotNumber, lotNumber);
		updateTagWithValues(filePath, TAG_LOT_NUMBER_CBVMDA, lotNum);
	}

	public LoginPage updateShipmentXmlForBahrain() {
		String filePath = getResourcePath(XML_BAHRAIN_SHIP);
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			epcData.add(palletBahrain[i]);
		}
		updateTagWithValues(filePath, TAG_EPC, epcData);
		String time = getFixedTimestamp();
		List<String> timeSt = new ArrayList<>();
		timeSt.add(time);
		updateTagWithValues(filePath, TAG_EVENT_TIME, timeSt);
		return new LoginPage();
	}

	public void updateAggregationEventForSaudiInternal() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SA_Comm_Agg_Event.xml";

		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			epcData.add(abc[i]);
		}
		for (int i = 0; i < 2; i++) {
			epcData.add(palletBahrain[i]);
		}
		for (int i = 0; i < 10; i++) {
			epcData.add(abc[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);

		List<String> parentData = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			parentData.add(palletBahrain[i]);
		}
		updateTagWithValues(filePath, "parentID", parentData);

		Long n = randomNumber(6);
		String text = "SAD" + Long.toString(n);
		lotNumber = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "gs1ushc:lotNumber", lotNum);
		String[] time = new String[4];

		List<String> a = new ArrayList<>();
		for (int i = 0; i < time.length; i++) {
			time[i] = getFixedTimestamp();
			a.add(time[i]);
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTagWithValues(filePath, "eventTime", a);
	}

	public LoginPage updateShipmentXmlForSaudiInternal() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SA_Shipment.xml";
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			epcData.add(palletBahrain[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		String time = getFixedTimestamp();
		List<String> timeSt = new ArrayList<>();
		timeSt.add(time);
		updateTagWithValues(filePath, "eventTime", timeSt);
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void updateAggregationEventForSaudiImport() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SA_IMPORT.xml";

		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			epcData.add(abc[i]);
		}
		for (int i = 0; i < 2; i++) {
			epcData.add(palletBahrain[i]);
		}
		for (int i = 0; i < 10; i++) {
			epcData.add(abc[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);

		List<String> parentData = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			parentData.add(palletBahrain[i]);
		}
		updateTagWithValues(filePath, "parentID", parentData);

		Long n = randomNumber(6);
		String text = "SAD" + Long.toString(n);
		lotNumber = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "gs1ushc:lotNumber", lotNum);
		String[] time = new String[4];

		List<String> a = new ArrayList<>();
		for (int i = 0; i < time.length; i++) {
			time[i] = getFixedTimestamp();
			a.add(time[i]);
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTagWithValues(filePath, "eventTime", a);
	}

	public LoginPage updateShipmentXmlForSaudiImport() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SA_ImportShipment.xml";
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			epcData.add(palletBahrain[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		String time = getFixedTimestamp();
		List<String> timeSt = new ArrayList<>();
		timeSt.add(time);
		updateTagWithValues(filePath, "eventTime", timeSt);
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void updateAggregationEventForSaudiExport() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SA_EXPORT.xml";

		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			epcData.add(abc[i]);
		}
		for (int i = 0; i < 2; i++) {
			epcData.add(palletBahrain[i]);
		}
		for (int i = 0; i < 10; i++) {
			epcData.add(abc[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);

		List<String> parentData = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			parentData.add(palletBahrain[i]);
		}
		updateTagWithValues(filePath, "parentID", parentData);

		Long n = randomNumber(6);
		String text = "SAD" + Long.toString(n);
		lotNumber = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "gs1ushc:lotNumber", lotNum);
		String[] time = new String[4];

		List<String> a = new ArrayList<>();
		for (int i = 0; i < time.length; i++) {
			time[i] = getFixedTimestamp();
			a.add(time[i]);
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTagWithValues(filePath, "eventTime", a);
	}

	public void updateShipmentXmlForSaudiExport() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SA_ExportShipment.xml";
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			epcData.add(palletBahrain[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		String time = getFixedTimestamp();
		List<String> timeSt = new ArrayList<>();
		timeSt.add(time);
		updateTagWithValues(filePath, "eventTime", timeSt);
	}

	public LoginPage updateShipmentXmlForSaudiExportCancel() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SA_ExportShipmentCancel.xml";
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			epcData.add(palletBahrain[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String time = getFixedTimestamp();
		List<String> timeSt = new ArrayList<>();
		timeSt.add(time);
		updateTagWithValues(filePath, "eventTime", timeSt);
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public LoginPage updateXmlForSaudiNegative() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SA_Negative.xml";
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			epcData.add(abc[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		Long n = randomNumber(6);
		String text = "Sad" + Long.toString(n);
		lotNumber = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		updateTagWithValues(filePath, "gs1ushc:lotNumber", lotNum);
		String time = getFixedTimestamp();
		List<String> timeSt = new ArrayList<>();
		timeSt.add(time);
		updateTagWithValues(filePath, "eventTime", timeSt);
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void enterProductCodeForKorea(String message, String pCode) {
		waitForElementToBeDisplayed(getDriver(), productCode, 40, 4);
		sendKeys(productCode, pCode, message);
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'" + pCode + "_" + pCode + "')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, message + "selecting unit");
	}

	public void enterCaseInformationForKorea(String message, String data, int count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), productCode, 30, 3);
		
		sendKeys(productCode, data, "Case Number entering in serialization tab for Korea GG");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+data+"_"+data+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new product code for Korea GG");
		selectSiteId();
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number for Korea GG");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			caseBahrain[i - 1] = el.getText();

		}
		getDriver().switchTo().defaultContent();
	}

	public void updateCommAggregationXmlForKoreaGG() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "KR030425.xml";
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			epcData.add(abc[i]);
		}
		for (int i = 0; i < 2; i++) {
			epcData.add(caseBahrain[i]);
		}
		for (int i = 0; i < 10; i++) {
			epcData.add(abc[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		Long n = randomNumber(6);
		String text = "KR" + Long.toString(n);
		lotNumber = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "gs1ushc:lotNumber", lotNum);
		List<String> parent = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			parent.add(caseBahrain[i]);
		}
		updateTagWithValues(filePath, "parentID", parent);
		String[] time = new String[4];

		List<String> a = new ArrayList<>();
		for (int i = 0; i < time.length; i++) {
			time[i] = getFixedTimestamp();
			a.add(time[i]);
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTagWithValues(filePath, "eventTime", a);
	}

	public LoginPage updateShipmentXmlForKoreaGG() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "KoreaGG_Shipment.xml";
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			epcData.add(caseBahrain[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String time = getFixedTimestamp();
		List<String> timeSt = new ArrayList<>();
		timeSt.add(time);
		updateTagWithValues(filePath, "eventTime", timeSt);
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void enterCaseInformationForKoreaGS(String message, String caseCode, int count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), productCode, 30, 3);
		sendKeys(productCode, caseCode, "Case Number entering in serialization tab for Korea GS");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+caseCode+"_"+caseCode+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new product code for Korea GS");
		selectSiteId();
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number for Korea GS");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			palletBahrain[i - 1] = el.getText();

		}
		getDriver().switchTo().defaultContent();
	}

	public void updateCommAggregationXmlForKoreaGS() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "KRG040425.xml";
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			epcData.add(abc[i]);
		}
		for (int i = 0; i < 2; i++) {
			epcData.add(palletBahrain[i]);
		}
		for (int i = 0; i < 10; i++) {
			epcData.add(abc[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		Long n = randomNumber(6);
		String text = "KRG" + Long.toString(n);
		lotNumber = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "gs1ushc:lotNumber", lotNum);
		List<String> parent = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			parent.add(palletBahrain[i]);
		}
		updateTagWithValues(filePath, "parentID", parent);
		String[] time = new String[4];

		List<String> a = new ArrayList<>();
		for (int i = 0; i < time.length; i++) {
			time[i] = getFixedTimestamp();
			a.add(time[i]);
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTagWithValues(filePath, "eventTime", a);
	}

	public LoginPage updateShipmentXmlForKoreaGS() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "KoreaGS_Shipment.xml";
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			epcData.add(palletBahrain[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String time = getFixedTimestamp();
		List<String> timeSt = new ArrayList<>();
		timeSt.add(time);
		updateTagWithValues(filePath, "eventTime", timeSt);
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void enterProductCodeForSwissLogUs(String message, String pCode) {
		waitForElementToBeDisplayed(getDriver(), productCode, 20, 2);
		sendKeys(productCode, pCode, message);
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'" + pCode + "_" + pCode + "')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the product code for Swiss log");
	}

	public LoginPage enterPalletNumberForSwissLogTest(String message, String palletCode, int count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), productCode, 30, 3);
		sendKeys(productCode, palletCode, "Pallet Number entering in serialization tab for Swiss Log Test");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'" + palletCode + "_" + palletCode + "')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new product code for Swiss Log Us Test");
		selectSiteId();
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number for swiss log us test");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			palletBahrain[i - 1] = el.getText();
		}
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void enterCaseInformationForSwissLog(String message,String secondCase, int count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), productCode, 30, 3);
		sendKeys(productCode, secondCase, "Case Number entering in serialization tab for Swiss log");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'" + secondCase + "_" + secondCase + "')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new case code for swiss log");
		selectSiteId();
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter count for case in Swiss log test");
		click(allocateButton, message);
		if (count > caseBahrain.length) {
			caseBahrain = new String[count]; // Create new array with required size
		}
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			caseBahrain[i - 1] = el.getText();
		}
		getDriver().switchTo().defaultContent();
	}

	public void updateAggregrateXmlForSwissLog() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SWL0704D.xml";

		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			epcData.add(palletBahrain[i]);
		}
		for (int i = 0; i < 4; i++) {
			epcData.add(caseBahrain[i]);
		}
		for (int i = 0; i < 20; i++) {
			epcData.add(abc[i]);
		}
		for (int i = 0; i < 20; i++) {
			epcData.add(abc[i]);
		}
		for (int i = 0; i < 4; i++) {
			epcData.add(caseBahrain[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		List<String> parentData = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			parentData.add(caseBahrain[i]);
		}
		for (int i = 0; i < 2; i++) {
			parentData.add(palletBahrain[i]);
		}

		updateTagWithValues(filePath, "parentID", parentData);
		Long n = randomNumber(6);
		String text = "SWL" + Long.toString(n);
		lotNumber = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "gs1ushc:lotNumber", lotNum);
		String[] time = new String[9];
		List<String> a = new ArrayList<>();
		for (int i = 0; i < time.length; i++) {
			time[i] = getFixedTimestamp();
			a.add(time[i]);
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTagWithValues(filePath, "eventTime", a);
	}

	public LoginPage updateShipmentXmlForSwissLog() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SwisslogUS_shipment.xml";
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			epcData.add(palletBahrain[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		String time = getFixedTimestamp();
		List<String> timeSt = new ArrayList<>();
		timeSt.add(time);
		updateTagWithValues(filePath, "eventTime", timeSt);
		List<String> bizValues = new ArrayList<>();
		Long n = randomNumber(7);
		String text = "CA" + Long.toString(n);
		bizTransNumber = text;
		bizValues.add(text);
		bizValues.add("CA9588692");
		bizValues.add("503015-3556");
		bizValues.add("023021307275790800854123200000218");
		updateTagWithValues(filePath, "bizTransaction", bizValues);
		LoginPage lp = new LoginPage();
		return lp;
	}

	public LoginPage returnToLoginPage() {
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public LoginPage clickAllocateButtonForSwissLogUsTest(String message, int count) {
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		Assert.assertTrue(excelIcon.isDisplayed(), "After clicking on allocation excel files are not generated");
		if (count > abc.length) {
			abc = new String[count]; // Create new array with required size
		}
		if (count < abc.length) {
			abc = new String[count]; // Create new array with required size
		}
		for (int i = 1; i <= count; i++) {
			WebElement ele = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + i + "]/td[1]"));
			abc[i - 1] = ele.getText();
		}
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void clickAllocateButtonForEuPallet(String message, int count) {
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		Assert.assertTrue(excelIcon.isDisplayed(), "After clicking on allocation excel files are not generated");
		if (count > palletBahrain.length) {
			palletBahrain = new String[count]; // Create new array with required size
		}
		if (count < palletBahrain.length) {
			palletBahrain = new String[count]; // Create new array with required size
		}
		for (int i = 1; i <= count; i++) {
			WebElement ele = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			palletBahrain[i - 1] = ele.getText();
		}
		getDriver().switchTo().defaultContent();
	}

	public void updateAggregrateXmlForEuRegression() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "EURTCommissionAggregate.xml";

		List<String> epcData = new ArrayList<>();

		for (int i = 0; i < 100; i++) {
			epcData.add(abc[i]);
		}
		for (int i = 0; i < 100; i++) {
			epcData.add(def[i]);
		}
		for (int i = 0; i < 9; i++) {
			epcData.add(palletBahrain[i]);
		}

		for (int i = 0; i < 100; i++) {
			epcData.add(abc[i]);
		}

		for (int i = 0; i < 80; i++) {
			epcData.add(def[i]);
		}

		updateTagWithValues(filePath, "epc", epcData);
		List<String> parentData = new ArrayList<>();
		for (int i = 0; i < 9; i++) {
			parentData.add(palletBahrain[i]);
		}

		updateTagWithValues(filePath, "parentID", parentData);
		Long n = randomNumber(6);
		String text = "EUR" + Long.toString(n);
		lotNumber = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "cbvmda:lotNumber", lotNum);
		String[] time = new String[12];
		List<String> a = new ArrayList<>();
		for (int i = 0; i < time.length; i++) {
			time[i] = getFixedTimestamp();
			a.add(time[i]);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTagWithValues(filePath, "eventTime", a);
	}

	public LoginPage updateShipmentXmlForEuRegression() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "EURTShippingHalleToSA8159002.xml";
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			epcData.add(palletBahrain[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		String time = getFixedTimestamp();
		List<String> timeSt = new ArrayList<>();
		timeSt.add(time);
		updateTagWithValues(filePath, "eventTime", timeSt);
		String[] data = new String[2];
		data[0] = lotNumber;
		data[1] = "2024051208135754137606";
		List<String> bizData = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			bizData.add(data[i]);
		}
		updateTagWithValues(filePath, "bizTransaction", bizData);
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void clickAllocateButtonSecondHundred(String message, int count) {
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		Assert.assertTrue(excelIcon.isDisplayed(), "After clicking on allocation excel files are not generated");
		if (count > def.length) {
			def = new String[count]; // Create new array with required size
		}
		if (count < def.length) {
			def = new String[count]; // Create new array with required size
		}
		for (int i = 1; i <= count; i++) {
			WebElement ele = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + i + "]/td[1]"));
			def[i - 1] = ele.getText();
		}
		getDriver().switchTo().defaultContent();

	}

	public LoginPage clickAllocateButtonForEuRegression(String message, int count) {
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		Assert.assertTrue(excelIcon.isDisplayed(), "After clicking on allocation excel files are not generated");
		if (count > abc.length) {
			abc = new String[count]; // Create new array with required size
		}
		if (count < abc.length) {
			abc = new String[count]; // Create new array with required size
		}
		for (int i = 1; i <= count; i++) {
			WebElement ele = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + i + "]/td[1]"));
			abc[i - 1] = ele.getText();
		}
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void selectSiteIdForEuRegression() {
		waitForElementToBeDisplayed(getDriver(), siteId, 30, 3);
		selectElementFromDropDownByValue(siteId, "122387");
	}

	public void updateSerialNumberToChangeEvent() {
		int j = 95;
		for (int i = 0; i < 5; i++) {
			euSerialNumber[i] = def[j + i];
		}

		for (int i = 5; i < 10; i++) {
			euSerialNumber[i] = def[i];
		}

		for (int i = 10; i < 15; i++) {
			euSerialNumber[i] = abc[i];
		}
	}

	public void updateAggregrateXmlForSharedEuSaTest() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SharedCode_EUSAQA1.xml";

		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 15; i++) {
			epcData.add(abc[i]);
		}
		for (int i = 0; i < 3; i++) {
			epcData.add(palletBahrain[i]);
		}
		for (int i = 0; i < 15; i++) {
			epcData.add(abc[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		String[] time = new String[5];
		List<String> a = new ArrayList<>();
		for (int i = 0; i < time.length; i++) {
			time[i] = getFixedTimestamp();
			a.add(time[i]);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTagWithValues(filePath, "eventTime", a);
		Long n = randomNumber(4);
		String text = "EUSA" + Long.toString(n);
		lotNumber = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "cbvmda:lotNumber", lotNum);
		List<String> bizTransaction = new ArrayList<>();
		bizTransaction.add(text);
		bizTransaction.add(text);
		bizTransaction.add(text);
		updateTagWithValues(filePath, "bizTransaction", bizTransaction);
	}

	public void updateFirstShipmentXmlForSharedEuSaTest() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SharedCode_NonSAU_Ship.xml";

		List<String> epcData = new ArrayList<>();
		epcData.add(palletBahrain[0]);
		updateTagWithValues(filePath, "epc", epcData);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String time = getFixedTimestamp();
		List<String> a = new ArrayList<>();
		a.add(time);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateTagWithValues(filePath, "eventTime", a);

		List<String> lotNum = new ArrayList<>();
		lotNum.add(lotNumber);
		lotNum.add("08132025");
		lotNum.add(lotNumber);
		lotNum.add("20250813QA");
		updateTagWithValues(filePath, "bizTransaction", lotNum);
	}

	public LoginPage clickAllocateButtonForSharedEuSa(String message, int count) {
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		Assert.assertTrue(excelIcon.isDisplayed(), "After clicking on allocation excel files are not generated");
		if (count > abc.length) {
			abc = new String[count]; // Create new array with required size
		}
		if (count < abc.length) {
			abc = new String[count]; // Create new array with required size
		}
		for (int i = 1; i <= count; i++) {
			WebElement ele = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + i + "]/td[1]"));
			abc[i - 1] = ele.getText();
		}
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public LoginPage updateLotSummaryXmlForSwissLog(String lotNum) {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "LotSummary.xml";
		String time = getFixedTimestamp();
		List<String> timeSt = new ArrayList<>();
		timeSt.add(time);
		updateTagWithValues(filePath, "eventTime", timeSt);
		List<String> epcClassData = new ArrayList<>();
		String data = "urn:epc:class:lgtin:030338.0956801." + lotNum;
		epcClassData.add(data);
		updateTagWithValues(filePath, "epcClass", epcClassData);
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void enterCaseNumberForSwissLogMultipleScenarioTest(String message, String code, int count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), productCode, 30, 3);
		sendKeys(productCode, code, "Pallet Number entering in serialization tab for Swiss Log");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+code+"_"+code+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new product code for Swiss Log Us Test");
		selectSiteId();
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number for swiss log us test");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			caseBahrain[i - 1] = el.getText();
		}
		getDriver().switchTo().defaultContent();
	}

	public void enterCaseNumberForSwissLogMultipleScenarioTestDevTwo(String message, String code, int count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), productCode, 30, 3);
		sendKeys(productCode, code, "Pallet Number entering in serialization tab for Swiss Log");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+code+"_"+code+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new product code for Swiss Log Us Test");
		selectSiteId();
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number for swiss log us test");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			caseSwissTwo[i - 1] = el.getText();
		}
		getDriver().switchTo().defaultContent();
	}

	public LoginPage updateSwlDev01XmlForSwissLog() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SWLDEV01.XML";
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			epcData.add(caseBahrain[i]);
		}
		for (int i = 0; i < 2; i++) {
			epcData.add(abc[i]);
		}
		for (int i = 0; i < 2; i++) {
			epcData.add(abc[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		List<String> parentData = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			parentData.add(caseBahrain[i]);
		}
		updateTagWithValues(filePath, "parentID", parentData);

		List<String> timeSt = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			String time = getFixedTimestamp();
			timeSt.add(time);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTagWithValues(filePath, "eventTime", timeSt);
		Long n = randomNumber(6);
		String text = "SWL" + Long.toString(n);
		lotNumberOneSwissLog = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "gs1ushc:lotNumber", lotNum);
		LoginPage lp = new LoginPage();
		return lp;
	}

	public LoginPage updateSwlDev02XmlForSwissLog() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SWLDEV02.XML";
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			epcData.add(caseSwissTwo[i]);
		}
		for (int i = 0; i < 2; i++) {
			epcData.add(abc[i]);
		}
		for (int i = 0; i < 2; i++) {
			epcData.add(abc[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		List<String> parentData = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			parentData.add(caseSwissTwo[i]);
		}
		updateTagWithValues(filePath, "parentID", parentData);

		List<String> timeSt = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			String time = getFixedTimestamp();
			timeSt.add(time);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTagWithValues(filePath, "eventTime", timeSt);
		Long n = randomNumber(6);
		String text = "SWL" + Long.toString(n);
		lotNumberTwoSwissLog = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "gs1ushc:lotNumber", lotNum);
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void enterCaseNumberForSwissLogMultipleScenarioTestDevThree(String message, String caseCode,int count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), productCode, 30, 3);
		sendKeys(productCode, caseCode, "Pallet Number entering in serialization tab for Swiss Log");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+caseCode+"_"+caseCode+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new product code for Swiss Log Us Test");
		selectSiteId();
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number for swiss log us test");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			caseSwissThree[i - 1] = el.getText();
		}
		getDriver().switchTo().defaultContent();
	}

	public LoginPage updateSwlDev03XmlForSwissLog() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SWLDEV03.XML";
		List<String> epcData = new ArrayList<>();

		for (int i = 0; i < 2; i++) {
			epcData.add(caseSwissThree[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		List<String> timeSt = new ArrayList<>();
		String time = getFixedTimestamp();
		timeSt.add(time);
		updateTagWithValues(filePath, "eventTime", timeSt);
		Long n = randomNumber(6);
		String text = "SWL" + Long.toString(n);
		lotNumberThreeSwissLog = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		updateTagWithValues(filePath, "gs1ushc:lotNumber", lotNum);
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void enterPalletNumberForSwissLogMultipleScenarioTestForDevThree(String message,String pCode, int count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), productCode, 30, 3);
		sendKeys(productCode, pCode, "Pallet Number entering in serialization tab for Swiss Log");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+pCode+"_"+pCode+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new product code for Swiss Log Us Test");
		selectSiteIdForEuRegression();
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number for swiss log us test");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			palletBahrain[i - 1] = el.getText();
		}
		getDriver().switchTo().defaultContent();
	}

	public LoginPage updatePalletAggregationForFirstThreeXmls() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "pallet_aggregation_swisslog.xml";
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			epcData.add(palletBahrain[i]);
		}
		for (int i = 0; i < 1; i++) {
			epcData.add(caseBahrain[i]);
		}
		for (int i = 0; i < 1; i++) {
			epcData.add(caseSwissTwo[i]);
		}
		for (int i = 0; i < 2; i++) {
			epcData.add(caseSwissThree[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);

		List<String> parentData = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			parentData.add(palletBahrain[i]);
		}

		updateTagWithValues(filePath, "parentID", epcData);

		List<String> timeSt = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			String time = getFixedTimestamp();
			timeSt.add(time);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTagWithValues(filePath, "eventTime", timeSt);

		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void enterPalletNumberForSwissLogMultipleScenarioTestForSwlDev04(String message,String pCode, int count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), productCode, 30, 3);
		sendKeys(productCode, pCode, "Pallet Number entering in serialization tab for Swiss Log");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+pCode+"_"+pCode+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new product code for Swiss Log Us Test");
		selectSiteIdForEuRegression();
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number for swiss log us test");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			palletSwissTwo[i - 1] = el.getText();
		}
		getDriver().switchTo().defaultContent();
	}

	public void enterCaseInformationForSwlDev04(String message,String caseCode, int count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), productCode, 30, 3);
		sendKeys(productCode, caseCode, "Case Number entering in serialization tab for Swiss Log");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+caseCode+"_"+caseCode+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new product code for Swiss Log Us Test");
		waitForElementToBeDisplayed(getDriver(), siteId, 30, 3);
		selectElementFromDropDownByValue(siteId, "122392");
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number for swiss log us test");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			abc[i - 1] = el.getText();
		}
		getDriver().switchTo().defaultContent();
	}

	public LoginPage updateSwlDev04XmlForSwissLog() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SWLDEV04.XML";
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			epcData.add(palletSwissTwo[i]);
		}
		for (int i = 0; i < 2; i++) {
			epcData.add(abc[i]);
		}
		for (int i = 0; i < 2; i++) {
			epcData.add(abc[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		List<String> parentData = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			parentData.add(palletBahrain[i]);
		}
		updateTagWithValues(filePath, "parentID", parentData);

		List<String> timeSt = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			String time = getFixedTimestamp();
			timeSt.add(time);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTagWithValues(filePath, "eventTime", timeSt);
		Long n = randomNumber(6);
		String text = "SWL" + Long.toString(n);
		lotNumberFourSwissLog = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "gs1ushc:lotNumber", lotNum);
		LoginPage lp = new LoginPage();
		return lp;
	}

	// KAZ

	public void EnterPIDAandAllocate(String KAZPID, String SiteCode, String quantity) {
		// sendKeys(pCode, KAZPID, "Enter product code to search & allocate");
		// String parentWindow1 = getDriver().getWindowHandle();
		sendKeys(xCode, KAZPID, "Entered Pallet code ");
		click(Select_ProductDropdown_Opt1, "Allocate product");
		String SiteCodexpath = "//option[@value='" + SiteCode + "']";
		WebElement SiteID = getDriver().findElement(By.xpath(SiteCodexpath));

		click(xSiteId, "Click site id");
		click(SiteID, "Select Baxter Halle");
		xSerialNumber.clear();
		sendKeys(xSerialNumber, quantity, "Entered '" + quantity + "' as serial  number ");
		click(allocateButton, "Allocate product"); //if product ready in dev env
		//click(PreviewButton, "preview Allocate product");

		if (Capture_sgtinSnumber.isDisplayed()) {
			System.out.println(" SGTIN generated ");
		} else {
			System.out.println(" SGTIN not generated ");
		}
	}

	public String GetSerialNumber(String GetSerialNumber) {

		WebElement SerialNumber = getDriver()
				.findElement(By.xpath("//table/tbody/tr[2]/td[@class='body_text' and starts-with(text(), 'urn')]"));
		String sgtin_Snumber = SerialNumber.getText();
		System.out.println(sgtin_Snumber);
		click(Clear_allocateSerialdata, "Clear the allocation field");
		return sgtin_Snumber;
	}

	public void clickOnAllocate(String message) throws Exception {
		click(allocateButton, message);
		JavaScriptExecutorUtil.waitUntilJavaScriptCompletes(getDriver());
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
	}

	public void createSerialNumbers(String productCode, String siteID, Integer noOfSerials) throws Exception {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		enterProductCode("Enter product code", productCode);
		selectSiteId(siteID);
		enterSerialNumber("Enter No of Serial Numbers required", noOfSerials);
		clickOnAllocate("Click on Allocate Button");
	}

	public String[] fetchAllSerialNumbers(Integer count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		String[] itemType = new String[count];
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		scrollToWebElement(getDriver().findElement(
				By.xpath("//td[normalize-space()='Serial Number List' and @class='left_pannel_boldhdng']")));
		for (int i = 1; i <= count; i++) {
			WebElement serialNumbers = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			itemType[i - 1] = serialNumbers.getText();
		}
		getDriver().switchTo().defaultContent();
		Arrays.stream(itemType).forEach(System.out::println);
		return itemType;
	}

	public void clearAllocatedData() throws Exception {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		click(Clear_allocateSerialdata, "Clear the allocation field");
		JavaScriptExecutorUtil.waitUntilJavaScriptCompletes(getDriver());
	}

	public void updateAggregrateXmlForSoftDelete(String activeCustomer) {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SDT5829_Test.xml";

		/**
		 * Update time stamp for the XML Here there are 6 time stamps used below unique
		 * 3 time events noOfAddActionObjectEvent = 3; noOfAddActionAggregationEvent =
		 * 2; noOfObservationActionObjectEvent=1;
		 **/
		String[] time = new String[3];
		List<String> eventTimeList = new ArrayList<>();

		for (int i = 0; i < time.length; i++) {
			time[i] = getFixedTimestamp();
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int noOfAddActionObjectEvent = 3;
		int noOfAddActionAggregationEvent = 2;
		int noOfObservationActionObjectEvent = 1;
		for (int i = 0; i < noOfAddActionObjectEvent; i++) {
			eventTimeList.add(time[0]);
		}
		for (int i = 0; i < noOfAddActionAggregationEvent; i++) {
			eventTimeList.add(time[1]);
		}
		for (int i = 0; i < noOfObservationActionObjectEvent; i++) {
			eventTimeList.add(time[2]);
		}
		updateTagWithValues(filePath, "eventTime", eventTimeList);
		eventTimeList.stream().forEach(System.out::println);

		/**
		 * Update product information products - 2 Case - 1 Pallet - 1
		 **/
		// ObjectEvenet
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			epcData.add(products[i]);
		}
		for (int i = 0; i < 1; i++) {
			epcData.add(cases[i]);
		}
		for (int i = 0; i < 1; i++) {
			epcData.add(pallets[i]);
		}

		// AggregationEvent

		for (int i = 0; i < 2; i++) {
			epcData.add(products[i]);
		}
		for (int i = 0; i < 1; i++) {
			epcData.add(cases[i]);
		}

		// ObservationObjectEvent
		for (int i = 0; i < 1; i++) {
			epcData.add(pallets[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		epcData.stream().forEach(System.out::println);

		/** update parent data **/
		List<String> parentData = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			parentData.add(cases[i]);
		}
		for (int i = 0; i < 1; i++) {
			parentData.add(pallets[i]);
		}
		updateTagWithValues(filePath, "parentID", parentData);
		parentData.stream().forEach(System.out::println);
		/**
		 * update Lot Number lot number tag for Soft Delete is used as
		 * "gs1ushc:lotNumber"
		 **/

		Long n = randomNumber(6);
		String text = "5829SDT" + Long.toString(n);
		this.lotNumber = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "gs1ushc:lotNumber", lotNum);
		lotNum.stream().forEach(System.out::println);
		/** Update bizTransaction **/
		List<String> bizTransaction = new ArrayList<>();
		bizTransaction.add("TO-" + this.lotNumber);
		bizTransaction.add("SN-" + this.lotNumber);
		bizTransaction.add("PO-" + this.lotNumber);
		bizTransaction.add("NA");
		updateTagWithValues(filePath, "bizTransaction", bizTransaction);
		bizTransaction.stream().forEach(System.out::println);
		/**
		 * Update destination owning_party and location
		 **/
		List<String> destination = new ArrayList<>();
		destination.add(activeCustomer);
		destination.add(activeCustomer);
		updateTagWithValues(filePath, "destination", destination);
		destination.stream().forEach(System.out::println);
	}

	public void updateShippingXmlForSoftDelete(String customerDeleted) {

		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SDT5827_ShippingTest.xml";

		/**
		 * Update time stamp for the XML
		 **/
		List<String> eventTimeList = new ArrayList<>();
		eventTimeList.add(getFixedTimestamp());
		updateTagWithValues(filePath, "eventTime", eventTimeList);
		eventTimeList.stream().forEach(System.out::println);

		/**
		 * Update product information Pallet - 1
		 **/
		// ObjectEvenet
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			epcData.add(pallets[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		epcData.stream().forEach(System.out::println);

		/** Update bizTransaction **/
		List<String> bizTransaction = new ArrayList<>();
		bizTransaction.add("TO-" + this.lotNumber);
		bizTransaction.add("SN-" + this.lotNumber);
		bizTransaction.add("PO-" + this.lotNumber);
		updateTagWithValues(filePath, "bizTransaction", bizTransaction);
		bizTransaction.stream().forEach(System.out::println);
		/**
		 * Update destination gs1ushc:transferredToId and gs1ushc:shipToLocationId
		 **/
		List<String> transferredTo = new ArrayList<>();
		transferredTo.add(customerDeleted);
		updateTagWithValues(filePath, "gs1ushc:transferredToId", transferredTo);
		transferredTo.stream().forEach(System.out::println);
		List<String> shipToLocation = new ArrayList<>();
		shipToLocation.add(customerDeleted);
		updateTagWithValues(filePath, "gs1ushc:shipToLocationId", shipToLocation);
		shipToLocation.stream().forEach(System.out::println);

	}

	public void enterProductCode(String message, String pCode) throws Exception {
		waitForElementToBeDisplayed(getDriver(), productCode, 40, 4);
		sendKeys(productCode, pCode, message);
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'" + pCode + "_" + pCode + "')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, message + "selecting unit");
		JavaScriptExecutorUtil.waitUntilJavaScriptCompletesByDuration(getDriver(), 10);
		Wait<WebDriver> wait = new FluentWait<WebDriver>(getDriver()).withTimeout(Duration.ofSeconds(30))
				.pollingEvery(Duration.ofSeconds(5)).ignoring(Exception.class);
		ExpectedCondition<Boolean> productCodeLoaded = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				List<WebElement> procuctCode = getDriver()
						.findElements(By.xpath("//div[@id='ajax_listOfOptions']/div"));

				return procuctCode.size() > 0;
			}
		};
		wait.until(productCodeLoaded);
		Thread.sleep(2000);
	}

	public void selectSiteId(String siteName) throws Exception {
		waitForElementToBeDisplayed(getDriver(), siteId, 30, 3);
		selectElementFromDropDownByVisibleText(siteId, siteName);
		JavaScriptExecutorUtil.waitUntilJavaScriptCompletesByDuration(getDriver(), 10);
	}

	public void updateAggregrateXmlForShipmentNegativeTest() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "5829STNA.xml";

		/**
		 * Update time stamp for the XML Here there are 6 time stamps used below unique
		 * 3 time events noOfAddActionObjectEvent = 3; noOfAddActionAggregationEvent =
		 * 2; noOfObservationActionObjectEvent=1;
		 **/
		String[] time = new String[4];
		List<String> eventTimeList = new ArrayList<>();

		for (int i = 0; i < time.length; i++) {
			time[i] = getFixedTimestamp();
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int noOfAddActionObjectEvent = 2;
		int noOfAddActionAggregationEvent1 = 1;
		int noOfAddActionAggregationEvent2 = 1;
		int noOfObservationActionObjectEvent = 1;
		for (int i = 0; i < noOfAddActionObjectEvent; i++) {
			eventTimeList.add(time[0]);
		}
		for (int i = 0; i < noOfAddActionAggregationEvent1; i++) {
			eventTimeList.add(time[1]);
		}
		for (int i = 0; i < noOfAddActionAggregationEvent2; i++) {
			eventTimeList.add(time[2]);
		}
		for (int i = 0; i < noOfObservationActionObjectEvent; i++) {
			eventTimeList.add(time[3]);
		}
		updateTagWithValues(filePath, "eventTime", eventTimeList);
		eventTimeList.stream().forEach(System.out::println);

		/**
		 * Update product information products - 1 Case - 1 Pallet - 1
		 **/
		// ObjectEvenet
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			epcData.add(products[i]);
		}
		for (int i = 0; i < 1; i++) {
			epcData.add(cases[i]);
		}

		for (int i = 0; i < 1; i++) {
			epcData.add(products[i]);
		}
		for (int i = 0; i < 1; i++) {
			epcData.add(pallets[i]);
		}

		for (int i = 0; i < 1; i++) {
			epcData.add(cases[i]);
		}

		updateTagWithValues(filePath, "epc", epcData);
		epcData.stream().forEach(System.out::println);

		/** update parent data **/
		List<String> parentData = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			parentData.add(cases[i]);
		}
		for (int i = 0; i < 1; i++) {
			parentData.add(pallets[i]);
		}
		updateTagWithValues(filePath, "parentID", parentData);
		parentData.stream().forEach(System.out::println);
		/**
		 * update Lot Number lot number tag is used as "<gs1ushc:lotNumber"
		 **/

		Long n = randomNumber(6);
		String text = "5829STNA" + Long.toString(n);
		this.lotNumber = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "gs1ushc:lotNumber", lotNum);
		lotNum.stream().forEach(System.out::println);
	}

	public void updateShippingXmlForShipmentNegativeTest(String SenderOwner, String SenderCustodian,
			String ReceiverOwner, String ReceiverCustodian) {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "5829STNA_shipping.xml";

		/**
		 * Update time stamp for the XML
		 **/
		List<String> eventTimeList = new ArrayList<>();
		eventTimeList.add(getFixedTimestamp());
		updateTagWithValues(filePath, "eventTime", eventTimeList);
		eventTimeList.stream().forEach(System.out::println);

		/**
		 * Update pallet information Pallet - 1
		 **/
		// ObjectEvenet
		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			epcData.add(pallets[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		epcData.stream().forEach(System.out::println);

		/** update parent data **/
		List<String> senderCustodianID = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			senderCustodianID.add(SenderCustodian);
		}
		updateTagWithValues(filePath, "id", senderCustodianID);
		senderCustodianID.stream().forEach(System.out::println);

		List<String> source = new ArrayList<>();
		source.add(SenderOwner);
		source.add(SenderCustodian);
		updateTagWithValues(filePath, "source", source);
		source.stream().forEach(System.out::println);

		List<String> destination = new ArrayList<>();
		destination.add(ReceiverOwner);
		destination.add(ReceiverCustodian);
		updateTagWithValues(filePath, "destination", destination);
		destination.stream().forEach(System.out::println);

	}
	
	public void updateSecondShipmentXmlForSharedEuSaTest() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SharedCode_NonSAU_Ship2.xml";
		
		List<String> epcData = new ArrayList<>();
		epcData.add(palletBahrain[1]);
		epcData.add(palletBahrain[2]);
		updateTagWithValues(filePath, "epc", epcData);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String time = getFixedTimestamp();
		List<String> a = new ArrayList<>();
		a.add(time);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateTagWithValues(filePath, "eventTime", a);
		
		List<String> lotNum = new ArrayList<>();
		lotNum.add(lotNumber);
		lotNum.add("08132025");
		lotNum.add(lotNumber);
		lotNum.add("20250813QA");
		updateTagWithValues(filePath, "bizTransaction", lotNum);
	}
	
	public void updateAggregrateXmlForSharedBhSaTest() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "BAHSAQA2_Comm_Agg.xml";

		List<String> epcData = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			epcData.add(abc[i]);
		}
		for (int i = 0; i < 2; i++) {
			epcData.add(palletBahrain[i]);
		}
		for (int i = 0; i < 4; i++) {
			epcData.add(abc[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		String[] time = new String[4];
		List<String> a = new ArrayList<>();
		for (int i = 0; i < time.length; i++) {
			time[i] = getFixedTimestamp();
			a.add(time[i]);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTagWithValues(filePath, "eventTime", a);
		Long n = randomNumber(4);
		String text = "BHSA" + Long.toString(n);
		lotNumber = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "cbvmda:lotNumber", lotNum);
	}

	public void updateFirstShipmentXmlForSharedBhSaTest() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SharedCode_Bahrain_SA_Ship3.xml";

		List<String> epcData = new ArrayList<>();
		epcData.add(palletBahrain[0]);
		updateTagWithValues(filePath, "epc", epcData);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String time = getFixedTimestamp();
		List<String> a = new ArrayList<>();
		a.add(time);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateTagWithValues(filePath, "eventTime", a);
	}
	
	public void updateSecondShipmentXmlForSharedBhSaTest() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SharedCode_Bahrain_SA_Ship4.xml";
		
		List<String> epcData = new ArrayList<>();
		epcData.add(palletBahrain[1]);
		updateTagWithValues(filePath, "epc", epcData);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String time = getFixedTimestamp();
		List<String> a = new ArrayList<>();
		a.add(time);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateTagWithValues(filePath, "eventTime", a);
	}

	public String updateAggregrateXmlForEventTimeTest() {

		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "5829ETT_Testing.xml";
		
		/** Update time stamp for the XML
		 * 2/5/1/1
		**/
		String[] time = new String[4];
		List<String> eventTimeList = new ArrayList<>();
		
		for (int i = 0; i < time.length; i++) {
			time[i] = getFixedTimestamp();
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int noOfObjectEventProduct = 2;
		int noOfAggregationEventCase = 5;
		int noOfObjectEventCase=1;
		int noOfAggregationEventPallet=1;
		for (int i = 0; i < noOfObjectEventProduct; i++) {
			eventTimeList.add(time[0]);
		}
		for (int i = 0; i < noOfAggregationEventCase; i++) {
			eventTimeList.add(time[1]);
		}
		for (int i = 0; i < noOfObjectEventCase; i++) {
			eventTimeList.add(time[2]);
		}
		for (int i = 0; i < noOfAggregationEventPallet; i++) {
			eventTimeList.add(time[3]);
		}
		updateTagWithValues(filePath, "eventTime", eventTimeList);
		eventTimeList.stream().forEach(System.out::println);
		
		/** 
		 * Update product information
		 * products - 2
		 * Case - 1
		 * Pallet - 1
		 * **/
		//ObjectEvenet
		List<String> epcData = new ArrayList<>();
		// 10 product
		for (int i = 0; i < 10; i++) {
			epcData.add(products[i]);
		}
		// 5 cases
		for (int i = 0; i < 5; i++) {
			epcData.add(cases[i]);
		}
		
		//1 case -> 2 product
		
		for (int i = 0; i < 10; i++) {
			epcData.add(products[i]);
		}
		
		
		//Pallets -> Case
		
		for (int i = 0; i < 1; i++) {
			epcData.add(pallets[i]);
		}
		
		for (int i = 0; i < 5; i++) {
			epcData.add(cases[i]);
		}
		
		
		
		updateTagWithValues(filePath, "epc", epcData);
		epcData.stream().forEach(System.out::println);
		
		/** update parent data **/
		List<String> parentData = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			parentData.add(cases[i]);
		}
		for (int i = 0; i < 1; i++) {
			parentData.add(pallets[i]);
		}
		updateTagWithValues(filePath, "parentID", parentData);
		parentData.stream().forEach(System.out::println);
		/** update Lot Number 
		 * lot number tag for Soft Delete is used as "gs1ushc:lotNumber"
		 * **/
		
		Long n = randomNumber(6);
		String text = "5829SDT" + Long.toString(n);
		this.lotNumber = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "gs1ushc:lotNumber", lotNum);
		lotNum.stream().forEach(System.out::println);
		return this.lotNumber;
	}
	
	
	public void updateReworkAggregrateXmlForEventTimeTest() {

		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "5829ETT_Rework_testing.xml";
		
		/** Update time stamp for the XML
		 * 2/5/1/1
		**/
		String[] time = new String[3];
		List<String> eventTimeList = new ArrayList<>();
		
		for (int i = 0; i < time.length; i++) {
			time[i] = getFixedTimestamp();
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		eventTimeList.add(time[0]);
		eventTimeList.add(time[1]);
		eventTimeList.add(time[2]);
		
		updateTagWithValues(filePath, "eventTime", eventTimeList);
		eventTimeList.stream().forEach(System.out::println);
		
		/** 
		 * Update product information
		 * de commission products - 4
		 * Pallet - 1
		 * **/
		//ObjectEvenet
		List<String> epcData = new ArrayList<>();
		// 4 cases
		for (int i = 0; i < 4; i++) {
			epcData.add(cases[i]);
		}
		// 1st pallet
		
		//2nd pallet -> 4 products
		
		for (int i = 1; i < 2; i++) {
			epcData.add(pallets[i]);
		}
		
		for (int i = 0; i < 4; i++) {
			epcData.add(cases[i]);
		}
		
		updateTagWithValues(filePath, "epc", epcData);
		epcData.stream().forEach(System.out::println);
		
		/** update parent data **/
		List<String> parentData = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			parentData.add(pallets[i]);
		}
		updateTagWithValues(filePath, "parentID", parentData);
		parentData.stream().forEach(System.out::println);
		/** update Lot Number 
		 * lot number tag for Soft Delete is used as "gs1ushc:lotNumber"
		 * **/
		
	}
	
	
	public void updateShippingXmlForEventTimeTest() {

		String filePath = getResourcePath(XML_EVENT_TIME_SHIPPING);
		
		/** Update time stamp for the XML
		 * 2/5/1/1
		**/
		List<String> eventTimeList = new ArrayList<>();

		eventTimeList.add(getFixedTimestamp());
		updateTagWithValues(filePath, TAG_EVENT_TIME, eventTimeList);
		eventTimeList.stream().forEach(System.out::println);
		
		/** 
		 * Update product information
		 **/
		List<String> epcData = new ArrayList<>();
		epcData.add(pallets[1]);
		
		updateTagWithValues(filePath, "epc", epcData);
		epcData.stream().forEach(System.out::println);
		
		/** 
		 * Update biztransaction information
		 **/
		List<String> biztransaction = new ArrayList<>();
		biztransaction.add(RandomStringUtils.randomNumeric(7));
		biztransaction.add(RandomStringUtils.randomNumeric(8));
		biztransaction.add(RandomStringUtils.randomNumeric(1)+RandomStringUtils.randomAlphabetic(2)+"-"+RandomStringUtils.randomNumeric(1)+RandomStringUtils.randomAlphabetic(2)+"-"+RandomStringUtils.randomNumeric(5)+"-"+RandomStringUtils.randomAlphabetic(1));
		biztransaction.add("0");
		String date = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
		biztransaction.add(date+RandomStringUtils.randomNumeric(15));
		
		updateTagWithValues(filePath, "bizTransaction", biztransaction);
		biztransaction.stream().forEach(System.out::println);
		
	}
	
	public LoginPage clickAllocateButtonForSharedBhsSa(String message, int count) {
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		Assert.assertTrue(excelIcon.isDisplayed(), "After clicking on allocation excel files are not generated");
		if (count > abc.length) {
			abc = new String[count]; // Create new array with required size
		}
		if (count < abc.length) {
			abc = new String[count]; // Create new array with required size
		}
		for (int i = 1; i <= count; i++) {
			WebElement ele = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i+1) + "]/td[1]"));
			abc[i - 1] = ele.getText();
		}
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}
	
	public void enterPalletInformationForSwlDev06(String message,String palletCode, int count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), productCode, 30, 3);
		sendKeys(productCode, palletCode, "Pallet Number entering in serialization tab for Swiss Log 06");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+palletCode+"_"+palletCode+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new product code for Swiss Log Us Test");
		waitForElementToBeDisplayed(getDriver(), siteId, 30, 3);
		selectElementFromDropDownByValue(siteId, "122392");
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number for swiss log us test");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			palletSwissThree[i - 1] = el.getText();
		}
		getDriver().switchTo().defaultContent();
	}
	
	public void enterPartialPalletInformationForSwlDev06(String message,String palletCode, int count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), productCode, 30, 3);
		sendKeys(productCode, palletCode, "Pallet Number entering in serialization tab for Swiss Log 06");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+palletCode+"_"+palletCode+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new product code for Swiss Log Us Test");
		waitForElementToBeDisplayed(getDriver(), siteId, 30, 3);
		selectElementFromDropDownByValue(siteId, "122392");
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number for swiss log us test");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			partialPallets[i - 1] = el.getText();
		}
		getDriver().switchTo().defaultContent();
	}
	
	public void enterCaseInformationForSwlDev06(String message,String caseCode, int count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), productCode, 30, 3);
		sendKeys(productCode, caseCode, "Pallet Number entering in serialization tab for Swiss Log 06");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+caseCode+"_"+caseCode+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new product code for Swiss Log Us Test");
		waitForElementToBeDisplayed(getDriver(), siteId, 30, 3);
		selectElementFromDropDownByValue(siteId, "122392");
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number for swiss log us test");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			caseSwissSix[i - 1] = el.getText();
		}
		getDriver().switchTo().defaultContent();
	}
	
	public void enterProductInformationForSwlDev06(String message,String pCode, int count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), productCode, 30, 3);
		sendKeys(productCode, pCode, "Pallet Number entering in serialization tab for Swiss Log 06");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+pCode+"_"+pCode+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new product code for Swiss Log Us Test");
		waitForElementToBeDisplayed(getDriver(), siteId, 30, 3);
		selectElementFromDropDownByValue(siteId, "122392");
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number for swiss log us test");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		if (count != abc.length) {
			abc = new String[count];
		}
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			abc[i - 1] = el.getText();
		}
		getDriver().switchTo().defaultContent();
	}
	
	public LoginPage updateSwlDev06XmlForSwissLog() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SWLDEV06.XML";
		List<String> epcData = new ArrayList<>();
		for(int i =0;i<1;i++) {
			epcData.add(palletSwissThree[i]);
		}
		for(int i =0;i<2;i++) {
			epcData.add(partialPallets[i]);
		}
		for(int i =0;i<1;i++) {
			epcData.add(caseSwissSix[i]);
		}
		
		for(int i =0;i<15;i++) {
			epcData.add(abc[i]);
		}
		for(int i =0;i<15;i++) {
			epcData.add(abc[i]);
		}
		for(int i =0;i<2;i++) {
			epcData.add(partialPallets[i]);
		}
		for(int i =0;i<1;i++) {
			epcData.add(caseSwissSix[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		List<String> parentData = new ArrayList<>();
		for(int i =0;i<2;i++) {
			parentData.add(partialPallets[i]);
		}
		for(int i =0;i<1;i++) {
			parentData.add(caseSwissSix[i]);
		}
		for(int i =0;i<1;i++) {
			parentData.add(palletSwissThree[i]);
		}
		updateTagWithValues(filePath, "parentID", parentData);	

		List<String> timeSt = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			String time = getFixedTimestamp();
			timeSt.add(time);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTagWithValues(filePath, "eventTime", timeSt);
		Long n = randomNumber(6);
		String text = "SWL" + Long.toString(n);
		lotNumberFourSwissLog = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "gs1ushc:lotNumber", lotNum);
		LoginPage lp = new LoginPage();
		return lp;
	}
	
	public void enterPalletInformationForSwlDev07(String message,String palletCode, int count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), productCode, 30, 3);
		sendKeys(productCode, palletCode, "Pallet Number entering in serialization tab for Swiss Log 06");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+palletCode+"_"+palletCode+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new product code for Swiss Log Us Test");
		waitForElementToBeDisplayed(getDriver(), siteId, 30, 3);
		selectElementFromDropDownByValue(siteId, "122392");
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number for swiss log us test");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			palletSwissFour[i - 1] = el.getText();
		}
		getDriver().switchTo().defaultContent();
	}
	
	public void enterCaseInformationForSwlDev07(String message,String caseCode, int count) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), productCode, 30, 3);
		sendKeys(productCode, caseCode, "Pallet Number entering in serialization tab for Swiss Log 06");
		WebElement ele = getDriver().findElement(By.xpath("//div[contains(text(),'"+caseCode+"_"+caseCode+"')]"));
		waitForElementToBeDisplayed(getDriver(), ele, 20, 3);
		click(ele, "Click on the new product code for Swiss Log Us Test");
		waitForElementToBeDisplayed(getDriver(), siteId, 30, 3);
		selectElementFromDropDownByValue(siteId, "122392");
		waitForElementToBeDisplayed(getDriver(), serialNumber, 30, 3);
		sendKeys(serialNumber, Integer.toString(count), "Enter lot number for swiss log us test");
		click(allocateButton, message);
		waitForElementToBeDisplayed(getDriver(), excelIcon, 30, 3);
		for (int i = 1; i <= count; i++) {
			WebElement el = getDriver()
					.findElement(By.xpath("//div[@id='divScrollable']/table/tbody/tr[" + (i + 1) + "]/td[1]"));
			caseSwissSeven[i - 1] = el.getText();
		}
		getDriver().switchTo().defaultContent();
	}
	
	public void updateSwlDev07XmlForSwissLog() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "SWLDEV07.XML";
		List<String> epcData = new ArrayList<>();
		for(int i =0;i<1;i++) {
			epcData.add(palletSwissFour[i]);
		}
		
		for(int i =0;i<2;i++) {
			epcData.add(caseSwissSeven[i]);
		}
		
		for(int i =0;i<10;i++) {
			epcData.add(abc[i]);
		}
		for(int i =0;i<10;i++) {
			epcData.add(abc[i]);
		}
		for(int i =0;i<2;i++) {
			epcData.add(caseSwissSeven[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		List<String> parentData = new ArrayList<>();
		for(int i =0;i<2;i++) {
			epcData.add(caseSwissSeven[i]);
		}
		
		for(int i =0;i<1;i++) {
			parentData.add(palletSwissFour[i]);
		}
		updateTagWithValues(filePath, "parentID", parentData);	

		List<String> timeSt = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			String time = getFixedTimestamp();
			timeSt.add(time);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTagWithValues(filePath, "eventTime", timeSt);
		Long n = randomNumber(6);
		String text = "SWL" + Long.toString(n);
		lotNumberFourSwissLog = text;
		List<String> lotNum = new ArrayList<>();
		lotNum.add(text);
		lotNum.add(text);
		lotNum.add(text);
		updateTagWithValues(filePath, "gs1ushc:lotNumber", lotNum);
	}
	
	public LoginPage updateShipmentForSwissLogMultiple() {
		String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "Swisslog_shipment_DEV.XML";
		List<String> epcData = new ArrayList<>();
		for(int i =0;i<1;i++) {
			epcData.add(palletBahrain[i]);
		}
		for(int i =0;i<1;i++) {
			epcData.add(palletSwissTwo[i]);
		}
		for(int i =0;i<1;i++) {
			epcData.add(palletSwissThree[i]);
		}
		for(int i =0;i<1;i++) {
			epcData.add(palletSwissFour[i]);
		}
		updateTagWithValues(filePath, "epc", epcData);
		
		List<String> timeSt = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			String time = getFixedTimestamp();
			timeSt.add(time);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTagWithValues(filePath, "eventTime", timeSt);
		LoginPage lp = new LoginPage();
		return lp;
	}
	
}
