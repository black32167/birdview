<#macro addForm sourceType>
<form id="${sourceType}-form" source="${sourceType}" action="/settings/${sourceType}/add-source" method="POST">
    <table>
    <#nested>
    </table>
    <div class="buttons">
        <input type="submit" value="Create">
    </div>
    <div id="token-help-${sourceType}" class="token-help">
        <img src="/img/get-token-${sourceType}.png"/>
    </div>
</form>

</#macro>

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
       <a href="/settings">< Settings</a>
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
                <tr>
                    <td class="sign">Source alias:</td>
                    <td><input type="text" name="sourceName" value="jira"></td>
                </tr>
                <tr>
                    <td class="sign">Base URL:</td>
                    <td><input type="text" name="baseUrl"></td>
                </tr>
                <tr>
                    <td class="sign">Email:</td>
                    <td><input type="text" name="user"></td>
                </tr>
                <tr>
                    <td class="sign">Token:</td>
                    <td>
                        <input type="text" name="secret"><br>
                        <a class="helpline" href="https://id.atlassian.com/manage/api-tokens">Generate token...</a>
                    </td>
                </tr>
        </@addForm>

        <@addForm "gdrive">
            <tr>
                <td class="sign">Source alias:</td>
                <td><input type="text" name="sourceName" value="gdrive"></td>
            </tr>
            <tr>
                <td class="sign">Email:</td>
                <td><input type="text" name="user"></td>
            </tr>
            <tr>
                <td class="sign">Client Id:</td>
                <td><input type="text" name="key"></td>
            </tr>
            <tr>
                <td class="sign">Client Secret:</td>
                <td>
                    <input type="text" name="secret">
                    <div class="helpline" >
                    <a href="https://console.developers.google.com/projectcreate">Register</a> and <a href="https://console.developers.google.com/">Generate</a>
                    </div>
                </td>
            </tr>
        </@addForm>

        <@addForm "slack">
            <tr>
                <td class="sign">Source alias:</td>
                <td><input type="text" name="sourceName" value="slack"></td>
            </tr>
            <tr>
                <td class="sign">UserId:</td>
                <td><input type="text" name="user"></td>
            </tr>
            <tr>
                <td class="sign">Client Id:</td>
                <td><input type="text" name="clientId"></td>
            </tr>
            <tr>
                <td class="sign">Client Secret:</td>
                <td>
                    <input type="text" name="clientSecret">
                    <div class="helpline" >
                    <!--a href="https://console.developers.google.com/projectcreate">Register</a> and <a href="https://console.developers.google.com/">Generate</a-->
                    </div>
                </td>
            </tr>
        </@addForm>

        <@addForm "github">
            <tr>
                <td class="sign">Source alias:</td>
                <td><input type="text" name="sourceName" value="github"></td>
            </tr><tr>
                <td class="sign">User:</td>
                <td><input type="text" name="user"></td>
            </tr><tr>
                <td class="sign">Token:</td>
                <td>
                    <input type="text" name="secret"><br>
                    <a class="helpline" href="https://github.com/settings/tokens">Generate token...</a>
                </td>
            </tr>
        </@addForm>

        <@addForm "trello">
            <tr>
                <td class="sign">Source alias:</td>
                <td><input type="text" name="sourceName" value="trello"></td>
            </tr>
            <tr>
                <td class="sign">User:</td>
                <td><input type="text" name="user"></td>
            </tr>
            <tr>
                <td class="sign">Key:</td>
                <td><input type="text" name="key"></td>
            </tr>
            <tr>
                <td class="sign">Token:</td>
                <td>
                    <input type="text" name="secret"><br>
                    <a class="helpline" href="https://trello.com/app-key">Generate token...</a>
                </td>
            </tr>
        </@addForm>
    </div>
    <div>
</body>
</html>