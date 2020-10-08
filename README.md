![](.common/joels-private-stock.png?raw=true)

# Selenium & Shadow Dom

## History

* 10/5/2020 Initial creation.
* 10/7/2020 Updated with a test that uses Selenium to override closed-mode Shadow DOM.

## Introduction

This project consists of JUnit 5 tests using Selenium 4 driving custom web elements that use Shadow DOM.
Custom elements used the "Shadow DOM" to block the application from directly accessing their internal elements.
CSS selectors and JavaScript element locator functions will not pass the shadow root node at the head of
a Shadow DOM, so CSS and JavaScript will not inadvertently change the internal workings of a custom element.

Custom elements do not need to build a Shadow DOM, in which case the element is affected by CSS and JavaScript at
the application level.

Custom elements may build a Shadow DOM in "open-mode", which means that JavaScript can find the root node,
and then JavaScript can use the locators on the root node to step inside.
So, JavaScript has to be a purposeful attempt to step around the root, and CSS still will not go inside.

The alternative is to use "closed-mode", where JavaScript cannot find the root node, it is blocked.
However, the custom element builds the Shadow DOM root node with the attachShadow function, so it is
trivial to replace that function in the browser with one that blocks closed mode from being used:

    Element.prototype._attachShadow = Element.prototype.attachShadow
    Element.prototype.attachShadow = funciton (reqMode) { return this._attachShadow({ mode: 'open' }) }

The trick is to inject this into the browser before the page loads; the last JUnit test shows you how to
achieve that with Selenium.

## Notes

Import the project as a Maven project in your IDE.
This is a Spring boot web application project.
The application context is "selshadowdom" and the port is 8081 (all configurable in the application.properties file).

Find the src/main/java/com/wonderfulwidgets/examples/selshadowdom/SelShadowDom application class.
Right click and run the application, and then in the browser visit the page "http://localhost:8081/selshadowdom/".
There is a detailed explanation of the application and the tests on that page.
Shutdown the application when you are finished.

Explore and run the JUnit tests to understand how Selenium interacts with elements using a Shadow DOM.

## License

This project is released under the MIT license. You may use and modify all or part of it as you choose, as long as attribution to the source is provided per the license. See the details in the [license file](./LICENSE.md) or at the [Open Source Initiative](https://opensource.org/licenses/MIT)

## Support

Since I give stuff away for free, and if you would like to keep seeing more stuff like this, then please consider
a contribution to *Joel's Coffee Fund* at **Smallrock Internet** to help keep the good stuff coming :)<br />

[![Donate](./.common/Donate-Paypal.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XPUGVGZZ8RUAA)

## Contributing

We are always looking for ways to make the template better. But remember: Keep it simple. Keep it minimal. Don't add every single feature just because you can, add a feature when a feature is required.

## Authors or Acknowledgments

* Joel Mussman
