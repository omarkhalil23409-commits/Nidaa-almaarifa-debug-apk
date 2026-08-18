package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.RadioChannels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("إذاعة نداء المعرفة", appName)
  }

  @Test
  fun `verify FM channels exist`() {
    val channels = RadioChannels.CHANNELS
    assertTrue(channels.any { it.frequencyMhz == "91.1 FM" })
    assertTrue(channels.any { it.frequencyMhz == "91.3 FM" })
    assertTrue(channels.any { it.frequencyMhz == "91.5 FM" })
  }
}

