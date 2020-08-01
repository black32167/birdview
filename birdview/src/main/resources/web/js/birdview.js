
function renderTree() {
    $("#reportTable").treetable({ expandable: true })
}
function createTable(rootElement, docs, parentId, level) {
    docs.forEach( doc => {
        var row = $('<tr>').attr('data-tt-id', doc.id)
        if(parent) {
            row.attr('data-tt-parent-id', parentId)
        }

        // Title
        var titleCol = $('<td>')
            .html(`${doc.title} (<a href="${doc.httpUrl}">${doc.key}</a>)`)
            .addClass(doc.subDocuments.length == 0 ? 'title_leaf' : 'title')
        row.append(titleCol)

        // Source
        var sourceCol = $('<td>').text(doc.sourceName)
        row.append(sourceCol)

        // Status
        var statusCol = $('<td>').text(doc.status)
        row.append(statusCol)

        // Updated date
        var updatedCol = $('<td>').text(doc.updated)
        row.append(updatedCol)

        rootElement.append(row)

        // Rendering subdocuments:
        createTable(rootElement, doc.subDocuments, doc.id, level+1)
    })
}
function update() {
    window.location.replace(window.location.pathname + "?refresh")
    return false
}
function refresh() {
    var reportType = $('#reportType').val()
    var daysBack = $('#daysBack').val()
    var userRole = $('#userRole').val()
    var user = $('#user').val()

    $.ajax(`${baseURL}/rest/documents` +
        `?reportType=${reportType}` +
        `&daysBack=${daysBack}` +
        `&user=${user}` +
        `&userRole=${userRole}`)
        .done(function( docs ) {
            var docsTable = $('<table>').attr('id', 'reportTable')
            createTable(docsTable, docs, null, 0)
            $('#reportTable').replaceWith(docsTable)
            renderTree()
        });
    // window.location.replace(window.location.pathname + "?refresh")
    return false
}