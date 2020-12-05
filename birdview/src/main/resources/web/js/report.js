
function applyTree(treeElement) {
    $(treeElement).treetable({ expandable: true })
}
function renderTable(rootElement, nodes, parentId, level) {
    nodes.forEach( node => {
        var doc = node.doc
        var row = $('<tr>').attr('data-tt-id', doc.internalId)
        if(parentId) {
            row.attr('data-tt-parent-id', parentId)
        }

        // Title
        var alternativeLinks = [doc].concat(node.alternativeDocs)
            .map(d=>`<a href="${d.httpUrl}">${d.key}</a>`)
        var title = `${doc.title} (${alternativeLinks.join(",")})`
        var titleCol = $('<td>')
            .html(title)
            .addClass(node.subNodes.length == 0 ? 'title_leaf' : 'title')
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
        renderTable(rootElement, node.subNodes, doc.internalId, level+1)
    })
}
function renderList(rootElement, nodes) {
    if(nodes.length == 0) {
        return
    }
    var ul = $('<ul>')
    nodes.forEach(node => {
        var doc = node.doc
        var li = $('<li>')
            .html(`${doc.title} (<a href="${doc.httpUrl}">${doc.key}</a>)`)
        ul.append(li)

        // Rendering subdocuments:
        renderList(ul, node.subNodes)
    })
    rootElement.append(ul)
}

function renderReport(nodes) {
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

            renderTable(tableContainer, nodes, null, 0)
            applyTree(tableContainer)
            reportContainer.append(tableContainer)
            break
        case "LIST":
            renderList(reportContainer, nodes)
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

function updateStatus() {
    $.ajax({
        url:`${baseURL}/rest/documents/status`,
        success: function( userLog ) {
            if (userLog.length > 0) {
                $(".log").show()

                var logHtml = userLog
                    .map(e=>formatLogEntry(e))
                    .join("")

                $(".log").html(logHtml)
            } else {
                $(".log").hide()
            }
        }
    })
}

function formatLogEntry(entry) {
  return `<div class="log-entry">
           <div class='log-time'>${entry.timestamp}</div>
           <div class='log-message'>${entry.message}</div>
        </div>`
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
        .done(function( nodes ) {
             renderReport(nodes)
         })
        .always(function() {
            showOverlay(false)
        })
    // window.location.replace(window.location.pathname + "?refresh")
    return false
}