
fun Int.npbScheduleMOnth(): List<Schedule> {
    val scrapingNpb = ScrapingNpb(this)
    return scrapingNpb.npbSchedule()
}