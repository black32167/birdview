<!DOCTYPE html>
<html>

<head>
<#include "/head.ftl">
</head>

<body>
    <div class="menu">
       <a href="/admin/secrets">< Settings</a>
    </div>

    <div>
    <div class="center">
        <form id="jira-form" action="/admin/secrets/${source.type}/update-secret" method="POST">
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