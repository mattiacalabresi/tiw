const BASE = "http://localhost:8080/EbuyPlus";

const CHECK_LOGIN = BASE + "/check-login";
const GET_SELL = BASE + "/sell";
const GET_BUYER = BASE + "/get-buyer";
const LOGOUT = BASE + "/logout";
const CREATE_AUCTION = BASE + "/create";
const GET_AUCTION_DETAILS = BASE + "/details";
const CLOSE_AUCTION = BASE + "/close";
const SEARCH_AUCTIONS = BASE + "/search";
const GET_BUY = BASE + "/buy";
const GET_OFFERS = BASE + "/offers";
const PLACE_OFFER = BASE + "/place";
const GET_IMAGE = BASE + "/get-image";

const LOGIN_PAGE = BASE + "/login.html";
const INDEX_PAGE = BASE + "/index.html";

const STATUS_OK = 200;
const STATUS_BAD_REQUEST = 400;
const STATUS_UNAUTHORIZED = 401;
const STATUS_NOT_FOUND = 404;
const STATUS_INTERNAL_SERVER_ERROR = 500;

/**
 * Performs a GET request to the given url and invokes the given callback,
 * after a response has been received.
 */
function doGet(url, callback) {
	makeCall("GET", url, null, callback);
}

/**
 * Performs a POST request to the given url passing the given data
 * as JSON payload.
 * Then invokes the given callback, after a response has been received.
 */
function doPost(url, data, callback) {
	makeCall("POST", url, data, callback);
}

/**
 * Performs a remote call to the given url, using the given method.
 * The request can be loaded with data as a payload.
 * After a response has been received, the given callback is called.
 */
function makeCall(method, url, data = null, callback) {
	var request = new XMLHttpRequest();

	request.onreadystatechange = function () {
		if (request.readyState == XMLHttpRequest.DONE) {
			callback(request);
		}
	};
	request.open(method, url);

	if (data == null) {
		request.send();
	} else {
		request.send(data);
	}
}