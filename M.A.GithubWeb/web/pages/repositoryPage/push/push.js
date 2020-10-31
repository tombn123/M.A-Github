var PUSH = "push";

function push() {
    $.ajax({

        url: PULLPUSH_URL,
        data: {"commandType": PUSH},

        success: function () {
            alert("Push Done!!");
            location.replace("repositoryPage.html");
        },

        error: function () {
            alert("Problem occured while pushing to remote");
        }
    });
}