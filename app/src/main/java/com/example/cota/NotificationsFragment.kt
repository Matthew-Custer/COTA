package com.example.cota

import androidx.fragment.app.Fragment
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import com.example.cota.databinding.ActivityMainBinding
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cota.R

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    lateinit var imageView: ImageView
    private lateinit var binding: ActivityMainBinding

    var title = "COTApp"
}