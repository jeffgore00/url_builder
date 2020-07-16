import java.net.MalformedURLException
import java.net.URL

fun main(args: Array<String>) {
//    var args = listOf("jeffttp:, https://www.americanexpress.com/", "en-us", "/loans/apply /personal-loan, business-loan?pcn=hello;login=simple,card")
    val joinedArgs = args.joinToString(separator = "")
    val (baseWithoutQuery, queryPortion) = joinedArgs.split( "?")
    //
    // PART 1. HANDLE THE BASE WITHOUT QUERY
    //
    val urlSegments = baseWithoutQuery.split("/")
    val optionsBySegment = mutableListOf<List<String>>()

    for (segment in urlSegments) {
        val segmentOptions = segment.split(",").map{ it.trim() }
        optionsBySegment.add(segmentOptions)
    }

    val finalCollection = mutableListOf<String>("")
    for ((segmentIndex, optionsForSegment) in optionsBySegment.withIndex()) {
        val prefix = if (segmentIndex == 0) "" else "/"

        // Since we
        val tempCollection = finalCollection.toMutableList()
        for ((optionIndex, option) in optionsForSegment.withIndex()) {

            // If this is the first option in the list, then just append to all the URLs in the collection.
            if (optionIndex == 0) {
                for ((i) in finalCollection.withIndex()) {
                    finalCollection[i] += "$prefix$option"
                }
            } else { // Otherwise this is a 2nd option which doubles the size of the list. We'll have to add new elements.
                for ((i) in tempCollection.withIndex()) {
                    finalCollection.add(tempCollection[i] + "$prefix$option")
                }
            }
        }
    }
    //
    // PART 2. HANDLE THE QUERY STRINGS
    //
    val queryStrings = queryPortion.split(";").toMutableList()
    queryStrings.sort()
    for (queryString in queryStrings) {
        val tempCollection = finalCollection.toMutableList()

        for ((i) in tempCollection.withIndex()) {
            val (queryKey, queryValueString) = queryString.split("=")
            val queryValues = queryValueString.split(",")
            for (queryValue in queryValues) {
                val prefix = if (tempCollection[i].contains("?")) "&" else "?"
                finalCollection.add(tempCollection[i] + "$prefix$queryKey=$queryValue")
            }
        }
    }

    data class MalformedURL(val url: String, val errReason: String?)
    val invalidUrls = mutableListOf<MalformedURL>()

    finalCollection.forEach{ url ->
        try {
            URL(url)
        } catch (err: MalformedURLException) {
            invalidUrls.add(MalformedURL(url, err.message))
        }
    }
    invalidUrls.forEach{ urlObj -> finalCollection.remove(urlObj.url) }

    finalCollection.sort()
    println("VALID URL LIST (${finalCollection.size} URLs):")
    println("-----------------------------------")
    println(finalCollection.joinToString(separator = "\n"))

    if (invalidUrls.size > 0) {
        val invalidUrlStrings = invalidUrls.map{ urlObj -> "${urlObj.url} (${urlObj.errReason})"}
        println("\nINVALID URL LIST (${invalidUrls.size} URLs):")
        println("-----------------------------------")
        println(invalidUrlStrings.joinToString(separator = "\n"))
    }
}