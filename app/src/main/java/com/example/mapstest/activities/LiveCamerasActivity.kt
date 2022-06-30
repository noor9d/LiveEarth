package com.example.mapstest.activities

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mapstest.YoutubePlayerActivity
import com.example.mapstest.adapters.LiveCamerasAdapter
import com.example.mapstest.databinding.ActivityLiveCamerasBinding
import com.example.mapstest.models.LiveCamera
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream


class LiveCamerasActivity : AppCompatActivity(), LiveCamerasAdapter.ItemClickListener {
    lateinit var binding : ActivityLiveCamerasBinding
    private var cameraList: ArrayList<LiveCamera> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveCamerasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            val obj = JSONObject(loadJSONFromAsset().toString())
            val jsonArray = obj.getJSONArray("cameras")

            for (i in 0 until jsonArray.length()) {
                val camera = jsonArray.getJSONObject(i)
                val model = LiveCamera(
                    camera.getString("name"),
                    camera.getString("country"),
                    camera.getString("thumbnail"),
                    camera.getString("videoId")
                )
                Log.d("json_tag", "name= ${model.name}")
                Log.d("json_tag", "country= ${model.country}")
                cameraList.add(model)
            }

            populateRecyclerview()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

//        Log.d(TAG, "onCreate: thumbnails= ${getThumbnails()}")
    }

    private fun getThumbnails(): ArrayList<String> {
        val list: ArrayList<String> = ArrayList()
        try {
            // to reach asset
            val assetManager = assets
            // to get all item in dogs folder.
            val images = assetManager.list("img")
            // to keep all image
            val drawables = arrayOfNulls<Drawable>(images!!.size)
            // the loop read all image in dogs folder and  aa
            for (i in images.indices) {
                val inputStream = assets.open("img/" + images[i])
                val drawable = Drawable.createFromStream(inputStream, null)
                drawables[i] = drawable
                Log.d(TAG, "foo: images= ${images[i]}")
                list.add(images[i])
            }
        } catch (e: IOException) {
            // you can print error or log.
            e.printStackTrace()
        }
        return list
    }

    private fun populateRecyclerview() {
        val recyclerView = binding.recyclerViewLiveCameras
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        val adapter = LiveCamerasAdapter(this, cameraList, this)
        recyclerView.adapter = adapter
    }

    private fun loadJSONFromAsset(): String? {
        val json: String? = try {
            val inputStream: InputStream = assets.open("live_cameras.json")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    override fun onLiveCameraClick(position: Int) {
        Log.d(TAG, "onLiveCameraClick: ${cameraList[position].name}")
        val intent = Intent(this@LiveCamerasActivity, YoutubePlayerActivity::class.java)
        intent.putExtra("video_id", cameraList[position].cameraId)
        startActivity(intent)
    }

    companion object {
        const val TAG = "LiveCamerasActivity"
    }
}