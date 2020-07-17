import java.net.MalformedURLException
import java.net.URL

fun constructBaseUrls(baseUrlPortion: String) : MutableList<String> {
    val urlSegments = baseUrlPortion.split("/")
    val optionsBySegment = mutableListOf<List<String>>()

    for (segment in urlSegments) {
        val segmentOptions = segment.split(",").map{ it.trim() }
        optionsBySegment.add(segmentOptions)
    }

    // Initialize to one string; there should be at least one URL output from this function
    val urlList = mutableListOf("")
    for ((segmentIndex, optionsForSegment) in optionsBySegment.withIndex()) {
        val prefix = if (segmentIndex == 0) "" else "/"

        // Since we
        val tempUrlList = urlList.toMutableList()
        for ((optionIndex, option) in optionsForSegment.withIndex()) {

            // If this is the first option in the list, then just append to all the URLs in the collection.
            if (optionIndex == 0) {
                for (i in 0 until urlList.size) {
                    urlList[i] += "$prefix$option"
                }
            } else { // Otherwise this is a 2nd option which doubles the size of the list. We'll have to add new elements.
                for (i in 0 until tempUrlList.size) {
                    urlList.add(tempUrlList[i] + "$prefix$option")
                }
            }
        }
    }
    return urlList
}

fun appendQueryStrings(queryPortion: String, urlList: MutableList<String>) {
    val queryStrings = queryPortion.split(";").toMutableList()
    queryStrings.sort()
    for (queryString in queryStrings) {
        val tempUrlList = urlList.toMutableList()

        for (i in 0 until tempUrlList.size) {
            val (queryKey, queryValueString) = queryString.split("=")
            val queryValues = queryValueString.split(",")
            for (queryValue in queryValues) {
                val prefix = if (tempUrlList[i].contains("?")) "&" else "?"
                urlList.add(tempUrlList[i] + "$prefix$queryKey=$queryValue")
            }
        }
    }
}

data class MalformedURL(val url: String, val errReason: String?)

fun removeInvalidUrls(urlList: MutableList<String>) : MutableList<MalformedURL> {
    val invalidUrlList = mutableListOf<MalformedURL>()

    urlList.forEach{ url ->
        try {
            URL(url)
        } catch (err: MalformedURLException) {
            invalidUrlList.add(MalformedURL(url, err.message))
        }
    }
    invalidUrlList.forEach{ urlObj -> urlList.remove(urlObj.url) }
    return invalidUrlList
}

fun printResults(urlList: MutableList<String>, invalidUrlList: MutableList<MalformedURL>) {
    urlList.sort()
    println("VALID URL LIST (${urlList.size} URLs):")
    println("-----------------------------------")
    println(urlList.joinToString(separator = "\n"))

    if (invalidUrlList.size > 0) {
        val invalidUrlStrings = invalidUrlList.map{ urlObj -> "${urlObj.url} (${urlObj.errReason})"}
        println("\nINVALID URL LIST (${invalidUrlList.size} URLs):")
        println("-----------------------------------")
        println(invalidUrlStrings.joinToString(separator = "\n"))
    }
}

fun main(args: Array<String>) {
//    var args = listOf("https://www.americanexpress.com/en-us/loans/apply/personal-loan,business-loan?pcn=hello;login=simple,card")
    val joinedArgs = args.joinToString(separator = "")
    val (baseUrlPortion, queryPortion) = joinedArgs.split( "?")

    val urlList = constructBaseUrls(baseUrlPortion)
    appendQueryStrings(queryPortion, urlList)
    val invalidUrlList = removeInvalidUrls(urlList)
    printResults(urlList, invalidUrlList)
}