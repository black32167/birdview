<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="css/bv.css"></link>
</head>
<body>
    <h1 class="center">Sign Up</h1>
    <#if errorMessage??>
    <div class="error">${errorMessage}</div>
    </#if>
    <form class="center" action="/signup" method="POST">
        <input type="hidden" id="csrf_token" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <table>
        <tr>
            <td class="sign">User:</td>
            <td><input type="text" name="user"></td>
        </tr>
        <tr>
            <td class="sign">Email:</td>
            <td><input type="text" name="email"></td>
        </tr>
        <tr>
            <td class="sign">Password:</td>
            <td><input type="password" name="password"></td>
        </tr>
        <tr>
            <td class="sign">Timezone:</td>
            <td>
              <select name="zoneId">
                  <#list availableTimeZoneIds as availableTimeZoneId>
                  <option value="${availableTimeZoneId}">${availableTimeZoneId}</option>
                  </#list>
              </select>
            </td>
        </tr>

        </table>
        <div class="buttons">
            <input type="submit" value="Signup"> or <a href="/login">Login</a>
        </div>
    </form>
</body>
</html>