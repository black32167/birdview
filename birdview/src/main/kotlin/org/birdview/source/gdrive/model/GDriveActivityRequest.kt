package org.birdview.source.gdrive.model

import org.birdview.request.TasksRequest

class GDriveActivityRequest (
    val filter: String? = null
) {
    companion object {
        fun from(taskRequest: TasksRequest) = GDriveActivityRequest(
                filter = taskRequest.user ?: "owner:me"
        )
    }
}