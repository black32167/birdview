<!DOCTYPE html>
<html>

<head>
<link rel="stylesheet" href="/css/bv.css"></link>
<script src="/js/jquery-3.5.1.min.js"></script>

</head>

<body>
    <div class="menu">
       <a href="/settings">< Settings</a>
    </div>

    <div>
    <div class="center">
        <form id="jira-form" action="/settings/github/update-source" method="POST">
            <input type="hidden" name="sourceName" value="${source.sourceName}">
            Source alias:${source.sourceName}
            <br>
            <label for="key">Client Id:</label>
            <input type="text" name="key" value="${source.key}" >
            <br>
            <label for="secret">Client Secret:</label>
            <input type="text" name="secret" value="${source.secret}" >
            <br>
            <input type="submit">
        </form>
    </div>
    <div>
</body>
</html>