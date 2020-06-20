package org.birdview.utils

object BVColorUtils {
    var useColors = true

    const val CLR_RED="\u001b[31m"
    const val CLR_RSET="\u001b[0m"
    const val BOLD="\u001b[1m"

    fun red(text:String) = "${set(CLR_RED)}${reset(text)}"
    fun bold(text:String) = "${set(BOLD)}${reset(text)}"

    fun reset(text:String) = if (text.endsWith(CLR_RSET)) text else "${text}${set(CLR_RSET)}"
    fun set(colorCode:String) = if (useColors) colorCode else ""
}