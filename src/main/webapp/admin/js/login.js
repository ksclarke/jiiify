/*var online = function(session) {
  var currentTime = (new Date()).getTime() / 1000;
  return session && session.access_token && session.expires > currentTime;
};*/

var encode = function(name) {
  return encodeURIComponent(name).replace(/[!'()*]/g, function(c) {
    return '%' + c.charCodeAt(0).toString(16);
  });
};

function login(site) {
  hello(site).login({
    'scope' : 'email'
  }).then(
      function() {
        var session = hello(site).getAuthResponse();
        var xhr = new XMLHttpRequest();

        xhr.open('POST', '/admin/login');
        xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');

        xhr.onload = function() {
          if (this.responseText == 'success') {
            var loginLink = document.getElementById('login');

            // After this, this is done server side, but for now...
            loginLink.text = "Logout";
            loginLink.setAttribute('href', '/admin/logout');

            // Let's give more of a visual representation of being logged in
            hello(site).api('me').then(
                function(json) {
                  var loginTd = document.getElementById(site + '-login');
                  var userImage = document.createElement('img');
                  var userName = document.createTextNode(' ' + json.name);
                  var divElement = document.createElement('div');

                  userImage.setAttribute('src', json.thumbnail);
                  divElement.setAttribute('id', 'login-response');

                  loginTd.firstChild.firstChild.data = 'Logged into '
                      + site.charAt(0).toUpperCase() + site.slice(1);
                  loginTd.appendChild(divElement);
                  divElement.appendChild(userImage);
                  divElement.appendChild(userName);
                });
          }
        };

        xhr.send('token=' + session.access_token + '&site=' + site /*+ '&name=' + encodeURIComponent(json.name)*/);
        console.log('Signed into ' + site);
      }, function(e) {
        console.log('Login error: ' + e.error.message);
      });
}