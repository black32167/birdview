<input type="hidden" name="authCodeUrl" value="https://accounts.google.com/o/oauth2/v2/auth?"/>
<input type="hidden" name="tokenExchangeUrl" value="https://oauth2.googleapis.com/token"/>
<input type="hidden" name="scope" value="https://www.googleapis.com/auth/drive"/>
<input type="hidden" name="baseUrl" value="https://www.googleapis.com/drive/v3"/>

<tr>
    <td class="sign">Source alias:</td>
    <td><input type="text" name="sourceName" value="gdrive"></td>
</tr>
<tr>
    <td class="sign">Email:</td>
    <td><input type="text" name="user"></td>
</tr>
<tr>
    <td class="sign">Client Id:</td>
    <td><input type="text" name="key"></td>
</tr>
<tr>
    <td class="sign">Client Secret:</td>
    <td>
        <input type="text" name="secret">
        <div class="helpline" >
        <a href="https://console.developers.google.com/projectcreate">Register</a> and <a href="https://console.developers.google.com/">Generate</a>
        </div>
    </td>
</tr>