package id.wahidiyah.miladiyyah.core.domain.calendar

data class MiladiyyahDate(
    val dayOfWeek: String,
    val dayMasehi: Int,
    val monthMasehiName: String,
    val yearMasehi: Int,
    val dayHijriyah: Int,
    val monthHijriyahName: String,
    val yearHijriyah: Int,
    val pasaran: String
) {
    val formattedMasehi: String get() = "$dayMasehi $monthMasehiName $yearMasehi"
    val formattedHijriyah: String get() = "$dayHijriyah $monthHijriyahName $yearHijriyah H"
}
