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

        // Read test data from YAML
        String disposition1 = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_State_Transition_Verification.Disposition1");
        String disposition2 = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_State_Transition_Verification.Disposition2");
        String siteID = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_State_Transition_Verification.SiteID");
        String eventType = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_State_Transition_Verification.EventType");

        TracePage tp = lp.clickOnTraceTab("Click on Trace Tab");
        tp.clickOnWhereIs("Click On Where is Link");

        String traceID = tp.serachForItemStateTransactionVerification();
        TracePage.serialNumber = traceID;
        tp.verifyDispositionPackedForTraceIDStateTransactionVerification(traceID);

        ScanPage sp = lp.clickOnScanTab("Click on Scan tab");
        sp.clickOnChangeDisposition("Click on Change Disposition Tab link");
        lp = sp.changeDispositionTo(siteID, disposition1);

        tp = lp.clickOnTraceTab("Click on Trace Tab");
        TracePage.serialNumber = traceID;
        tp.searchEventByTraceID(traceID);
        tp.verifyDispositionForTraceID(eventType, disposition1);

        sp = lp.clickOnScanTab("Click on Scan tab");
        sp.clickOnChangeDisposition("Click on Change Disposition Tab link");
        lp = sp.changeDispositionTo(siteID, disposition2);

        tp = lp.clickOnTraceTab("Click on Trace Tab");
        tp.searchEventByTraceID(traceID);
        tp.verifyDispositionForTraceID(eventType, disposition2);
    }
}
