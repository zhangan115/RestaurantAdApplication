package com.restaurant.ad.application.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.restaurant.ad.application.R
import com.restaurant.ad.application.mode.Restaurant
 class ViewHolder(itemView: View, val imageView: ImageView
                         , val resName: TextView
                         , val resLocal: TextView)
    : RecyclerView.ViewHolder(itemView)

interface RestaurantChoose {
    fun resChoose(res: Restaurant)
}

class ResAdapter(private val datas: List<Restaurant>, private val content: Context, val restaurantChoose: RestaurantChoose?) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(content).inflate(R.layout.item_restaurant, parent, false)
        val image = view.findViewById<ImageView>(R.id.iv_icon)
        val title = view.findViewById<TextView>(R.id.tvRestaurantName)
        val time = view.findViewById<TextView>(R.id.tvRestaurantLocal)
        return ViewHolder(view, image, title, time)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.resName.text = datas[position].restaurantName
        holder.resLocal.text = datas[position].restaurantAddress
        holder.imageView.setImageDrawable(
                if (datas[position].isChoose) content.resources.getDrawable(R.drawable.select_a)
                else content.resources.getDrawable(R.drawable.select_n))
        holder.itemView.tag = datas[position]
        holder.itemView.setOnClickListener {
            val restaurant = it.tag as Restaurant
            for (r in datas) {
                r.isChoose = r.restaurantId == restaurant.restaurantId
            }
            notifyDataSetChanged()
            restaurantChoose?.resChoose(restaurant)
        }
    }

}