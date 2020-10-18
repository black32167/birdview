<#macro panel title>
  <div class="panel">
  <div class="panel-header">${title}</div>
  <div class="panel-body">
    <div class="panel-content">
    <#nested>
    </div>
  </div>
  </div>
</#macro>