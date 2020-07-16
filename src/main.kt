fun main(args: Array<String>) {
    var url = arrayListOf<String>("https://www.americanexpress.com/", "en-us", "/loans/apply/personal-loan, business-loan?pcn=hello;login=simple,card")

    val joinedArgs = url.joinToString(separator = "")
    var (baseWithoutQuery, queryPortion) = joinedArgs.split( "?")

    //
    // PART 1. HANDLE THE BASE WITHOUT QUERY
    //
    val urlSegments = baseWithoutQuery.split("/")
    var optionsBySegment = mutableListOf<List<String>>()

    for (segment in urlSegments) {
        val segmentOptions = segment.split(",").map{ it.trim() }
        optionsBySegment.add(segmentOptions)
    }

    var finalCollection = mutableListOf<String>("")
    for ((segmentIndex, optionsForSegment) in optionsBySegment.withIndex()) {
        var prefix = if (segmentIndex == 0) "" else "/"

        // Since we
        var tempCollection = finalCollection.toMutableList()
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
    var queryStrings = queryPortion.split(";").toMutableList()
    queryStrings.sort()
    for (queryString in queryStrings) {
        var tempCollection = finalCollection.toMutableList()

        for ((i) in tempCollection.withIndex()) {
            var (queryKey, queryValueString) = queryString.split("=")
            var queryValues = queryValueString.split(",")
            for (queryValue in queryValues) {
                var prefix = if (tempCollection[i].contains("?")) "&" else "?"
                finalCollection.add(tempCollection[i] + "$prefix$queryKey=$queryValue")
            }
        }
    }

    finalCollection.sort()

//    Patterns.WEB_URL.matcher(potentialUrl).matches()
    println(finalCollection.joinToString(separator = "\n"))
}