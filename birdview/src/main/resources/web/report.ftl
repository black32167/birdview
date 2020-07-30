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

<#macro long_table docs parentId level>
<#list docs as doc>
    <#if level == 0>
      <tr data-tt-id="${doc.id}">
    <#else>
      <tr data-tt-id="${doc.id}" data-tt-parent-id="${parentId}">
    </#if>
        <#if doc.subDocuments?has_content>
            <td class="title">
        <#else>
            <td class="title_leaf">
        </#if>
        ${doc.title} (<a href="${doc.httpUrl}">${doc.key}</a>)</td>
        <td class="source">${doc.sourceName}</td>
        <td class="status">${doc.status}</td>
        <td class="date">${doc.updated?date}</td>
    </tr>
    <@long_table doc.subDocuments doc.id level+1/>
</#list>
</#macro>

<#macro long docs>
<#if docs?has_content>
<table id="reportTable">
<@long_table docs "" 0 />
</table>
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
<script>
    function renderTree() {
        $("#reportTable").treetable({ expandable: true })
    }
    function createTable(rootElement, docs, parentId, level) {
        docs.forEach( doc => {
            var row = $('<tr>').attr('data-tt-id', doc.id)
            if(parent) {
                row.attr('data-tt-parent-id', parentId)
            }

            // Title
            var titleCol = $('<td>').text(doc.title)
            if(doc.subDocuments.length == 0) {
                titleCol.addClass('title_leaf')
            } else {
                titleCol.addClass('title')
            }
            row.append(titleCol)

            rootElement.append(row)

            // Rendering subdocuments:
            createTable(rootElement, doc.subDocuments, doc.id, level+1)
        })
    }
    function refresh() {
        $.ajax("${baseURL}/rest/report?report=planned")
            .done(function( docs ) {
                var docsTable = $('<table>').attr('id', 'reportTable')
                createTable(docsTable, docs, null, 0)
                $('#reportTable').replaceWith(docsTable)
                renderTree()
            });
        // window.location.replace(window.location.pathname + "?refresh")
        return false
    }
    $(function() {
        console.log( "document loaded!" )
        renderTree()
    })
</script>
</head>
<!-- -------------------------------------------------------------- -->
<body>

<div class="menu">
|
<#list reportLinks as reportLink>
<a href="${reportLink.reportUrl}">${reportLink.reportName}</a>
|
</#list>

<span class="menu_right">
<a href="#" onclick="refresh()" class="refresh">Refresh</a>
|
${.now?date}
</span>
</div>
<hr>

<!-- #import reportPath as report -->
<@.vars[format] docs=docs/>

</body>
</html>