package com.tests;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.pages.AddCustomerPage;
import com.pages.CustomerPage;
import com.pages.SerializePage;
import com.pages.SetupPage;
import com.pages.TracePage;
import com.report.ExtentReportNG;
import com.utils.JavaScriptExecutorUtil;

public class SoftDeleteVerification extends BaseTest {
	
	ExtentReportNG ern = new ExtentReportNG();
	
	@Test
	public void softDeleteTest() throws Exception {
		JavaScriptExecutorUtil.waitForPageLoad(getDriver());
		ern.enterLog("RTS Application Loaded");
		boolean status = lp.validateHomePageDisplayed();
		Assert.assertTrue(status, "Home page is not displayed");
		SetupPage stp = lp.clickOnSetupTab("Click on Set up page");
		boolean setUpTabStatus = stp.validateSetupTabCustomerStatus();
		Assert.assertTrue(setUpTabStatus, "Setup page is not displayed");
		CustomerPage cp = stp.clickOnCustomerLink("Click on Customer link");
		AddCustomerPage acp = cp.clickOnAddCustomer();
		String customerID1 = acp.addCuctomerDetailsAndSave(acp.getCustomer("Customer1"));
		cp.verifyCustomerAndInterChangeID(customerID1);
		acp = cp.clickOnAddCustomer();
		String customerID2 = acp.addCuctomerDetailsAndSave(acp.getCustomer("Customer2"));
		cp= new CustomerPage(getDriver());
		cp.verifyCustomerAndInterChangeID(customerID2);
		cp= new CustomerPage(getDriver());
		cp.deleteCustomer(customerID2);
		cp= new CustomerPage(getDriver());
		
		cp.verifyCustomerDeleted(customerID2);
		cp= new CustomerPage(getDriver());
		cp.addDeletedCustomerIDInActiveCustomerInterChangeID(customerID1,customerID2);
		/** allocate products**/
		SerializePage sp = lp.clickOnSerializeTab("Open Serialize Tab for Soft delete");
		sp.createSerialNumbers("20343066995243", "Baxter Healthcare Corporation", 2);
		SerializePage.products = sp.fetchAllSerialNumbers(2);
		sp.clearAllocatedData();
		
		/** allocate case**/
		sp.createSerialNumbers("50343066995244", "Baxter Healthcare Corporation", 1);
		SerializePage.cases = sp.fetchAllSerialNumbers(1);
		sp.clearAllocatedData();
		
		/** allocate pallet**/
		sp.createSerialNumbers("5413760-3", "Baxter Halle", 1);
		SerializePage.pallets = sp.fetchAllSerialNumbers(1);
		sp.clearAllocatedData();
		
		/** Update Aggregate XML**/
		sp.updateAggregrateXmlForSoftDelete(customerID1);
		/** Upload Aggregate XML**/
		TracePage tp = lp.clickOnTraceTab("Click on Trace tab");
		tp.clickOnTraceTabUpload("Click on Upload under Trace tab");
		String pathToXML = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator
				+ "resources" + File.separator + "SDT5829_Test.xml";
		tp.uploadXml(pathToXML);
		tp.clickOnImportButton("Click On Import Button");
		/** Update Shipping XML**/
		sp.updateShippingXmlForSoftDelete(customerID2);
		/** Upload Aggregate XML**/
		tp = lp.clickOnTraceTab("Click on Trace tab");
		tp.clickOnTraceTabUpload("Click on Upload under Trace tab");
		String pathToXML2 = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator
				+ "resources" + File.separator + "SDT5827_ShippingTest.xml";
		tp.uploadXml(pathToXML2);
		tp.clickOnImportButton("Click On Import Button");
		
		
		tp = lp.clickOnTraceTab("Click on Trace tab");
		tp.searchEvent(sp.lotNumber, "Ship");
		
		tp.verifySoftDeleteEvent(customerID1);
		
		tp = lp.clickOnTraceTab("Click on Trace tab");
		tp.searchBatchSummaryReport(sp.lotNumber);
		
		
	}
	
	
}
