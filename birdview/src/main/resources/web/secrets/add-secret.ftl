<#macro addForm sourceType>
<form id="${sourceType}-form" source="${sourceType}" action="<@add_source_post_link sourceType />" method="POST">
    <input type="hidden" id="csrf_token" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    <table>
    <#include "include/create/create-secret-${sourceType}.ftl">
    </table>
    <div class="buttons">
        <input type="submit" value="Create">
    </div>
    <div id="token-help-${sourceType}" class="token-help">
        <img src="/img/get-token-${sourceType}.png"/>
    </div>
</form>
</#macro>
<#include "/links.ftl">
<!DOCTYPE html>
<html>

<head>
<link rel="stylesheet" href="/css/bv.css"></link>
<script src="/js/jquery-3.5.1.min.js"></script>
<script>
function showHelp(sourceType) {
  var divId = 'token-help-' + sourceType
  $('#' + divId).show()
}
function hideAll() {
  $('form').hide()
}
function showForm(sourceType) {
  hideAll()
  $('#' + sourceType + '-form').show()
}
$(function(){
    hideAll()
    $('form').each( (i, form) => {
        console.log($(form).attr('id'))
        var source = $(form).attr('source')
        var link = $(form).find('.helpline')
        link.on('mouseenter', (e) => {
            var image = $('#token-help-' + source)
            image.parent().css({position: 'relative'});
            image.css({top:e.clientY+10, left:e.clientX+10})

            image.show()
        })
        link.on('mouseleave', (e) => {
            var image = $('#token-help-' + source)
            image.hide()
        })
    })
})
</script>
</head>

<body>
    <div class="menu">
       <@admin_link />
    </div>

    <div>
    <div class="center">
        <div class="buttons">
            <select onchange="showForm(this.value)">
                <option value="">Select source type...</option>
            <#list sourceTypes as sourceType>
                <option value="${sourceType}">${sourceType}</option>
            </#list>
            </select>
        </div>

        <@addForm "jira">
        </@addForm>

        <@addForm "confluence">
        </@addForm>

        <@addForm "gdrive">
        </@addForm>

        <@addForm "slack">
        </@addForm>

        <@addForm "github">
        </@addForm>

        <@addForm "trello">
        </@addForm>
    </div>
    <div>
</body>
</html>