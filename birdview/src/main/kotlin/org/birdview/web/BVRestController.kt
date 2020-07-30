package org.birdview.web

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController("/rest")
class BVRestController {
    class ReportLink(val reportUrl:String, val reportName:String)
    @RequestMapping("/")
    fun index(): ReportLink? {
        return ReportLink("Greetings from Spring Boot!", "Report name")
    }

}