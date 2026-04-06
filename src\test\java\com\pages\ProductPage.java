package com.pages;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;

import com.tests.BaseTest;
import com.utils.JavaScriptExecutorUtil;
import com.utils.TestData;

public class ProductPage extends BasePage{
	
	TestData td = new TestData();
	
	
	public ProductPage(WebDriver driverInstance) {
		super(BaseTest.getDriver()); // Pass the ThreadLocal WebDriver to BasePage
		this.td = new TestData(); // Create a new instance for each thread
		PageFactory.initElements(getDriver(), this); // Initialize elements
	}
	
	@FindBy(xpath="//frame[@name='body']")
	WebElement bodyFrame;
	
	@FindBy(xpath="//frame[@name='head']")
	WebElement headFrame;
	
	@FindBy(id="showBrand1")
	WebElement showSearch;
	
	@FindBy(id="schProdCode")
	WebElement productCode;
	
	@FindBy(id="btnSearch")
	WebElement searchButton;
	
	@FindBy(xpath="//td[contains(text(),'View Product')]")
	WebElement viewProduct;
	
	@FindBy(xpath="//td[contains(text(),'Organization')]/following-sibling::td[contains(text(),'Baxter Healthcare Corporation')]")
	WebElement orgValue;
	
	@FindBy(xpath="//td[contains(text(),'Site Name')]/parent::tr/following-sibling::tr/td[contains(text(),'United Arab Emirates(AE)')]")
	WebElement siteValue;
	
	@FindBy(xpath="//td[contains(text(),'Site Name')]/parent::tr/following-sibling::tr[2]/td[contains(text(),'Bahrain(BH)')]")
	WebElement siteValueBahrain;
	
	@FindBy(id="schProductID")
	WebElement productId;
	
	@FindBy(xpath="//td[contains(text(),'Site Name')]/parent::tr/following-sibling::tr[1]/td[contains(text(),'Saudi Arabia(SA)')]")
	WebElement siteValueSaudi;
	
	@FindBy(xpath="//td[contains(text(),'Site Name')]/parent::tr/following-sibling::tr[1]/td[contains(text(),'South Korea(KR)')]")
	WebElement siteValueKorea;
	
	@FindBy(xpath="//td[contains(text(),'Manufacturing Site List')]")
	WebElement manufacturingSiteList;
	
	@FindBy(xpath="//img[contains(@id,'brandMod')]")
	WebElement modifyProduct;
	
	@FindBy(xpath="//td[contains(text(),'Product Name')]")
	WebElement productNameText;
	
	@FindBy(id="brandProductName")
	WebElement brandProductName;
	
	@FindBy(id="btnSearch")
	WebElement saveButton;
	
	//------------------------------
	
	@FindBy(id="brandList_$brandproductList.brandId")
	WebElement KAZ_ProductName;
	//	PHYSIONEAL 40 1.36% 2L SY2 TB(RUSSIA/UKRAINE/ KAZAKHSTAN)
	
	@FindBy(xpath="//tr[@class='trbg']//td[@colspan='2' and text()='50085412606779']")
	WebElement KAZ_PID;
	
	@FindBy(xpath="//table[@id='mfrSiteTable']//tr[@class='trbg']//td[@class='body_text'][contains(text(), 'Kazakhstan(KZ)')]")
	WebElement aTargetMarket;
	
	@FindBy(xpath="//input[@name='cancel']")
	WebElement ViewProduct_Cancel;
	
	@FindBy(xpath="//td[contains(normalize-space(.), 'Baxter Healthcare Corporation')]/preceding-sibling::td[1]/a")
	WebElement productSearchResult;
	
	@FindBy(xpath="//td[contains(text(),'Site Name')]/parent::tr/following-sibling::tr/td[contains(text(),'Spain(ES)')]")
	WebElement siteValueOneSces;
	
	@FindBy(xpath="//td[contains(text(),'Site Name')]/parent::tr/following-sibling::tr/td[contains(text(),'Saudi Arabia(SA)')]")
	WebElement siteValueTwoSces;
	
	@FindBy(xpath="//td[contains(text(),'Site Name')]/parent::tr/following-sibling::tr/td[contains(text(),'Saudi Arabia(SA)')]")
	WebElement siteValueOneScbs;
	
