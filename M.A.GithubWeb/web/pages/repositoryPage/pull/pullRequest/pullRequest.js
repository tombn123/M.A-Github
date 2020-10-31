var PULL_REQUEST_TYPE = "4";
var USER_INFO = "userinfo";
var WATCH_PR_URL = "watchPR";
var SEND_NOTIFICATION_URL = "sendnotification";
var APPROVED_URL = "approvedpr";

//need to do setInterval


/*----------------------------------------------------bringing the pull requests----------------------------------------------------*/

/*variablesForCardBody:*/


var beginTillName = "class=\"card\" style=\"width: 18rem;\">\n" +
    "<div class=\"card-body\">\n" +
    "<h5 class=\"card-title\">";

var middleTillID = " </div>\n" +
    "<ul class=\"list-group list-group-flush\">\n" +
    "<li class=\"list-group-item\">";

var elementInCard = "<li class=\"list-group-item\">";

/*variablesForLabels:*/

var repositoryNameLabel = "Repository Name: ";
var idLabel = "ID: ";
var branchBaseLabel = "Branch Base: ";
var branchTargetLabel = "Branch Target: ";


/*----------------------*/
$(function () {

    $.ajax({

        url: USER_INFO,
        dataType: "json",
        data: {
            "dataType": "pullRequests"
        },

        success: function (pullRequests) {
            console.log(pullRequests);
            creatingPullRequestsCards(pullRequests);
        },

        error: function (e) {
            alert("error occured while bringing pull requests");
        }
    })

});


function creatingPullRequestsCards(pullRequests) {

    var approve = "approve";
    var denie = "denie";
    var watch = "watch";
    var card = "card";

    $(pullRequests).each(function (index, element) {

        console.log(element);

        approve += index;
        denie += index;
        watch += index;
        card += index;

        $(`<div id=\"${card}\" ` + beginTillName + repositoryNameLabel + element.repositoryName + '</h5>' +
            '<p class="card-text">' + element.message + '</p>' +
            middleTillID + idLabel + element.id + '</li>' +
            elementInCard + branchBaseLabel + element.baseBranchName + '</li>' +
            elementInCard + branchTargetLabel + element.targetBranchName + '</li>' + " <div class=\"card-body\">" +
            `<a href="#" class=\"card-link\" id=\"${approve}\">Approve</a>\n` +
            `<a href="#" class=\"card-link\" id=\"${denie}\">Denie</a>\n` +
            `<a href="#" class=\"card-link\" id=\"${watch}\">Watch</a>\n` + '</div></div>'
        ).appendTo($("#pullRequestsCards"));

        $("#" + approve).click(function () {

            ajaxUproveStatus(element, card);
        });


        $("#" + watch).click(function () {

            ajaxWatchClick(element);
        });

        $("#" + denie).click(function () {


            ajaxDenieStatus(element, card);
        });
    });
}


/*-----------------------------------------------------APPROVED-----------------------------------------------------------*/
async function ajaxUproveStatus(element, card) {

    var prID = element.id;

    var stam = await sendNotificationResponse("Approved", element.message, prID);

    $.ajax({

        url: APPROVED_URL,
        data: {
            "prID": prID,
        },

        success: function () {
            $("#" + card).empty();
            console.log("in success function");
        },

        error: function (e) {
            alert("error occured while approving pull request");
        }
    });


}

/*-----------------------------------------------------DENIED-----------------------------------------------------------*/


function ajaxDenieStatus(element, card) {

    console.log("click detected");

    var message = prompt("Enter you reason of denied");

    if (message == null || message == "")
        return;

    var prID = element.id;

    sendNotificationResponse("Denied", message, prID);

}

/*-----------------------------------------------------WATCH-----------------------------------------------------------*/

function ajaxWatchClick(element) {

    window.location.href = 'viewPR/viewPR.html?PRID=' + element.id;
}


/*-----------------------------------------------------FUNCTIONS:-----------------------------------------------------------*/


function sendNotificationResponse(status, message, prID) {
    $.ajax({

        url: SEND_NOTIFICATION_URL,
        data: {
            "prID": prID,
            "status": status,
            "message": message
        },


        success: function () {
            $("#" + card).empty();
        },

        error: function (e) {
            alert("error ocured while send notification");
        }
    });
}
