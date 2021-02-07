<!DOCTYPE html>
<html>

<head>
<link rel="stylesheet" href="/css/bv.css"></link>
</head>
<body>
    <div class="menu">
       <a href="/">< Home</a>
       |
       <a href="/user/settings/source/add">Add source</a>
    </div>

    <div class="center">
    <form class="center" style="float:left" action="/user/settings" method="POST">
        <input type="hidden" id="csrf_token" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <table class="settings">
            <tr>
                <td class="sign">Timezone:</td>
                <td>
                  <select name="zoneId">
                      <#list availableTimeZoneIds as availableTimeZoneId>
                        <#if profileForm.zoneId == availableTimeZoneId>
                          <option value="${availableTimeZoneId}" selected="selected">${availableTimeZoneId}</option>
                        <#else>
                          <option value="${availableTimeZoneId}">${availableTimeZoneId}</option>
                        </#if>
                      </#list>
                  </select>
                </td>
            </tr>
        </table>
        <div class="buttons">
            <input type="submit" value="Update">
        </div>
    </form>
    <table class="settings" style="float:right">
    <tr>
        <th>Source Name</th>
        <th></th>
    </tr>
    <#list sourceNames as sourceName>
        <tr>
        <td>
            <a href="/user/settings/source/${sourceName}/edit">${sourceName}</a>
        </td>
        <td>
            <a href="/user/settings/source/${sourceName}/delete">X</a>
        </td>
        </tr>
    </#list>
    </table>
    </div>
</body>
</html>