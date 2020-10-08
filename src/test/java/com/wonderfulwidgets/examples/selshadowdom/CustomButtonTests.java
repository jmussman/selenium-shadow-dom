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
//		Element.prototype._attachShadow = Element.prototype.attachShadow
//		Element.prototype.attachShadow = function (reqMode) => { return this._attachShadow({ mode: 'open' }) }
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { SelShadowDomApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomButtonTests {

	private static String URLFORMATTER = "http://localhost:%d/selshadowdom";

	@LocalServerPort
	private int port;

	/**
	 * These tests present the normal interaction with no Shadow DOM, open-mode Shadow DOM, and closed-mode
	 * Shadow DOM.
	 */
	@Nested
	@DisplayName("No shadow root, open-mode, and closed-mode shadow root")
	class ShadowRoot {

		private ChromeDriver driver;
		private WebDriverWait wait;

		@BeforeEach
		public void setup() {

			driver = new ChromeDriver();
			wait = new WebDriverWait(driver, 10);    // Changed from seconds to Duration in S4

			driver.get(String.format(CustomButtonTests.URLFORMATTER, CustomButtonTests.this.port));
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
			WebElement shadowRoot = (WebElement) ((JavascriptExecutor) driver).executeScript("return arguments[0].shadowRoot", customButton);

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
			WebElement shadowRoot = (WebElement) ((JavascriptExecutor) driver).executeScript("return arguments[0].shadowRoot", customButton);

			assertNull(shadowRoot);    // JavaScript returned null.
		}
	}

	/**
	 * These tests go around a closed-mode Shadow DOM by preventing it from being created in the first place (the
	 * addition of the injectClosedMOdeShadowDomOverride() in setup).
	 */
	@Nested
	@DisplayName("Override closed mode shadow root")
	class ClosedModeOverride {

		private ChromeDriver driver;
		private Wait wait;

		/**
		 * This setup differes from the enclosing class in that it injects the orverride script.
		 */
		@BeforeEach
		public void setup() {

			driver = new ChromeDriver();
			this.wait = new WebDriverWait(driver, 10);	// Changed from seconds to Duration in S4

			injectClosedModeShadowDomOverride();	// Take this out if you want to see the last test to fail!

			driver.get(String.format(CustomButtonTests.URLFORMATTER, CustomButtonTests.this.port));
			driver.manage().window().maximize();
		}

		/**
		 * Comment out the driver.quit() if you want the browser to remain open after each test.
		 */
		@AfterEach
		public void tearDown() {

			driver.quit();
		}

		@Test
		public void canOverrideClosedShadowDom() {

			String customButtonId = "custom-button-three";

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id(customButtonId)));

			WebElement customButton = driver.findElement(By.id(customButtonId));
			WebElement shadowRoot = (WebElement) ((JavascriptExecutor) driver).executeScript("return arguments[0].shadowRoot", customButton);
			WebElement button = shadowRoot.findElement(By.id("button-three"));

			button.click();

			assertEquals("id=button-three, closed shadow-dom: selected!", button.getText());
		}

		/**
		 * Build and inject a script to run on page load. This version is a hack using Reflection to call
		 * internal methods in Selenium 3 to access the DevTools interface.
		 */
		private void injectClosedModeShadowDomOverride() {

			String overrideScript = "Element.prototype._attachShadow = Element.prototype.attachShadow; "
					+ "Element.prototype.attachShadow = function (reqMode) { return this._attachShadow({ mode: 'open' }) }";
			Map<String, Object> parameters = new HashMap<String, Object>();
			Map<String, Object> command = new HashMap<>();

			parameters.put("source", overrideScript);
			command.put("cmd", "Page.addScriptToEvaluateOnNewDocument");
			command.put("params", parameters);

			try {

				// Set up sendCommand to talk to  DevTools.

				CommandInfo cmd = new CommandInfo("/session/:sessionId/chromium/send_command_and_get_result", HttpMethod.POST);
				Method defineCommand = HttpCommandExecutor.class.getDeclaredMethod("defineCommand", String.class, CommandInfo.class);
				defineCommand.setAccessible(true);
				defineCommand.invoke(((RemoteWebDriver) this.driver).getCommandExecutor(), "sendCommand", cmd);

				// Set up the script to run using Page.addScriptToEvaluateOnNewDocument. The response is a map that
				// provides the identifier for the script, which means it was set properly. We do not care, if the
				// script is not set the test will fail.

				Method execute = RemoteWebDriver.class.getDeclaredMethod("execute", String.class, Map.class);
				execute.setAccessible(true);
				execute.invoke(driver, "sendCommand", command);
			}

			catch (Throwable t) {

				throw new RuntimeException(t);
			}
		}
	}
}
