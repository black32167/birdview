package org.birdview.storage.sql.model

import javax.persistence.Entity

import javax.persistence.Id


@Entity
open class BVJpaDocument (
    @Id
    open var id: String,

    open var data: String
)