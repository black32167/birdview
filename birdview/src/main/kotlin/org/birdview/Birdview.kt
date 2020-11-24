package org.birdview

import org.springframework.boot.runApplication


fun main(vararg args:String) {
    runApplication<BirdviewConfiguration>(*args)
    readLine()
}
