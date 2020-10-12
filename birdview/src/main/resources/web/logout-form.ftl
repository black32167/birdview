<form style="display:inline" action="/logout" method="POST">
    <input type="hidden" id="csrf_token" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    <input type="submit" class="link-button" value="Logout">
</form>