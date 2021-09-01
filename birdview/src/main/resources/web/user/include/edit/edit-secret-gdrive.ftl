<input type="hidden" name="baseUrl" value="${baseUrl}" />

<tr>
    <td class="sign">Email:</td>
    <td><input type="text" name="filter" value="${filter}"></td>
</tr>
<tr>
    <td class="sign">Client Id:</td>
    <td><input type="text" name="principal" value="${principal}" ></td>
</tr>
<tr>
    <td class="sign">Client Secret:</td>
    <td>
        <input type="text" name="clientSecret" value="${secretToken}">
        <a class="helpline" href="${authorizationUrl}">Authorize...</a>
    </td>
</tr>