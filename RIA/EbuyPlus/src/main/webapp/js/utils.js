// on upload file retrieves num files and output lable, then triggers a 'fileselect' event.
$(document).on('change', ':file', function () {
    let input = $(this);
    let numFiles = input.get(0).files ? input.get(0).files.length : 1;
    let label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
    input.trigger('fileselect', [numFiles, label]);
});

$(document).ready(function () {
    // on 'fileselect' event fills the image image-form with the appropriate text
    $(':file').on('fileselect', function (numFiles, label) {

        let input = document.getElementById("create-item-image-input");

        if (numFiles > 0) {
            input.value = label;
        } else {
            input.value = "";
            input.placeholder = "Max. size 10MB";
        }
    });
});

/**
 * Returns the currency representation of
 * the given string.
 * Currency is expressed in Euros and has the "€" symbol at
 * the end of the string.
 */
function toEur(string) {
    let formatter = Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" });
    return formatter.format(string);
}

/**
 * Returns the current date and time in the form of a datetime-compliant
 * string.
 * Expected result is "yyyy-MM-ddThh:mm".
 */
function now() {
    let date = new Date().toLocaleString();
    date = date.substring(0, date.length - 3).replaceAll("/", "-").replace(", ", "T");

    let parts = date.split("T");
    let dateParts = parts[0].split("-");

    let day = parseInt(dateParts[0]);
    let month = parseInt(dateParts[1]);
    let year = dateParts[2];

    let timeParts = parts[1].split(":");
    let hour = parseInt(timeParts[0]);
    let minute = parseInt(timeParts[1]);

    return year + "-" + zeroPad(month) + "-" + zeroPad(day) + "T" + zeroPad(hour) + ":" + zeroPad(minute);
}

/**
 * Returns the current date and time in a plain string format.
 * Expected result is "yyyy-MM-dd hh:mm".
 */
function nowStr() {
    return now().replace("T", " ");
}

/**
 * Returns the string representation of the given datetime
 * in the format "yyyy-MM-dd hh:mm".
 */
function toDateTimeStr(datetime) {
    return datetime.date.year + "-" + zeroPad(datetime.date.month) + "-" + zeroPad(datetime.date.day) + " "
        + zeroPad(datetime.time.hour) + ":" + zeroPad(datetime.time.minute);
}

/**
 * Returns the string representation of the given datetime
 * in the format "yyyy-MM-ddThh:mm".
 */
function toDateTimeStrFormatted(datetime) {
    return toDateTimeStr(datetime).replace(" ", "T");
}

/**
 * Returns the string representation of the given number, padded with an extra
 * zero if the number is single-digit.
 */
function zeroPad(number) {
    return number <= 9 ? "0" + number : "" + number;
}

/**
 * An utility class used to handle cookies for the
 * current application.
 */
class CookieManager {
    constructor() {
        /**
         * Average number of days in a month.
         */
        this.DAYS_IN_A_MONTH = 30;

        /**
         * The separator for IDs list items.
         */
        const LIST_SEPARATOR = "-";

        /**
         * Sets the cookie identified by "name" to the given "value" for the current user.
         * The cookie will expire in "expireDays" days.
         * If the cookie "name" corresponds to an already st cookie, its "value" and "expireDays"
         * will be only updated.
         * @param {string} name the name of the cookie to set.
         * @param {any} value the value to set in the cookie.
         * @param {number} expireDays the number of days (starting from today) after which the cookie expires.
         */
        this.set = function (name, value, expireDays = undefined) {
            const expireDate = new Date();
            expireDate.setTime(expireDate.getTime() + (expireDays * 24 * 60 * 60 * 1000));

            let expires = "expires=" + expireDate.toUTCString();
            let cname = window.sessionStorage.getItem("username").trim() + "_" + name;

            document.cookie = cname + "=" + value + ";" + expires + ";path=/EbuyPlus";
        }

        /**
         * Returns the cookie value for the current user associated to the given "name".
         * @param {string} name the name of the cookie whose value has to be retrieved.
         * @returns the cookie value corresponding to the given "name" if any, "undefined" otherwise.
         */
        this.get = function (name) {
            let cookies = document.cookie.split(";");

            for (let cookie of cookies) {
                let coockieData = cookie.trim().split("=");
                let key = coockieData[0];
                let cname = window.sessionStorage.getItem("username").trim() + "_" + name;

                if (key === cname) {
                    let value = coockieData[1];
                    return value === "" ? undefined : value;
                }
            }
            return undefined;
        }

        /**
         * Removes the cookie associated to the given "name" for the given user, by removing its value
         * and setting an expire date in the past.
         * @param {string} name the name of the cookie to be removed.
         */
        this.remove = function (name) {
            let cname = window.sessionStorage.getItem("username").trim() + "_" + name;

            document.cookie = cname + "=;" + "expires=Thu, 01 Jan 1970 00:00:01 GMT;path=/EbuyPlus";
        }

        /**
         * Sets the first-time cookie to the given "value" for the current user.
         * @param {boolean} value the value to be stored in the first-time cookie.
         */
        this.setFirstTime = function (value) {
            this.set("first_time", value, this.DAYS_IN_A_MONTH);
        }

        /**
         * Returns the first-time cookie value for the current user if already set, true otherwise.
         * @returns true the value associated to the first-time cookie.
         */
        this.isFirstTime = function () {
            let firstTime = this.get("first_time");

            if (firstTime === undefined) {
                firstTime = "true";
                this.setFirstTime(firstTime);
            }

            return firstTime === "true";
        }

        /**
         * Sets the last-created cookie to the given "value" for the current user.
         * 
         * This value should be true if this is the first time the user accesses the
         * website, false otherwise.
         * @param {boolean} value the value to be stored in the last-created cookie.
         */
        this.setLastCreated = function (value) {
            this.set("last_created", value, this.DAYS_IN_A_MONTH);
        }

        /**
         * Returns the last-created cookie value for the current user if already set, false otherwise.
         * 
         * This value should be true if the last action performed by the current user was
         * to create a new auction, false otherwise.
         * @returns true the value associated to the last-created cookie.
         */
        this.isLastCreated = function () {
            let lastCreated = this.get("last_created");

            if (lastCreated === undefined) {
                lastCreated = "false";
                this.setLastCreated(lastCreated);
            }

            return lastCreated === "true";
        }

        /**
         * Adds the given "id" to the list of ids corresponding to the auctions that the
         * current user has recently clicked in the website.
         * No duplicates are admitted: if a duplicate ID is given, it is ignored and not added
         * to the list.
         * @param {integer} id the ID of the recently clicked auction to be stored.
         */
        this.addRecentId = function (id) {
            let recentIds = this.get("recent_ids");
            let ids;

            if (recentIds) { // add ID to an existing list
                ids = recentIds + LIST_SEPARATOR + id;

                // remove duplicate IDs
                ids = [...new Set(ids.split(LIST_SEPARATOR))].join(LIST_SEPARATOR);
            } else { // no list found, create it
                ids = id;
            }

            this.set("recent_ids", ids, this.DAYS_IN_A_MONTH);
        }

        /**
         * Returns a list of integer IDs corresponding to the auctions that the
         * current user has visited recently.
         * 
         * @returns the list of integer IDs corresponding to the recently visited auctions.
         */
        this.getRecentIds = function () {
            let ids = this.get("recent_ids");

            if (ids === undefined) {
                this.set("recent_ids", [], this.DAYS_IN_A_MONTH);
            }
            return ids ? ids.split(LIST_SEPARATOR).map(id => parseInt(id)) : [];
        }
    }
}