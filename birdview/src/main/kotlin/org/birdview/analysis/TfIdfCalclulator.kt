package org.birdview.analysis

import java.util.concurrent.atomic.AtomicInteger

class TfIdfCalclulator {
    private val termsDocsCounts = mutableMapOf<String, AtomicInteger>()
    private var docsCount = 0
//
//    fun addDoc(doc:BVDocument) {
//        val termSet = mutableSetOf<String>()
//        doc.getTerms().forEach { term ->
//            if(termSet.add(term)) { // Count term only once per document
//                termsDocsCounts.computeIfAbsent(term) {
//                    AtomicInteger(0)
//                }.incrementAndGet()
//            }
//        }
//        docsCount++
//    }
//
//    fun idf(term:String):Double = termsDocsCounts[term]?.let {
//        termCount -> 1 + log10(docsCount.toDouble()/termCount.get())
//    } ?: 1.0
//
//    fun calculate(term:String, doc:BVDocument) = doc.getTermFrequency(term) * idf(term)
}