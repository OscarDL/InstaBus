package com.franscar.instabus.ui.settings

import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.franscar.instabus.R
import dev.sasikanth.colorsheet.ColorSheet
import dev.sasikanth.colorsheet.utils.ColorSheetUtils
import kotlinx.android.synthetic.main.home_list_item.*

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        private const val COLOR_SELECTED = "selectedColor"
        private const val NO_COLOR_OPTION = "noColorOption"
    }

    private var selectedColor: Int = ColorSheet.NO_COLOR
    private var noColorOption = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val colors = resources.getIntArray(R.array.colors)
        selectedColor = savedInstanceState?.getInt(COLOR_SELECTED) ?: colors.first()
        //setColor(selectedColor)

        noColorOption = savedInstanceState?.getBoolean(NO_COLOR_OPTION) ?: false

        val myPref: Preference? = findPreference("cardColorDark") as Preference?
        myPref?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            ColorSheet()
                .colorPicker(
                    colors = resources.getIntArray(R.array.colors),
                    noColorOption = false,
                    selectedColor = savedInstanceState?.getInt("selectedColor")
                        ?: resources.getIntArray(R.array.colors).first(),
                    listener = { color ->
                        selectedColor = color
                        //setColor(selectedColor)
                    })
                .show(parentFragmentManager)
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(COLOR_SELECTED, selectedColor)
        outState.putBoolean(NO_COLOR_OPTION, noColorOption)
    }

    /*private fun setColor(@ColorInt color: Int) {
        home_card.setBackgroundColor(color)
    }*/
}