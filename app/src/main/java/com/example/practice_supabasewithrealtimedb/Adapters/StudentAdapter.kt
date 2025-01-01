package com.example.practice_supabasewithrealtimedb.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.practice_supabasewithrealtimedb.R
import com.example.practice_supabasewithrealtimedb.StudentInfo
import com.example.practice_supabasewithrealtimedb.fragments.StudentFragment
import com.google.android.material.card.MaterialCardView

class StudentAdapter(var contex:StudentFragment,var array :ArrayList<StudentInfo> , val studentInterface :StudentInterface):RecyclerView.Adapter<StudentAdapter.ViewHolder>(){
    class ViewHolder(var view:View):RecyclerView.ViewHolder(view){
        val img=view.findViewById<ImageView>(R.id.imgV)
        val name=view.findViewById<TextView>(R.id.itemName)
        val Class =view.findViewById<TextView>(R.id.itemClass)
        val updaeBtn = view.findViewById<Button>(R.id.updateBtn)
        val deleteBtn = view.findViewById<Button>(R.id.deleteBtn)
        val cardSelected = view.findViewById<MaterialCardView>(R.id.materialCardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view =LayoutInflater.from(parent.context).inflate(R.layout.recycler_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return array.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text= array[position].name
        holder.Class.text=array[position].Class
        Glide.with(contex)
            .load(array[position].imag)
            .centerCrop()
            .into(holder.img)
        holder.updaeBtn.setOnClickListener { studentInterface.Update(position) }
        holder.deleteBtn.setOnClickListener { studentInterface.deleteClick(array[position],position) }
        holder.cardSelected.setOnClickListener { studentInterface.itemClicked(position,array[position]) }


    }


}
