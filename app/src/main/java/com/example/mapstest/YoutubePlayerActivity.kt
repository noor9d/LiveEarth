package com.example.mapstest

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import com.example.mapstest.BuildConfig.MAPS_API_KEY
import com.example.mapstest.databinding.ActivityYoutubePlayerBinding
import com.example.mapstest.utils.Utils
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView

class YoutubePlayerActivity : YouTubeBaseActivity() {
    lateinit var binding: ActivityYoutubePlayerBinding
    private var youTubePlayerView: YouTubePlayerView? = null
    private lateinit var mOnInitializedListener: YouTubePlayer.OnInitializedListener

    companion object {
        const val TAG = "YoutubePlayerActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // hide status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController!!.hide(android.view.WindowInsets.Type.statusBars())
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        binding = ActivityYoutubePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var videoId = ""
        val intent = intent
        if (intent.hasExtra("video_id")) {
            videoId = intent.getStringExtra("video_id").toString()
        }

        youTubePlayerView = binding.youtubePlayerView

        mOnInitializedListener = (object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                p0: YouTubePlayer.Provider?,
                player: YouTubePlayer?,
                p2: Boolean
            ) {
                Log.d(TAG, "onInitializationSuccess: ")
                player!!.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL)
                player.loadVideo(videoId)
                player.play()
            }

            override fun onInitializationFailure(
                p0: YouTubePlayer.Provider?,
                p1: YouTubeInitializationResult?
            ) {
                Log.d(TAG, "onInitializationFailure: ${p1.toString()}")
            }

        })

        youTubePlayerView!!.initialize(MAPS_API_KEY, mOnInitializedListener)
    }
}