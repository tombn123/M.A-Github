var USER_NOTIFICATIONS_URL = "../../notifications";

$(function () {

    setInterval(notificationsUsersList, refreshRate);
});

function notificationsUsersList() {
    $.ajax({
        url: USER_NOTIFICATIONS_URL,
        success: function (notifications) {
            console.log(notifications);

            refreshNotificationsList(notifications);
        },

        error: function () {
            alert("Problen in bringing notification");
        }
    });
}

function refreshNotificationsList(notifications) {
    //clear all current notifications
    $("#notificationsList").empty();

    $.each(notifications || [], function (index, notification) {
        console.log("Adding notification #" + index + ": " + notification);


        $(`<li> <p class="media-heading"><b>${notification}</b></p> </li>`).appendTo($("#notificationsList"));
        /*<li> <p class="media-heading"><b>Sahar </b>was forked you</p> </li>*/
    });
}
