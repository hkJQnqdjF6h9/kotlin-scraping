import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.time.LocalDate
import java.util.concurrent.TimeUnit


fun Element.trimText(): String  = this.text().trim()

/**
 * npb.jpのスクレイピング
 */
class ScrapingNpb (private val year: Int = LocalDate.now().year) {
    fun npbScheduleMonth(month: Int) : List<Schedule> {
        val doc: Document = getSiteHtml(month)

        val newsHeadlines: Elements = doc.select("#schedule_detail > div > table > tbody > tr")
        if (newsHeadlines.isEmpty()) return arrayListOf()

        val npbGames = newsHeadlines.map { NpbGame(it) }.filter { !it.invalid }
        return npbGames.map {
            val state = when {
                it.cancel.isNotBlank() && it.cancel == "(予備日)" -> ScheduleState.PREPARATION_DAY
                it.cancel.isNotBlank() && it.cancel == "中止" -> ScheduleState.CANCELLED
                it.cancel.isNotBlank() && it.cancel == "ノーゲーム" -> ScheduleState.NO_GAME
                it.scoreHome.isBlank() && it.scoreHome.isBlank() -> ScheduleState.NOT_YET_HELD
                else  -> ScheduleState.MATCH_ALREADY_HELD
            }
            val date = LocalDate.of(year, it.date.take(2).toInt(), it.date.takeLast(2).toInt())

            var startingPitcherHome = ""
            var startingPitcherAway = ""
            var winningPitcher = ""
            var losingPitcher = ""

            if (state == ScheduleState.MATCH_ALREADY_HELD) {
                winningPitcher = if(it.pitcher1.isNotBlank()) it.pitcher1.split('：')[1].trim() else ""
                losingPitcher = if(it.pitcher2.isNotBlank()) it.pitcher2.split('：')[1].trim()else ""
            } else if (state == ScheduleState.NOT_YET_HELD) {
                startingPitcherHome = if(it.pitcher1.isNotBlank()) it.pitcher1.split('：')[1].trim() else ""
                startingPitcherAway = if(it.pitcher2.isNotBlank()) it.pitcher2.split('：')[1].trim()else ""
            }

            Schedule(
                state = state,
                date = date,
                time = it.time,
                venue = it.venue,
                home = Score(team = it.teamHome, score = it.scoreHome),
                away = Score(team = it.teamAway, score = it.scoreAway),
                startingPitcherHome = startingPitcherHome,
                startingPitcherAway = startingPitcherAway,
                winningPitcher = winningPitcher,
                losingPitcher = losingPitcher,
                remarks = it.remarks
            )
        }.toList()
    }

    /**
     * @throws org.jsoup.HttpStatusException
     * リクエストエラー (登録されていない年を指定した場合 など)
     * @throws java.net.UnknownHostException
     * 接続エラー
     */
    fun npbSchedule() : List<Schedule> {
        val schedules = arrayListOf<Schedule>()
        for (month in 3..10) {
            schedules.addAll(npbScheduleMonth(month))
            TimeUnit.SECONDS.sleep(1)
        }
        return schedules
    }

    private fun getSiteHtml(month: Int) : Document {
        return Jsoup.connect("https://npb.jp/games/${year}/schedule_${month.toString().padStart(2, '0')}_detail.html").get()
    }

    private inner class NpbGame(private val gameHtmlElement: Element) {
        val invalid: Boolean
            get() {
                val element = gameHtmlElement.select("td")
                val value = element.first()?.trimText() ?: ""
                return value.isEmpty()
            }

        val date : String
            get() {
                val dateValue = gameHtmlElement.attr("id")
                return if (dateValue.isEmpty()) "" else dateValue.takeLast(4)
            }

        val time: String
            get() {
                val timeValue = gameHtmlElement.select(".time")
                return timeValue.first()?.trimText() ?: ""
            }

        val venue: String
            get() {
                val placeValue = gameHtmlElement.select(".place")
                return placeValue.first()?.trimText() ?: ""
            }

        val teamHome: String
            get() {
                val team1Value = gameHtmlElement.select(".team1")
                return team1Value.first()?.trimText() ?: ""
            }

        val scoreHome: String
            get() {
                val score1Value = gameHtmlElement.select(".score1")
                return score1Value.first()?.trimText() ?: ""
            }

        val teamAway: String
            get() {
                val team2Value = gameHtmlElement.select(".team2")
                return team2Value.first()?.trimText() ?: ""
            }

        val scoreAway: String
            get() {
                val score2Value = gameHtmlElement.select(".score2")
                return score2Value.first()?.trimText() ?: ""
            }

        val cancel: String
            get() {
                val cancelValue = gameHtmlElement.select(".cancel")
                return cancelValue.first()?.trimText() ?: ""
            }

        val pitcher1: String
            get() {
                val pitValue = gameHtmlElement.select(".pit")
                return pitValue.first()?.trimText() ?: ""
            }

        val pitcher2: String
            get() {
                val pitValue = gameHtmlElement.select(".pit")
                return pitValue.last()?.trimText() ?: ""
            }

        val remarks: String
            get() {
                val commentValue = gameHtmlElement.select(".comment")
                return commentValue.first()?.trimText() ?: ""
            }
    }
}