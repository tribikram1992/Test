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
        cp = new CustomerPage(getDriver());
        cp.verifyCustomerAndInterChangeID(customerID2);

        cp = new CustomerPage(getDriver());
        cp.deleteCustomer(customerID2);

        cp = new CustomerPage(getDriver());
        cp.verifyCustomerDeleted(customerID2);

        cp = new CustomerPage(getDriver());
        cp.addDeletedCustomerIDInActiveCustomerInterChangeID(customerID1, customerID2);

        // Read serialize config from YAML
        String productGTIN = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_SoftDeleteTest.SerializeConfig.ProductGTIN");
        String productSiteID = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_SoftDeleteTest.SerializeConfig.ProductSiteID");
        int productCount = (int) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_SoftDeleteTest.SerializeConfig.ProductCount");

        String caseGTIN = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_SoftDeleteTest.SerializeConfig.CaseGTIN");
        String caseSiteID = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_SoftDeleteTest.SerializeConfig.CaseSiteID");
        int caseCount = (int) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_SoftDeleteTest.SerializeConfig.CaseCount");

        String palletGTIN = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_SoftDeleteTest.SerializeConfig.PalletGTIN");
        String palletSiteID = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_SoftDeleteTest.SerializeConfig.PalletSiteID");
        int palletCount = (int) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_SoftDeleteTest.SerializeConfig.PalletCount");

        String eventTypeToSearch = (String) td.getTestDataByPath(BaseTest.environment,
                "TestCase.SeP_RTS_OQ_SoftDeleteTest.SerializeConfig.EventTypeToSearch");

        /** allocate products **/
        SerializePage sp = lp.clickOnSerializeTab("Open Serialize Tab for Soft delete");
        sp.createSerialNumbers(productGTIN, productSiteID, productCount);
        SerializePage.products = sp.fetchAllSerialNumbers(productCount);
        sp.clearAllocatedData();

        /** allocate case **/
        sp.createSerialNumbers(caseGTIN, caseSiteID, caseCount);
        SerializePage.cases = sp.fetchAllSerialNumbers(caseCount);
        sp.clearAllocatedData();

        /** allocate pallet **/
        sp.createSerialNumbers(palletGTIN, palletSiteID, palletCount);
        SerializePage.pallets = sp.fetchAllSerialNumbers(palletCount);
        sp.clearAllocatedData();

        /** Update Aggregate XML **/
        sp.updateAggregrateXmlForSoftDelete(customerID1);

        /** Upload Aggregate XML **/
        TracePage tp = lp.clickOnTraceTab("Click on Trace tab");
        tp.clickOnTraceTabUpload("Click on Upload under Trace tab");

        String pathToXML = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
                + File.separator + "resources" + File.separator
                + (String) td.getTestDataByPath(BaseTest.environment,
                        "TestCase.SeP_RTS_OQ_SoftDeleteTest.AggregateXmlFile");

        tp.uploadXml(pathToXML);
        tp.clickOnImportButton("Click On Import Button");

        /** Update Shipping XML **/
        sp.updateShippingXmlForSoftDelete(customerID2);

        /** Upload Shipping XML **/
        tp = lp.clickOnTraceTab("Click on Trace tab");
        tp.clickOnTraceTabUpload("Click on Upload under Trace tab");

        String pathToXML2 = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
                + File.separator + "resources" + File.separator
                + (String) td.getTestDataByPath(BaseTest.environment,
                        "TestCase.SeP_RTS_OQ_SoftDeleteTest.ShippingXmlFile");

        tp.uploadXml(pathToXML2);
        tp.clickOnImportButton("Click On Import Button");

        tp = lp.clickOnTraceTab("Click on Trace tab");
        tp.searchEvent(sp.lotNumber, eventTypeToSearch);
        tp.verifySoftDeleteEvent(customerID1);

        tp = lp.clickOnTraceTab("Click on Trace tab");
        tp.searchBatchSummaryReport(sp.lotNumber);
    }
}
