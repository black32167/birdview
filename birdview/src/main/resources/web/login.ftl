<html>
<head>
    <link rel="stylesheet" href="css/bv.css"></link>
</head>
<body>
    <div class="center">
    <!-- #if RequestParameters.error??>
        <p>Invalid login.</p>
    </#if -->
    <h1 class="center">Login</h1>
    <form name='f' action="/login" method='POST'>
        <input type="hidden" id="csrf_token" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <table>
            <tr>
                <td class="sign">User:</td>
                <td><input type='text' name='username' value=''></td>
            </tr>
            <tr>
                <td class="sign">Password:</td>
                <td><input type='password' name='password' /></td>
            </tr>
        </table>
        <div class="buttons">
            <input name="submit" type="submit" value="Login" /> or <a href="/signup">Sign Up</a>
        </div>
    </form>
    </div>
</body>
</html>