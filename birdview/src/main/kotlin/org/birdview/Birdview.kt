package org.birdview

import org.springframework.boot.runApplication


fun main(vararg args:String) {
   // AnnotationConfigApplicationContext(BirdviewConfiguration::class.java).use {ctx->

    runApplication<BirdviewConfiguration>(*args) {

    }
    readLine()
    0
   // }
}
