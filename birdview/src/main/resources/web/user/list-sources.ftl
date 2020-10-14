<!DOCTYPE html>
<html>

<head>
<link rel="stylesheet" href="/css/bv.css"></link>
</head>
<body>
    <div class="menu">
       <a href="/">< Home</a>
       |
       <a href="/user/sources/add">Add source</a>
    </div>

    <div class="center">
    <table class="settings">
    <tr>
        <th>Source Name</th>
        <th></th>
    </tr>
    <#list sourceNames as sourceName>
        <tr>
        <td>
            <a href="/user/sources/${sourceName}/edit">${sourceName}</a>
        </td>
        <td>
            <a href="/user/sources/${sourceName}/delete">X</a>
        </td>
        </tr>
    </#list>
    </table>
    </div>
</body>
</html>