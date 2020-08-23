<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet" href="css/jquery.treetable.css"></link>
<link rel="stylesheet" href="css/jquery.treetable.theme.default.css"></link>
<link rel="stylesheet" href="css/bv.css"></link>

<script src="js/jquery-3.5.1.min.js"></script>
<script src="js/jquery.treetable.js"></script>
<script src="js/report.js"></script>
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

<table class="layout" width="100%" height="100%">
<tr>
<td class="menu-left">
    <div class="menu-item">
        ${.now?date}
    </div>
    <table class="settings">
        <tr><td class="sign">Report:</td>
        <td>
            <select id="reportType" onchange="refresh()">
                <#list reportTypes as reportType>
                <option value="${reportType}">${reportType}</option>
                </#list>
            </select>
        </td></tr>
        <tr><td class="sign">Role:</td>
        <td>
            <select id="userRole" onchange="refresh()">
                <#list userRoles as userRole>
                <option value="${userRole}">${userRole}</option>
                </#list>
            </select>
        </td></tr>
        <tr><td class="sign">Days back</td>
        <td>
            <input id="daysBack" type="number" value="1" onchange="refresh()"></input>
        </td></tr>
        <tr><td class="sign">User:</td>
        <td>
            <select id="user" onchange="refresh()">
                <option value="">Select...</option>
                <#list users as user>
                <option value="${user}">${user}</option>
                </#list>
            </select>
        </td></tr>
        <tr><td class="sign">Representation:</td>
        <td>
            <select id="representation" onchange="refresh()">
                <option value="tree">Tree</option>
                <option value="list">List</option>
            </select>
        </td></tr>
        <tr><td class="sign">Source:</td>
        <td>
            <select id="source" onchange="refresh()">
                <option value="">All</option>
                <#list sources as source>
                <option value="${source}">${source}</option>
                </#list>
            </select>
        </td></tr>
    </table>

    <div class="menu-item">
        <a href="settings">Settings...</a>
        |
        <a href="#" onclick="reindex()" class="refresh">Update</a>
    </div>
    </span>
</td>
<td width="100%">
    <!-- #import reportPath as report -->
    <div id="reportContainer">
    </div>
</td>
</tr>
</table>

<div id="overlay">
Loading...
</div>

</body>
</html>