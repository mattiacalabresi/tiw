/**
 * SORT UTILITY CLASS
 * Allows multiple tables to be sorted using all of their fields.
 * 
 * Instructions:
 * 
 * Include this file inside the page containing the table(s) to be ordered.
 * Also include the "sort.css" file.
 * 
 * Each table must have the following structure:
 * 
 * 	<table>
 * 		<thead>
 * 			...
 * 				<th>...</th>
 * * 			<th>...</th>
 * 				...
 * 			...
 * 		</thead>
 * 		...
 * </table>
 * 
 * Each column header (<th>) must have:
 * 	- a unique id
 * 	- the "sortable" class
 * 	- plain text as the only child of each <td> field.
 *  - (OPTIONAL) the "sorted-default-asc" or "sorted-default-desc" classes
	  to indicate the column that corresponds to the default ordering of the table.
 */

// iife to prevent inner variables being exposed to the global context
(function() {
	window.addEventListener("load", function() {
		let sortables = document.getElementsByClassName("sortable");

		for (let sortable of sortables) {
			let buttonUp = createButtonUp();
			buttonUp.onclick = () => sortTable(sortable.id, true);

			let buttonDn = createButtonDn();
			buttonDn.onclick = () => sortTable(sortable.id, false);

			sortable.appendChild(buttonUp);
			sortable.appendChild(buttonDn);
		}

		initButtons(sortables)
		selectDefaultButtons();

	});

	function createButtonUp() {
		let d = "M8 12a.5.5 0 0 0 .5-.5V5.707l2.146 2.147a.5.5 0 0 0 .708-.708l-3-3a.5.5 0 0 0-.708 0l-3 3a.5.5 0 1 0 .708.708L7.5 5.707V11.5a.5.5 0 0 0 .5.5z";
		return createButton(d, "button_up");
	}

	function createButtonDn() {
		let d = "M8 4a.5.5 0 0 1 .5.5v5.793l2.146-2.147a.5.5 0 0 1 .708.708l-3 3a.5.5 0 0 1-.708 0l-3-3a.5.5 0 1 1 .708-.708L7.5 10.293V4.5A.5.5 0 0 1 8 4z";
		return createButton(d, "button_dn");
	}

	function createButton(d, id) {
		let button = document.createElement("button");
		let svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
		let path = document.createElementNS("http://www.w3.org/2000/svg", "path");

		path.setAttribute("fill-rule", "evenodd");
		path.setAttribute("d", d);

		svg.setAttribute("width", "20");
		svg.setAttribute("height", "20");
		svg.setAttribute("fill", "currnetColor");
		svg.setAttribute("class", "bi bi-arrow-down-short");
		svg.setAttribute("viewBox", "0 0 16 16");

		button.classList.add("btn", "h-100", "p-0", "my-0", "ms-1");
		button.style.boxShadow = "none";
		button.id = id;

		svg.appendChild(path);
		button.append(svg);

		return button;
	}

	/**
	 * Return true if the given string represents a time delta
	 * in the format "--d --h" where "-" represents an integer value, 
	 * return false otherwise.
	 */
	function isTimeDelta(string) {
		// expired time delta will count as 0
		if (string === "EXPIRED") {
			return true;
		}

		// check for hours field
		if (!string.endsWith("h")) {
			return false;
		}

		// splits days and hours
		let times = string.split("d ");

		if (times.length != 2) {
			return false;
		}

		// get days
		let days = times[0];

		// check days
		if (isNaN(days)) {
			return false;
		}

		// get hours and remove trailing "h"
		let hours = times[1].substring(0, times[1].length - 1);

		// check hours
		if (isNaN(hours)) {
			return false;
		}

		// return the integer representing days and hours
		return true;
	}

	/**
	 * Return the content of the cell identified by row and index.
	 */
	function getCellContent(row, index) {
		return row.children[index].textContent.trim();
	}

	/**
	 * Select the button just clicked.
	 */
	function selectButton(header, asc) {
		let selected = asc ? header.querySelector("#button_up") : header.querySelector("#button_dn");
		selected.classList.remove("button_normal")
		selected.classList.add("button_selected");
	}

	/**
	 * Select the default button (for each table) when the page loads for the first time.
	 */
	function selectDefaultButtons() {
		document.querySelectorAll("table > thead th.sorted-default-asc")
			.forEach(th => selectButton(th, true));

		document.querySelectorAll("table > thead th.sorted-default-desc")
			.forEach(th => selectButton(th, false));
	}

	/**
	 * Initialize all buttons to the default style
	 */
	function initButtons(headers) {
		for (header of headers) {
			header.querySelectorAll("#button_up, #button_dn").forEach(btn => btn.classList.add("button_normal"));
		}
	}

	/**
	 * Reset the style for all the buttons.
	 */
	function resetButtons(headers) {
		for (header of headers) {
			let buttons = header.querySelectorAll("#button_up, #button_dn");

			for (button of buttons) {
				button.classList.remove("button_selected");
				button.classList.add("button_normal");
			}
		}
	}

	/*
	* Creates a function that compares two rows based on the cell in the idx
	* position.
	*/
	function createComparer(idx, asc) {
		return function(rowa, rowb) {
			// get values to compare at column idx
			// if order is ascending, compare 1st row to 2nd , otherwise 2nd to 1st
			let v1 = getCellContent(asc ? rowa : rowb, idx);
			let v2 = getCellContent(asc ? rowb : rowa, idx);

			// compare currency
			if (v1.endsWith("€") && v2.endsWith("€")) {
				v1 = parseFloat(v1.substring(0, v1.length - 1).trim().replace(".", "").replace(",", "."));
				v2 = parseFloat(v2.substring(0, v2.length - 1).trim().replace(".", "").replace(",", "."));

			} else if (isTimeDelta(v1) && isTimeDelta(v2)) {
				return compareTimeDeltas(v1, v2); // compare time deltas
			}

			// If non numeric value
			if (v1 === '' || v2 === '' || isNaN(v1) || isNaN(v2)) {
				return v1.toString().localeCompare(v2); // lexical comparison
			}

			// If numeric value
			return v1 - v2; // v1 greater than v2 --> true
		};
	}

	/**
	 * Returns the comparison result between two time deltas
	 */
	function compareTimeDeltas(v1, v2) {
		let d1, h1, d2, h2;

				if(v1 === "EXPIRED") {
					d1 = 0;
					h1 = 0;
				} else {
					let times1 = v1.split("d ");
					d1 = parseInt(times1[0]);
					h1 = parseInt(times1[1].substring(0, times1[1].length - 1));
				}

				if(v2 === "EXPIRED") {
					d2 = 0;
					h2 = 0;
				} else {
					let times2 = v2.split("d ");
					d2 = parseInt(times2[0]);
					h2 = parseInt(times2[1].substring(0, times2[1].length - 1));
				}

				if (d1 === d2)
					return h1 - h2;
				return d1 - d2;
	} 

	/**
	 * Sort the closest table to the given column ID.
	 * 
	 * @param {string} colId The ID of the column to be used to sort.
	 * @param {boolean} asc whether the ordering is ascendant (true) or descendant (false).
	 */
	function sortTable(colId, asc) {
		let colHeader = document.getElementById(colId);

		let table = colHeader.closest("table");
		let headers = table.querySelectorAll("th");

		let colIndex = Array.from(headers).indexOf(colHeader);

		let rows = Array.from(table.querySelectorAll("tbody > tr"));

		// sort table rows based on the clicked index, with the chosen ordering criterion
		rows.sort(createComparer(colIndex, asc));

		// change selected button
		resetButtons(headers);
		selectButton(colHeader, asc);

		// append the sorted rows to the table body
		let tBody = table.querySelector('tbody');
		rows.forEach(row => tBody.appendChild(row))
	}
})();
