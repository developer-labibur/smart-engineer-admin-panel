package com.rudhashi.seadminpanel.adaptor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rudhashi.seadminpanel.R
import com.rudhashi.seadminpanel.databinding.ItemAllUserBinding
import com.rudhashi.seadminpanel.model.User
import de.hdodenhof.circleimageview.CircleImageView

class AllUserAdapter(
    private val context: Context,
    private val usersList: List<User>,
    private val listener: OnUserClickListener
) : RecyclerView.Adapter<AllUserAdapter.UserViewHolder>() {

    interface OnUserClickListener {
        fun onDeleteClicked(userId: String)
        fun onEditClicked(user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_all_user, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = usersList[position]

        holder.apply {
            binding.apply {
                // Set up the UI with user data
                Glide.with(context)
                    .load(user.picture)
                    .placeholder(R.drawable.avatar)
                    .into(userProfilePic)

                userName.text = user.name
                userEmail.text = user.email
                userRole.text = user.role

                // Set up buttons
                iconBtnDelete.setOnClickListener {
                    listener.onDeleteClicked(user.userId)
                }

                iconBtnEdit.setOnClickListener {
                    listener.onEditClicked(user)
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return usersList.size
    }
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding = ItemAllUserBinding.bind(itemView)
    }
}