	@FindBy(xpath="//td[contains(text(),'Site Name')]/parent::tr/following-sibling::tr/td[contains(text(),'Bahrain(BH)')]")
	WebElement siteValueTwoScbs;
	
	@FindBy(id="strength")
	WebElement strength;
	
	@FindBy(id="unit")
	WebElement unit;
	
	@FindBy(id="quantity")
	WebElement quantity;
	
	@FindBy(id="packaging")
	WebElement packaging;
		
	@FindBy(xpath="//*[@id='schTargetmarket']")
	WebElement TargetMarket;
	
	@FindBy(xpath="//select[@name='schTargetmarket' and @class='list_width_02' and @id='schTargetmarket']/option[@value='1264253557063681']")
	WebElement TargetMarket_Kazakhstan;
	
	@FindBy(xpath="//tr[@class='trbg']//td[7][@class='body_text']")
	WebElement Catpure_1stPID;
	
	@FindBy(xpath="//input[@name='schProduct']")
	WebElement srchProductName;
	
	@FindBy(xpath="//input[@name='btnClear']")
	WebElement Clear;
	
	
	@FindBy(xpath="//img[@title='Modify']")
	WebElement Product_Modify;
	
	@FindBy(xpath="//input[@id='reorderQty0']")
	WebElement Enter_Quantity;
	
	@FindBy(xpath="//input[@name='packaging']")
	WebElement Enter_Package_quantity;
	
	
	@FindBy(xpath="//input[@value='Save']")
	WebElement Save_Quantity;
	
	@FindBy(xpath="//a[@id='customer' and text()='Marketing Authorization Holder']")
	WebElement linkMarketingAuthorizationHolder;
	
	@FindBy(xpath = "//td[@class='header_hdng' and normalize-space()='Marketing Authorization Holder List']")
	WebElement headingTextMarketingAuthorizationHolderList;
	
	
	@FindBy(xpath = "//a[@id='showEntSearchId']")
	WebElement linkShowSearch;
	
	@FindBy(xpath = "//a[normalize-space()='Show Search' or normalize-space()='Hide Search' ]")
	WebElement linkShowOrHideSearch;
	
	@FindBy(xpath = "//input[@id='schEnterprise']")
	WebElement inputMarketingAuthorizationHolderName;
	
	@FindBy(xpath = "//input[@id='btnSearch']")
	WebElement buttonSearch;
	
	@FindBy(xpath = "//td[@class='header_hdng' and normalize-space()='View Marketing Authorization Holder']")
	WebElement headingTextViewMarketingAuthorizationHolder;
	
	@FindBy(xpath = "//select[@id='cmbEnterprise']")
	WebElement listMarketingAuthorizationHolderNameType;
	
	public List<WebElement> linkMarketingAuthorizationHolderSearchResults(){

		return getDriver().findElements(By.xpath("//a[contains(@id,'manu_')]"));

	}

	@FindBy(xpath = "//a[contains(@id,'manu_')]")
	WebElement linkMarketingAuthorizationHolderSearchResult;

	@FindBy(xpath = "//a[@title='Modify']")
	WebElement linkModifyMarketingAuthorizationHolder;

	@FindBy(xpath = "//td[@class='header_hdng' and normalize-space()='Marketing Authorization Holder']")
	WebElement headingTextMarketingAuthorizationHolder;

	@FindBy(xpath = "//td[@class='header_hdng' and normalize-space()='Modify Marketing Authorization Holder']")
	WebElement headingTextModifyMarketingAuthorizationHolder;
	
	@FindBy(xpath = "//input[@id='entZip']")
	WebElement inputMarketingAuthourizationHolderZip;

	@FindBy(xpath = "//input[@id='entSave4']")
	WebElement buttonMarketingAuthourizationHolderSave;

	@FindBy(xpath = "(//td[normalize-space()='Zip Code']/following-sibling::td)[1]")
	WebElement webElementZipCodeMarketingAuthourizationHolder;

	@FindBy(xpath = "//input[@name='BackButton']")
	WebElement buttonBackMarketingAuthourizationHolder;
	
	//-----------------------------------------------------
	
	
	
	public boolean validateProductPageIsDisplayed() {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForElementToBeDisplayed(getDriver(), showSearch, 30, 3);
		boolean status = showSearch.isDisplayed();
		return status;
	}
	
