package org.birdview.source.gdrive.model

class GDriveFileListResponse(
        val files: List<GDriveFile>,
        val nextPageToken: String?
)

class GDriveFile(
        val id: String,
        val name: String,
        val modifiedTime: String, //"2019-09-02T23:41:13.684Z"
        val webViewLink: String,
        var owners: List<GDriveUser>,
        var lastModifyingUser: GDriveUser?,
        var sharingUser: GDriveUser?
)

class GDriveUser(
        var displayName: String?,
        var emailAddress: String?
)