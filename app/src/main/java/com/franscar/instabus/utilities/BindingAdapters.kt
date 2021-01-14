package com.franscar.instabus.utilities

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.util.*

// example function that lets us manipulate data
@BindingAdapter("street_name")
fun formatName(view: TextView, text: String) {
    view.text = text.toUpperCase(Locale.getDefault())
}