	public void clickOnShowSearch(String message) {
		click(showSearch, message);
	}
	
	public void enterProductId(String message, String data) {
		waitForElementToBeDisplayed(getDriver(), productId, 20, 2);
		sendKeys(productId, data, message);
	}
	
	public void clickOnSearchButton(String message) {
		click(searchButton, message);
	}
	
	public boolean validateSearchResults() {
		String text = td.getTestData(BaseTest.environment, "productName");
		text = text.replaceAll("_", " ");
		WebElement productName = getDriver().findElement(By.xpath("//a[contains(text(),'"+text+"')]"));
		waitForElementToBeDisplayed(getDriver(), productName, 20, 2);
		boolean status = productName.isDisplayed();
		return status;
	}

	public void clickOnProductName(String message) {
		String text = td.getTestData(BaseTest.environment, "productName");
		text = text.replaceAll("_", " ");
		WebElement productName = getDriver().findElement(By.xpath("//a[contains(text(),'"+text+"')]"));
		click(productName, message);
	}
	
	public boolean validateProductDetails() {
		boolean status = viewProduct.isDisplayed();
		return status;
	}
	
	public boolean validateOrgDetails(String message) {
		String text = getAttribute(orgValue,"innerText" , message);
		System.out.println(text);
		boolean status = orgValue.isDisplayed();
		return status;
	}
	
	public boolean validateSiteDetails(String message) {
		scrollToWebElement(siteValue);
		String text = getAttribute(siteValue,"innerText" , message);
		System.out.println(text);
		boolean status = siteValue.isDisplayed();
		return status;
	}
	
	public LoginPage returnToLogin() {
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}
	
	public boolean validateSearchResultsForBahrain() {
		String text = td.getTestData(BaseTest.environment, "productNameForBahrain");
		text = text.replaceAll("_", " ");
		WebElement productName = getDriver().findElement(By.xpath("//a[contains(text(),'"+text+"')]"));
		waitForElementToBeDisplayed(getDriver(), productName, 20, 2);
		boolean status = productName.isDisplayed();
		return status;
	}
	
	public void clickOnProductNameForBahrain(String message) {
		String text = td.getTestData(BaseTest.environment, "productNameForBahrain");
		text = text.replaceAll("_", " ");
		WebElement productName = getDriver().findElement(By.xpath("//a[contains(text(),'"+text+"')]"));
		click(productName, message);
	}
	
	public boolean validateSiteDetailsForBahrain(String message) {
		scrollToWebElement(siteValueBahrain);
		String text = getAttribute(siteValueBahrain,"innerText" , message);
		System.out.println(text);
		boolean status = siteValueBahrain.isDisplayed();
		return status;
	}
	
	public boolean validateSiteDetailsForSaudi(String message) {
		scrollToWebElement(siteValueSaudi);
		String text = getAttribute(siteValueSaudi,"innerText" , message);
		System.out.println(text);
		boolean status = siteValueSaudi.isDisplayed();
		return status;
	}
	
	public void enterProductIdForKorea(String message,String data) {
		waitForElementToBeDisplayed(getDriver(), productId, 20, 2);
		sendKeys(productId, data, message);
	}
	
	public boolean validateSearchResultsForKorea() {
		String text = td.getTestData(BaseTest.environment, "productNameForKorea");
		text = text.replaceAll("_", " ");
		WebElement productName = getDriver().findElement(By.xpath("//a[contains(text(),'"+text+"')]"));
		waitForElementToBeDisplayed(getDriver(), productName, 20, 2);
		boolean status = productName.isDisplayed();
		return status;
	}
	
	public void clickOnProductNameForKorea(String message) {
		String text = td.getTestData(BaseTest.environment, "productNameForKorea");
		text = text.replaceAll("_", " ");
		WebElement productName = getDriver().findElement(By.xpath("//a[contains(text(),'"+text+"')]"));
		click(productName, message);
	}
	
	public boolean validateSiteDetailsForKorea(String message) {
		scrollToWebElement(siteValueKorea);
		String text = getAttribute(siteValueKorea,"innerText" , message);
		System.out.println(text);
		boolean status = siteValueKorea.isDisplayed();
		return status;
	}
	
