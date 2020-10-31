function addAllBranchesForCheckout() {
    $.ajax({
        url: REPOSITORY_INFO_URL,
        data: {"requestType": ALL_BRANCHES},

        success: function (branches) {
            console.log(branches);
            $("#checkout").empty();

            /*if ((branches === undefined) || (branches.length == 0))
                $("#branchesList").append("<h4>There are no local branches</h4>");
            else {
                $("#branchesList").append("<h4>Choose Branch for pushing</h4>");*/

            addBranchesToDropDownCheckout(branches);
        },
        error: function () {
            alert("Problem while bringing all branches");
        }
    });
}

function addBranchesToDropDownCheckout(branches) {
    $.each(branches, function (index, branch) {

        var branchNameID = branch.m_BranchName;

        if (branchNameID.includes(" ") || branchNameID.includes("/")) {
            branchNameID = branchNameID.replace(/ /g, "_").replace("/", "_");
        }

        branchNameID += FOR_PUSHING;

        /*    <a href="#" class="dropdown-item">Divisão Militar</a>*/

        $("#checkout").append("<a href=\"#\" class=\"dropdown-item\" id=" + "\"" + branchNameID + "\"" + ">" + branch.m_BranchName + "</a>");

        $("#" + branchNameID).click(function () {
            checkout(branch.m_BranchName);
        })
    });
}


function checkout(branchName) {

    //check if the currentBranch is rtb
    if (branchName.includes('/')) {
        createRTB(branchName);
    } else {
        executeCheckout(branchName);
    }
}


function executeCheckout(branchName) {
    $.ajax({

        url: CHECKOUT_URL,
        data: {"branchName": branchName},

        success: function () {
            location.replace("repositoryPage.html");
            alert("checkout done!");
        }
    });
}
