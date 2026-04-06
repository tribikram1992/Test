package com.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.pages.ScanPage;
import com.pages.TracePage;
import com.report.ExtentReportNG;
import com.utils.JavaScriptExecutorUtil;

public class StateTransitionCSAVerification extends BaseTest {
	
	ExtentReportNG ern = new ExtentReportNG();
	
	@Test
	public void SeP_RTS_State_Transition_Verification() throws Exception {
		JavaScriptExecutorUtil.waitForPageLoad(getDriver());
		ern.enterLog("RTS Application Loaded");
		boolean status = lp.validateHomePageDisplayed();
		Assert.assertTrue(status, "Home page is not displayed");
		
		TracePage tp = lp.clickOnTraceTab("Click on Trace Tab");
		tp.clickOnWhereIs("Click On Where is Link");
		String traceID = tp.serachForItemStateTransactionVerification();
		TracePage.serialNumber= traceID;
		tp.verifyDispositionPackedForTraceIDStateTransactionVerification(traceID);
		ScanPage sp = lp.clickOnScanTab("Click on Scan tab");
		sp.clickOnChangeDisposition("Click on Change Disposition Tab link");
		String disposition = "Non Sellable Other";
		String siteID = "Baxter Halle - Manufacturing";
		lp = sp.changeDispositionTo(siteID, disposition);
		
		tp = lp.clickOnTraceTab("Click on Trace Tab");
		TracePage.serialNumber= traceID;
		tp.searchEventByTraceID(traceID);
		String eventType = "Change Disposition";
		
		tp.verifyDispositionForTraceID(eventType,disposition);
		sp = lp.clickOnScanTab("Click on Scan tab");
		sp.clickOnChangeDisposition("Click on Change Disposition Tab link");
		disposition = "Received";
		lp = sp.changeDispositionTo("Baxter Halle - Manufacturing",disposition);
		tp = lp.clickOnTraceTab("Click on Trace Tab");
		tp.searchEventByTraceID(traceID);
		eventType = "Change Disposition";
		tp.verifyDispositionForTraceID(eventType,disposition);
		
	}
	
	
}
