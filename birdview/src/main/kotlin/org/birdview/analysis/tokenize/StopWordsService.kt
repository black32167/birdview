package org.birdview.analysis.tokenize

import java.util.stream.Collectors.toSet
import javax.inject.Named

@Named
class StopWordsService {
    companion object {
        const val STOP_WORDS_RESOURCE_PATH = "stop_words.txt"
    }

    val stopWords:Set<String>

    init {
        stopWords = this.javaClass.classLoader.getResourceAsStream(STOP_WORDS_RESOURCE_PATH)
                ?.bufferedReader() ?.lines() ?. collect(toSet())
                ?: setOf()
    }

    fun isStopWord(token:String)
            = stopWords.contains(token)
}