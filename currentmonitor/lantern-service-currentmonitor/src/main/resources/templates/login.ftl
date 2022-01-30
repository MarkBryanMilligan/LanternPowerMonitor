<!DOCTYPE html>
<html lang="en">
<head>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js"></script>
    <script src="https://apis.google.com/js/client:platform.js?onload=start" async defer></script>
    <script>
        function start() {
            gapi.load('auth2', function() {
                auth2 = gapi.auth2.init({client_id: '412929846491-r3uh0t67mpeouicjvlara580i9cfchol.apps.googleusercontent.com', cookie_policy: 'none', redirect_uri: 'postmessage'});
            });
        }
    </script>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Lantern Power Monitor</title>
    <link rel="icon" type="image/png" href="${link_prefix}img/favicon.png">
    <link href="${link_prefix}bootstrap/css/bootstrap.min.css" rel="stylesheet">
    <link href="${link_prefix}bootstrap/css/style.min.css" rel="stylesheet">
</head>
<body>
<main>
    <div class="container-fluid login-container">
        <form method="POST">
        <div class="row">
            <div class="col-1 col-lg-3 col-4k-4"></div>
            <div class="col-10 col-lg-6 col-4k-4">
                <div class="row pt-5">
                    <div class="col-12">
                        <img class="img-fluid" alt='Lantern Logo' src='${link_prefix}img/lantern_cm.png'/>
                    </div>
                </div>
                <div class="row pt-5">
                    <div class="col-1 col-lg-2"></div>
                    <div class="col-10 col-lg-8">
                        <input type="email" name="username" class="login-input" placeholder="email"/>
                    </div>
                    <div class="col-1 col-lg-2"></div>
                </div>
                <div class="row pt-2">
                    <div class="col-1 col-lg-2"></div>
                    <div class="col-10 col-lg-8">
                        <input type="password" name="password" class="login-input" placeholder="password"/>
                    </div>
                    <div class="col-1 col-lg-2"></div>
                </div>
                <div class="row pt-2">
                    <div class="col-1 col-lg-2"></div>
                    <div class="col-8 col-lg-6 d-flex"><button type="button" id="signinButton" class="gso"></button></div>
                    <div class="col-2 d-flex justify-content-end">
                        <input type="submit" class="btn-primary border-none px-3" value="Login"/>
                    </div>
                    <div class="col-1 col-lg-2"></div>
                </div>
                <div class="row pt-5">
                    <div class="col-1 col-lg-2"></div>
                    <div class="col-10 col-lg-8 error">
                        ${error!}
                    </div>
                    <div class="col-1 col-lg-2"></div>
                </div>
            </div>
            <div class="col-1 col-lg-3 col-4k-4"></div>
        </div>
        </form>
    </div>
    <div class="login-bkgnd"></div>
    <script>
        $('#signinButton').click(function() {
            auth2.grantOfflineAccess().then(signInCallback);
        });
        function signInCallback(authResult) {
            if (authResult['code']) {
                $.ajax({
                    type: 'POST',
                    url: 'gso',
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    contentType: 'application/octet-stream; charset=utf-8',
                    success: function(result) {
                        window.location.replace('./');
                    },
                    processData: false,
                    data: authResult['code']
                });
            } else {
                // There was an error.
            }
        }
    </script>
</main>
</body>
</html>
