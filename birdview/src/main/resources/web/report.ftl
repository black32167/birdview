<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet" href="css/bv.css"></link>
<link rel="stylesheet" href="css/jquery.treetable.css"></link>
<link rel="stylesheet" href="css/jquery.treetable.theme.default.css"></link>

<script src="js/jquery-3.5.1.min.js"></script>
<script src="js/jquery.treetable.js"></script>
<script src="js/birdview.js"></script>
<script>
    baseURL = "${baseURL}"
    $(function() {
        console.log( "document loaded!" )
        refresh()
    })
</script>
</head>
<!-- -------------------------------------------------------------- -->
<body>

<div class="menu">
    <label for="reportType">Report type:</label>
    <select id="reportType" onchange="refresh()">
        <#list reportTypes as reportType>
        <option value="${reportType}">${reportType}</option>
        </#list>
    </select>
    |
    <label for="userRole">Role:</label>
    <select id="userRole" onchange="refresh()">
        <#list userRoles as userRole>
        <option value="${userRole}">${userRole}</option>
        </#list>
    </select>
    |
    <label for="daysBack">Days back</label>
    <input id="daysBack" type="number" value="1" onchange="refresh()"></input>
    |
    <label for="user">User:</label>
    <select id="user" onchange="refresh()">
        <#list users as user>
        <option value="${user}">${user}</option>
        </#list>
    </select>
    |
    <label for="representation">Representation:</label>
    <select id="representation" onchange="refresh()">
        <option value="tree">Tree</option>
        <option value="list">List</option>
    </select>
    |
    <label for="source">Source:</label>
    <select id="source" onchange="refresh()">
        <option value="">All</option>
        <#list sources as source>
        <option value="${source}">${source}</option>
        </#list>
    </select>
    |
    <a href="settings">Settings...</a>

    <span class="menu_right">
    <a href="#" onclick="reindex()" class="refresh">Update</a>
    |
    ${.now?date}
    </span>
</div>
<hr>

<!-- #import reportPath as report -->
<div id="tableContainer">
<table id="reportTable">
</table>
</div>

<div id="reportList">
</div>

<div id="overlay">
Loading...
</div>

</body>
</html>