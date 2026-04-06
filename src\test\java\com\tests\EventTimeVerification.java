package com.tests;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.pages.SerializePage;
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

        // Read serialize config from YAML
        String products = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.ProductGTIN");
        String cases = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.Case");
        String pallets = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.Pallet");
        String productsSiteCode = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.ProductSiteID");
        String casesSiteCode = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.CaseSiteID");
        String palletsSiteCode = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.PalletSiteID");
        int productCount = (int) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.ProductCount");
        int caseCount = (int) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.CaseCount");
        int palletCount = (int) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.PalletCount");

        // Read XML file names from YAML
        String aggregateXmlFile = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.AggregateXmlFile");
        String reworkXmlFile = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.ReworkXmlFile");
        String shippingXmlFile = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.ShippingXmlFile");

        // Read verification config from YAML
        int eventTimeVerificationCount = (int) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.VerificationConfig.EventTimeVerificationCount");
        int shipEventCount = (int) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.VerificationConfig.ShipEventCount");
        int aggregateEventCount = (int) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.VerificationConfig.AggregateEventCount");
        int disaggregateEventCount = (int) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.VerificationConfig.DisaggregateEventCount");
        int commissionEventCount = (int) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.VerificationConfig.CommissionEventCount");
        int noOfPackedProduct = (int) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.VerificationConfig.BatchSummary.NoOfPackedProduct");
        int noOfInTransitProduct = (int) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.VerificationConfig.BatchSummary.NoOfInTransitProduct");
        int noOfPackedCases = (int) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.VerificationConfig.BatchSummary.NoOfPackedCases");
        int noOfInTransitCases = (int) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.VerificationConfig.BatchSummary.NoOfInTransitCases");
        int noOfPackedPallets = (int) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_EvetTime_Test.VerificationConfig.BatchSummary.NoOfPackedPallets");

        /** allocate products **/
        SerializePage sp = lp.clickOnSerializeTab("Open Serialize Tab for Event Time test");
        sp.createSerialNumbers(products, productsSiteCode, productCount);
        SerializePage.products = sp.fetchAllSerialNumbers(productCount);
        sp.clearAllocatedData();

        /** allocate case **/
        sp.createSerialNumbers(cases, casesSiteCode, caseCount);
        SerializePage.cases = sp.fetchAllSerialNumbers(caseCount);
        sp.clearAllocatedData();

        /** allocate pallet **/
        sp.createSerialNumbers(pallets, palletsSiteCode, palletCount);
        SerializePage.pallets = sp.fetchAllSerialNumbers(palletCount);
        sp.clearAllocatedData();

        /** Update Aggregate XML **/
        String lotID = sp.updateAggregrateXmlForEventTimeTest();

        /** Upload Aggregate XML **/
        TracePage tp = lp.clickOnTraceTab("Click on Trace tab");
        tp.clickOnTraceTabUpload("Click on Upload under Trace tab");

        String pathToXML = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
                + File.separator + "resources" + File.separator + aggregateXmlFile;

        tp.uploadXml(pathToXML);
        tp.clickOnImportButton("Click On Import Button");

        /** Update Rework XML **/
        sp.updateReworkAggregrateXmlForEventTimeTest();

        /** Upload Rework XML **/
        tp = lp.clickOnTraceTab("Click on Trace tab");
        tp.clickOnTraceTabUpload("Click on Upload under Trace tab");

        String pathToXML2 = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
                + File.separator + "resources" + File.separator + reworkXmlFile;

        tp.uploadXml(pathToXML2);
        tp.clickOnImportButton("Click On Import Button");

        /** Update Ship XML **/
        sp.updateShippingXmlForEventTimeTest();

        /** Upload Ship XML **/
        tp = lp.clickOnTraceTab("Click on Trace tab");
        tp.clickOnTraceTabUpload("Click on Upload under Trace tab");

        String pathToXML3 = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
                + File.separator + "resources" + File.separator + shippingXmlFile;

        tp.uploadXml(pathToXML3);
        tp.clickOnImportButton("Click On Import Button");

        tp = lp.clickOnTraceTab("Click on Trace tab");
        tp.searchEventByLotID(lotID);
        tp.verifyShipEventEventTime(eventTimeVerificationCount);
        tp.verifyAggregrateEventFor2ndPalletEventTime(eventTimeVerificationCount);
        tp.verifyDisAggregrateEventFor1stPalletEventTime(eventTimeVerificationCount);

        tp.verifyEventsCountByEventType(shipEventCount, "Ship");
        tp.verifyEventsCountByEventType(aggregateEventCount, "Aggregate");
        tp.verifyEventsCountByEventType(disaggregateEventCount, "Disaggregate");
        tp.verifyEventsCountByEventType(commissionEventCount, "Commission");

        tp.verifyBatchSummaryReportEventTime(lotID, noOfPackedProduct, noOfInTransitProduct,
                noOfPackedCases, noOfInTransitCases, noOfPackedPallets);
    }
}
