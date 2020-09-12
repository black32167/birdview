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
        val modifiedByMe: Boolean,
        var modifiedByMeTime: String?,
        var lastModifyingUser: GDriveUser?,
        var sharingUser: GDriveUser?
)

class GDriveFilePermissions (
        val type: String,
        val emailAddress: String,
        val domain: String,
        val role: String,
        val view: String,
        val allowFileDiscovery: Boolean,
        val displayName: String,
        val photoLink: String,
        val expirationTime: String,
        val teamDrivePermissionDetails: List<TeamDrivePermissionDetails>,
        val permissionDetails: List<PermissionDetails>,
        val deleted: Boolean
)
class TeamDrivePermissionDetails (
        val teamDrivePermissionType: String,
        val role: String,
        val inheritedFrom: String,
        val inherited: Boolean
)
class PermissionDetails (
    val permissionType: String,
    val role: String,
    val inheritedFrom: String,
    val inherited: Boolean
)

class GDriveUser(
        var displayName: String?,
        var emailAddress: String?
)