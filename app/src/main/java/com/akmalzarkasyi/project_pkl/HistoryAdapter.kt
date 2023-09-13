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
    private val modelDatabase: MutableList<ModelDatabase>,
    private val mAdapterCallback: HistoryAdapterCallback
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    fun setDataAdapter(items: List<ModelDatabase>) {
        modelDatabase.clear()
        modelDatabase.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ListHistoryAbsenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = modelDatabase[position]

        // Gunakan binding untuk mengakses elemen UI
        val binding = holder.binding
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

        // Setel click listener menggunakan binding
        binding.cvHistory.setOnClickListener {
            val modelLaundry = modelDatabase[holder.adapterPosition]
            mAdapterCallback.onDelete(modelLaundry)
        }

        // Setel latar belakang dan tint untuk colorStatus berdasarkan data.keterangan
        when (position % 3) {
            0 -> {
                binding.colorStatus.setBackgroundResource(R.drawable.bg_circle_radius)
                binding.colorStatus.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
            }
            1 -> {
                binding.colorStatus.setBackgroundResource(R.drawable.bg_circle_radius)
                binding.colorStatus.backgroundTintList = ColorStateList.valueOf(Color.RED)
            }
            2 -> {
                binding.colorStatus.setBackgroundResource(R.drawable.bg_circle_radius)
                binding.colorStatus.backgroundTintList = ColorStateList.valueOf(Color.BLUE)
            }
        }
    }

    override fun getItemCount(): Int {
        return modelDatabase.size
    }

    inner class ViewHolder(val binding: ListHistoryAbsenBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface HistoryAdapterCallback {
        fun onDelete(modelDatabase: ModelDatabase?)
    }
}