	public void validateManufacturingSiteLimit() {
		waitForElementToBeDisplayed(getDriver(), manufacturingSiteList, 20, 2);
		scrollToWebElement(manufacturingSiteList);
		WebElement el = getDriver().findElement(By.xpath("//td[contains(text(),'Baxter Healthcare Corporation')]/following-sibling::td[3]"));
		String text = getAttribute(el, "innerText", "Checking manufacturing site threshold value");
		int a = Integer.parseInt(text);
		Assert.assertTrue(a>10, "Threshold value is less than 10");
	}
	
	public boolean validateSearchResultsForProductEdit() {
		String text = td.getTestData(BaseTest.environment, "productNameFor58174");
		text = text.replaceAll("_", " ");
		WebElement productName = getDriver().findElement(By.xpath("//a[contains(text(),'"+text+"')]"));
		waitForElementToBeDisplayed(getDriver(), productName, 20, 2);
		boolean status = productName.isDisplayed();
		return status;
	}
	
	public void clickOnProductModify(String message) {
		click(modifyProduct, message);
		waitForElementToBeDisplayed(getDriver(), productNameText, 20, 2);
	}
	
	public void updateProductName(String message) {
		String randomName = generateRandomString(50);
		sendKeys(brandProductName, randomName, message);
		click(saveButton, "Saving product name details");
		WebElement productName = getDriver().findElement(By.xpath("//a[contains(text(),'"+randomName+"')]"));
		waitForElementToBeDisplayed(getDriver(), productName, 20, 2);
		String text = getAttribute(getDriver().findElement(By.xpath("//td[contains(text(),'Baxter Healthcare Corporation')]/preceding-sibling::td/a")), "innerText", "Checking the product name is updated or not");
		Assert.assertTrue(text.trim().equalsIgnoreCase(randomName), "Product name is not updated");
		String a = td.getTestData(BaseTest.environment, "productNameFor58174");
		a = a.replaceAll("_", " ");
		click(modifyProduct, message);
		waitForElementToBeDisplayed(getDriver(), productNameText, 20, 2);
		sendKeys(brandProductName, a, "Re changing the name back to old name");
		click(saveButton, "Saving product name details");
		WebElement b = getDriver().findElement(By.xpath("//a[contains(text(),'"+a+"')]"));
		waitForElementToBeDisplayed(getDriver(), b, 20, 2);
		String text1 = getAttribute(getDriver().findElement(By.xpath("//td[contains(text(),'Baxter Healthcare Corporation')]/preceding-sibling::td/a")), "innerText", "Checking the product name is updated to old name or not");
		Assert.assertTrue(text1.trim().equalsIgnoreCase(a), "Product name is not updated second time");
	}
	
	//KAZ
	
		public void Enter_ProductId(String PID) {
			waitForElementToBeDisplayed(getDriver(), productId, 20, 2);
			sendKeys(productId, PID, "Product ID entered");
		}
		
		public boolean Verify_KAZProductNameAndOpen(String message) {
			String text = getAttribute(KAZ_ProductName,"innerText", "KAZ_Product");
			System.out.println(text);
			boolean status = KAZ_ProductName.isDisplayed();
			if (status = true) {
				click(KAZ_ProductName, "PHYSIONEAL");
			}else
				System.out.println("Product : " + "PHYSIONEAL" + ":is not listed ");
			return status;
		}
		
		public void Verify_KAZ_GTIN(String aPID) {
			String ePID = getAttribute(KAZ_PID,"innerText"," Actual Product ID");
			System.out.println(ePID);
			if (ePID.trim().equals(aPID.trim())) {
				System.out.println("Product : " + aPID + " matched with -"+ ePID);
			}else
				System.out.println("Product : " + aPID + ":is not listed ");	
		}
		
		public void Verify_TargetMarket_KAZ(String TargetMarket) {
			String eTargetMarket = getAttribute(aTargetMarket,"innerText"," Actual Target Market Name");
			System.out.println(eTargetMarket);
			if (eTargetMarket.trim().equals(TargetMarket.trim())) {
				System.out.println("Product : " + TargetMarket + " matched with -"+ eTargetMarket);
			}else
				System.out.println("Product : " + TargetMarket + ":is not listed ");	
		}	
		
