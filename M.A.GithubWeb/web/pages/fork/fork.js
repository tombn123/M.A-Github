var CLONE_SERVLET_URL = "CloneServlet";
var All_REPOSITORIES_DETAILS_URL = "allRepositoriesDetails";
var USER_LIST_HISTORY_URL = "../../usersListHistory";
//stam message

/*var refreshRate = 4000;*/
/*-----------------------------------refreshRepositoryTable-------------------------------------*/
$(function () {
    $.ajax({
        url: USER_LIST_HISTORY_URL,
        success: function (users) {
            refreshUsersList(users);
        },
        error: function () {
            console.log("couldnt refresh the user list");

        }
    });
});


function refreshUsersList(users) {
    //clear all current users
    $("#userList").empty();
    var repoNameTrimmed;
    // rebuild the list of users: scan all users and add them to the list of users
    $.each(users || [], function (index, user) {
        repoNameTrimmed = user.userName;
        console.log("Adding user #" + index + ": " + repoNameTrimmed);
        if (user.userName.indexOf(" ") >= 0) {
            repoNameTrimmed = repoNameTrimmed.replace(/ /g, "_");
            console.log(repoNameTrimmed);
        }
        $("<li id=" + "\"" + repoNameTrimmed + "\"" + ">" + user.userName + "</li>").appendTo($("#userList"));

        $("#" + repoNameTrimmed).on("click", function (event) {
            console.log("click detected");
            $.ajax({
                url: All_REPOSITORIES_DETAILS_URL,
                data: {"username": user.userName},
                success: function (repositories) {

                    uploadRepositoryData(repositories)
                },
                error: function () {
                    console.log("error occured while trying bringing repositories");
                }
            });
        });
    });
}

function uploadRepositoryData(repositories) {
    $("#repositoriesDetails").empty();

    $(repositories).each(function (index, element) {
        var button = "<input type   =\"button\" id =\"" + index + "\" value=\"clone this\">";

        $("<tr>" +
            `<th>${index}</th>` +
            "<td>" + button + "</td>" +
            "<td >" + element.repositoryName + "</td>" +
            "<td>" + element.latestCommit + "</td>" +
            "<td>" + element.message + "</td>" +
            "<td>" + element.activeBranch + "</td>" +
            "<td>" + element.commitAmount + "</td>" +
            "</tr>").appendTo($("#repositoriesDetails"));

        $("#" + index).click(function (event) {
            console.log(index);


            var repoName = prompt("Enter The Name Of The Repository", "Repositoryation");

            if (!(repoName != "" && repoName != null))
                return;

            $.ajax({
                url: CLONE_SERVLET_URL,
                data: {
                    "repositoryName": element.repositoryName,
                    "username": element.userName,
                    "repoNewName": repoName
                },
                success: function () {
                    location.replace("../repositoryHub/repositoryHub.html");
                    console.log("Clone succeeded!!");
                },
                error: function () {
                    console.log("Error while trying clone action!");
                },

                return: true
            });
        });
    })
}

