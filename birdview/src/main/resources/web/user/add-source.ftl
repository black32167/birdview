<!DOCTYPE html>
<html>

<head>
<link rel="stylesheet" href="/css/bv.css"></link>
</head>
<body>
    <div class="menu">
       <a href="/user/settings">< Settings</a>
    </div>
    <form class="center" action="/user/settings/source" method="POST">
        <input type="hidden" id="csrf_token" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <table>
            <tr>
                <td class="sign">Source name:</td>
                <td>
                    <select name="sourceName">
                        <#list availableSourceNames as sourceName>
                        <option value="${sourceName}">${sourceName}</option>
                        </#list>
                    </select>
                </td>
            </tr>
            <tr>
                <td class="sign">User Id:</td>
                <td><input type="text" name="sourceUserName"></td>
            </tr>
        </table>
        <div class="buttons">
            <input type="submit" value="Create">
        </div>
    </form>
</body>
</html>