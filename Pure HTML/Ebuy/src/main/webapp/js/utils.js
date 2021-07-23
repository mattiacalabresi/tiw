// on upload file retrieves num files and output lable, then triggers a 'fileselect' event.
$(document).on('change', ':file', function () {
    let input = $(this);
    let numFiles = input.get(0).files ? input.get(0).files.length : 1;
    let label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
    input.trigger('fileselect', [numFiles, label]);
});

$(document).ready(function () {
	// on 'fileselect' event fills the image image-form with the appropriate text
    $(':file').on('fileselect', function (event, numFiles, label) {

        let input = document.getElementById("image-form");

        if (numFiles > 0) {
            input.value = label;
        } else {
            input.value="";
            input.placeholder = "Max. size 10MB";
        }
    });
});