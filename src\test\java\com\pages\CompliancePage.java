package com.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;

import com.tests.BaseTest;
import com.utils.TestData;

public class CompliancePage extends BasePage {

	public String gtinValue;

	TestData td = new TestData();

	public CompliancePage(WebDriver driverInstance) {
		super(BaseTest.getDriver()); // Pass the ThreadLocal WebDriver to BasePage
		this.td = new TestData(); // Create a new instance for each thread
		PageFactory.initElements(getDriver(), this); // Initialize elements
	}

	@FindBy(xpath = "//a[@id='compliance_uae_pharma']")
	WebElement uaeCompliance;

	@FindBy(id = "compliance_uae_pharma_home")
	WebElement complianceUaeReports;

	@FindBy(xpath = "//frame[@name='body']")
	WebElement bodyFrame;

	@FindBy(xpath = "//span[contains(text(),'Search')]")
	WebElement searchButton;

	@FindBy(id = "gtin")
	WebElement gtin;

	@FindBy(xpath = "//tbody/tr[1]/td[6]")
	WebElement gtinValueFromCompliance;

	@FindBy(xpath = "//a/img[@title='Edit']")
	WebElement clickEditImg;

	@FindBy(xpath = "//input[@id='lot']")
	WebElement inputLotNumber;

	@FindBy(xpath = "//input[@id='shipmentPermitNumber_0']")
	WebElement inputShipmentNumber;

	@FindBy(xpath = "//*[@id='btnSubmit']")
	WebElement btnShipmentSubmit;

	@FindBy(xpath = "//a[@id='compliance_bahrain']")
	WebElement bahrainCompliance;

	@FindBy(id = "compliance_bahrain_home")
	WebElement complianceBahrainReports;

	@FindBy(xpath = "//a[@id='compliance_sau']")
	WebElement saudiCompliance;

	@FindBy(id = "compliance_report_sau")
	WebElement complianceSaudiReports;

	@FindBy(id = "viewResponseReport_1")
	WebElement responseReport;

	@FindBy(id = "viewImg_2")
	WebElement createNotificationReport;

	@FindBy(id = "compliance_configuration_sau")
	WebElement complianceSaudiConfiguration;

	@FindBy(xpath = "//input[contains(@value,'Disabled')]")
	WebElement importSaudiOption;

	@FindBy(id = "createdDateTimeSelect")
	WebElement createdDateTimeSelect;

	@FindBy(id = "createdDateTime")
	WebElement createdDateTime;

	@FindBy(id = "endDateValue")
	WebElement endDateValue;

	@FindBy(id = "viewImg_1")
	WebElement negativeNotificationReport;

	@FindBy(id = "compliance_eu")
	WebElement complianceEu;

	@FindBy(id = "compliance_configuration")
	WebElement complianceEuConfiguration;

	@FindBy(id = "enableNonEU")
	WebElement createExportPpsu;

	// -----------
	@FindBy(xpath = "//a[@id='compliance_kz_pharma']")
	WebElement KAZCompliance;
	@FindBy(xpath = "//a[@id='compliance_kz_pharma_home']")
	WebElement KAZCompliance_Submenu;
	@FindBy(xpath = "//a[@id='complianceTab']")
	WebElement complianceTab;
	@FindBy(xpath = "//input[@id='lot']")
	WebElement Lot;
	@FindBy(xpath = "//frame[@name='head']")
	WebElement headerFrame;
	// ---------

	@FindBy(id = "compliance_report")
	WebElement complianceEuReport;

	@FindBy(id = "htmlImg_report_1")
	WebElement complianceHtmlReport1;

	@FindBy(id = "htmlImg_report_2")
	WebElement complianceHtmlReport2;

	@FindBy(xpath = "//td[contains(text(),'Pack List')]")
	WebElement packList;

	@FindBy(id = "reportname")
	WebElement reportname;

	@FindBy(id = "compliance_verification")
	WebElement complianceEuVerificaion;

	@FindBy(id = "itemType")
	WebElement itemType;

	@FindBy(id = "lot")
	WebElement lot;

	@FindBy(xpath = "//span[contains(text(),'Search')]")
	WebElement searchTextInVerification;

