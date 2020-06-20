package org.birdview.analysis.tokenize

import javax.inject.Named

@Named
class TextTokenizer(val stopWordsService: StopWordsService) {
    companion object {
        val TOKEN_DELIMETERS = "[ '\\-(),.:\"/]".toRegex()
        const val HEADING_TOKEN_WEIGHT = 2.0//4.0
        const val JIRA_KEY_WEIGHT = 2.0//3.0
        const val SEQUENTIAL_WEIGHT = 1.0//2.0
        const val DEFAULT_WEIGHT = 1.0
    }

    fun tokenize(text:String) : List<String> {
        val termsList = mutableListOf<String>()

        //TODO
        // Leading terms extraction:
        val leadingTerms = split(text.substringBefore(':', ""))
        leadingTerms.forEach { token->
            termsList.add( token )
        }

        // Create initial list of individual tokens
        val filteredTokens = split(text.substringAfter(":"))
        filteredTokens.forEach { token->
            termsList.add(stem(token))
        }

        // Generate sequential tokens
        val sequentialTokens = filteredTokens.windowed(2, 1).map { window->
            window.joinToString (" ") { it }
        }
        sequentialTokens.forEach { token->
            termsList.add(token)
        }

        return termsList.toList()
    }

    private fun stem(token: String): String
        = if(token.endsWith("s"))  token.substringBeforeLast("s")
        else token


    private fun split(text: String)
        = text.split(TOKEN_DELIMETERS)
            .map { it.trim().toLowerCase() }
            .filter { !it.isBlank() && !stopWordsService.isStopWord(it) }
            .toList()
}