		public void Click_ViewProduct_Cancel(String message) {
			click(ViewProduct_Cancel, message);
		}
		
		public void OpenAndModifyQuantity(String ProductQuantity) {
			click(Product_Modify, "Cancel Details page");
			sendKeys(Enter_Quantity, ProductQuantity, "ProductQuantity entered ");
			//sendKeys(Enter_Package_quantity, ProductQuantity, " Package Quantity entered");
			click(Save_Quantity, "Save Quantity & close the page");	
		}
		
		public String CapturePID(String KAZPID) {
			
		    WebElement PID_Text = getDriver().findElement(By.xpath("//tr[@class='trbg']//td[7][@class='body_text']"));
		    // Store the body text
		    String aPID = PID_Text.getText();
		    System.out.println(aPID);
		    return aPID;
		}
		
		
		
		public void TargetMarketKazakhstan(String message) {
			click(TargetMarket,message);
			click(TargetMarket_Kazakhstan,message);
		}
		
		public void FetchProductID_TargetMarketKazakhstan(String ProductName) {
			click(Clear, "Clear the filed");
			click(TargetMarket,"Select Target market");
			click(TargetMarket_Kazakhstan,"Select Target market Kazakhstan");
			sendKeys(srchProductName, ProductName, "ENter ProductQuantity name - " + ProductName);
		}
		
		
		public void searchMarketingAuthorizationHolder(String marketingAuthorizationHolderName) throws Exception {
			String searchType = "=";
			getDriver().switchTo().defaultContent();
			getDriver().switchTo().frame(bodyFrame);
			if(linkShowOrHideSearch.getText().trim().equals("Show Search")) {
				click(linkShowOrHideSearch, "Click on Show Search");
			}
			waitForElementToBeDisplayed(getDriver(), buttonSearch, 10, 1);
			
			sendKeys(inputMarketingAuthorizationHolderName, marketingAuthorizationHolderName, "Enter Marketing Authorization Holder Name");
			click(buttonSearch, "Click on button Search");
			JavaScriptExecutorUtil.waitUntilJavaScriptCompletesByDuration(getDriver(),10);
		}
		
	public boolean validateSearchResultsForSaudiExport() {
		String text = td.getTestData(BaseTest.environment, "productNameForSaudi");
		text = text.replaceAll("_", " ");
		WebElement productName = getDriver().findElement(By.xpath("//a[contains(text(),'"+text+"')]"));
		waitForElementToBeDisplayed(getDriver(), productName, 20, 2);
		boolean status = productName.isDisplayed();
		return status;
	}
	
	public void clickOnProductNameForSaudiExport(String message) {
		String text = td.getTestData(BaseTest.environment, "productNameForSaudi");
		text = text.replaceAll("_", " ");
		WebElement productName = getDriver().findElement(By.xpath("//a[contains(text(),'"+text+"')]"));
		click(productName, message);
	}
	
	public boolean validateSearchResultsForSharedEuSa() {
		waitForElementToBeDisplayed(getDriver(), productSearchResult, 20, 2);
		boolean status = productSearchResult.isDisplayed();
		return status;
	}
	
	public void clickOnProductNameInSearchResult(String message) {
		click(productSearchResult, message);
	}
	
	public boolean validateSiteDetailsForSharedCodeEuSa(String message) {
		scrollToWebElement(siteValueOneSces);
		String text = getAttribute(siteValueOneSces,"innerText" , message);
		System.out.println(text);
		boolean status = siteValueOneSces.isDisplayed();
		return status;
	}
	
	public boolean validateSecondSiteDetailsForSharedCodeEuSa(String message) {
		scrollToWebElement(siteValueTwoSces);
		String text = getAttribute(siteValueTwoSces,"innerText" , message);
		System.out.println(text);
		boolean status = siteValueTwoSces.isDisplayed();
		return status;
	}
	
	public boolean validateSiteDetailsForSharedCodeBhSa(String message) {
		scrollToWebElement(siteValueOneScbs);
		String text = getAttribute(siteValueOneScbs,"innerText" , message);
		System.out.println(text);
		boolean status = siteValueOneScbs.isDisplayed();
		return status;
	}
	
