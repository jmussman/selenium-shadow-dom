// CustomButtonTests.java
// Copyright © 2020 Joel Mussman. All rights reserved.
//
// The website to test the buttons is set up as a Spring Boot application. This is not important for understanding
// the tests; the class is configured with @ExtendWidth and @SpringBootTest to launch the application before the tests
// so that the page can be accessed by the browser. What the tests do is what is important to understanding
// Selenium and the Shadow DOM. FYI: @ExtendWith launches the tests using the Spring test runner, and @SpringBootTest
// defines which class the application luanches from and a random open port for the application to sit on which
// is passed to the test via the @LocalServerPort injection.
//
// The page is located in main/resources/templates/index.html. The CustomButton is served out of
// main/resources/static/assets/script/custom-button.js, and the CSS is served out of
// main/resources/static/assets/style/application.css. Except for the CSS that affects the background color of the
// buttons the rest of the CSS is fluff.
//
// The question is, can you beat the "closed" shadow root? Yes! But it is complicated and is one of those things
// that is subject to change based on the version of the browser and other things. The answer is to disable closed
// shadow root in the application. To do that, we can override the function to attach the shadow root:
//
//		Element.prototype._attachShadow = Element.prototype.attachShadow;
//		Element.prototype.attachShadow = (mode) => { return Element.prototype._attachShadow({ mode: 'open' });
//
// The problem is, you have to get this executed before the web components are resolved on the page. there isn't any
// way to do that from inside of Selenium, because there isn't any way to rewrite the page as it enters the browser.
// The three options are to go through a proxy server, use a browser extension to inject the JavaScript, or use
// a browser that is controllable during the load cycle. The proxy server has issues if you are loading from an
// HTTPS server. The browser extension has potential support problems: Is support continuing? what browser can I
// use? And the controllable browser also suffers from support issues, e.g. PhantomJS has been discontinued.
//
// These potential solutions to driving an interface with "closed" components is beyond the scope of this project.
//

package com.wonderfulwidgets.examples.selshadowdom;

import com.wonderfulwidgets.examples.selshadowdom.SelShadowDomApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { SelShadowDomApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomButtonTests {

	@LocalServerPort
	private int port;

	private String URL;
	private WebDriver driver;
	private WebDriverWait wait;

	@BeforeEach
	public void setup() {

		URL = String.format("http://localhost:%d/selshadowdom", port);
		driver = new ChromeDriver();
		wait = new WebDriverWait(driver, 10);

		driver.get(URL);
		driver.manage().window().maximize();
	}

	/**
	 * Comment out the driver.quit() if you want the browser to remain open after each test.
	 */
	@AfterEach
	public void tearDown() {

		driver.quit();
	}

	/**
	 * The first button is in the Light DOM, so everything is reachable.
	 */
	@Test
	public void selectsButtonInLightDom() {

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custom-button-one")));

		WebElement button = driver.findElement(By.id("button-one"));

		button.click();

		assertEquals("id=button-one, no shadow-dom: selected!", button.getText());
	}

	/**
	 * The second button is in a Shadow DOM, so Selenium cannot see it.
	 */
	@Test
	public void cannotFindButtonInOpenShadowDom() {

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custom-button-two")));

		assertThrows(NoSuchElementException.class, () -> {

			WebElement button = driver.findElement(By.id("button-two"));
		});
	}

	/**
	 * JavaScript can be injected an run on the page to find the root node for the custom button.
	 */
	@Test
	public void selectsButtonInShadowOpenDomUnderRoot() {

		String customButtonId = "custom-button-two";

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id(customButtonId)));

		WebElement customButton = driver.findElement(By.id(customButtonId));
		WebElement shadowRoot = (WebElement)((JavascriptExecutor)driver).executeScript("return arguments[0].shadowRoot", customButton);

		WebElement button = shadowRoot.findElement(By.id("button-two"));

		button.click();

		assertEquals("id=button-two, open shadow-dom: selected!", button.getText());
	}

	/**
	 * Even JavaScript cannot find the root for a "closed" node. See potential solutions described in the notes
	 * at the top of the document.
	 */
	@Test
	public void cannotFindRootInClosedShadowDom() {

		String customButtonId = "custom-button-three";

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id(customButtonId)));

		WebElement customButton = driver.findElement(By.id(customButtonId));
		WebElement shadowRoot = (WebElement)((JavascriptExecutor)driver).executeScript("return arguments[0].shadowRoot", customButton);

		assertNull(shadowRoot);	// JavaScript returned null.
	}
}
