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
        <td class="date">${doc.updated?date}</td>
        <td class="status">${doc.status}</td>
        <td class="source">${doc.sourceName}</td>
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
<style>
.menu {
    background: cyan;
}
.date {
    color:gray;
}
.title {
    color:gray;
}
.title_leaf {
    color:red;
}
.source {
}
.current_date {
  float:right;
}
</style>
<body>

<div class="menu">
|
<#list reportTypes as reportType>
<a href="${baseURL}?report=${reportType}" >${reportType?capitalize}</a>
|
</#list>
<span class="current_date">
${.now?date}
</span>
</div>
<hr>

<!-- #import reportPath as report -->
<@.vars[format] docs=docs/>

</body>
</html>