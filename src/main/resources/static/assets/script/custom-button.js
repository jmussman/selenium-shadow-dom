// custom-button.js
// Copyright © 2020 Joel Mussman. All rights reserved.
//
// A very simple component. The component is wrapped in a function to isolate the variables; ES6 modules are great
// but we are trying to simplify this example as much as possible. The component defines a template with style for
// a button, initially the default colors. When the button is clicked it turns green with white text and a
// different message (ADA, Section 508 compliant).
//
// Element attributes:
//	mode = the mode of the root node for the Shadow DOM, use "open", "closed", or "none". None is not exactly a
//		mode for the root node, instead it skips creating the root node.
//	identifier - the id the button will have.
//	text - the text to go into the button.
//	selected-text - the text to go into the button when selected.
//

( () => {

	const customButtonTemplate = document.createElement("template");

	customButtonTemplate.innerHTML = `
<div>
	<style>
	button {
		margin: 10px;
		padding: 5px;
		width: 800px;
		font-size: 28pt;
		border: 1px solid black;
		border-radius: 3px;
		cursor: pointer;
	}
	button.clicked {
		background-color: green;
		color: white;
	}
	</style>
	<button></button>
</div>
`

	class CustomButton extends HTMLElement {

		connectedCallback() {

			const shadowMode = this.getAttribute('shadow-mode')
			const identifier = this.getAttribute('identifier')
			const buttonText = this.getAttribute('text')
			const selectedText = this.getAttribute('selected-text')
			let root = this

			if (shadowMode && (shadowMode === 'open' || shadowMode == 'closed')) {

				root = this.attachShadow({mode: this.getAttribute('shadow-mode')})
			}

			const clone = customButtonTemplate.content.cloneNode(true)
			const button = clone.querySelector("button")

			button.id = identifier
			button.innerHTML = buttonText

			button.addEventListener('click', (e) => {

				button.classList.add('clicked')
				button.innerHTML = selectedText
			})

			root.appendChild(clone)
		}
	}

	window.customElements.define('custom-button', CustomButton)

}).call(this)
