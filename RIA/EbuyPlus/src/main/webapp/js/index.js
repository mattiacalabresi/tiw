// iife to prevent inner variables being exposed to the global context
(function () {
	window.addEventListener("load", function () {
		let orchestrator = new PageOrchestrator();
		orchestrator.init();
	});

	/**
	 * The main Orchestrator responsible for handling all the interactions between
	 * the user and all the sections in the index page.
	 */
	class PageOrchestrator {
		constructor() {
			/**addRecentId
			 * Initializes the page components and displays the default section.
			 */
			this.init = function () {
				this.homeSectionOrchestrator = new HomeSectionOrchestrator();
				this.buySectionOrchestrator = new BuySectionOrchestrator();
				this.sellSectionOrchestrator = new SellSectionOrchestrator();
				this.detailsSectionOrchestrator = new DetailsSectionOrchestrator();
				this.offersSectionOrchestrator = new OffersSectionOrchestrator();

				this.cmanager = new CookieManager();

				this.homeSectionOrchestrator.registerEvents(this);
				this.buySectionOrchestrator.registerEvents(this);
				this.sellSectionOrchestrator.registerEvents(this);
				this.detailsSectionOrchestrator.registerEvents(this);
				this.offersSectionOrchestrator.registerEvents(this);

				this.setupNavigationBar();

				let nextSectionOrchestrator = this.getOrchestratorFromCookies();
				this.switchTo(null, nextSectionOrchestrator);
			}

			this.getOrchestratorFromCookies = function () {
				let firstTime = this.cmanager.isFirstTime();
				let lastCreated = this.cmanager.isLastCreated();

				if (!firstTime && lastCreated) {
					return this.sellSectionOrchestrator;
				}
				return this.buySectionOrchestrator;
			}

			/**
			 * Initializes all the components in the top navigation bar.
			 */
			this.setupNavigationBar = function () {
				this.setupHomeButton();
				this.displayUsername();
				this.setupLogoutButton();
			}

			/**
			 * Initializes the behavior of the home button.
			 */
			this.setupHomeButton = function () {
				let self = this;
				let homeButton = document.getElementById("home-button");

				homeButton.onclick = function () {
					self.switchTo(null, self.homeSectionOrchestrator);
				}
			}

			/**
			 * Displays the username inside the session storage in the
			 * top right corner of the index page.
			 */
			this.displayUsername = function () {
				let username = window.sessionStorage.getItem("username");
				document.getElementById("username-text").innerText = username;
			}

			/**
			 * Initializes the behavior of the logout button.
			 */
			this.setupLogoutButton = function () {
				let logoutButton = document.getElementById("logout-button");

				let self = this;
				logoutButton.onclick = function () {
					self.logout();
				}
			}

			/**
			 * Executes the logout procedure for the current user.
			 */
			this.logout = function () {
				doGet(LOGOUT, () => {
					if (this.cmanager.isFirstTime()) {
						// next time will no longer be the first time the user visits this site
						this.cmanager.setFirstTime(false);
					}

					window.sessionStorage.removeItem("username");
					window.location.href = LOGIN_PAGE;
				});
			}

			/**
			 * Switches to the section indicated by the given
			 * orchestrator.
			 */
			this.switchTo = function (originSectionOrchestrator, destinationSectionOrchestrator) {
				if (window.sessionStorage.getItem("username")) {
					if (originSectionOrchestrator) {
						originSectionOrchestrator.hide();
					} else {
						this.homeSectionOrchestrator.hide();
						this.buySectionOrchestrator.hide();
						this.sellSectionOrchestrator.hide();
						this.detailsSectionOrchestrator.hide();
						this.offersSectionOrchestrator.hide();
					}

					destinationSectionOrchestrator.show();
				} else {
					window.location.href = LOGIN_PAGE;
				}
			}
		}
	}

	// -------------------- HOME SECTION --------------------

	/**
	 * An Orchestrator responsible for handling all the interactions
	 * between the user and the Home section.
	 */
	class HomeSectionOrchestrator {
		constructor() {
			this.sectionDiv = document.getElementById("home-section");
			this.showBuyButton = document.getElementById("show-buy-button");
			this.showSellButton = document.getElementById("show-sell-button");

			/**
			 * Initializes all the UI components of the current section.
			 */
			this.registerEvents = function (pageOrchestrator) {
				let username = window.sessionStorage.getItem("username");
				document.getElementById("welcome-text").innerText = username;

				let self = this;

				this.showBuyButton.onclick = function () {
					pageOrchestrator.switchTo(self, pageOrchestrator.buySectionOrchestrator);
				}

				this.showSellButton.onclick = function () {
					pageOrchestrator.switchTo(self, pageOrchestrator.sellSectionOrchestrator);
				}
			}

			/**
			 * Shows the current section.
			 */
			this.show = function () {
				document.title = "Ebuy Plus - Home";
				this.sectionDiv.style.display = "block";
			}

			/**
			 * Hides the current section.
			 */
			this.hide = function () {
				this.sectionDiv.style.display = "none";
			}
		}
	}

	// -------------------- BUY SECTION --------------------

	/**
	 * An Orchestrator responsible for handling all the interactions
	 * between the user and the Buy section.
	 */
	class BuySectionOrchestrator {
		constructor() {
			this.sectionDiv = document.getElementById("buy-section");
			this.backButton = document.getElementById("buy-back-button");
			this.searchButton = document.getElementById("buy-search-button");

			this.cmanager = new CookieManager();

			/**
			 * Initializes all the UI components of the current section.
			 */
			this.registerEvents = function (pageOrchestrator) {
				this.pageOrchestrator = pageOrchestrator;

				// back button
				let self = this;
				this.backButton.onclick = function () {
					pageOrchestrator.switchTo(self, pageOrchestrator.homeSectionOrchestrator);
				}

				// search button
				this.searchButton.onclick = function (event) {
					self.hideErrorMessage();

					// last action was not the creation of a new auction
					self.cmanager.setLastCreated(false);

					if (window.sessionStorage.getItem("username")) {
						// retrieve search query
						let form = event.target.closest("form");
						if (form.checkValidity()) {
							let searchQuery = form.elements[0].value;
							self.showSearchedAuctions(searchQuery);
						} else {
							form.reportValidity();
						}
					} else {
						window.location.href = LOGIN_PAGE;
					}
				}
			}

			/**
			 * Shows the current section.
			 */
			this.show = function () {
				document.title = "Ebuy Plus - Buy";
				this.showBuy();

				this.sectionDiv.style.display = "block";
			}

			/**
			 * Hides the current section.
			 */
			this.hide = function () {
				this.hideErrorMessage();
				this.emptySearchBar();
				this.hideSearchResults();

				this.sectionDiv.style.display = "none";
			}

			/**
			 * Shows the content of the Buy section, including:
			 * - auctions won by the current user
			 * - auctions recently clicked by the user
			 * 
			 * recent IDs will be included in the cookie request header: no need to post them.
			 */
			this.showBuy = function () {
				doGet(GET_BUY, response => {
					let data;

					switch (response.status) {
						case STATUS_INTERNAL_SERVER_ERROR:
						case STATUS_BAD_REQUEST:
							this.showErrorMessage(response.responseText);
							return;
						case STATUS_OK:
							data = JSON.parse(response.responseText);
							break;
						case STATUS_UNAUTHORIZED:
							this.pageOrchestrator.logout();
							return;
						default:
							this.showErrorMessage("An unexpected error occurred, retry");
							return;
					}

					let wonAuctions = data[0];
					this.showWonAuctions(wonAuctions);

					let recentAuctions = data[1];
					this.showRecentAuctions(recentAuctions);
				});
			}

			/**
			 * Shows the list of won auctions for the current user.
			 */
			this.showWonAuctions = function (auctions) {
				// show no auctions message
				if (auctions.length <= 0) {
					document.getElementById("no-won-auctions-message").style.display = "inline-block";
					document.getElementById("won-auctions-table").style.display = "none"
					return;
				}

				// show auctions table
				let tbody = document.querySelector("#won-auctions-table > tbody");
				$("#won-auctions-table > tbody").empty(); // empty the auctions table

				for (let auction of auctions) {
					this.createWonAuctionRow(tbody, auction);
				}

				document.getElementById("no-won-auctions-message").style.display = "none";
				document.getElementById("won-auctions-table").style.display = "table"
			}

			/**
			 * Shows the list of recent auctions visited by the current user.
			 */
			this.showRecentAuctions = function (auctions) {
				// show no auctions message
				if (auctions.length <= 0) {
					document.getElementById("no-recent-auctions-message").style.display = "inline-block";
					document.getElementById("recent-auctions-table").style.display = "none"
					return;
				}

				// show auctions table
				let tbody = document.querySelector("#recent-auctions-table > tbody");
				$("#recent-auctions-table > tbody").empty(); // empty the auctions table

				for (let auction of auctions) {
					this.createAuctionRow(tbody, auction);
				}

				document.getElementById("no-recent-auctions-message").style.display = "none";
				document.getElementById("recent-auctions-table").style.display = "table"
			}

			/**
			 * Crates a row for the given tbody containing data from
			 * the given auction.
			 */
			this.createWonAuctionRow = function (tbody, auction) {
				let row = tbody.insertRow();

				let codeCell = document.createElement("th");
				row.appendChild(codeCell);

				let nameCell = row.insertCell();
				let finalPriceCell = row.insertCell();

				codeCell.innerHTML = auction.itemCode;
				nameCell.innerHTML = auction.itemName;
				finalPriceCell.innerHTML = toEur(auction.maximumOffer);
			}

			/**
			 * Shows all the auctions that corresponds to the given search query.
			 * If no auctions are found, an appropriate message is shown instead.
			 */
			this.showSearchedAuctions = function (searchQuery) {
				let url = SEARCH_AUCTIONS + "?search_query=" + searchQuery;
				let auctions = [];

				doGet(url, response => {
					switch (response.status) {
						case STATUS_UNAUTHORIZED:
							this.pageOrchestrator.logout();
							return;
						case STATUS_INTERNAL_SERVER_ERROR:
						case STATUS_BAD_REQUEST:
							this.showErrorMessage(response.responseText);
							return;
						case STATUS_OK:
							auctions = JSON.parse(response.responseText);
							break;
						default:
							this.showErrorMessage("An unexpected error occurred, retry");
							return;
					}

					// show search frame
					document.getElementById("searched-auctions-frame").style.display = "block";

					// show no auctions message
					if (auctions.length <= 0) {
						document.getElementById("no-searched-auctions-message").style.display = "inline-block";
						document.getElementById("searched-auctions-table").style.display = "none"
						return;
					}

					// show auctions table
					let tbody = document.querySelector("#searched-auctions-table > tbody");
					$("#searched-auctions-table > tbody").empty(); // empty the auctions table

					for (let auction of auctions) {
						this.createAuctionRow(tbody, auction);
					}

					document.getElementById("no-searched-auctions-message").style.display = "none";
					document.getElementById("searched-auctions-table").style.display = "table"
				});
			}

			/**
			 * Crates a row for the given tbody containing data from
			 * the given auction.
			 */
			this.createAuctionRow = function (tbody, auction) {
				let row = tbody.insertRow();

				let codeCell = document.createElement("th");
				row.appendChild(codeCell);

				let nameCell = row.insertCell();
				let maximumOfferCell = row.insertCell();
				let timeLeftCell = row.insertCell();
				let offersButtonCell = row.insertCell();

				codeCell.innerHTML = auction.itemCode;
				nameCell.innerHTML = auction.itemName;
				maximumOfferCell.innerHTML = toEur(auction.maximumOffer);
				timeLeftCell.innerHTML = auction.timeLeft.days + "d " + auction.timeLeft.hours + "h";
				offersButtonCell.appendChild(this.createOffersButton(auction.id));
			}

			/**
			 * Creates the offers button to be shown in the searched auctions table.
			 */
			this.createOffersButton = function (auctionId) {
				let button = document.createElement("button");
				button.classList.add("btn", "btn-outline-primary", "btn-sm");

				let self = this;
				button.onclick = function () {
					window.sessionStorage.setItem("auction_id", auctionId);

					// add the cliked auction to the recent list
					self.cmanager.addRecentId(auctionId);

					self.pageOrchestrator.switchTo(self, self.pageOrchestrator.offersSectionOrchestrator);
				}
				button.innerHTML = "OFFERS";
				return button;
			}

			/**
			 * Removes the search query from the search bar.
			 */
			this.emptySearchBar = function () {
				document.getElementById("buy-search-button").closest("form").elements[0].value = null;
			}

			/**
			 * Removes the searched auctions from the screen.
			 */
			this.hideSearchResults = function () {
				document.getElementById("searched-auctions-frame").style.display = "none";
			}

			/**
			 * Shows an error alert displaying the given message.
			 */
			this.showErrorMessage = function (message) {
				let errorAlert = document.getElementById("buy-error-alert");
				errorAlert.style.display = "block";
				errorAlert.firstElementChild.innerText = message;
			}

			/**
			 * Hides the error alert.
			 */
			this.hideErrorMessage = function () {
				document.getElementById("buy-error-alert").style.display = "none";
			}
		}
	}

	// -------------------- SELL SECTION --------------------

	/**
	 * An Orchestrator responsible for handling all the interactions
	 * between the user and the Sell section.
	 */
	class SellSectionOrchestrator {
		constructor() {
			this.sectionDiv = document.getElementById("sell-section");
			this.backButton = document.getElementById("sell-back-button");
			this.createAuctionButton = document.getElementById("create-auction-button");

			this.cmanager = new CookieManager();

			/**
			 * Initializes all the UI components of the current section.
			 */
			this.registerEvents = function (pageOrchestrator) {
				this.pageOrchestrator = pageOrchestrator;

				// back button
				let self = this;
				this.backButton.onclick = function () {
					pageOrchestrator.switchTo(self, pageOrchestrator.homeSectionOrchestrator);
				}

				// create auctions button
				this.createAuctionButton.onclick = function (event) {
					// retrieve form
					let form = event.target.closest("form");

					if (form.checkValidity()) {
						let data = new FormData(form);

						let validator;

						doPost(CREATE_AUCTION, data, response => {
							self.hideCreateErrorMessage();
							self.resetForm(form);

							switch (response.status) {
								case STATUS_UNAUTHORIZED:
									self.pageOrchestrator.logout();
									return;
								case STATUS_INTERNAL_SERVER_ERROR:
									self.showCreateErrorMessage(response.responseText);
									return;
								case STATUS_OK:
									validator = JSON.parse(response.responseText);
									break;
								default:
									self.showCreateErrorMessage("An unexpected error occurred, retry");
									return;
							}

							if (validator.successMessage) { // form filled correctly, no errors
								self.showCreateSuccessMessage(validator.successMessage);
								self.addToOpenAuctionsTable(validator);

								// the last action made by the user was the creation of a new auction
								self.cmanager.setLastCreated(true);
							} else { // there may be some errors
								let itemCodeInput = document.getElementById("create-item-code-input");
								let itemNameInput = document.getElementById("create-item-name-input");
								let itemDescriptionInput = document.getElementById("create-item-description-input");
								let itemImageInput = document.getElementById("create-item-image-input");
								let auctionBasePriceInput = document.getElementById("create-auction-base-price-input");
								let auctionMinimumRiseInput = document.getElementById("create-auction-minimum-rise-input");
								let auctionExpireDatetimeInput = document.getElementById("create-auction-expire-datetime-input");

								let itemCodeInputError = document.getElementById("create-item-code-input-error");
								let itemNameInputError = document.getElementById("create-item-name-input-error");
								let itemDescriptionInputError = document.getElementById("create-item-description-input-error");
								let itemImageInputError = document.getElementById("create-item-image-input-error");
								let auctionBasePriceInputError = document.getElementById("create-auction-base-price-input-error");
								let auctionMinimumRiseInputError = document.getElementById("create-auction-minimum-rise-input-error");
								let auctionExpireDatetimeInputError = document.getElementById("create-auction-expire-datetime-input-error");

								self.updateFormInput(itemCodeInput, itemCodeInputError, validator.itemCode, validator.itemCodeError);
								self.updateFormInput(itemNameInput, itemNameInputError, validator.itemName, validator.itemNameError);
								self.updateFormInput(itemDescriptionInput, itemDescriptionInputError, validator.itemDescription, validator.itemDescriptionError);
								self.updateFormImageInput(itemImageInput, itemImageInputError, validator.itemImageError);
								self.updateFormInput(auctionBasePriceInput, auctionBasePriceInputError, validator.basePrice, validator.basePriceError);
								self.updateFormInput(auctionMinimumRiseInput, auctionMinimumRiseInputError, validator.minimumRise, validator.minimumRiseError);
								self.updateFormInput(auctionExpireDatetimeInput, auctionExpireDatetimeInputError, validator.expireDateTime, validator.expireDateTimeError);
							}
						});
					} else {
						form.reportValidity();
					}
				}
			}

			/**
			 * Resets the create-auction form and fills the datetime field with
			 * the current datetime.
			 */
			this.resetForm = function (form) {
				form.reset();
				document.getElementById("create-auction-expire-datetime-input").value = now();
			}

			/**
			 * Updates the form "input" with the given "value", then shows the corresponding
			 * "errorValue" in the given "inputError" if any.
			 */
			this.updateFormInput = function (input, inputError, value, errorValue) {
				if (value) {
					input.value = value;
				} else {
					input.value = now();
				}
				if (errorValue) {
					input.classList.remove("is-valid");
					input.classList.add("is-invalid");
					inputError.firstElementChild.innerText = errorValue;
				} else {
					input.classList.remove("is-invalid");
					input.classList.add("is-valid");
				}
			}

			/**
			 * Displays into "imageInputError" the appropriate "errorValue",
			 * otherwise an upload request is displayed.
			 * 
			 * This is done because it is not possoble to kee the image in the form
			 * and therefore, if the form has some invalid fields, the image is discarded
			 * and it must be uploaded again.
			 */
			this.updateFormImageInput = function (imageInput, imageInputError, errorValue) {
				imageInput.classList.remove("is-valid");
				imageInput.classList.add("is-invalid");

				if (errorValue) { // error regarding the image
					imageInputError.firstElementChild.innerText = errorValue;
				} else { // error regarding some other field, repeat the upload error
					imageInputError.firstElementChild.innerText = "Please, upload the image again";
				}
			}

			/**
			 * Shows the current section.
			 */
			this.show = function () {
				document.title = "Ebuy Plus - Sell";
				this.showSell();
				this.prepareFormFields();

				this.sectionDiv.style.display = "block";
			}

			/**
			 * Hides the current section.
			 */
			this.hide = function () {
				this.hideErrorMessage();
				this.hideCreateErrorMessage();
				this.clearCreateAuctionForm();

				this.sectionDiv.style.display = "none";
			}

			/**
			 * Shows the Sell section content, including:
			 * - open auctions created by the current user
			 * - closed auctions created by the current user
			 */
			this.showSell = function () {
				doGet(GET_SELL, response => {
					let data;

					switch (response.status) {
						case STATUS_INTERNAL_SERVER_ERROR:
							this.showErrorMessage(response.responseText);
							return;
						case STATUS_OK:
							// data contains open and closed auctions
							data = JSON.parse(response.responseText);
							break;
						case STATUS_UNAUTHORIZED:
							this.pageOrhestrator.logout();
							return;
						default:
							this.showErrorMessage("An unexpected error occurred, retry");
							return;
					}

					let openAuctions = data[0];
					this.showOpenAuctions(openAuctions);

					let closedAuctions = data[1];
					this.showClosedAuctions(closedAuctions);
				});
			}

			/**
			 * Shows all the open auctions the current user has created.
			 * If no auctions are found, an appropriate message is shown instead.
			 */
			this.showOpenAuctions = function (auctions) {
				// show no auctions message
				if (auctions.length <= 0) {
					document.getElementById("no-open-auctions-message").style.display = "inline-block";
					document.getElementById("open-auctions-table").style.display = "none"
					return;
				}

				// show auctions table
				let tbody = document.querySelector("#open-auctions-table > tbody");
				$("#open-auctions-table > tbody").empty(); // empty the auctions table

				for (let auction of auctions) {
					this.createOpenAuctionRow(tbody, auction);
				}

				document.getElementById("no-open-auctions-message").style.display = "none";
				document.getElementById("open-auctions-table").style.display = "table"
			}

			/**
			 * Shows all the closed auctions the current user has created.
			 * If no auctions are found, an appropriate message is shown instead.
			 */
			this.showClosedAuctions = function (auctions) {
				// show no auctions message
				if (auctions.length <= 0) {
					document.getElementById("no-closed-auctions-message").style.display = "inline-block";
					document.getElementById("closed-auctions-table").style.display = "none"
					return;
				}

				// show auctions table
				let tbody = document.querySelector("#closed-auctions-table > tbody");
				$("#closed-auctions-table > tbody").empty(); // empty the auctions table

				for (let auction of auctions) {
					this.createClosedAuctionRow(tbody, auction);
				}

				document.getElementById("no-closed-auctions-message").style.display = "none";
				document.getElementById("closed-auctions-table").style.display = "table"
			}

			/**
			 * Crates a row for the given tbody containing data from
			 * the given auction.
			 */
			this.createOpenAuctionRow = function (tbody, auction, highlight = false) {
				let row = tbody.insertRow();

				let codeCell = document.createElement("th");
				row.appendChild(codeCell);

				let nameCell = row.insertCell();
				let maximumOfferCell = row.insertCell();
				let expiresInCell = row.insertCell();

				let detailsButtonCell = row.insertCell();
				detailsButtonCell.classList.add("ps-0");

				codeCell.innerHTML = auction.itemCode;
				nameCell.innerHTML = auction.itemName;
				maximumOfferCell.innerHTML = toEur(auction.maximumOffer);

				if (auction.expired) {
					expiresInCell.appendChild(this.createExpiredText());
					row.classList.add("table-warning")
				} else {
					expiresInCell.innerHTML = auction.timeLeft.days + "d " + auction.timeLeft.hours + "h";
				}

				detailsButtonCell.appendChild(this.createDetailsButton(auction.id, auction.expired));

				if (highlight) {
					row.style.background = "#BADBCB";
				}
			}

			/**
			 * Creates the "EXPIRED" warning text to be displayed if an auction
			 * has been expired.
			 */
			this.createExpiredText = function () {
				let strong = document.createElement("strong");
				strong.classList.add("text-warning");
				strong.innerText = "EXPIRED";
				return strong;
			}

			/**
			 * Crates a row for the given tbody containing data from
			 * the given auction.
			 */
			this.createClosedAuctionRow = function (tbody, auction) {
				let row = tbody.insertRow();

				let codeCell = document.createElement("th");
				row.appendChild(codeCell);

				let nameCell = row.insertCell();
				let finalPriceCell = row.insertCell();
				let expiredForCell = row.insertCell();

				let detailsButtonCell = row.insertCell();
				detailsButtonCell.classList.add("ps-0");

				codeCell.innerHTML = auction.itemCode;
				nameCell.innerHTML = auction.itemName;
				finalPriceCell.innerHTML = toEur(auction.maximumOffer);
				expiredForCell.innerHTML = auction.timeLeft.days + "d " + auction.timeLeft.hours + "h";
				detailsButtonCell.appendChild(this.createDetailsButton(auction.id, false));
			}

			/**
			 * Creates the details button to be shown in the open and closed auctions tables.
			 */
			this.createDetailsButton = function (auctionId, expired) {
				let button = document.createElement("button");
				button.classList.add("btn", "btn-sm");

				let buttonColor = expired ? "btn-outline-warning" : "btn-outline-primary";
				button.classList.add(buttonColor);

				let self = this;
				button.onclick = function () {
					// pass auction id to details page
					window.sessionStorage.setItem("auction_id", auctionId);
					// show details page
					self.pageOrchestrator.switchTo(self, self.pageOrchestrator.detailsSectionOrchestrator);
				}
				button.innerHTML = "DETAILS";
				return button;
			}

			/**
			 * Initialize the create-auction-form fields with predefined values.
			 */
			this.prepareFormFields = function () {
				let dateTimeField = document.getElementById("create-auction-expire-datetime-input");
				dateTimeField.setAttribute("min", now());
				dateTimeField.value = now();
			}

			/**
			 * Shows an error alert displaying the given message for the Sell section.
			 */
			this.showErrorMessage = function (message) {
				let alert = document.getElementById("sell-error-alert");
				alert.firstElementChild.innerText = message;
				alert.style.display = "block";
			}

			/**
			 * Hides the error alert for the Sell section.
			 */
			this.hideErrorMessage = function () {
				document.getElementById("sell-error-alert").style.display = "none";
			}

			/**
			 * Shows an error alert displaying the given message for the create-auction form.
			 */
			this.showCreateErrorMessage = function (message) {
				let alert = document.getElementById("create-auction-alert");
				alert.firstElementChild.innerText = message;
				alert.classList.remove("alert-success");
				alert.classList.add("alert-danger");
				alert.style.display = "block";
			}

			/**
			 * Shows an error alert displaying the given message for the create-auction form.
			 */
			this.showCreateSuccessMessage = function (message) {
				let alert = document.getElementById("create-auction-alert");
				alert.firstElementChild.innerText = message;
				alert.classList.remove("alert-danger");
				alert.classList.add("alert-success");
				alert.style.display = "block";
			}

			/**
			 * Adds the auction whose fields are contained in the given
			 * "validator" to the open-auctions list.
			 */
			this.addToOpenAuctionsTable = function (validator) {
				let itemCode = validator.itemCode;
				let itemName = validator.itemName;
				let maximumOffer = 0;

				// compute the time left
				let nowDate = new Date();
				let expireDate = new Date(toDateTimeStrFormatted(validator.expireDateTime));
				let seconds = (expireDate - nowDate) / 1000;
				let days = parseInt(Math.floor(seconds / (3600 * 24)));
				let hours = parseInt(Math.floor((seconds % (3600 * 24)) / 3600));

				// create the proper auction
				let auction = {
					"id": validator.auctionId,
					"itemCode": itemCode,
					"itemName": itemName,
					"maximumOffer": maximumOffer,
					"expired": false,
					"timeLeft": {
						"days": days,
						"hours": hours
					}
				};

				// insert the new auction in the table
				let tbody = document.querySelector("#open-auctions-table > tbody");
				this.createOpenAuctionRow(tbody, auction, true);

				// simulate user click on arrow to sort the table
				let defaultSortButton = document.getElementById("open-item-expire-datetime").childNodes[1];
				defaultSortButton.click();
			}

			/**
			 * Hides the error/success alert for the create-auction form.
			 */
			this.hideCreateErrorMessage = function () {
				document.getElementById("create-auction-alert").style.display = "none";
			}

			/**
			 * Clears the reate-auction form by removing all inputs and
			 * error/success messages.
			 */
			this.clearCreateAuctionForm = function () {
				// clear form fields
				document.getElementById("create-auction-form").reset();

				// remove all error/success messages
				let itemCodeInput = document.getElementById("create-item-code-input");
				let itemNameInput = document.getElementById("create-item-name-input");
				let itemDescriptionInput = document.getElementById("create-item-description-input");
				let itemImageInput = document.getElementById("create-item-image-input");
				let auctionBasePriceInput = document.getElementById("create-auction-base-price-input");
				let auctionMinimumRiseInput = document.getElementById("create-auction-minimum-rise-input");
				let auctionExpireDatetimeInput = document.getElementById("create-auction-expire-datetime-input");

				itemCodeInput.classList.remove("is-valid", "is-invalid");
				itemNameInput.classList.remove("is-valid", "is-invalid");
				itemDescriptionInput.classList.remove("is-valid", "is-invalid");
				itemImageInput.classList.remove("is-valid", "is-invalid");
				auctionBasePriceInput.classList.remove("is-valid", "is-invalid");
				auctionMinimumRiseInput.classList.remove("is-valid", "is-invalid");
				auctionExpireDatetimeInput.classList.remove("is-valid", "is-invalid");
			}
		}
	}

	// -------------------- DETAILS SECTION --------------------

	/**
	 * An Orchestrator responsible for handling all the interactions
	 * between the user and the Details section.
	 */
	class DetailsSectionOrchestrator {
		constructor() {
			this.sectionDiv = document.getElementById("details-section");
			this.backButton = document.getElementById("details-back-button");

			this.cmanager = new CookieManager();

			/**
			 * Initializes all the UI components of the current section.
			 */
			this.registerEvents = function (pageOrchestrator) {
				this.pageOrchestrator = pageOrchestrator;

				// back button
				let self = this;
				this.backButton.onclick = function () {
					self.removeCurrentAuctionId();
					pageOrchestrator.switchTo(self, pageOrchestrator.sellSectionOrchestrator);
				}
			}

			/**
			 * Shows the current section.
			 */
			this.show = function () {
				document.title = "Ebuy Plus - Details";
				this.sectionDiv.style.display = "block";

				// retrieve the auction to be displayed
				let auctionId = this.getCurrentAuctionId();

				// show section content
				this.showAuctionDetails(auctionId);
			}

			/**
			 * Hides the current section.
			 */
			this.hide = function () {
				this.hideBuyer();
				this.hideOffers();
				this.hideMessage();
				this.removeCloseAuctionButton();

				this.sectionDiv.style.display = "none";
			}

			/**
			 * Returns the id corresponding to the current auction.
			 */
			this.getCurrentAuctionId = function () {
				let id = window.sessionStorage.getItem("auction_id");

				if (window.sessionStorage.getItem("username") && id) {
					return id;
				} else {
					this.pageOrhestrator.logout();
				}
			}

			/**
			 * Removes from session the id corresponding to the current auction.
			 */
			this.removeCurrentAuctionId = function () {
				window.sessionStorage.removeItem("auction_id");
			}

			/**
			 * Loads and shows all the content inside the Details section.
			 */
			this.showAuctionDetails = function (auctionId) {
				let url = GET_AUCTION_DETAILS + "?auction_id=" + auctionId;
				doGet(url, response => {
					let details;

					switch (response.status) {
						case STATUS_INTERNAL_SERVER_ERROR:
						case STATUS_BAD_REQUEST:
							this.showErrorMessage(response.responseText);
							return;
						case STATUS_OK:
							details = JSON.parse(response.responseText);
							break;
						case STATUS_UNAUTHORIZED:
							this.pageOrhestrator.logout();
							return;
						default:
							this.showErrorMessage("An unexpected error occurred, retry");
							return;
					}

					let auction = details[0];
					this.showAuction(auction);

					let item = details[1];
					this.showItem(item);

					if (auction.closed) {
						this.getBuyer();
						this.hideOffers();
					} else {
						let offers = details[2];
						this.showOffers(offers);
						this.hideBuyer();
					}
				});
			}

			/**
			 * Shows the info box containing all data contained in the given auction.
			 */
			this.showAuction = function (auction) {
				document.getElementById("details-auction-base-price-text").innerText = toEur(auction.basePrice);
				document.getElementById("details-auction-minimum-rise-text").innerText = toEur(auction.minimumRise);
				document.getElementById("details-auction-expire-datetime-text").innerText = toDateTimeStr(auction.expireDateTime);

				// select proper auction status
				let statusDiv = document.getElementById("details-auction-status-div");
				let statusText = document.getElementById("details-auction-status-text");

				if (auction.closed) { // closed
					statusDiv.classList.remove("text-white", "text-black", "bg-success", "bg-warning");
					statusDiv.classList.add("text-muted");

					statusText.innerText = "CLOSED";
				} else {
					if (auction.expired) { // open but expired
						statusDiv.classList.remove("text-muted", "text-white", "bg-success");
						statusDiv.classList.add("text-black", "bg-warning");

						statusText.innerText = "EXPIRED";
					} else { // open and not expired
						statusDiv.classList.remove("text-black", "text-muted", "bg-warning");
						statusDiv.classList.add("text-white", "bg-success");

						statusText.innerText = "OPEN";
					}
				}

				// show close button only if appropriate
				if (auction.expired & !auction.closed) {
					let closeAuctionDiv = document.getElementById("close-auction-div");
					closeAuctionDiv.innerHTML = "";
					closeAuctionDiv.appendChild(this.createCloseAuctionButton(auction.id));
				}
			}

			/**
			 * Shows the info box containing all data contained in the given item.
			 */
			this.showItem = function (item) {
				document.getElementById("details-item-code-text").innerText = item.code;
				document.getElementById("details-item-name-text").innerText = item.name;
				document.getElementById("details-item-description-text").innerText = item.description;

				// show item image
				let url = GET_IMAGE + "/" + item.imageName;
				document.getElementById("details-item-image").src = url;
			}

			/**
			 * Shows the info box containing all data of curretn auction the buyer (winner).
			 */
			this.getBuyer = function () {
				let url = GET_BUYER + "?auction_id=" + this.getCurrentAuctionId();
				doGet(url, response => {
					switch (response.status) {
						case STATUS_UNAUTHORIZED:
							this.pageOrhestrator.logout();
							break;
						case STATUS_BAD_REQUEST:
						case STATUS_INTERNAL_SERVER_ERROR:
							this.showErrorMessage(response.responseText);
							break;
						case STATUS_OK:
							let data = JSON.parse(response.responseText);
							let buyer = data[0];
							let finalPrice = data[1];
							this.showBuyer(buyer, finalPrice);
							break;
					}
				});
			}

			/**
			 * Shows the info box containing all data contained in the given buyer,
			 * plus the ginve final price.
			 */
			this.showBuyer = function (buyer, finalPrice) {
				document.getElementById("buyer-info-div").style.display = "flex";

				let buyerNameText = document.getElementById("buyer-name-text");
				let finalPriceText = document.getElementById("final-price-text");
				let shippingAddresText = document.getElementById("buyer-shipping-address-text");

				if (buyer) {
					buyerNameText.innerText = buyer.name;
					finalPriceText.innerText = toEur(finalPrice);
					shippingAddresText.innerText = buyer.shippingAddress;
				} else {
					// no buyer found (eg. no one placed any offer on the auction: no winner, no buyer, no user to display)
					buyerNameText.innerText = "NO BUYER";
					finalPriceText.innerText = "-";
					shippingAddresText.innerText = "-";
				}
			}

			/**
			 * Hides the buyer of the current auction.
			 */
			this.hideBuyer = function () {
				document.getElementById("buyer-info-div").style.display = "none";
			}

			/**
			 * Shows the table containing all data from the given offers.
			 */
			this.showOffers = function (offers) {
				document.getElementById("details-offers-div").style.display = "block";

				// show no offers message
				if (offers.length <= 0) {
					document.getElementById("no-details-offers-message").style.display = "inline-block";
					document.getElementById("details-offers-table").style.display = "none"
					return;
				}

				// show offers table
				let tbody = document.querySelector("#details-offers-table > tbody");
				$("#details-offers-table > tbody").empty(); // empty the offers table

				for (let offer of offers) {
					this.createOfferRow(tbody, offer);
				}

				document.getElementById("no-details-offers-message").style.display = "none";
				document.getElementById("details-offers-table").style.display = "table"
			}

			/**
			 * Hides the offer table.
			 */
			this.hideOffers = function () {
				document.getElementById("details-offers-div").style.display = "none";
			}

			/**
			 * Crates a row for the given tbody containing data from
			 * the given offer.
			 */
			this.createOfferRow = function (tbody, offer) {
				let row = tbody.insertRow();

				let userCell = document.createElement("th");
				row.appendChild(userCell);

				let priceCell = row.insertCell();
				let dateteimeCell = row.insertCell();

				userCell.innerHTML = offer.userName;
				priceCell.innerHTML = toEur(offer.price);
				dateteimeCell.innerHTML = toDateTimeStr(offer.dateTime);
			}

			/**
			 * Creates the close auction button to be shown in the
			 * below the auction data.
			 */
			this.createCloseAuctionButton = function (auctionId) {
				let button = document.createElement("button");
				button.classList.add("btn", "btn-warning");
				button.innerHTML = "CLOSE AUCTION";

				let self = this;
				button.onclick = function () {
					// last action was not the creation of a new auction
					self.cmanager.setLastCreated(false);
					self.closeAuction(auctionId);
				}
				return button;
			}

			/**
			 * Removes the close auction button from the current section.
			 */
			this.removeCloseAuctionButton = function () {
				document.getElementById("close-auction-div").innerHTML = "";
			}

			/**
			 * Closes the auction corresponding to the given ID,
			 * then shows the appropriate result message and updates the UI.
			 */
			this.closeAuction = function (auctionId) {
				let url = CLOSE_AUCTION + "?auction_id=" + auctionId;
				doGet(url, response => {
					this.removeCloseAuctionButton();

					switch (response.status) {
						case STATUS_UNAUTHORIZED:
							this.pageOrhestrator.logout();
							break;
						case STATUS_BAD_REQUEST:
						case STATUS_INTERNAL_SERVER_ERROR:
							this.showErrorMessage(response.responseText);
							break;
						case STATUS_OK:
							this.changeAuctionStatus();
							this.hideOffers();
							this.getBuyer();
							this.showSuccessMessage(response.responseText);
							break;
						default:
							this.showErrorMessage("An unexpected error has occurred, retry");
							break;
					}
				});
			}

			/**
			 * Switches the status of the current auction from "EXPIRED"
			 * to "CLOSED".
			 */
			this.changeAuctionStatus = function () {
				let statusDiv = document.getElementById("details-auction-status-div");
				statusDiv.classList.remove("text-black", "bg-warning");
				statusDiv.classList.add("text-secondary", "bg-light");

				document.getElementById("details-auction-status-text").innerText = "CLOSED";
			}

			/**
			 * Shows an error alert displaying the given message.
			 */
			this.showErrorMessage = function (message) {
				let alertCol = document.getElementById("details-alert-col");
				let alert = alertCol.firstElementChild;

				alert.firstElementChild.innerText = message;
				alert.classList.remove("alert-success");
				alert.classList.add("alert-danger");

				alertCol.style.display = "block";
			}

			/**
			 * Shows a success alert displaying the given message.
			 */
			this.showSuccessMessage = function (message) {
				let alertCol = document.getElementById("details-alert-col");
				let alert = alertCol.firstElementChild;

				alert.firstElementChild.innerText = message;
				alert.classList.remove("alert-danger");
				alert.classList.add("alert-success");
				alertCol.style.display = "block";
			}

			/**
			 * Hides the success/error alert.
			 */
			this.hideMessage = function () {
				document.getElementById("details-alert-col").style.display = "none";
			}
		}
	}

	// -------------------- OFFERS SECTION --------------------

	/**
	 * An Orchestrator responsible for handling all the interactions
	 * between the user and the Offers section.
	 */
	class OffersSectionOrchestrator {
		constructor() {
			this.sectionDiv = document.getElementById("offers-section");
			this.backButton = document.getElementById("offers-back-button");

			this.cmanager = new CookieManager();

			/**
			 * Initializes all the UI components of the current section.
			 */
			this.registerEvents = function (pageOrchestrator) {
				this.pageOrhestrator = pageOrchestrator;

				// back button
				let self = this;
				this.backButton.onclick = function () {
					self.removeCurrentAuctionId();
					pageOrchestrator.switchTo(self, pageOrchestrator.buySectionOrchestrator);
				}
			}

			/**
			 * Shows the current section.
			 */
			this.show = function () {
				document.title = "Ebuy Plus - Offers";
				this.sectionDiv.style.display = "block";

				// retrieve the auction to be displayed
				let auctionId = this.getCurrentAuctionId();

				// show section content
				this.showOffers(auctionId);
			}

			/**
			 * Hides the current section.
			 */
			this.hide = function () {
				this.sectionDiv.style.display = "none";
			}

			/**
			 * Returns the id corresponding to the current auction.
			 */
			this.getCurrentAuctionId = function () {
				let id = window.sessionStorage.getItem("auction_id");

				if (window.sessionStorage.getItem("username") && id) {
					return id;
				} else {
					this.pageOrhestrator.logout();
				}
			}

			/**
			 * Removes from session the id corresponding to the current auction.
			 */
			this.removeCurrentAuctionId = function () {
				window.sessionStorage.removeItem("auction_id");
			}

			/**
			 * Loads and shows all the content inside the Offers section.
			 */
			this.showOffers = function (auctionId) {
				let url = GET_OFFERS + "?auction_id=" + auctionId;
				let data;

				doGet(url, response => {
					switch (response.status) {
						case STATUS_BAD_REQUEST:
						case STATUS_INTERNAL_SERVER_ERROR:
							this.showErrorMessage(response.responseText);
							return;
						case STATUS_OK:
							data = JSON.parse(response.responseText);
							break;
						case STATUS_UNAUTHORIZED:
							this.pageOrhestrator.logout();
							return;
						default:
							this.showErrorMessage("An unexpected error occurred, retry");
							return;
					}

					let auction = data[0];
					this.showAuction(auction);

					let item = data[1];
					this.showItem(item);

					let offers = data[2];
					this.showOffersTable(offers);

					this.setupMakeOfferBox(auction.id, auction.minimumOffer, auction.minimumRise);
				});
			}

			/**
			 * Shows the info box containing all data contained in the given auction.
			 */
			this.showAuction = function (auction) {
				document.getElementById("offers-auction-base-price-text").innerText = toEur(auction.basePrice);
				document.getElementById("offers-auction-minimum-rise-text").innerText = toEur(auction.minimumRise);
				document.getElementById("offers-auction-expire-datetime-text").innerText = toDateTimeStr(auction.expireDateTime);

				// select proper auction status
				let statusDiv = document.getElementById("offers-auction-status-div");
				let statusText = document.getElementById("offers-auction-status-text");

				statusDiv.classList.add("text-white");

				if (!auction.closed && !auction.expired) { // auctions can only be open here
					statusDiv.classList.remove("bg-error");
					statusDiv.classList.add("bg-success");

					statusText.innerText = "OPEN";
				} else {
					statusDiv.classList.remove("bg-success");
					statusDiv.classList.add("bg-error");

					statusText.innerText = "ERROR";
				}
			}

			/**
			 * Shows the info box containing all data contained in the given item.
			 */
			this.showItem = function (item) {
				document.getElementById("offers-item-code-text").innerText = item.code;
				document.getElementById("offers-item-name-text").innerText = item.name;
				document.getElementById("offers-item-description-text").innerText = item.description;

				// show item image
				let url = GET_IMAGE + "/" + item.imageName;
				document.getElementById("offers-item-image").src = url;
			}

			/**
			 * Shows the table containing all data from the given offers.
			 */
			this.showOffersTable = function (offers) {
				// show no offers message
				if (offers.length <= 0) {
					document.getElementById("no-offers-offers-message").style.display = "inline-block";
					document.getElementById("offers-offers-table").style.display = "none"
					return;
				}

				// show offers table
				let tbody = document.querySelector("#offers-offers-table > tbody");
				$("#offers-offers-table > tbody").empty(); // empty the offers table

				for (let offer of offers) {
					offer.dateTime = toDateTimeStr(offer.dateTime);
					this.createOfferRow(tbody, offer);
				}

				document.getElementById("no-offers-offers-message").style.display = "none";
				document.getElementById("offers-offers-table").style.display = "table"
			}

			/**
			 * Crates a row for the given tbody containing data from
			 * the given offer.
			 */
			this.createOfferRow = function (tbody, offer, highlight = false) {
				let row = tbody.insertRow();

				let userCell = document.createElement("th");
				row.appendChild(userCell);

				let priceCell = row.insertCell();
				let dateteimeCell = row.insertCell();

				userCell.innerHTML = offer.userName;
				priceCell.innerHTML = toEur(offer.price);
				dateteimeCell.innerHTML = offer.dateTime;

				if (highlight) {
					row.style.background = "#BADBCB";
				}
			}

			/**
			 * Sets up the offer box corresponding to the auction with the given ID.
			 * This will set the minimum price, the hint to display and the place-offer
			 *  button.
			 */
			this.setupMakeOfferBox = function (auctionId, minimumOffer, minimumRise) {
				this.updateMinimumOffer(minimumOffer);

				document.getElementById("offers-auction-id").value = auctionId;

				let placeOfferButton = document.getElementById("place-offer-button");
				let self = this;
				placeOfferButton.onclick = function (event) {
					// last action was not the creation of a new auction
					self.cmanager.setLastCreated(false);

					let form = event.target.closest("form");

					if (form.checkValidity()) {
						let data = new FormData(form);

						self.placeOffer(data, minimumRise);
					} else {
						form.reportValidity();
					}
				}
			}

			/**
			 * Places the offer contained in the given data, then
			 * updates the place-offer box and inserts the placed offer
			 * in the offers table.
			 */
			this.placeOffer = function (data, minimumRise) {
				let offeredPrice = parseFloat(document.getElementById("offers-offer-input").value);

				doPost(PLACE_OFFER, data, response => {
					switch (response.status) {
						case STATUS_BAD_REQUEST:
						case STATUS_INTERNAL_SERVER_ERROR:
							this.showErrorMessage(response.responseText);
							break;
						case STATUS_UNAUTHORIZED:
							this.pageOrhestrator.logout();
							break;
						case STATUS_OK:
							this.showSuccessMessage(response.responseText);

							// insert new offer in offers table
							let offer = {
								"userName": window.sessionStorage.getItem("username"),
								"price": offeredPrice,
								"dateTime": nowStr()
							}
							this.updateOffersTable(offer);

							// set new minimum offer price
							let minimumOffer = offeredPrice + minimumRise;
							this.updateMinimumOffer(minimumOffer);
							break;
						default:
							this.showErrorMessage("An unexpected error occurred, retry");
							break;
					}
				});
			}

			/**
			 * inserts a new offer into the offers table,
			 * then the table is sorted by date and time.
			 */
			this.updateOffersTable = function (offer) {
				// insert the new offer in the table
				let tbody = document.querySelector("#offers-offers-table > tbody");
				this.createOfferRow(tbody, offer, true);

				// simulate user click on arrow to sort the table
				let defaultSortButton = document.getElementById("offers-offer-datetime").lastElementChild;
				defaultSortButton.click();
			}

			/**
			 * Updates the place-offer input with the value for the
			 * next minimum offer.
			 */
			this.updateMinimumOffer = function (minimumOffer) {
				// limit the number to two decimal digits
				let minimumOfferStr = minimumOffer.toFixed(2)

				let offersInput = document.getElementById("offers-offer-input");
				offersInput.placeholder = minimumOfferStr;
				offersInput.setAttribute("min", minimumOfferStr);
				offersInput.value = "";
			}

			/**
			 * Shows an error alert displaying the given message.
			 */
			this.showErrorMessage = function (message) {
				let alert = document.getElementById("offers-alert");

				alert.firstElementChild.innerText = message;
				alert.classList.remove("alert-success");
				alert.classList.add("alert-danger");
				alert.style.display = "block";
			}

			/**
			 * Shows a success alert displaying the given message.
			 */
			this.showSuccessMessage = function (message) {
				let alert = document.getElementById("offers-alert");

				alert.firstElementChild.innerText = message;
				alert.classList.remove("alert-danger");
				alert.classList.add("alert-success");
				alert.style.display = "block";
			}

			/**
			 * Hides the success/error alert.
			 */
			this.hideMessage = function () {
				document.getElementById("offers-alert-col").style.display = "none";
			}
		}
	}

})();