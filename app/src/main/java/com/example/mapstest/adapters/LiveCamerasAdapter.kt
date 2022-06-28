package com.example.mapstest.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mapstest.R
import com.example.mapstest.models.LiveCamera
import java.net.URL


class LiveCamerasAdapter(
    private val context: Context,
    private val cameraList: List<LiveCamera>,
    private val itemClickListener: ItemClickListener
    ): RecyclerView.Adapter<LiveCamerasAdapter.CameraViewHolder>() {

    // Holds the views for adding it to image and text
    class CameraViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val icLive: ImageView = itemView.findViewById(R.id.ic_live)
        val tvName: TextView = itemView.findViewById(R.id.tv_camera_name)
        val tvCountry: TextView = itemView.findViewById(R.id.tv_country)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CameraViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_live_camera, parent, false)

        return CameraViewHolder(view)
    }

    override fun onBindViewHolder(holder: CameraViewHolder, position: Int) {
        val camera = cameraList[position]

        val thread = Thread {
            try {
                //Code here
                val url = URL("https://img.youtube.com/vi/${camera.cameraId}/mqdefault.jpg")
                val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                holder.imageView.setImageBitmap(bmp)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()

        /*Glide.with(context).load(Uri.parse(camera.thumbnail)).into(holder.imageView)*/

        holder.icLive.setImageResource(R.drawable.ic_live)
        holder.tvName.text = camera.name
        holder.tvName.isSelected = true // for scrolling text
        holder.tvCountry.text = camera.country

        holder.itemView.setOnClickListener() {
            itemClickListener.onLiveCameraClick(position)
        }
    }

    override fun getItemCount(): Int {
        return cameraList.size
    }

    interface ItemClickListener{
        fun onLiveCameraClick(position: Int)
    }
}