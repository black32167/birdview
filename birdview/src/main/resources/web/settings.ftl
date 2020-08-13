<!DOCTYPE html>
<html>

<head>
<link rel="stylesheet" href="css/bv.css"></link>
<script src="js/jquery-3.5.1.min.js"></script>
</head>

<body>
    <div class="menu">
       <a href="/">< Home</a>
       |
       <a href="settings/add-source">Add source</a>
    </div>

    <div>
    <div class="center">
    <table>
    <tr>
        <th>Name</th>
        <th>Type</th>
        <th></th>
    </tr>
    <#list sources as source>
        <tr>
        <td>
            <a href="settings/${source.type?lower_case}/edit-source?sourceName=${source.name}">${source.name}</a>
        </td>
        <td>
            ${source.type}
        </td>
        <td>
            <a href="settings/delete?sourceName=${source.name}">X</a>
        </td>
        </tr>
    </#list>
    </table>
    </div>
    </div>
</body>
</html>