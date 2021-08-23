<!DOCTYPE html>
<html>

<head>
<link rel="stylesheet" href="/css/bv.css"></link>
</head>
<body>
    <div class="menu">
       <a href="/user/settings">< Settings</a>
    </div>
    <form class="center" action="/user/settings/source/${sourceName}" method="POST">
        <input type="hidden" id="csrf_token" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <input type="hidden" name="sourceName" value="${sourceName}"/>
        <input type="hidden" name="sourceType" value="${sourceType}"/>
        <table>
            <tr>
                <td class="sign">Source Type:</td>
                <td>${sourceType}</td>
            </tr>
            <tr>
                <td class="sign">User Id:</td>
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
            <#include "include/edit/edit-secret-${sourceType}.ftl">
        </table>
        <div class="buttons">
            <input type="submit" value="Update">
        </div>
    </form>
</body>
</html>