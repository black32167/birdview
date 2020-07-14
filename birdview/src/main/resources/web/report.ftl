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

<#macro long_table docs level>
<#list docs as doc>
    <tr>
        <td class="source">${doc.sourceName}</td>
        <td class="status">${doc.status}</td>
        <td class="date">${doc.updated?date}</td>
        <#if doc.subDocuments?has_content>
            <td class="title">
        <#else>
            <td class="title_leaf">
        </#if>
        <#list 0..<level*4 as i>&nbsp;</#list>
        ${doc.title} (<a href="${doc.httpUrl}">${doc.key}</a>)</td>
    </tr>
    <@long_table doc.subDocuments level+1/>
</#list>
</#macro>

<#macro long docs>
<#if docs?has_content>
<table>
<@long_table docs 0/>
</table>
</#if>
</#macro>


<html>
<!-- -------------------------------------------------------------- -->
<style>
.menu {
    background: cyan;
    height: 1.5em;
}
.date {
    color: gray;
    text-align: right;
}
.title {
    color: gray;
}
.title_leaf {
    color: red;
}
.source {
}
.menu_right {
  float: right;
}
</style>

<script>
<!--
    function refresh() {
        window.location.replace(window.location.pathname + "?refresh")
        return false
    }
-->
</script>

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