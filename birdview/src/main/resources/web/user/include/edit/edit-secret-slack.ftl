<input type="hidden" name="baseUrl" value="${baseUrl}" />

<tr>
    <td class="sign">Email:</td>
    <td><input type="text" name="email" value="${sourceUserName}"></td>
</tr>
<tr>
    <td class="sign">Client Id:</td>
    <td><input type="text" name="clientId" value="${secret.clientId}" ></td>
</tr>
<tr>
    <td class="sign">Client Secret:</td>
    <td>
        <input type="text" name="clientSecret" value="${secret.clientSecret}">
        <a class="helpline" href="${authorizationUrl}">Authorize...</a>
    </td>
</tr>