package org.birdview.migration

interface MigrationTask {
    fun commence()
    fun getName(): String
}