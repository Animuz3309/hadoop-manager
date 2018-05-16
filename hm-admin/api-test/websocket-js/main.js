// just web socket don't use stomp
function connect() {

    var url = "http://localhost:8080/hm-admin/api/ws/tty";
    var token;
    var container;
    url = url + "?token=" + token + "&container=" + container;
    var ws = new SockJS(url);

    ws.onopen = function (ev) {
        console.log("connect to ws tty");
    };

    ws.onerror = function (ev) {
      console.log("connect error ", ev);
      ws.close();
    };

}

connect();