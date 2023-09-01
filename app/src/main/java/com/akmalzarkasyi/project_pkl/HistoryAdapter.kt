package com.akmalzarkasyi.project_pkl

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akmalzarkasyi.project_pkl.databinding.ListHistoryAbsenBinding
import com.akmalzarkasyi.project_pkl.model.ModelDatabase
import com.akmalzarkasyi.project_pkl.utils.BitmapManager.base64ToBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class HistoryAdapter(
    private val mContext: Context,
    private val mAdapterCallback: MutableList<ModelDatabase>,
    historyActivity: HistoryActivity
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private var modelDatabase: MutableList<ModelDatabase> = mutableListOf()
    private val inflater: LayoutInflater = LayoutInflater.from(mContext)
    private lateinit var binding: ListHistoryAbsenBinding

    fun setDataAdapter(items: List<ModelDatabase>) {
        modelDatabase.clear()
        modelDatabase.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ListHistoryAbsenBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = modelDatabase[position]
        holder.bind(data)
    }

    override fun getItemCount(): Int = modelDatabase.size

    inner class ViewHolder(private val binding: ListHistoryAbsenBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ModelDatabase) {
            binding.tvNomor.text = data.uid.toString()
            binding.tvNama.text = data.nama
            binding.tvLokasi.text = data.lokasi
            binding.tvAbsenTime.text = data.tanggal
            binding.tvStatusAbsen.text = data.keterangan

            Glide.with(mContext)
                .load(base64ToBitmap(data.fotoSelfie))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_photo_camera)
                .into(binding.imageProfile)

            when (data.keterangan) {
                "Absen Masuk" -> setColorStatus(Color.GREEN)
                "Absen Keluar" -> setColorStatus(Color.RED)
                "Izin" -> setColorStatus(Color.BLUE)
                else -> setColorStatus(Color.BLACK) // Default color
            }

            binding.cvHistory.setOnClickListener {
                val modelDatabase = modelDatabase[adapterPosition]
                mAdapterCallback.onDelete(modelDatabase)
            }
        }

        private fun setColorStatus(color: Int) {
            binding.colorStatus.setBackgroundResource(R.drawable.bg_circle_radius)
            binding.colorStatus.backgroundTintList = ColorStateList.valueOf(color)
        }
    }

    interface HistoryAdapterCallback {
        fun onDelete(modelDatabase: ModelDatabase?)
    }
}