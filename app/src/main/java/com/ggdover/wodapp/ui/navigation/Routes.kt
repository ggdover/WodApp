package com.ggdover.wodapp.ui.navigation

object Routes {
    const val Moments = "moments"
    const val Workouts = "workouts"
    const val PersonalBests = "personal_bests"
    const val WorkoutDetail = "workout_detail/{id}"
    const val WorkoutNew = "workout_new"
    const val WorkoutEdit = "workout_edit/{id}"
    const val MomentDetail = "moment/{id}"

    fun workoutDetail(id: String) = "workout_detail/$id"
    fun workoutEdit(id: String) = "workout_edit/$id"
    fun momentDetail(id: String) = "moment/$id"
}
