
function applyTree(treeElement) {
    $(treeElement).treetable({ expandable: true })
}
function renderTable(rootElement, docs, parentId, level) {
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

        // Last updater
        var lastUpdaterCol = $('<td>').text(doc.lastUpdater)
        row.append(lastUpdaterCol)

        rootElement.append(row)

        // Rendering subdocuments:
        renderTable(rootElement, doc.subDocuments, doc.id, level+1)
    })
}
function renderList(rootElement, docs) {
    if(docs.length == 0) {
        return
    }
    var ul = $('<ul>')
    docs.forEach(doc => {
        var li = $('<li>')
            .html(`${doc.title} (<a href="${doc.httpUrl}">${doc.key}</a>)`)
        ul.append(li)

        // Rendering subdocuments:
        renderList(ul, doc.subDocuments)
    })
    rootElement.append(ul)
}

function renderReport(docs) {
    var representationType = $('#representation').val()

    var reportContainer = $('#reportContainer')
    reportContainer.empty()
    switch (representationType) {
        case "TREE":
            var tableContainer = $('<table class="list" id="reportTable">')
            var headerRow = $('<tr>')
                    .append('<th>Title</th>')
                    .append('<th>Source</th>')
                    .append('<th>Status</th>')
                    .append('<th>Updated</th>')
                    .append('<th>Updater</th>')

            tableContainer.append(headerRow)

            renderTable(tableContainer, docs, null, 0)
            applyTree(tableContainer)
            reportContainer.append(tableContainer)
            break
        case "LIST":
            renderList(reportContainer, docs)
            //reportContainer.append(listContainer)
            break
    }
}
function showOverlay(show) {
  if(show) {
    $("#overlay").show()
  } else {
    $("#overlay").hide()
  }
}
function authenticate(url) {
  if(url != "") {
    window.location.replace(url)
  }
}
function reindex() {
    showOverlay(true)
    $.ajax(`${baseURL}/rest/documents/reindex`)
    .always(function(){
        showOverlay(false)
        refresh()
    })

    //window.location.replace(window.location.pathname + "?refresh")
    return false
}
function refresh() {
    var reportType = $('#reportType').val()
    var daysBack = $('#daysBack').val()
    var userRole = $('#userRole').val()
    var user = $('#user').val()
    var source = $('#source').val()
    var representationType = $('#representation').val()

    showOverlay(true)
    $.ajax(`${baseURL}/rest/documents` +
        `?reportType=${reportType}` +
        `&daysBack=${daysBack}` +
        `&user=${user}` +
        `&sourceType=${source}` +
        `&representationType=${representationType}` +
        `&userRole=${userRole}`)
        .done(function( docs ) {
             renderReport(docs)
         })
        .always(function() {
            showOverlay(false)
        })
    // window.location.replace(window.location.pathname + "?refresh")
    return false
}