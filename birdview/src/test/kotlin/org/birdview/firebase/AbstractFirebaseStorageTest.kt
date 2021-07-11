package org.birdview.firebase

import org.birdview.storage.firebase.BVFirebaseClientProvider
import org.birdview.utils.TestConfig
import org.junit.Before
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import javax.inject.Inject

@Import(TestConfig::class)
@SpringBootTest("spring.main.allow-bean-definition-overriding=true")
abstract class AbstractFirebaseStorageTest {
    @Inject
    private lateinit var clientProvider: BVFirebaseClientProvider

    @Before
    fun initTest() {
        val client = clientProvider.getClient()
        for (coll in client.listCollections()) {
            for(doc in coll.listDocuments()) {
                doc.delete()
            }
        }
    }
}