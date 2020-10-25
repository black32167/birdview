package org.birdview.storage

@FunctionalInterface
interface BVSourceUserNameResolver {
    fun resolve(bvUser:String, sourceName: String): String?
}