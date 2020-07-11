<#macro list_docs docs>
<#if docs?has_content>
<ul>
<#list docs as doc>
    <li>
        <td>${doc.title} (<a href="${doc.httpUrl}">${doc.key}</a>)</td>
    </li>
    <@list_docs doc.subDocuments />
</#list>
</ul>
</#if>
</#macro>

<#macro brief docs>
Brief:
<@list_docs docs/>
</#macro>

<#macro long docs>
Long:
<@list_docs docs/>
</#macro>

<html>
<style>
.menu {
    background: cyan;
}
</style>
<body>

<div class="menu">
|
<#list reportTypes as reportType>
<a href="${baseURL}?report=${reportType}" >${reportType?capitalize}</a>
|
</#list>
</div>
<hr>

<!-- #import reportPath as report -->
<@.vars[format] docs=docs/>

</body>
</html>