<!DOCTYPE html>
<html>

<head>
<link rel="stylesheet" href="/css/bv.css"></link>
<script src="js/jquery-3.5.1.min.js"></script>
</head>

<body>
    <div class="menu">
        <a href="secrets/add-secret">Add secret</a>

        <div class="menu_right">
            <#include "/logout-form.ftl">
        </div>
    </div>

    <div>
    <div class="center">
    <table class="settings">
    <tr>
        <th>Name</th>
        <th>Type</th>
        <th></th>
    </tr>
    <#list sources as source>
        <tr>
        <td>
            <a href="secrets/${source.type?lower_case}/edit-secret?sourceName=${source.name}">${source.name}</a>
        </td>
        <td>
            ${source.type}
        </td>
        <td>
            <a href="secrets/delete?sourceName=${source.name}">X</a>
        </td>
        </tr>
    </#list>
    </table>
    </div>
    </div>
</body>
</html>