var NEW_BRANCH_URL = "newBranch";
var RTB = "2";
var LocalBranch = "1";
var BRANCH_NAME = "branchName";

var SHA1 = "sha1";


function newLocalBranch() {

    var branchName = prompt("Please enter your branch name");

    if (!inputIsValid(branchName))
        return;

    var sha1Commit = prompt("Please enter your sha1 commit");

    if (!inputIsValid(sha1Commit))
        return;

    $.ajax({

        url: NEW_BRANCH_URL,
        data: {
            "sha1": sha1Commit,
            "branchName": branchName,
            "branchType": LocalBranch
        },

        success: function () {
            alert("Local Branch was created successfully!");
            location.replace("repositoryPage.html");
        },
        error: function () {
            alert("Problem occured while creating local branch");
        }
    });

}


function createRTB(branchName) {
    $.ajax({

        url: NEW_BRANCH_URL,
        data: {
            "branchName": branchName,
            "branchType": RTB
        },

        success: function (RTBbranchName) {
            alert("RTB was created for checkout!");
            location.replace("repositoryPage.html");

            executeCheckout(RTBbranchName);
        },
        error: function(){
            alert("problem occured while creating RTB");
        }
    });
}

function inputIsValid(input) {
    return (input != "" && input != null);
}



