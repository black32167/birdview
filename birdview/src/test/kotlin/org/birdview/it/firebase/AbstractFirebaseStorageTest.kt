package org.birdview.it.firebase

import org.birdview.BVProfiles
import org.birdview.storage.firebase.BVFirebaseClientProvider
import org.birdview.utils.TestConfig
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import javax.inject.Inject

@Import(TestConfig::class)
@SpringBootTest("spring.main.allow-bean-definition-overriding=true")
@ActiveProfiles(profiles = [BVProfiles.CLOUD])
@RunWith(SpringRunner::class)
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
