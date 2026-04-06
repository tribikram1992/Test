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

public class EventTimeVerification extends BaseTest {
	
	ExtentReportNG ern = new ExtentReportNG();
	
	@Test
	public void SeP_RTS_OQ_EvetTime_Test() throws Exception {
		JavaScriptExecutorUtil.waitForPageLoad(getDriver());
		ern.enterLog("RTS Application Loaded");
		boolean status = lp.validateHomePageDisplayed();
		Assert.assertTrue(status, "Home page is not displayed");
		
		String products = (String)td.getTestDataByPath(BaseTest.environment, "TestCase.SeP_RTS_OQ_EvetTime_Test.ProductGTIN");
		String cases = (String)td.getTestDataByPath(BaseTest.environment, "TestCase.SeP_RTS_OQ_EvetTime_Test.Case");
		String pallets = (String)td.getTestDataByPath(BaseTest.environment, "TestCase.SeP_RTS_OQ_EvetTime_Test.Pallet");
		
		String productsSiteCode = (String)td.getTestDataByPath(BaseTest.environment, "TestCase.SeP_RTS_OQ_EvetTime_Test.ProductSiteID");
		String casesSiteCode = (String)td.getTestDataByPath(BaseTest.environment, "TestCase.SeP_RTS_OQ_EvetTime_Test.CaseSiteID");
		String palletsSiteCode = (String)td.getTestDataByPath(BaseTest.environment, "TestCase.SeP_RTS_OQ_EvetTime_Test.PalletSiteID");
		
		/** allocate products**/
		SerializePage sp = lp.clickOnSerializeTab("Open Serialize Tab for Event Time test");
		sp.createSerialNumbers(products, productsSiteCode, 10);
		SerializePage.products = sp.fetchAllSerialNumbers(10);
		sp.clearAllocatedData();
		
		/** allocate case**/
		sp.createSerialNumbers(cases, casesSiteCode, 5);
		SerializePage.cases = sp.fetchAllSerialNumbers(5);
		sp.clearAllocatedData();
		
		/** allocate pallet**/
		sp.createSerialNumbers(pallets, palletsSiteCode, 2);
		SerializePage.pallets = sp.fetchAllSerialNumbers(2);
		sp.clearAllocatedData();
		
		/** Update Aggregate XML**/
		String lotID = sp.updateAggregrateXmlForEventTimeTest();
		/** Upload Aggregate XML**/
		
		TracePage tp = lp.clickOnTraceTab("Click on Trace tab");
		tp.clickOnTraceTabUpload("Click on Upload under Trace tab");
		String pathToXML = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator
				+ "resources" + File.separator + "5829ETT_Testing.xml";
		tp.uploadXml(pathToXML);
		tp.clickOnImportButton("Click On Import Button");
		
		
		
		/** Update Rework XML**/
		sp.updateReworkAggregrateXmlForEventTimeTest();
		/** Upload Rework XML**/
		tp = lp.clickOnTraceTab("Click on Trace tab");
		tp.clickOnTraceTabUpload("Click on Upload under Trace tab");
		String pathToXML2 = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator
				+ "resources" + File.separator + "5829ETT_Rework_testing.xml";
		tp.uploadXml(pathToXML2);
		tp.clickOnImportButton("Click On Import Button");
		
		
		/** Update Ship XML**/
		sp.updateShippingXmlForEventTimeTest();
		/** Upload Ship XML**/
		tp = lp.clickOnTraceTab("Click on Trace tab");
		tp.clickOnTraceTabUpload("Click on Upload under Trace tab");
		String pathToXML3 = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator
				+ "resources" + File.separator + "5829ETT_Shipping_Testing.xml";
		tp.uploadXml(pathToXML3);
		tp.clickOnImportButton("Click On Import Button");
		
		//Once XML is working fine obsolete this line
		lotID= "5829SDT128236";
		
		tp = lp.clickOnTraceTab("Click on Trace tab");
		tp.searchEventByLotID(lotID);
		
		tp.verifyShipEventEventTime(4);
		tp.verifyAggregrateEventFor2ndPalletEventTime(4);
		
		tp.verifyDisAggregrateEventFor1stPalletEventTime(4);
		int shipEvents = 1;
		int aggregrateEvent = 7;
		int disaggregrateEvent = 1;
		int commissionEvent = 3;		
		tp.verifyEventsCountByEventType(shipEvents,"Ship");
		tp.verifyEventsCountByEventType(aggregrateEvent,"Aggregate");
		tp.verifyEventsCountByEventType(disaggregrateEvent,"Disaggregate");
		tp.verifyEventsCountByEventType(commissionEvent,"Commission");
		
		int noOfPackedProduct=2;
		int noOfIntransitProduct=8;
		int noOfPackedCases=1;
		int noOfIntransitCases=4;
		int noOfPackedPallets=1;
		
		tp.verifyBatchSummaryReportEventTime(lotID,noOfPackedProduct,noOfIntransitProduct,noOfPackedCases,noOfIntransitCases,noOfPackedPallets);
		
		
		
	}
	
	
}
