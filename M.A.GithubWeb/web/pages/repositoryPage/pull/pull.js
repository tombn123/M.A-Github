var PULLPUSH_URL = "pullpush";
var PULL = "pull";

function pull() {

    $.ajax({

        url: PULLPUSH_URL,
        data: {"commandType": PULL},

        success: function () {
            alert("Pull Done!!");
            location.replace("repositoryPage.html");
        },

        error: function () {
            alert("Problem occured while pulling from remote");
        }
    });
}