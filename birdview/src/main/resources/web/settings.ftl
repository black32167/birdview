<!DOCTYPE html>
<html>

<head>
<link rel="stylesheet" href="css/bv.css"></link>
<script src="js/jquery-3.5.1.min.js"></script>
</head>

<body>
    <div class="menu">
       <a href="/">< Home</a>
    </div>

    <div>
    <div class="center">
    <table>
    <#list sources as source>
        <tr>
        <td>
            ${source.name}
        </td>
        <td>
            <#if source.authenticated>
                Yes
            <#else>
                <#if source.type == "gdrive">
                    <a href="${source.authUrl}">Authorize...</a>
                <#else>
                    No
                </#if>
            </#if>
        </td>
        </tr>
    </#list>
    </table>
    </div>
    </div>
</body>
</html>