<#include '/links.ftl'/>
<#include '/include/panel.ftl'/>
<!DOCTYPE html>
<html>

<head>
<link rel="stylesheet" href="/css/bv.css"></link>
<script src="js/jquery-3.5.1.min.js"></script>
</head>
<body>
    <div class="menu">
        <@add_secret_link />

        <div class="menu_right">
            <@logout_link />
        </div>
    </div>

    <div>
    <div class="center">
    <@panel "Secrets">
    <table>
    <tr>
        <th>Name</th>
        <th>Type</th>
        <th></th>
    </tr>
    <#list sources as source>
        <tr>
        <td>
            <@edit_secret_link source />
        </td>
        <td>
            ${source.type}
        </td>
        <td>
            <@delete_secret_link source />
        </td>
        </tr>
    </#list>
    </table>
    </@panel>
    <div style="float:right">
    <@panel "Users">
        <table>
        <tr>
            <th>User</th>
            <th></th>
        </tr>
        <#list userNames as userName>
                <tr>
                <td>
                    ${userName}
                </td>
                <td>
                    X
                </td>
                </tr>
            </#list>
        </table>
        </@panel>
        </div>
    </div>
    </div>
</body>
</html>