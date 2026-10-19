package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.AdvocateEntity
import com.example.model.AdvocateWithClerk
import com.example.model.ClerkEntity
import com.example.ui.components.AdvocateCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun advocate_card_screenshot() {
    val sample = AdvocateWithClerk(
        advocate = AdvocateEntity(
            id = 1,
            name = "NITIN M AMIN",
            code = "126",
            primaryMobile = "9825049155",
            secondaryMobile = "9825049156",
            clerkId = 1,
            initialSrNo = 1
        ),
        clerk = ClerkEntity(
            id = 1,
            name = "PRADIPBHAI S SHUKLA",
            primaryMobile = "9825049155",
            secondaryMobile = "9825049156"
        )
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        AdvocateCard(
            item = sample,
            onEdit = {},
            onDelete = {},
            onReplaceClerk = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/advocate_card.png")
  }
}
