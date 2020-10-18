<!DOCTYPE html>
<html>

<head>
<link rel="stylesheet" href="/css/bv.css"></link>
</head>
<body>
    <div class="menu">
       <a href="/user/sources">< Sources</a>
    </div>
    <form class="center" action="/user/sources/${sourceName}" method="POST">
        <input type="hidden" id="csrf_token" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <table>
            <tr>
                <td class="sign">User alias:</td>
                <td><input type="text" name="sourceUserName" value="${sourceUserName}"></td>
            </tr>
            <tr>
                <td class="sign">Enabled:</td>
                <#if enabled == "yes">
                    <#assign checkedHint="checked">
                <#else>
                    <#assign checkedHint="">
                </#if>
                <td><input type="checkbox" name="enabled" ${checkedHint}></td>
            </tr>
        </table>
        <div class="buttons">
            <input type="submit" value="Update">
        </div>
    </form>
</body>
</html>