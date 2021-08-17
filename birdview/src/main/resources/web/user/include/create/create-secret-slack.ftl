<input type="hidden" name="authCodeUrl" value="https://slack.com/oauth/v2/authorize?user_scope=identity.basic&"/>
<input type="hidden" name="tokenExchangeUrl" value="https://slack.com/api/oauth.v2.access"/>
<input type="hidden" name="scope" value="channels:history,channels:read"/>
<input type="hidden" name="baseUrl" value="https://slack.com/api"/>

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
        </div>
    </td>
</tr>