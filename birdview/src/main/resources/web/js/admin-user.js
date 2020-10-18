function updateUserState(userName, enabled) {
    console.log(userName + " = " + enabled)
    showOverlay(true)
    var data = {"name":userName, "enabled": enabled}
    data[csrf_token_parameter_name] = csrf_token
    $.post(`/admin/user/update`, data)
    .always(function(){
      showOverlay(false)
    })
}