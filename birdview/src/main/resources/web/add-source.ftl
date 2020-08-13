<!DOCTYPE html>
<html>

<head>
<link rel="stylesheet" href="/css/bv.css"></link>
<script src="/js/jquery-3.5.1.min.js"></script>
<script>
function hideAll() {
  $('form').hide()
}
function showForm(sourceType) {
  hideAll()
  $('#' + sourceType + '-form').show()
}
$(function(){
    hideAll()
})
</script>
</head>

<body>
    <div class="menu">
       <a href="/settings">< Settings</a>
    </div>

    <div>
    <div class="center">
        <select onchange="showForm(this.value)">
            <option value="">Select source type...</option>
        <#list sourceTypes as sourceType>
            <option value="${sourceType}">${sourceType}</option>
        </#list>
        </select>

        <form id="jira-form" action="/settings/jira/add-source" method="POST">
            <label for="sourceName">Source alias:</label>
            <input type="text" name="sourceName" value="jira">
            <br>
            <label for="baseUrl">Base URL:</label>
            <input type="text" name="baseUrl">
            <br>
            <label for="key">Key:</label>
            <input type="text" name="key">
            <br>
            <label for="secret">Token:</label>
            <input type="text" name="secret">
            <br>
            <input type="submit">
        </form>

        <form id="gdrive-form" action="/settings/gdrive/add-source" method="POST">
            <label for="sourceName">Source alias:</label>
            <input type="text" name="sourceName" value="gdrive">
            <br>
            <label for="baseUrl">Base URL:</label>
            <input type="text" name="baseUrl">
            <br>
            <label for="key">Client Id:</label>
            <input type="text" name="key">
            <br>
            <label for="secret">Client Secret:</label>
            <input type="text" name="secret">
            <br>
            <input type="submit">
        </form>

        <form id="github-form" action="/settings/github/add-source" method="POST">
            <label for="sourceName">Source alias:</label>
            <input type="text" name="sourceName" value="github">
            <br>
            <label for="key">User:</label>
            <input type="text" name="key">
            <br>
            <label for="secret">Token:</label>
            <input type="text" name="secret">
            <br>
            <input type="submit">
        </form>

        <form id="trello-form" action="/settings/trello/add-source" method="POST">
            <label for="sourceName">Source alias:</label>
            <input type="text" name="sourceName" value="trello">
            <br>
            <label for="key">Key:</label>
            <input type="text" name="key">
            <br>
            <label for="secret">Token:</label>
            <input type="text" name="secret">
            <br>
            <input type="submit">
        </form>
    </div>
    <div>
</body>
</html>