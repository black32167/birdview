<#macro brief docs>
<#if docs?has_content>
<ul>
<#list docs as doc>
    <li>
        ${doc.title} (<a href="${doc.httpUrl}">${doc.key}</a>)
    </li>
    <@brief doc.subDocuments />
</#list>
</ul>
</#if>
</#macro>



<!-- -------------------------------------------------------------- -->
<html>
<head>
<link rel="stylesheet" href="css/report.css"></link>
<link rel="stylesheet" href="css/jquery.treetable.css"></link>
<link rel="stylesheet" href="css/jquery.treetable.theme.default.css"></link>

<script src="js/jquery-3.5.1.min.js"></script>
<script src="js/jquery.treetable.js"></script>
<script src="js/birdview.js"></script>
<script>
    baseURL = "${baseURL}"
    renderTree()
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
    <label for="userRole">User role:</label>
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

    <span class="menu_right">
    <a href="#" onclick="update()" class="refresh">Update</a>
    |
    ${.now?date}
    </span>
</div>
<hr>

<!-- #import reportPath as report -->
<table id="reportTable">
</table>

</body>
</html>