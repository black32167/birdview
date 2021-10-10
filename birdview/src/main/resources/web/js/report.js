
function applyTree(treeElement) {
    $(treeElement)
        .treetable({
            expandable: true,
            initialState: "collapsed",
            expanderTemplate: "<a class='expander' onclick='onExpand(this)' href='#'>&nbsp;</a>",
        })
}

// This function is responsible for complete branch expansion
function onExpand(element) {
    var el = element
    while(el != undefined && $(el).attr('data-tt-id') == undefined) {
      el = $(el).parent()
    }
    if (el != undefined && $(el).attr('level') == 1) {
        var idPrefix = $(el).attr('data-tt-id')
        $('.tree-node').each(function() {
            if ($(this).attr('data-tt-id').startsWith(idPrefix+'/')) {
                console.log("Expanding " + $(this).attr('data-tt-id'))
                $('#reportTable').treetable("expandNode", $(this).attr('data-tt-id'))
            }
        })
    }
}

function renderTable(rootElement, nodes, parentId, level) {
    nodes.forEach( node => {
        var doc = node.doc
        var nodeId = doc.internalId
        if (parentId !== null) {
            nodeId = parentId+"/"+nodeId
        }
        var row = $('<tr>').attr('data-tt-id', nodeId).attr('level', level)
        row.addClass('tree-node')
        if(parentId) {
            row.attr('data-tt-parent-id', parentId)
        }

        // Title
        var alternativeLinks = [doc].concat(node.alternativeDocs)
            .map(d=>`<a href="${d.httpUrl}">${d.key}</a>`)
        var title = '<div class="title-content">' + doc.title + "</div>"
        if (doc.key !== "") {
          title += ` <div class="title-links">(${alternativeLinks.join(",")}</div><div class="title-links">)</div>`
        }
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
        renderTable(rootElement, node.subNodes, nodeId, level+1)
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
        if (doc.httpUrl != '') {
            li.html(`${doc.title} (<a href="${doc.httpUrl}">${doc.key}</a>)`)
        } else {
            li.html(doc.title)
        }
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

            $('.tree-node').each(function() {
                var level = $(this).attr('level')
                if (level < 1) {
                    var nodeId = $(this).attr('data-tt-id')
                    $(tableContainer).treetable("expandNode", nodeId)
                }
            })
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
        `&sourceName=${source}` +
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