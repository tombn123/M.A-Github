function getUrlParameter(sParam) {
    var sPageURL = window.location.search.substring(1),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : decodeURIComponent(sParameterName[1]);
        }
    }
}

// ASSUMPTION : when getting PRID we can get the specific PR that contains the target and base branches.
var PRID = getUrlParameter('PRID');

// some identifier that we can get the specific repository with.(maybe name or path?)
var repositoryIdentifier = getUrlParameter('repositoryIdentifier');

$(function() { //onload function
    $('#backButton').on('click', function(e){
        e.preventDefault();
        var url = "../pullRequest.html";
        window.location.href = url;
    });





    $.ajax({
        // this ajax is calling the PRContent servlet in order to get the list of paths for all the FolderDifference items.
        url: "../watchPR",
        data: {"PRID": PRID},
        error: function () {
            console.log("no");
        },
        success: function (data) {
            var files = data.split("\n"), i; // ASSUMPTION: instead of object, we print each path in the servlet with out.println
            var fileButtonClick = function(event){
                $.ajax({
                    url: "../watchPR",
                    data: {"filePath": event.data.filePath,"requestType":"getContent"},
                    error: function () {
                        console.log("no");
                    },
                    success: function (data) {
                        // break the textblock into an array of lines
                        var lines = data.split('\n');
                        var status = lines[0];
                        // remove one line, starting at the first position
                        lines.splice(0,1);
                        // join the array back into a single string
                        var newtext = lines.join('\n');
                        $("#fileStatus").empty();
                        $("#fileStatus").append(status);
                        $("#fileContent").empty();
                        $("#fileContent").append(newtext);
                        $("#fileName").empty();
                        $("#fileName").append(event.data.filePath);
                    }
                });
            };
            for(i=0;i<files.length-1;i++) // notice - its length-1 because files also includes another cell of newline only
            {

                var fileName = files[i].replace(/(\r\n|\n|\r)/gm, "").trim();
                var element = document.createElement("li");
                element.innerHTML = '<button class="commitFile" type="button">'+ fileName + '</button>';
                document.getElementById("fileTreeDemo_1").appendChild(element);
                //document.getElementById("fileTreeDemo_1").appendChild(document.createElement("BR"));
                $(".commitFile:last").click({filePath : fileName}, fileButtonClick);
            }

            $(".commitFile:last").trigger("click");
        }
    });
});