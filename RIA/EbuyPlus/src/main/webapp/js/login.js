// iife to prevent inner variables being exposed to the global context
(function () {
	window.onload = function () {
		// login on enter
		window.addEventListener("keypress", loginOnEnter);

		// login on button click
		let loginButton = document.getElementById("login-button");
		loginButton.onclick = function (event) {
			checkLogin(event);
		}
	}

	/**
	 * Starts the login procedure if the enter button is pressed.
	 */
	function loginOnEnter(event) {
		if (event.key === "Enter") {
			checkLogin(event);
		}
	}

	function checkLogin(event) {
		hideLoginError();
		// retrieve form
		let form = event.target.closest("form");

		if (form.checkValidity()) {
			let data = new FormData(form);

			// check login on the server
			doPost(CHECK_LOGIN, data, response => {
				form.reset();

				switch (response.status) {
					case STATUS_OK:
						// authentication success: proceed to home
						window.sessionStorage.setItem("username", response.responseText);
						window.location.href = INDEX_PAGE;
						break;
					case STATUS_BAD_REQUEST:
					case STATUS_UNAUTHORIZED:
					case STATUS_INTERNAL_SERVER_ERROR:
						// authentication failed
						showLoginError(response.responseText);
						break;
					default:
						// unexpected error
						showLoginError("An error has occurred, retry");
						break;
				}

			});
		} else {
			form.reportValidity();
		}
	}

	/**
	 * Shows the given message inside the login error alert.
	 */
	function showLoginError(message) {
		let errorAlert = document.getElementById("login-error-alert");
		errorAlert.style.display = "block";
		errorAlert.firstElementChild.innerText = message;
	}

	/**
	 * Hides the login error alert.
	 */
	function hideLoginError() {
		document.getElementById("login-error-alert").style.display = "none";
	}
})();