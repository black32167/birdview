<!DOCTYPE html>
<html>

<head>
<link rel="stylesheet" href="/css/bv.css"></link>
</head>
<body>
    <div class="menu">
       <a href="/">< Home</a>
    </div>

    <#if message??>
        <div>${message}</div>
    </#if>
    <div class="center">
        <table style="border:0px; padding:0px">
        <tr><td>
            <div>
                <form class="center" action="/user/settings/profile" method="POST" style="width:100%">
                    <input type="hidden" id="csrf_token" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    <table class="settings" style="width:30em; margin:0px;">
                        <tr>
                            <td class="sign" style="width:11em;">Timezone:</td>
                            <td>
                              <select name="zoneId" style="width:15em;">
                                  <#list availableTimeZoneIds as availableTimeZoneId>
                                    <#if user.zoneId == availableTimeZoneId>
                                      <option value="${availableTimeZoneId}" selected="selected">${availableTimeZoneId}</option>
                                    <#else>
                                      <option value="${availableTimeZoneId}">${availableTimeZoneId}</option>
                                    </#if>
                                  </#list>
                              </select>
                              <input type="submit" value="Update">
                            </td>
                        </tr>
                    </table>
                </form>
            </div>
        </td></tr>
        <tr><td>
            <div>
                <form class="center" action="/user/settings/password" method="POST" style="width:100%">
                    <input type="hidden" id="csrf_token" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    <table class="settings" style="width:30em; margin:0px;">
                        <tr>
                            <td class="sign" style="width:11em;">Change Password:</td>
                            <td>
                              <input name="newPassword" style="width:15em;"></input>
                              <input type="submit" value="Update">
                            </td>
                        </tr>
                    </table>
                </form>
            </div>
        </td></tr>
        </table>

        <div style="float:left">
            <fieldset class="settings">
                <legend>Sources</legend>
                <table>
                    <!-- tr>
                        <th>Source Name</th>
                        <th></th>
                    </tr -->
                    <#list sourceNames as sourceName>
                    <tr>
                    <td>
                        <a href="/user/source/${sourceName?url}">${sourceName}</a>
                    </td>
                    <td>
                        <a href="/user/source/${sourceName?url}/delete">X</a>
                    </td>
                    </tr>
                    </#list>
                </table>
                <div class="form settings">
                    <a href="/user/source">Add source</a>
                </div>
            </fieldset>
        </div>
        <div style="float:left">
            <fieldset class="settings">
                <legend>Workgroups</legend>

                <table>
                    <#list user.workGroups as workGroup>
                    <tr>
                        <td>${workGroup}</td>
                        <td><a href="/user/group/${workGroup?url}/delete">X</a></td>
                    </tr>
                    </#list>
                </table>
                <form class="center" action="/user/group" method="POST">
                    <input type="hidden" id="csrf_token" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    <div class="form settings">
                        <input type="text" name="workGroup">
                        <input type="submit" value="Add">
                    </div>
                </form>
            </fieldset>
        </div>
    </div>
</body>
</html>