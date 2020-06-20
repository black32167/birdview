package org.birdview.source.gdrive.model

class GDriveFileListResponse(
        val files: List<GDriveFile>
)

class GDriveFile(
        val id: String,
        val name: String,
        val modifiedTime: String, //"2019-09-02T23:41:13.684Z"
        val webViewLink: String
)
