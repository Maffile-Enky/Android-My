package com.toolbox.services

import com.toolbox.models.*
import com.toolbox.repository.VersionRepository

class VersionService {

    fun checkForUpdate(clientVersionCode: Int): VersionCheckResponse {
        val latest = VersionRepository.getLatest()
        return if (latest != null && latest.versionCode > clientVersionCode) {
            VersionCheckResponse(hasUpdate = true, latestVersion = latest)
        } else {
            VersionCheckResponse(hasUpdate = false)
        }
    }

    fun getLatestVersion(): VersionInfo? = VersionRepository.getLatest()

    fun saveVersion(request: VersionSaveRequest): VersionInfo = VersionRepository.save(request)

    fun getAllVersions(): List<VersionInfo> = VersionRepository.getAll()

    fun deleteVersion(id: Long): Boolean = VersionRepository.delete(id)
}
