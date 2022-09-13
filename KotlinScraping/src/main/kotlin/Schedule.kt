import java.time.LocalDate

/**
 * Schedule
 * @property date
 * 試合日
 * @property time
 * 開催時間
 * @property venue
 * 開催場所
 * @property home
 * 対戦結果(Home)
 * @property away
 * 対戦結果(away)
 * @property startingPitcherHome
 * 予告先発投手(Home) ※ ScheduleState.MATCH_ALREADY_HELD(開催済)の場合はブランク
 * @property startingPitcherAway
 * 予告先発投手(away) ※ ScheduleState.MATCH_ALREADY_HELD(開催済)の場合はブランク
 * @property winningPitcher
 * 勝利投手 ※ ScheduleState.MATCH_ALREADY_HELD(開催済)の場合設定される
 * @property losingPitcher
 * 負け発投手 ※ ScheduleState.MATCH_ALREADY_HELD(開催済)の場合設定される
 * @property remarks
 * 備考
 */
data class Schedule(
    val state: ScheduleState,
    val date: LocalDate,
    val time: String,
    val venue: String,
    val home: Score,
    val away: Score,
    val startingPitcherHome: String = "",
    val startingPitcherAway: String = "",
    val winningPitcher: String = "",
    val losingPitcher: String = "",
    val remarks: String = "",
)

/**
 * ScheduleState
 * 試合の状態
 */
enum class ScheduleState {
    /**
     * 開催済
     */
    MATCH_ALREADY_HELD,
    /**
     * 未開催
     */
    NOT_YET_HELD,
    /**
     * 予備日
     */
    PREPARATION_DAY,
    /**
     * 中止
     */
    CANCELLED,
    /**
     * ノーゲーム
     */
    NO_GAME,
}

/**
 * Score
 * @property team
 * チーム名
 * @property score
 * スコア
 */
data class Score (
    val team: String = "",
    val score: String = "",
)
