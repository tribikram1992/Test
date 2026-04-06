package com.pages;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.awt.AWTException;
import java.awt.Rectangle;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;

import com.google.common.base.Function;
import com.report.ExtentReportNG;

public class BasePage {
	private static ThreadLocal<WebDriver> driver = new ThreadLocal<WebDriver>();
	protected ExtentReportNG ern = new ExtentReportNG();

	public BasePage(WebDriver driverInstance) {
		driver.set(driverInstance);
		PageFactory.initElements(driver.get(), this);
	}

	protected WebDriver getDriver() {
		return driver.get();
	}

	public boolean SwitchAlert() {
		boolean Flag = false;
		try {
			if (getDriver().switchTo().alert() != null) {
				getDriver().switchTo().alert().accept();
				Flag = true;
			}
		} catch (NoAlertPresentException e) {
		}
		return Flag;
	}

	public void doubleClick(WebElement element, String message) {
		String text = message.replaceAll("\\s+", "");
		String filePath = null;
		try {
			filePath = highlightElement(element, text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ern.enterLogAndCapture(message, filePath);
		if ((getDriver() != null) && (element != null)) {
			(new Actions(getDriver())).doubleClick(element).build().perform();
		}
	}

	public boolean waitForVisibility(WebElement ele) {
		WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(20));
		return wait.until(ExpectedConditions.visibilityOf(ele)) != null;
	}

	public void click(WebElement ele, String message) {
		String text = message.replaceAll("\\s+", "");
		String filePath = null;
		try {
			filePath = highlightElement(ele, text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ern.enterLogAndCapture(message, filePath);
		waitForVisibility(ele);
		ele.click();
	}

	public void sendKeys(WebElement ele, String txt, String message) {
		waitForVisibility(ele);
		ele.clear();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ele.sendKeys(txt);
		String text = message.replaceAll("\\s+", "");
		String filePath = null;
		try {
			filePath = highlightElement(ele, text);
			Thread.sleep(3000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ern.enterLogAndCapture(message, filePath);
	}

	public String getAttribute(WebElement ele, String attribute, String message) {
		String text = message.replaceAll("\\s+", "");
		String filePath = null;
		try {
			filePath = highlightElement(ele, text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ern.enterLogAndCapture(message, filePath);
		return ele.getAttribute(attribute);
	}

	public void selectDropDownValue(WebElement element, String value) {
		try {
			if (element != null) {
				Select selectBox = new Select(element);
				selectBox.selectByValue(value);
				Thread.sleep(3000);
			}
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void switchWindow() {
		String parentWindow = getDriver().getWindowHandle();
		Set<String> s = getDriver().getWindowHandles();
		System.out.println("Total Windows::::::::::::" + s.size());
		Iterator<String> I1 = s.iterator();
		while (I1.hasNext()) {
			String child_window = I1.next();
			if (!parentWindow.equals(child_window)) {
				getDriver().switchTo().window(child_window);
			}
		}
		System.out.println(getDriver().getTitle());
	}

	public void switchToParentWindow(String parentWindow) {
		getDriver().switchTo().window(parentWindow);
	}

	public String getCurrentDateAndTime() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		System.out.println(dtf.format(now));
		return dtf.format(now);
	}

	public void waitUntilPageLoad() {
		Wait<WebDriver> wait = new FluentWait<WebDriver>(getDriver()).withTimeout(Duration.ofSeconds(30))
				.pollingEvery(Duration.ofSeconds(5)).ignoring(Exception.class);
		ExpectedCondition<Boolean> documentReady = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				JavascriptExecutor js = (JavascriptExecutor) driver;
				String state = (String) js.executeScript("return document.readyState;");
				return state.equals("complete");
			}
		};
		wait.until(documentReady);
	}

	public String highlightElement(WebElement element, String stepName) {
		Thread currentThread = Thread.currentThread();
		String threadName = currentThread.getName();

		@SuppressWarnings("unused")
		TakesScreenshot ts = (TakesScreenshot) getDriver();
		for (int i = 0; i < 2; i++) {
			JavascriptExecutor js = (JavascriptExecutor) getDriver();
			js.executeScript("arguments[0].style.border='3px solid orange'", element);
		}
		Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		BufferedImage screenFullImage;
		try {
			screenFullImage = new Robot().createScreenCapture(screenRect);
			String screenshotPath = System.getProperty("user.dir") + File.separator + "reports" + ExtentReportNG.time
					+ File.separator + threadName + stepName + ".png";
			ImageIO.write(screenFullImage, "png", new File(screenshotPath));
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		js.executeScript("arguments[0].style.border='0px solid orange'", element);
		return System.getProperty("user.dir") + File.separator + "reports" + ExtentReportNG.time + File.separator
				+ threadName + stepName + ".png";
	}

	public static WebElement waitForElementToBeDisplayed(WebDriver driver, WebElement element, int timeoutInSeconds,
			int pollingIntervalInSeconds) {
		Wait<WebDriver> wait = new FluentWait<>(driver).withTimeout(Duration.ofSeconds(timeoutInSeconds))
				.pollingEvery(Duration.ofSeconds(pollingIntervalInSeconds)).ignoring(NoSuchElementException.class)
				.ignoring(StaleElementReferenceException.class).ignoring(NoAlertPresentException.class);

		return wait.until(new Function<WebDriver, WebElement>() {
			public WebElement apply(WebDriver driver) {
				if (element.isDisplayed()) {
					return element;
				} else {
					return null;
				}
			}
		});
	}

	public void scrollTableForWebElementIntoView(By by, By e) {
		List<WebElement> elements = getDriver().findElements(by);
		int a = elements.size();
		WebElement ele = elements.get(a - 1).findElement(e);
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		js.executeScript("arguments[0].scrollIntoView(true);", ele);
	}

	public String returnTodayDate() {
		SimpleDateFormat formatDate = new SimpleDateFormat("dd-MMM-yyyy");
		Date date = new Date();
		return formatDate.format(date);
	}

	public long randomNumber(int numberOfDigits) {
		Random random = new Random();
		long min = (long) Math.pow(10, numberOfDigits - 1);
		long max = (long) Math.pow(10, numberOfDigits) - 1;
		return min + (long) (random.nextDouble() * (max - min + 1));
	}

	public void selectElementFromDropDownByValue(WebElement ele, String value) {
		Select dropdown = new Select(ele);
		dropdown.selectByValue(value);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String path = highlightElement(ele, "Selecting " + value + " from the dropdown");
		ern.enterLogAndCapture("Selecting " + value + " from the dropdown", path);
	}

	public void selectElementFromDropDownByVisibleText(WebElement ele, String value) {
		Select dropdown = new Select(ele);
		dropdown.selectByVisibleText(value);
	}

	public String decryptString(String decryptValue) {
		String result = new String();
		char[] charArray = decryptValue.toCharArray();
		for (int i = 0; i < charArray.length; i = i + 2) {
			String st = "" + charArray[i] + "" + charArray[i + 1];
			char ch = (char) Integer.parseInt(st, 16);
			result = result + ch;
		}
		return result;
	}

	public static String generateRandomString(int length) {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder randomString = new StringBuilder();
		Random rnd = new Random();
		while (randomString.length() <= length) {
			int index = (int) (rnd.nextFloat() * chars.length());
			randomString.append(chars.charAt(index));
		}
		String saltStr = randomString.toString();
		return saltStr;
	}

	public void scrollToWebElement(WebElement ele) {
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		js.executeScript("arguments[0].scrollIntoView(true);", ele);
	}

	public void validateUsernameFormat(String name) {
		String pattern = "^\\s*([a-zA-Z0-9,\\s-,\\.]*\\s*)*,\\s*([a-zA-Z0-9,\\s-,\\.]*\\s*)*$";
		Pattern compiledPattern = Pattern.compile(pattern);
		Matcher matcher = compiledPattern.matcher(name);
		if (name.contains("Anders") || name.contains("Cristina") || name.contains("Mercedes") || name.contains("Patrik")
				|| name.contains("Paola") || name.contains("Huet") || name.contains("Katriina")
				|| name.contains("Tiina") || name.contains("Therese") || name.contains("Christelle")
				|| name.contains("'") || name.contains("-") || name.contains("_") || name.contains("Luisa")) {
		} else {
			Assert.assertTrue(matcher.matches(), "Username is not in the expected format" + name);
		}

	}

	public void compareStatus(String expected, String actual) {
		Assert.assertTrue(actual.contains(expected), "Status didn't match");
	}

	public boolean compareFalseStatus(String expected, String actual) {
		boolean status = actual.contains(expected);
		return status;
	}

	public int generateRandomTwoDigitNumber() {
		Random random = new Random();
		int num = random.nextInt(90) + 10;
		return num;
	}

	public String getLastDayOfMonth() {
		Calendar calendar = Calendar.getInstance();
		int currentYear = calendar.get(Calendar.YEAR);
		int currentMonth = calendar.get(Calendar.MONTH) + 1;
		calendar.set(currentYear, currentMonth - 1, 1);
		calendar.roll(Calendar.DAY_OF_MONTH, -1);
		@SuppressWarnings("unused")
		int lastDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		return dateFormat.format(calendar.getTime());
	}

	public String getYesterdayDate() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -1);
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
		String yesterday = sdf.format(cal.getTime());
		return yesterday;
	}

	public String getFixedTimestamp() {
		ZonedDateTime dateTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String datePart = dateTime.format(dateFormatter);
		int milli = dateTime.getNano() / 1_000_000;
		String milliPart = String.format(".%03d", milli);
		return datePart + milliPart + "Z";
	}

	public void updateTagWithValues(String filePath, String tagName, List<String> newValues) {
		try {
			File xmlFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			// Remove whitespace-only text nodes
			removeWhitespaceNodes(doc.getDocumentElement());

			NodeList nodeList = doc.getElementsByTagName(tagName);
			int count = Math.min(nodeList.getLength(), newValues.size());

			for (int i = 0; i < count; i++) {
				Node node = nodeList.item(i);
				node.setTextContent(newValues.get(i));
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

			System.out.println("Tag '" + tagName + "' updated successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeWhitespaceNodes(Element element) {
		NodeList children = element.getChildNodes();
		for (int i = children.getLength() - 1; i >= 0; i--) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE && child.getTextContent().trim().isEmpty()) {
				element.removeChild(child);
			} else if (child.getNodeType() == Node.ELEMENT_NODE) {
				removeWhitespaceNodes((Element) child);
			}
		}
	}

	public String getTimestampPlusMinutes(int minutesToAdd) {
		ZonedDateTime dateTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).plusMinutes(minutesToAdd);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		return dateTime.format(formatter);
	}

	public void updateLastBizTransaction(String filePath, String newValue) {
        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList bizTransactionList = doc.getElementsByTagName("bizTransaction");

            if (bizTransactionList.getLength() > 0) {
                Node lastBizTransaction = bizTransactionList.item(bizTransactionList.getLength() - 1);
                lastBizTransaction.setTextContent(newValue);
                System.out.println("Updated last <bizTransaction> to: " + newValue);
            } else {
                System.out.println("No <bizTransaction> tags found.");
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
  }

	// Code Added by Ajay for China-XML manipulation.

public static String generateAlphanumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        Random rand = new Random();

        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(rand.nextInt(chars.length())));
        }

        return result.toString();
    }

	public static String getLotNumber(String filePath) throws Exception {
		File xmlFile = new File(filePath); // Replace with your XML file path
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true); // Important for handling namespaces
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(xmlFile);

		// Create XPath
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		// Register namespace for gs1ushc
		javax.xml.namespace.NamespaceContext nsContext = new javax.xml.namespace.NamespaceContext() {
			public String getNamespaceURI(String prefix) {
				if ("gs1ushc".equals(prefix)) {
					return "http://epcis.gs1us.org/hc/ns";
				} else if ("epcis".equals(prefix)) {
					return "urn:epcglobal:epcis:xsd:1";
				}
				return javax.xml.XMLConstants.NULL_NS_URI;
			}

			public String getPrefix(String uri) {
				return null;
			}

			public java.util.Iterator getPrefixes(String uri) {
				return null;
			}
		};
		xpath.setNamespaceContext(nsContext);

		// XPath expression to get lotNumber
		String expression = "//gs1ushc:lotNumber";
		XPathExpression expr = xpath.compile(expression);

		String lotNumber = (String) expr.evaluate(doc, XPathConstants.STRING);
		return lotNumber;

	}

	public String getCurrentDate() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		LocalDateTime now = LocalDateTime.now();
		System.out.println(dtf.format(now));
		return dtf.format(now);
	}

	public String getDatePrior(int days) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		LocalDate datePrior = LocalDate.now().minusDays(days);
		return dtf.format(datePrior);
	}

}
