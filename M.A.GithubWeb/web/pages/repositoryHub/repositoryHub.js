var USER_LIST_URL = "../../userslist";
var REPOSITORY_DATA_URL = "../../repositorydata";
var LOAD_REPOSITORY_URL = "../loadRepository";
var REPOSITORY_UPLOAD_URL = "upload";
var refreshRate = 2000;
var REPO_NAME = "repoName";

function ajaxUsersList() {

    console.log("sahar");


    $.ajax({
        url: USER_LIST_URL,
        success: function (users) {
            refreshUsersList(users);
        },

        error: function () {
            alert("Error while trying bringing all users");
        }

    });
}

function refreshUsersList(users) {
    //clear all current users
    $("#usersList").empty();

    $('<li class="sidebar-brand"><a href="#">Users</a></li>').appendTo($("#usersList"));

    // rebuild the list of users: scan all users and add them to the list of users
    $.each(users || [], function (index, user) {
        console.log("Adding user #" + index + ": " + user);
        //create a new <option> tag with a value in it and
        //appeand it to the #userslist (div with id=userslist) element
        // <!--            <li><a href="#">Contact</a></li>-->

        $(`<li> <a href="#"> ${user} </a> </li>`).appendTo($("#usersList"));

    });
};

$(function () {
    //The users list is refreshed automatically every second
    setInterval(ajaxUsersList, refreshRate);
});

/*-----------------------------------refreshRepositoryTable-------------------------------------*/

function uploadRepositoryData(repositories) {
    $(repositories).each(function (index, element) {

        $("<tr id=\"" + index + "\">" +
            `<th>${index}</th>` +
            "<td id=\"" + REPO_NAME + index + "\">" + element.repositoryName + "</td>" +
            "<td>" + element.latestCommit + "</td>" +
            "<td>" + element.message + "</td>" +
            "<td>" + element.activeBranch + "</td>" +
            "<td >" + element.commitAmount + "</td>" +
            "</tr>").appendTo($("#repositoriesDetails"));

        $("#" + index).click(function (event) {
            console.log(index);
            var repoName = $("#" + REPO_NAME + index).text();
            console.log(repoName);

            $.ajax({
                url: LOAD_REPOSITORY_URL,
                data: {"repoName": repoName},
                success: function () {
                    location.replace("../repositoryPage/repositoryPage.html");
                },

                error: function(){
                    alert("error occured in loading repository");
                }
            });
        });
    });
}

$(function () {

    $.ajax({
        url: REPOSITORY_DATA_URL,
        success: function (repositories) {
            console.log(repositories);

            uploadRepositoryData(repositories);
        },
        error: function () {
            alert("Problem occured while bringing repositories");

        }
    });
});

$("#logoutButton").on('click', function () {
    $.ajax({
        url: "logoutServlet",
        success: function () {

            location.replace("../login/signup.html");
            alert("logout Done!");
        },

        error: function (e) {
            alert("problen occured while logout");
            console.log(e);
        }
    });
});

$("#uploadRepo").click(function () {
    $("#file-input").trigger('click');
});

$("#file-input").change(function () {
    var input = $(this)[0].files[0];

    var formData = new FormData();

    formData.append("xml", input);

    $.ajax({
        url: REPOSITORY_UPLOAD_URL,
        method: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: function () {
            alert("Repository Upload Succeeded");
            location.replace("repositoryHub.html");
        },

        error: function (err) {
            alert("in error function");
            console.log(err);
        }

    })
});

