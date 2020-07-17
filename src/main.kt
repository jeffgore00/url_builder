import java.net.MalformedURLException
import java.net.URL

fun constructBaseUrls(baseUrlPortion: String) : MutableList<String> {
    val urlSegments = baseUrlPortion.split("/")
    val optionsBySegment = mutableListOf<List<String>>()

    // An array representing URL segments, within each element is another array of all the options for that segment:
    // Ex: [[http:],[],[localhost:3000][home, about, contact]]
    for (segment in urlSegments) {
        val segmentOptions = segment.split(",").map{ it.trim() }
        optionsBySegment.add(segmentOptions)
    }

    // Initialize to one string; there should be at least one URL output from this function.
    val urlList = mutableListOf("")

    for ((segmentIndex, optionsForSegment) in optionsBySegment.withIndex()) {
        val prefix = if (segmentIndex == 0) "" else "/"

        // Since we are going to append to the URL list in the process of discovering more options, we need to use a copy
        // of the pre-loop URL list to avoid a `ConcurrentModificationException`. This is also necessary so that all options
        // are operating upon the same base.
        val preOptionsUrlList = urlList.toMutableList()

        for ((optionIndex, option) in optionsForSegment.withIndex()) {
            // If this is the first option in the list, then just append to all the URLs in the collection. The URLs
            // require this segment to be populated.
            if (optionIndex == 0) {
                for (i in 0 until urlList.size) {
                    urlList[i] += "$prefix$option"
                }
            } else { // Otherwise this is the 2nd+ option for this URL segment; we'll have to add new URLs.
                // We'll use our preOptionsUrlList as a basis, which contains the list before the first option was added.
                for (i in 0 until preOptionsUrlList.size) {
                    urlList.add(preOptionsUrlList[i] + "$prefix$option")
                }
            }
        }
    }
    return urlList
}

fun appendQueryStrings(queryPortion: String, baseUrlList: MutableList<String>) : MutableList<String> {
    var urlList = baseUrlList.toMutableList()
    // Because query keys can have multiple possible values, the values are separated with commas, and the key-value
    // pairs are separated with semicolons.
    val queryStrings = queryPortion.split(";").toMutableList()
    queryStrings.sort()

    for (queryString in queryStrings) {
        // Since we are going to append to the URL list in the process of discovering more options, we need to use a copy
        // of the pre-loop URL list to avoid a `ConcurrentModificationException`. This is also necessary so that all query
        // value options are operating upon the same base.
        val preOptionsUrlList = urlList.toMutableList()

        for (i in 0 until preOptionsUrlList.size) {
            val (queryKey, queryValueString) = queryString.split("=")
            val queryValues = queryValueString.split(",")

            // In this case, we assume that we always want a version of the URL without the query string. So we will
            // always choose to add a new value to the URL list, rather than mutating the existing value.
            for (queryValue in queryValues) {
                val prefix = if (preOptionsUrlList[i].contains("?")) "&" else "?"
                urlList.add(preOptionsUrlList[i] + "$prefix$queryKey=$queryValue")
            }
        }
    }
    return urlList
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
    // // Shadowed args to facilitate local testing:
    // var args = listOf("http://localhost:3000/home,about,contact,store?promo=friend,bff,enemy;goodUx=true")
    val joinedArgs = args.joinToString(separator = "")
    val (baseUrlPortion, queryPortion) = joinedArgs.split( "?")

    val baseUrlList = constructBaseUrls(baseUrlPortion)
    val urlList = appendQueryStrings(queryPortion, baseUrlList)
    val invalidUrlList = removeInvalidUrls(urlList)
    printResults(urlList, invalidUrlList)
}