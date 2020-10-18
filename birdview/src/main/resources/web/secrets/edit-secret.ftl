<#include "/links.ftl">
<!DOCTYPE html>
<html>

<head>
<#include "/head.ftl">
</head>

<body>
    <div class="menu">
       <@admin_link />
    </div>

    <div>
    <div class="center">
        <form action="<@update_secret_post_link source.type />" method="POST">
            <input type="hidden" id="csrf_token" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <input type="hidden" name="sourceName" value="${source.sourceName}">
            <table>
            <tr>
                <td class="sign">Source alias:</td>
                <td>${source.sourceName}</td>
            </tr>
            <#include "edit-secret-${source.type}.ftl">
            </table>
            <div class="buttons">
                <input type="submit" value="Update">
            </div>
        </form>
    </div>
    <div>
</body>
</html>