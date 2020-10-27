<#macro admin_link>
    <a href="/admin">< Admin Panel</a>
</#macro>
<#macro add_secret_link>
    <a href="/admin/secrets/add-secret">Add secret</a>
</#macro>
<#macro add_secret_post_link sourceType>/admin/secrets/${sourceType}/add-secret</#macro>
<#macro update_secret_post_link sourceType>/admin/secrets/${sourceType}/update-secret</#macro>

<#macro edit_secret_link source>
   <a href="/admin/secrets/${source.type?lower_case}/edit-secret?sourceName=${source.name}">${source.name}</a>
</#macro>
<#macro delete_secret_link source>
   <a href="/admin/secrets/delete?sourceName=${source.name}">X</a>
</#macro>
<#macro delete_user userName>
    <a href="/admin/user/delete?user=${userName}">X</a>
</#macro>

<#macro logout_link>
    <form style="display:inline" action="/logout" method="POST">
        <input type="hidden" id="csrf_token" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <input type="submit" class="link-button" value="Logout">
    </form>
</#macro>