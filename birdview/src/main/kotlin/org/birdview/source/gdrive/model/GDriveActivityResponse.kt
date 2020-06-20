package org.birdview.source.gdrive.model

class GDriveActivityResponse (
        val activities: List<GDriveActivityItem>
)

class GDriveActivityItem(
        val primaryActionDetail: GDriveActivityActionDetail,
        val actions: List<GDriveActivityAction>,
        val targets: List<GDriveActivityTarget>
)

class GDriveActivityAction (
        val target: GDriveActivityTarget?
)

class GDriveActivityTarget(
        val driveItem: GDriveItem
)

class GDriveItem (
        val name:String,
        val title: String
)

class GDriveActivityActionDetail(
        val edit: GDriveEditDetails?
)

class GDriveEditDetails