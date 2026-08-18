package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.FrequencyChannel
import com.example.model.LebaneseCityPrayer
import com.example.model.PlaybackState
import com.example.model.ProgramCategory
import com.example.model.RadioChannels
import com.example.model.RadioProgram
import com.example.service.RadioController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val radioController = RadioController(application.applicationContext)

    val playbackState: StateFlow<PlaybackState> = radioController.playbackState
    val currentChannel: StateFlow<FrequencyChannel> = radioController.currentChannel
    val audioAmplitudes: StateFlow<List<Float>> = radioController.audioAmplitudes
    val errorMessage: StateFlow<String?> = radioController.errorMessage

    private val _volume = MutableStateFlow(0.90f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    // Audio Note / Tone Degree (درجة النوتة / حدة الصوت)
    private val _noteDegree = MutableStateFlow(0.05f) // Raised slightly above 0.0 as requested
    val noteDegree: StateFlow<Float> = _noteDegree.asStateFlow()

    // Sleep Timer
    private val _sleepTimerMinutesLeft = MutableStateFlow<Int?>(null)
    val sleepTimerMinutesLeft: StateFlow<Int?> = _sleepTimerMinutesLeft.asStateFlow()
    private var sleepTimerJob: Job? = null

    // Selected Tab
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Selected Lebanese City for Prayer Times
    private val _selectedCityIndex = MutableStateFlow(0)
    val selectedCityIndex: StateFlow<Int> = _selectedCityIndex.asStateFlow()

    // Time ticker for Live updates
    private val _currentTimeString = MutableStateFlow(getCurrentTimeFormatted())
    val currentTimeString: StateFlow<String> = _currentTimeString.asStateFlow()

    // Programs schedule
    private val _programs = MutableStateFlow(samplePrograms())
    val programs: StateFlow<List<RadioProgram>> = _programs.asStateFlow()

    private val _selectedProgramCategory = MutableStateFlow<ProgramCategory?>(null)
    val selectedProgramCategory: StateFlow<ProgramCategory?> = _selectedProgramCategory.asStateFlow()

    // Prayer Times for Lebanon
    val lebaneseCities = listOf(
        LebaneseCityPrayer("بيروت وجبل لبنان", "04:42", "06:08", "12:47", "16:26", "19:25", "20:48"),
        LebaneseCityPrayer("طرابلس والشمال", "04:40", "06:07", "12:47", "16:27", "19:26", "20:50"),
        LebaneseCityPrayer("صيدا والجنوب", "04:44", "06:09", "12:48", "16:26", "19:25", "20:47"),
        LebaneseCityPrayer("زحلة والبقاع", "04:41", "06:07", "12:46", "16:25", "19:24", "20:47"),
        LebaneseCityPrayer("صور", "04:45", "06:10", "12:48", "16:27", "19:26", "20:48"),
        LebaneseCityPrayer("بعلبك", "04:39", "06:06", "12:45", "16:24", "19:23", "20:46")
    )

    init {
        radioController.bind()
        startClockTicker()
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            while (isActive) {
                _currentTimeString.value = getCurrentTimeFormatted()
                updateLiveProgramStatus()
                delay(1000)
            }
        }
    }

    private fun updateLiveProgramStatus() {
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)

        _programs.value = _programs.value.map { prog ->
            val isNow = when (prog.id) {
                "prog_1" -> currentHour in 5..6
                "prog_2" -> currentHour in 7..8
                "prog_3" -> currentHour in 9..10
                "prog_4" -> currentHour in 11..12
                "prog_5" -> currentHour in 13..14
                "prog_6" -> currentHour in 15..16
                "prog_7" -> currentHour in 17..18
                "prog_8" -> currentHour in 19..20
                "prog_9" -> currentHour in 21..22
                else -> currentHour in 23..24 || currentHour in 0..4
            }
            prog.copy(isLiveNow = isNow)
        }
    }

    fun selectChannel(channel: FrequencyChannel) {
        radioController.play(channel)
    }

    fun nextChannel() {
        val currentIndex = RadioChannels.CHANNELS.indexOfFirst { it.id == currentChannel.value.id }
        val nextIndex = if (currentIndex >= 0 && currentIndex < RadioChannels.CHANNELS.size - 1) currentIndex + 1 else 0
        selectChannel(RadioChannels.CHANNELS[nextIndex])
    }

    fun previousChannel() {
        val currentIndex = RadioChannels.CHANNELS.indexOfFirst { it.id == currentChannel.value.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else RadioChannels.CHANNELS.size - 1
        selectChannel(RadioChannels.CHANNELS[prevIndex])
    }

    fun togglePlayback() {
        radioController.togglePlayPause()
    }

    fun turnRadioOn() {
        radioController.turnOn()
    }

    fun turnRadioOff() {
        radioController.turnOff()
    }

    fun toggleRadioPower() {
        if (playbackState.value == PlaybackState.PLAYING || playbackState.value == PlaybackState.BUFFERING) {
            turnRadioOff()
        } else {
            turnRadioOn()
        }
    }

    fun stopPlayback() {
        radioController.stop()
    }

    fun setVolume(vol: Float) {
        _volume.value = vol
        radioController.setVolume(vol)
    }

    fun setNoteDegree(degree: Float) {
        val clamped = (Math.round(degree * 100.0) / 100.0).toFloat().coerceIn(-0.25f, 0.25f)
        _noteDegree.value = clamped
        radioController.setPitchOffset(clamped)
    }

    fun incrementNote() {
        setNoteDegree(_noteDegree.value + 0.02f)
    }

    fun decrementNote() {
        setNoteDegree(_noteDegree.value - 0.02f)
    }

    fun resetNoteToDefault() {
        setNoteDegree(0.0f)
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun selectCity(index: Int) {
        _selectedCityIndex.value = index
    }

    fun filterCategory(cat: ProgramCategory?) {
        _selectedProgramCategory.value = cat
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _sleepTimerMinutesLeft.value = null
            return
        }

        _sleepTimerMinutesLeft.value = minutes
        sleepTimerJob = viewModelScope.launch {
            var remaining = minutes * 60
            while (remaining > 0 && isActive) {
                delay(1000)
                remaining--
                _sleepTimerMinutesLeft.value = (remaining + 59) / 60
            }
            if (isActive) {
                radioController.pause()
                _sleepTimerMinutesLeft.value = null
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerMinutesLeft.value = null
    }

    private fun getCurrentTimeFormatted(): String {
        val sdf = SimpleDateFormat("hh:mm:ss a", Locale("ar", "LB"))
        return sdf.format(Date())
    }

    override fun onCleared() {
        radioController.unbind()
        super.onCleared()
    }

    companion object {
        private fun samplePrograms(): List<RadioProgram> = listOf(
            RadioProgram(
                id = "prog_1",
                timeSlot = "05:00 ص - 06:30 ص",
                titleArabic = "نفحات الفجر والقرآن الكريم",
                presenterArabic = "تلاوات مباركة ونفحات إيمانية",
                description = "استفتاح اليوم المبارك بآيات عطرة من الذكر الحكيم وأذكار الصباح المأثورة.",
                category = ProgramCategory.QURAN
            ),
            RadioProgram(
                id = "prog_2",
                timeSlot = "07:00 ص - 08:30 ص",
                titleArabic = "صباح الخير من نداء المعرفة",
                presenterArabic = "تقديم: أسرة الإذاعة",
                description = "برنامج صباحي تفاعلي يضم فقرات توعوية، إرشادات أسرية، ورسائل تربوية هادفة.",
                category = ProgramCategory.FAMILY
            ),
            RadioProgram(
                id = "prog_3",
                timeSlot = "09:00 ص - 10:30 ص",
                titleArabic = "في رحاب الفقه الإسلامي",
                presenterArabic = "فضيلة الشيخ المفتي",
                description = "شرح مبسط لأحكام العبادات والمعاملات وإجابات وافية على أسئلة المستمعين الفقهية.",
                category = ProgramCategory.TAFSEER
            ),
            RadioProgram(
                id = "prog_4",
                timeSlot = "11:00 ص - 12:30 م",
                titleArabic = "قطوف من السيرة النبوية",
                presenterArabic = "نخبة من علماء الفكر الإسلامي",
                description = "محطات مضيئة من حياة الحبيب المصطفى ﷺ ودروس وعبر لبناء الشخصية المسلمة المعاصرة.",
                category = ProgramCategory.HADITH
            ),
            RadioProgram(
                id = "prog_5",
                timeSlot = "01:00 م - 02:30 م",
                titleArabic = "البث المباشر لصلاة الظهر والموعظة",
                presenterArabic = "نقل مباشر من مساجد لبنان",
                description = "نقل أذان الظهر وصلاة الجماعة متبوعة بخاطرة إيمانية وتوجيهات دينية.",
                category = ProgramCategory.DAWAH
            ),
            RadioProgram(
                id = "prog_6",
                timeSlot = "03:30 م - 04:30 م",
                titleArabic = "أحبائي الصغار (براعم المعرفة)",
                presenterArabic = "قسم برامج الأطفال والناشئة",
                description = "قصص الأنبياء، القيم الأخلاقية، وأناشيد تربوية ممتعة للأطفال والفتيان.",
                category = ProgramCategory.CHILDREN
            ),
            RadioProgram(
                id = "prog_7",
                timeSlot = "05:00 م - 06:30 م",
                titleArabic = "واحة المعرفة والأسرة",
                presenterArabic = "مختصون في التربية والاستشارات",
                description = "حوارات اجتماعية تسلط الضوء على تماسك الأسرة وتربية الأبناء وحلول المشكلات اليومية.",
                category = ProgramCategory.FAMILY
            ),
            RadioProgram(
                id = "prog_8",
                timeSlot = "07:00 م - 08:30 م",
                titleArabic = "رياض الأحاديث والسنن",
                presenterArabic = "شروح لكتب الحديث المعتمدة",
                description = "دراسة تحليلية لأحاديث المصطفى ﷺ واستنباط الهدايات الربانية.",
                category = ProgramCategory.HADITH
            ),
            RadioProgram(
                id = "prog_9",
                timeSlot = "09:00 م - 10:30 م",
                titleArabic = "ابتهالات وأناشيد الهدى",
                presenterArabic = "أعذب الأصوات والقصائد الروحية",
                description = "باقة من الأناشيد الدينية الهادفة والمدائح النبوية العطرة دون إيقاع.",
                category = ProgramCategory.NASHEED
            ),
            RadioProgram(
                id = "prog_10",
                timeSlot = "11:00 م - 04:30 ص",
                titleArabic = "الختمة القرآنية الليلية",
                presenterArabic = "كبار قراء العالم الإسلامي",
                description = "تلاوات خاشعة مرتلة ومجودة متواصلة عبر الأثير طوال ساعات الليل المباركة.",
                category = ProgramCategory.QURAN
            )
        )
    }
}
