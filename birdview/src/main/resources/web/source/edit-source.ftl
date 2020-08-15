<!DOCTYPE html>
<html>

<head>
<#include "/head.ftl">
</head>

<body>
    <div class="menu">
       <a href="/settings">< Settings</a>
    </div>

    <div>
    <div class="center">
        <form id="jira-form" action="/settings/${source.sourceName}/update-source" method="POST">
            <input type="hidden" name="sourceName" value="${source.sourceName}">
            <table>
            <tr>
                <td class="sign">Source alias:</td>
                <td>${source.sourceName}</td>
            </tr>
            <#include "edit-source-${source.sourceName}.ftl">
            </table>
            <div class="buttons">
                <input type="submit" value="Update">
            </div>
        </form>
    </div>
    <div>
</body>
</html>