	public boolean validateSecondSiteDetailsForSharedCodeBhSa(String message) {
		scrollToWebElement(siteValueTwoScbs);
		String text = getAttribute(siteValueTwoScbs,"innerText" , message);
		System.out.println(text);
		boolean status = siteValueTwoScbs.isDisplayed();
		return status;
	}
	
	public LoginPage updateProductDetails() {
		sendKeys(strength, "4", "update product strength");
		sendKeys(unit, "ml", "update product unit");
		sendKeys(quantity, "1", "update product quantity");
		sendKeys(packaging, "Carton", "update product packaging");
		click(saveButton, "Saving product editing details");
		waitForElementToBeDisplayed(getDriver(), productSearchResult, 30, 2);
		getDriver().switchTo().defaultContent();
		LoginPage lp = new LoginPage();
		return lp;
	}
	
	public void openMarketingAuthorizationHolder() throws Exception {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(headFrame);
		
		click(linkMarketingAuthorizationHolder, "Click on link Marketing Authorization Holder");
		JavaScriptExecutorUtil.waitUntilJavaScriptCompletesByDuration(getDriver(), 10);
		
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		waitForVisibility(headingTextMarketingAuthorizationHolderList);
		
		
		
	}
	
	public void searchMarketingAuthorizationHolder() throws Exception {
		String searchType = "=";
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		if(linkShowOrHideSearch.getText().trim().equals("Show Search")) {
			click(linkShowOrHideSearch, "Click on Show Search");
		}
		waitForElementToBeDisplayed(getDriver(), buttonSearch, 10, 1);
		selectElementFromDropDownByVisibleText(listMarketingAuthorizationHolderNameType, searchType);
		String marketingAuthorizationHolderName = (String) td.getTestDataByPath(BaseTest.environment, "TestCase.SeP_RTS_OQ_5828Test.MarketingAuthorizationHolderName");
		sendKeys(inputMarketingAuthorizationHolderName, marketingAuthorizationHolderName, "Enter Marketing Authorization Holder Name");
		click(buttonSearch, "Click on button Search");
		JavaScriptExecutorUtil.waitUntilJavaScriptCompletesByDuration(getDriver(),10);
		
	}

	public String updateMarketingAuthourizationHolderZipCode() {
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		
		waitForElementToBeDisplayed(getDriver(), linkMarketingAuthorizationHolderSearchResult, 10, 1);
		Assert.assertTrue(linkMarketingAuthorizationHolderSearchResults().size()>0  , "No Marketing Authorization Holder Found");
		click(linkModifyMarketingAuthorizationHolder, "Click on Marketing Authorization Holder Modify link");
		
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		
		waitForElementToBeDisplayed(getDriver(), headingTextModifyMarketingAuthorizationHolder, 10, 1);
		String zipCode = RandomStringUtils.randomNumeric(5)+RandomStringUtils.randomAlphabetic(1);
		inputMarketingAuthourizationHolderZip.clear();
		sendKeys(inputMarketingAuthourizationHolderZip, zipCode, "");
		click(buttonMarketingAuthourizationHolderSave, "Click on Save Button");
		
		waitForElementToBeDisplayed(getDriver(), headingTextMarketingAuthorizationHolderList, 10, 1);
		
		return zipCode;
	}

	public void verifyMarketingAuthorizationHolderZipUpdated(String zipCode) {
		
		getDriver().switchTo().defaultContent();
		getDriver().switchTo().frame(bodyFrame);
		
		waitForElementToBeDisplayed(getDriver(), linkMarketingAuthorizationHolderSearchResult, 10, 1);
		click(linkMarketingAuthorizationHolderSearchResult, "Click on the searched marketing authorization holder found");
		waitForElementToBeDisplayed(getDriver(), headingTextViewMarketingAuthorizationHolder, 10, 1);
		String actualZip = webElementZipCodeMarketingAuthourizationHolder.getText().trim();
		Assert.assertTrue(actualZip.equals(zipCode), "Zip code is not saved");
		ern.enterLogAndCapture("Zip code Saved successfully",highlightElement(webElementZipCodeMarketingAuthourizationHolder, "Zip code Saved successfully") );
		
		click(buttonBackMarketingAuthourizationHolder, "Click on back button");
		
		waitForElementToBeDisplayed(getDriver(), headingTextMarketingAuthorizationHolderList, 10, 1);
		
	}
}
