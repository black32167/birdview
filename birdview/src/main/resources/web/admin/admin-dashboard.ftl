<#include '/links.ftl'/>
<#include '/include/panel.ftl'/>
<!DOCTYPE html>
<html>

<head>
<link rel="stylesheet" href="/css/bv.css"></link>
<script src="js/jquery-3.5.1.min.js"></script>
<script src="js/report.js"></script>
<script src="js/admin-user.js"></script>
<script>
var csrf_token_parameter_name="${_csrf.parameterName}"
var csrf_token = "${_csrf.token}"
</script>
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
            <th>Enabled</th>
        </tr>
        <#list users as user>
                <tr>
                <td>
                    ${user.name}
                </td>
                <td>
                    <#if user.enabled>
                        <#assign checkedHint="checked">
                    <#else>
                        <#assign checkedHint="">
                    </#if>
                    <input type="checkbox" user="${user.name}" name="enabled" ${checkedHint}
                        onchange="updateUserState(this.getAttribute('user'), this.checked)">
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