	@FindBy(id = "chkPackData")
	WebElement chkPackData;

	@FindBy(xpath = "//span[contains(text(),'Submit')]")
	WebElement submitButton;

	@FindBy(xpath = "//frame[@name='head']")
	WebElement headFrame;
	
	@FindBy(id="btnClose")
	WebElement closeButton;

	public void clickOnUAECompliancePharma() {
		Actions actions = new Actions(getDriver());
		actions.moveToElement(uaeCompliance).perform();
		waitForElementToBeDisplayed(getDriver(), complianceUaeReports, 20, 2);
		actions.moveToElement(complianceUaeReports).click().perform();
	}

	public void clickOnSearch(String message) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), searchButton, 20, 3);
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		click(searchButton, message);
	}

	public LoginPage captureGtinValue() {
		waitForElementToBeDisplayed(getDriver(), gtinValueFromCompliance, 20, 3);
		gtinValue = gtinValueFromCompliance.getText();
		System.out.println(gtinValue);
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void clickOnEdit(String message) {
		waitForElementToBeDisplayed(getDriver(), clickEditImg, 20, 3);
		click(clickEditImg, message);
	}

	public void enterLotID(String message, SerializePage sp) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), inputLotNumber, 20, 3);
		sendKeys(inputLotNumber, sp.lotNumber, message);
	}

	public void enterShipmentNumber(String message) {
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String parentWindow1 = getDriver().getWindowHandle();
		switchWindow();
		getDriver().switchTo().defaultContent();
		String data = (String) td.getTestDataByPath(BaseTest.environment,
				"TestCase.UAERegressionTest.shipmentNumber");
		sendKeys(inputShipmentNumber, data, message);
		click(btnShipmentSubmit, "Submit button clicked for UAE after shipment");
		switchToParentWindow(parentWindow1);
	}

	public void clickOnBahrainCompliancePharma() {
		Actions actions = new Actions(getDriver());
		actions.moveToElement(bahrainCompliance).perform();
		waitForElementToBeDisplayed(getDriver(), complianceBahrainReports, 20, 2);
		actions.moveToElement(complianceBahrainReports).click().perform();
	}

	public void validateBahrainLotNumber(SerializePage sp) {
		try {
			Thread.sleep(60000);
			click(searchButton, "validation for bahrain");
			Thread.sleep(60000);
			click(searchButton, "validation for bahrain");
		} catch (Exception e) {
			e.printStackTrace();
		}
		WebElement el = getDriver().findElement(By.xpath(
				"//td[contains(text(),'" + sp.lotNumber + "')]/following-sibling::td[contains(text(),'GENERATED')]"));

		String text = getAttribute(el, "innerText", "validating Shipment for Bahrain is generated");
		System.out.println(text);

		WebElement el1 = getDriver().findElement(By.xpath("//td[contains(text(),'" + sp.lotNumber
				+ "')]/following-sibling::td[contains(text(),'GENERATED')]/following-sibling::td[contains(text(),'SUCCESS')]"));
		String text1 = getAttribute(el1, "innerText", "validating success message for Bahrain is generated");
		System.out.println(text1);
	}

	public void clickOnSaudiCompliancePharma() {
		Actions actions = new Actions(getDriver());
		actions.moveToElement(saudiCompliance).perform();
		waitForElementToBeDisplayed(getDriver(), complianceSaudiReports, 20, 2);
		actions.moveToElement(complianceSaudiReports).click().perform();
	}

	public void validateSaudiInternalSupply() {
		try {
			Thread.sleep(60000);
			click(searchButton, "validation for saudi internal supply");
			Thread.sleep(60000);
			click(searchButton, "validation for saudi internal supply");
		} catch (Exception e) {
			e.printStackTrace();
		}
		WebElement el = getDriver().findElement(
				By.xpath("//td[contains(text(),'Supply')]/following-sibling::td[10][contains(text(),'SUCCESS')]"));
		String text = getAttribute(el, "innerText",
				"validating success message for Saudi Internal Supply success is generated");
		System.out.println(text);
	}

	public LoginPage validateSaudiInternalSupplyResponse(SerializePage sp) {
		click(responseReport, "validation saudi internal supply response report");
		String parentWindow1 = getDriver().getWindowHandle();
		switchWindow();
		getDriver().switchTo().defaultContent();
		String text = getAttribute(getDriver().findElement(By.xpath("//td[contains(text(),'" + sp.lotNumber + "')]")),
				"innerText", "validate shipment");
		System.out.println(text);
		getDriver().close();
		switchToParentWindow(parentWindow1);
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void validateSaudiInternalDispatch() {
		try {
			Thread.sleep(30000);
			click(searchButton, "validation for Saudi Internal Dispatch");

		} catch (Exception e) {
			e.printStackTrace();
		}
		WebElement el1 = getDriver().findElement(By.xpath("//td[contains(text(),'Dispatch')]"));
		scrollToWebElement(el1);
		String text = getAttribute(el1, "innerText",
				"validating success message for Saudi Internal dispatch is created");
		Assert.assertTrue(text.contains("Dispatch"), "Dispatch is not created");

		WebElement el = getDriver().findElement(By.xpath("//td[contains(text(),'Package Transfer Upload')]"));
		String text1 = getAttribute(el, "innerText",
				"validating success message for Saudi Internal package transfer is created");
		Assert.assertTrue(text1.contains("Package Transfer Upload"), "Package transfer is not created");
	}

	public void validateSaudiImportDispatch() {
		try {
			Thread.sleep(30000);
			click(searchButton, "validation for Saudi Internal Dispatch");

		} catch (Exception e) {
			e.printStackTrace();
		}
		WebElement el1 = getDriver().findElement(By.xpath("//td[contains(text(),'Dispatch')]"));
		scrollToWebElement(el1);
		String text = getAttribute(el1, "innerText",
				"validating success message for Saudi Internal dispatch is created");
		Assert.assertTrue(text.contains("Dispatch"), "Dispatch is not created");

		WebElement el = getDriver().findElement(By.xpath("//td[contains(text(),'Package Transfer Upload')]"));
		String text1 = getAttribute(el, "innerText",
				"validating success message for Saudi Internal package transfer is created");
		Assert.assertTrue(text1.contains("Package Transfer Upload"), "Package transfer is not created");
	}

	public void validateSaudiImportCreateResponse() {
		click(createNotificationReport, "validation saudi import create response notification");
		String text = getAttribute(getDriver().findElement(By.xpath("//td[contains(text(),'Undefined product')]")),
				"innerText", "validate create response notification");
		System.out.println(text);
		Assert.assertTrue(text.contains("Undefined product"), "Saudi Import is not working as expected");
	}

	public LoginPage validateSaudiExportCreateResponse() {
		WebElement el = getDriver().findElement(
				By.xpath("//td[contains(text(),'Export')]/following-sibling::td[contains(text(),'Create')]"));
		waitForElementToBeDisplayed(getDriver(), el, 20, 2);
		scrollToWebElement(el);
		String text = getAttribute(el, "innerText", "validate export create display");
		System.out.println(text);
		Assert.assertTrue(el.isDisplayed(), "Saudi export is not working as expected");
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void validateSaudiExportCancelReport() {
		WebElement el = getDriver().findElement(
				By.xpath("//td[contains(text(),'Export Cancel')]/following-sibling::td[contains(text(),'Create')]"));
		waitForElementToBeDisplayed(getDriver(), el, 20, 2);
		scrollToWebElement(el);
		String text = getAttribute(el, "innerText", "validate export cancel display");
		System.out.println(text);
		Assert.assertTrue(el.isDisplayed(), "Saudi export cancelis not working as expected");
	}

	public void clickOnSaudiComplianceConfiguration() {
		Actions actions = new Actions(getDriver());
		actions.moveToElement(saudiCompliance).perform();
		waitForElementToBeDisplayed(getDriver(), complianceSaudiConfiguration, 20, 2);
		actions.moveToElement(complianceSaudiConfiguration).click().perform();
	}

	public LoginPage validateImportDisabledIsCheckedOrNot() {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), importSaudiOption, 20, 2);
		boolean isChecked = importSaudiOption.isSelected();
		System.out.println(
				getAttribute(importSaudiOption, "innerText", "validating disable status for saudi additional test"));
		Assert.assertTrue(isChecked, "Disabled is not checked");
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void enterLotIDSaudiAdditional(String lotNum, String message) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), inputLotNumber, 20, 3);
		sendKeys(inputLotNumber, lotNum, message);
	}

	public LoginPage returnToLoginPage() {
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void selectBetweenForCreateInSaudi() {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), createdDateTimeSelect, 20, 3);
		selectElementFromDropDownByValue(createdDateTimeSelect, "3");
		waitForElementToBeDisplayed(getDriver(), endDateValue, 20, 2);
	}

	public void enterDates() {
		String date = getCurrentDate();
		String past = getDatePrior(7);
		sendKeys(createdDateTime, past, "Entering old date for saudi additional test");
		sendKeys(endDateValue, date, "Entering current date for saudi additional test");
	}

	public void validateDateRange() {
		WebElement el = getDriver().findElement(By.xpath("//tbody/tr/td[11]"));
		scrollToWebElement(el);
		String text = el.getText();
		String[] data = text.split(" ");
		String[] t = data[0].split("-");

		String date = getCurrentDate();
		String past = getDatePrior(7);
		String[] a = date.split("/");
		String[] x = past.split("/");
		int d = Integer.parseInt(x[1]);
		int b = Integer.parseInt(a[1]);
		int c = Integer.parseInt(t[2]);
		Assert.assertTrue(isBetween(c, d, b), "Current Date didn't match");
		int expectedCurrentMonth = Integer.parseInt(a[0]);
		int actualCurrentMonth = Integer.parseInt(t[1]);
		Assert.assertTrue(expectedCurrentMonth == actualCurrentMonth, "Month didn't match");
		int expectedCurrentYear = Integer.parseInt(a[2]);
		int actualCurrentyear = Integer.parseInt(t[0]);
		Assert.assertTrue(expectedCurrentYear == actualCurrentyear, "Year didn't match");
		scrollToWebElement(getDriver().findElement(By.xpath("//a[contains(text(),'Created Date')]")));
		click(getDriver().findElement(By.xpath("//a[contains(text(),'Created Date')]")),
				"Clicking on created date during saudi additional test");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scrollToWebElement(getDriver().findElement(By.xpath("//a[contains(text(),'Created Date')]")));
		WebElement el1 = getDriver().findElement(By.xpath("//tbody/tr/td[11]"));
		String text1 = el1.getText();
		String[] data1 = text1.split(" ");
		String[] t1 = data1[0].split("-");

		int d1 = Integer.parseInt(x[1]);
		int b1 = Integer.parseInt(a[1]);
		int c1 = Integer.parseInt(t1[2]);
		Assert.assertTrue(isBetween(c1, d1, b1), "Date didn't match");

		int expectedCurrentMonth1 = Integer.parseInt(x[0]);
		int actualCurrentMonth1 = Integer.parseInt(t1[1]);
		Assert.assertTrue(expectedCurrentMonth1 == actualCurrentMonth1, "Month didn't match");
		int expectedCurrentYear1 = Integer.parseInt(x[2]);
		int actualCurrentyear1 = Integer.parseInt(t1[0]);
		Assert.assertTrue(expectedCurrentYear1 == actualCurrentyear1, "Year didn't match");

	}

	public boolean isBetween(int number, int lowerBound, int upperBound) {
		return number >= lowerBound && number <= upperBound;
	}

	public void validateSaudiNegativeCreateResponse() {
		try {
			Thread.sleep(60000);
			click(searchButton, "validation for Saudi Internal Dispatch");

		} catch (Exception e) {
			e.printStackTrace();
		}
		scrollToWebElement(negativeNotificationReport);
		try {
			Thread.sleep(6000);
			click(searchButton, "validation for Saudi Internal Dispatch");

		} catch (Exception e) {
			e.printStackTrace();
		}
		click(negativeNotificationReport, "validation saudi negative response notification");
		String text = getAttribute(
				getDriver().findElement(By.xpath(
						"//td[contains(text(),'The format of the batch number of the product is incompatible')]")),
				"innerText", "validate create response notification");
		System.out.println(text);
		Assert.assertTrue(text.contains("The format of the batch number of the product is incompatible"),
				"Saudi negative is not working as expected");
	}

	public void clickOnEuEvmsConfiguration() {
		Actions actions = new Actions(getDriver());
		actions.moveToElement(complianceEu).perform();
		waitForElementToBeDisplayed(getDriver(), complianceEuConfiguration, 20, 2);
		actions.moveToElement(complianceEuConfiguration).click().perform();
	}

	public LoginPage validateCreatedExportPpsuIsCheckedOrNot() {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), createExportPpsu, 20, 2);
		boolean isChecked = createExportPpsu.isSelected();
		System.out.println(
				getAttribute(createExportPpsu, "innerText", "validating yes or no status for Create export ppsu"));
		Assert.assertTrue(isChecked, "Disabled is not checked");
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	// KAZ

	public void clickOnKAZCompliancePharma() {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headerFrame);
		click(complianceTab, "click complianceTab ");
		Actions actions = new Actions(getDriver());
		actions.moveToElement(KAZCompliance).perform();
		waitForElementToBeDisplayed(getDriver(), KAZCompliance_Submenu, 20, 2);
		actions.moveToElement(KAZCompliance_Submenu).click().perform();
	}

	public void SearchWithLot(String lotNumber) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		sendKeys(Lot, lotNumber, "Enter lot id to search");
		waitForElementToBeDisplayed(getDriver(), searchButton, 20, 3);
		click(searchButton, "Enter lot Number and search");
	}

	public void ValidateSuccessMsg(String LotNumber) {
		String eLotID1 = "//td[text() = '" + LotNumber + "']";
		WebElement eLotID = getDriver().findElement(By.xpath(eLotID1));
		if (eLotID.isDisplayed()) {
			String eSent1 = "(//td[text() = '" + LotNumber
					+ "']/following-sibling::td[6][contains(text(), 'SENT')])[1]";
			WebElement eSent = getDriver().findElement(By.xpath(eSent1));
			Assert.assertEquals("SENT", eSent.getText(), "SENT Message displayed");
			String eSuccess1 = "(//td[text() = '" + LotNumber
					+ "']/following-sibling::td[7][contains(text(), 'SUCCESS')])[1]";
			WebElement eSuccess = getDriver().findElement(By.xpath(eSuccess1));
			if (eSuccess.getText().equals("SUCCESS")) {
				System.out.println("Test PASSED- Product successfully allocated and sent with success message");
			} else {
				System.out.println("Test FAILD-Product didnot allocate and failed ");
			}
		} else {
			System.out.println("Lot ID-" + LotNumber + " did not display ");
		}
	}

	public void clickOnEuEvmsComplianceReports() {
		Actions actions = new Actions(getDriver());
		actions.moveToElement(complianceEu).perform();
		waitForElementToBeDisplayed(getDriver(), complianceEuReport, 20, 2);
		actions.moveToElement(complianceEuReport).click().perform();
	}

	public void validateSearchResultsForEu(String message, SerializePage sp) {
		WebElement el = getDriver().findElement(By.xpath("(//td[contains(text(),'" + sp.lotNumber + "')])[2]"));
		scrollToWebElement(el);
		String text = getAttribute(el, "innerText", message);
		System.out.println(text);
	}

	public void validateReportDataOne(String message) {
		click(complianceHtmlReport1, message);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String parentWindow1 = getDriver().getWindowHandle();
		switchWindow();
		getDriver().switchTo().defaultContent();
		String text = getAttribute(packList, "innerText", "validate orphans data");
		boolean status = text.trim().contains("20") || text.contains("180");
		Assert.assertTrue(status, "Orphans are not found in lot id");
		getDriver().close();
		switchToParentWindow(parentWindow1);
	}

	public void validateReportDataTwo(String message) {
		getDriver().switchTo().frame(bodyFrame);
		click(complianceHtmlReport2, message);
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String parentWindow1 = getDriver().getWindowHandle();
		switchWindow();
		getDriver().switchTo().defaultContent();
		String text = getAttribute(packList, "innerText", "validate pallet data");
		boolean status = text.trim().contains("20") || text.contains("180");
		Assert.assertTrue(status, "Pallet not found in lot id");
		getDriver().close();
		switchToParentWindow(parentWindow1);
	}

	public LoginPage validateSharedCodeEuSide(String message) {
		WebElement el = getDriver().findElement(By.xpath("//td[contains(text(),'Product Pack Data')]"));
		scrollToWebElement(el);
		String text = getAttribute(el, "innerText", message);
		System.out.println(text);
		Assert.assertTrue(el.isDisplayed(), "Product pack data event is not displayed");
		WebElement el1 = getDriver().findElement(By
				.xpath("//td[contains(text(),'Product Pack Data')]/following-sibling::td[contains(text(),'Create')]"));
		String text1 = getAttribute(el1, "innerText", message + "Report Option");
		System.out.println(text1);
		Assert.assertTrue(el1.isDisplayed(), "Product pack data report option is not displayed");
		for(int i =0;i<10;i++) {
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			searchButton.click();
		}
		WebElement el2 = getDriver()
				.findElement(By.xpath("//td[contains(text(),'Product Pack Data')]/following-sibling::td[12]"));
		String text2 = getAttribute(el2, "innerText", message + "System status");
		Assert.assertTrue(text2.equalsIgnoreCase("SUCCESS"),"Product pack data system status is not success");
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public LoginPage validateSharedCodeEuSideShipment(String message) {
		for (int i = 0; i < 10; i++) {
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			searchButton.click();
		}
		click(searchButton, "Click on search second time for PPSU report");
		WebElement el = getDriver().findElement(By.xpath("//td[contains(text(),'Product Pack State Update')]"));
		scrollToWebElement(el);
		String text = getAttribute(el, "innerText", message);
		System.out.println(text);
		Assert.assertTrue(el.isDisplayed(), "Product Pack State Update event is not displayed");
		WebElement el1 = getDriver().findElement(By.xpath(
				"//td[contains(text(),'Product Pack State Update')]/following-sibling::td[contains(text(),'Create')]"));
		String text1 = getAttribute(el1, "innerText", message + "Report Option");
		System.out.println(text1);
		Assert.assertTrue(el1.isDisplayed(), "Product pack status update report option is not displayed");
		LoginPage lp = new LoginPage();
		return lp;
	}

	public LoginPage validateSharedCodeSaSideShipment(String message) {
		for (int i = 0; i < 10; i++) {
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			searchButton.click();
		}
		click(searchButton, "Click on search second time for PPSU report");
		WebElement el = getDriver().findElement(By.xpath("//td[contains(text(),'Package Transfer Upload')]"));
		scrollToWebElement(el);
		String text = getAttribute(el, "innerText", message);
		System.out.println(text);
		Assert.assertTrue(el.isDisplayed(), "Product Pack State Update event is not displayed");
		WebElement el1 = getDriver().findElement(By.xpath(
				"//td[contains(text(),'Package Transfer Upload')]/following-sibling::td[contains(text(),'Create')]"));
		String text1 = getAttribute(el1, "innerText", message + "Report Option");
		System.out.println(text1);
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void selectReportName() {
		selectElementFromDropDownByVisibleText(reportname, "Product Pack State Update");
	}

	public LoginPage validateDispositionResult(String message) {
		for (int i = 1; i <= 15; i++) {
			WebElement el = getDriver().findElement(By.id("htmlImg_report_" + i));
			scrollToWebElement(el);
			click(el, message + i);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String parentWindow1 = getDriver().getWindowHandle();
			switchWindow();
			getDriver().switchTo().defaultContent();
			String text = getAttribute(getDriver().findElement(By.xpath("//td[contains(text(),'New Pack Status')]")),
					"innerText", "validate status" + i);
			System.out.println(text);
			getDriver().close();
			switchToParentWindow(parentWindow1);
			getDriver().switchTo().defaultContent();
			getDriver().switchTo().frame(bodyFrame);
		}
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}

	public void selectReportNameForProductEdit() {
		waitForElementToBeDisplayed(getDriver(), reportname, 30, 2);
		selectElementFromDropDownByVisibleText(reportname, "Product Master Data");
	}

	public void enterGtinValue(String message, String data) {
		waitForElementToBeDisplayed(getDriver(), gtin, 30, 2);
		sendKeys(gtin, data, message);
		waitForElementToBeDisplayed(getDriver(),
				getDriver().findElement(By.xpath("//div[contains(text(),'03400957557720')]")), 30, 3);
		click(getDriver().findElement(By.xpath("//div[contains(text(),'03400957557720')]")), message + "click");
	}

	public void validateProductMasterData() {
		WebElement el = getDriver().findElement(By.xpath("//td[contains(text(),'Product Master Data')]"));
		scrollToWebElement(el);
		Assert.assertTrue(el.isDisplayed(), "Product Master Data event is not displayed");
	}

	public void clickOnEuEvmsComplianceVerification() {
		Actions actions = new Actions(getDriver());
		actions.moveToElement(complianceEu).perform();
		waitForElementToBeDisplayed(getDriver(), complianceEuVerificaion, 20, 2);
		actions.moveToElement(complianceEuVerificaion).click().perform();
	}

	public void enterItemType(String message, String data) {
		waitForElementToBeDisplayed(getDriver(), itemType, 20, 3);
		sendKeys(itemType, data, message);
		waitForElementToBeDisplayed(getDriver(),
				getDriver().findElement(By.xpath("//div[contains(text(),'35413760481020')]")), 30, 3);
		click(getDriver().findElement(By.xpath("//div[contains(text(),'35413760481020')]")), message + "click");
	}

	public void enterLotNumber(String message, SerializePage sp) {
		waitForElementToBeDisplayed(getDriver(), lot, 20, 3);
		sendKeys(lot, sp.lotNumber, message);
	}

	public void clickOnSearchVerification(String message) {
		waitForElementToBeDisplayed(getDriver(), searchTextInVerification, 30, 2);
		click(searchTextInVerification, message);
		waitForElementToBeDisplayed(getDriver(),
				getDriver().findElement(By.xpath("//*[contains(text(),'Search Results')]")), 30, 3);
	}

	public void clickOnUncheckPackData(String message) {
		click(chkPackData, message);
	}

	public void clickOnSubmitButton(String message) {
		click(submitButton, message);
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headFrame);
	}

	public void selectReportNameForProductVerification() {
		waitForElementToBeDisplayed(getDriver(), reportname, 30, 2);
		selectElementFromDropDownByVisibleText(reportname, "Product Pack Verification");
	}

	public void validateProductPackDataWithoutIncludePackData(String message) {
		WebElement el = getDriver().findElement(By.xpath("//td[contains(text(),'Product Pack Verification')]"));
		scrollToWebElement(el);
		String text = getAttribute(el, "innerText", message);
		System.out.println(text);
		Assert.assertTrue(el.isDisplayed(), "Product Pack Verification event is not displayed");
		WebElement el1 = getDriver().findElement(By.xpath(
				"//td[contains(text(),'Product Pack Verification')]/following-sibling::td[contains(text(),'Create')]"));
		String text1 = getAttribute(el1, "innerText", message + "Report Option");
		System.out.println(text1);
		Assert.assertTrue(el1.isDisplayed(), "Product pack data report option is not displayed");
		WebElement el2 = getDriver()
				.findElement(By.xpath("//td[contains(text(),'Product Pack Verification')]/following-sibling::td[12]"));
		String text2 = getAttribute(el2, "innerText", message + "System status");
		Assert.assertTrue(text2.trim().equalsIgnoreCase("SUCCESS"),
				"System status not matching during product pack verification");
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headFrame);
	}
	
	public LoginPage validateSharedCodeEuSideAfterSecondShipment(String message) {
		for(int i =0;i<10;i++) {
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			searchButton.click();
		}
		click(searchButton, "Click on search second time for PPSU report");
		WebElement el = getDriver().findElement(By.xpath("//td[contains(text(),'Product Pack State Update')]"));
		scrollToWebElement(el);
		String text = getAttribute(el, "innerText", message);
		System.out.println(text);
		Assert.assertTrue(el.isDisplayed(), "Product Pack State Update event is not displayed");
		WebElement el1 = getDriver().findElement(By.xpath("//td[contains(text(),'Product Pack State Update')]/following-sibling::td[contains(text(),'Create')]"));
		String text1 = getAttribute(el1, "innerText", message+"Report Option");
		System.out.println(text1);
		Assert.assertTrue(el1.isDisplayed(), "Product pack status update report option is not displayed");
		WebElement el2 = getDriver().findElement(By.xpath("//td[contains(text(),'Product Pack State Update')]/following-sibling::td[2][contains(text(),'10')]"));
		String text2 = getAttribute(el2, "innerText", message+"Quantity");
		System.out.println(text2);
		LoginPage lp = new LoginPage();
		return lp;
	}
	
	public LoginPage validateSharedCodeBhSide(String message) {
		WebElement el = getDriver().findElement(By.xpath("//td[contains(text(),'Supply')]"));
		scrollToWebElement(el);
		String text = getAttribute(el, "innerText", message);
		System.out.println(text);
		Assert.assertTrue(el.isDisplayed(), "Supply event is not displayed");
		WebElement el1 = getDriver().findElement(By
				.xpath("//td[contains(text(),'Supply')]/following-sibling::td[contains(text(),'Create')]"));
		String text1 = getAttribute(el1, "innerText", message + "Report Option");
		System.out.println(text1);
		Assert.assertTrue(el1.isDisplayed(), "Supply report option is not displayed");
		for(int i =0;i<10;i++) {
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			searchButton.click();
		}
		WebElement el2 = getDriver()
				.findElement(By.xpath("//td[contains(text(),'Supply')]/following-sibling::td[11]"));
		String text2 = getAttribute(el2, "innerText", message + "System status");
		Assert.assertTrue(text2.equalsIgnoreCase("SUCCESS"),"Supply system status is not success");
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}
	
	public LoginPage validateSharedCodeBhSideShipment(String message) {
		for (int i = 0; i < 5; i++) {
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			searchButton.click();
		}
		WebElement el = getDriver().findElement(By.xpath("//td[contains(text(),'Export')]"));
		scrollToWebElement(el);
		String text = getAttribute(el, "innerText", message);
		System.out.println(text);
		Assert.assertTrue(el.isDisplayed(), "Export event is not displayed");
		WebElement el1 = getDriver().findElement(By.xpath(
				"//td[contains(text(),'Export')]/following-sibling::td[contains(text(),'Create')]"));
		String text1 = getAttribute(el1, "innerText", message + "Report Option");
		System.out.println(text1);
		Assert.assertTrue(el1.isDisplayed(), "Export report option is not displayed");
		LoginPage lp = new LoginPage();
		return lp;
	}
	
	public LoginPage validateSharedCodeBhSideShipmentAfterFirstShipment(String message) {
		WebElement el = getDriver().findElement(By.xpath("//td[contains(text(),'EPCIS-Bahrain')]"));
		scrollToWebElement(el);
		String text = getAttribute(el, "innerText", message);
		System.out.println(text);
		Assert.assertTrue(el.isDisplayed(), "EPCIS Bahrain event is not displayed");
		WebElement el1 = getDriver().findElement(By.xpath(
				"//td[contains(text(),'EPCIS-Bahrain')]/following-sibling::td[contains(text(),'Create')]"));
		String text1 = getAttribute(el1, "innerText", message + "Report Option");
		System.out.println(text1);
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}
	
	public void validateShipmentSuccessUae(String message, SerializePage sp) {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), inputLotNumber, 20, 3);
		WebElement el = getDriver().findElement(By.xpath(
				"//td[contains(text(),'"+sp.lotNumber+"')]/following-sibling::td[5]"));
		scrollToWebElement(el);
		try {
			Thread.sleep(300000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		click(searchButton, "Click on search button in UAE Compliance to check final status");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		WebElement el1 = getDriver().findElement(By.xpath(
				"//td[contains(text(),'"+sp.lotNumber+"')]/following-sibling::td[5]"));
		scrollToWebElement(el1);
		String text = getAttribute(el1, "innerText", message);
		Assert.assertTrue(text.trim().equalsIgnoreCase("SUCCESS"), "Expected SUCCESS but found "+text.trim());
	}

}
