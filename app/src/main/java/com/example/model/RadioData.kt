package com.example.model

enum class PlaybackState {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}

data class FrequencyChannel(
    val id: String,
    val frequencyMhz: String,
    val nameArabic: String,
    val coverageRegion: String,
    val streamUrl: String,
    val backupStreamUrl: String,
    val transmitterLocation: String,
    val powerKw: String,
    val isDefault: Boolean = false
)

object RadioChannels {
    val CHANNELS = listOf(
        FrequencyChannel(
            id = "freq_91_1",
            frequencyMhz = "91.1 FM",
            nameArabic = "إذاعة نداء المعرفة - بيروت وجبل لبنان",
            coverageRegion = "بيروت، الضاحية، الشوف، عاليه، بعبدا، المتن، كسروان",
            streamUrl = "http://nidaa.fm:8811/stream.mp3",
            backupStreamUrl = "https://nidaa.fm:8811/stream.mp3",
            transmitterLocation = "تلة الخياط / بيت مري",
            powerKw = "5 kW",
            isDefault = true
        ),
        FrequencyChannel(
            id = "freq_91_3",
            frequencyMhz = "91.3 FM",
            nameArabic = "إذاعة نداء المعرفة - الشمال وطرابلس",
            coverageRegion = "طرابلس، الميناء، زغرتا، الكورة، البترون، عكار، الضنية",
            streamUrl = "http://nidaa.fm:8811/stream.mp3",
            backupStreamUrl = "https://nidaa.fm:8811/stream.mp3",
            transmitterLocation = "جبل تربل - الشمال",
            powerKw = "5 kW",
            isDefault = false
        ),
        FrequencyChannel(
            id = "freq_91_5",
            frequencyMhz = "91.5 FM",
            nameArabic = "إذاعة نداء المعرفة - الجنوب والبقاع",
            coverageRegion = "صيدا، صور، النبطية، جزين، زحلة، بعلبك، البقاع الغربي والأوسط",
            streamUrl = "http://nidaa.fm:8811/stream.mp3",
            backupStreamUrl = "https://nidaa.fm:8811/stream.mp3",
            transmitterLocation = "جبل الرفيع / جبل صنين",
            powerKw = "5 kW",
            isDefault = false
        ),
        FrequencyChannel(
            id = "freq_hd",
            frequencyMhz = "HD الرقمي",
            nameArabic = "البث الرقمي العالمي عالي النقاوة",
            coverageRegion = "بث رقمي عالي الجودة لجميع دول العالم عبر الإنترنت 192kbps",
            streamUrl = "http://nidaa.fm:8811/stream.mp3",
            backupStreamUrl = "https://nidaa.fm:8811/stream.mp3",
            transmitterLocation = "خوادم البث المباشر السحابية",
            powerKw = "Digital HQ",
            isDefault = false
        )
    )
}

data class RadioProgram(
    val id: String,
    val timeSlot: String,
    val titleArabic: String,
    val presenterArabic: String,
    val description: String,
    val category: ProgramCategory,
    val days: String = "يومياً",
    val isLiveNow: Boolean = false
)

enum class ProgramCategory(val titleArabic: String) {
    QURAN("قرآن كريم"),
    TAFSEER("تفسير وفقه"),
    DAWAH("دروس ومواعظ"),
    HADITH("سنة وسيرة"),
    FAMILY("الأسرة والمجتمع"),
    CHILDREN("أطفال وناشئة"),
    NEWS("أخبار وتقارير"),
    NASHEED("ابتهالات وأناشيد")
}

data class LebaneseCityPrayer(
    val cityNameArabic: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)
