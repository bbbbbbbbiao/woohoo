package com.interview.assessment.ui.character

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.interview.assessment.R
import com.interview.assessment.data.model.Character

/** 角色列表适配器，把 Character 数据绑定到 item_character.xml。 */
class CharacterAdapter(
    private val onCharacterClick: (Character) -> Unit
) : ListAdapter<Character, CharacterAdapter.CharacterViewHolder>(CharacterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_character, parent, false)
        return CharacterViewHolder(itemView, onCharacterClick)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CharacterViewHolder(
        itemView: View,
        private val onCharacterClick: (Character) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.characterImageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.characterNameTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.characterStatusTextView)
        private val speciesTextView: TextView = itemView.findViewById(R.id.characterSpeciesTextView)

        fun bind(character: Character) {
            nameTextView.text = character.name
            statusTextView.text = itemView.context.getString(
                R.string.character_status_format,
                character.status
            )
            speciesTextView.text = itemView.context.getString(
                R.string.character_species_format,
                character.species
            )

            // Coil 异步加载头像；请求失败时显示应用图标，避免出现空白区域。
            imageView.load(character.image) {
                crossfade(true)
                placeholder(R.color.image_placeholder)
                error(R.mipmap.ic_launcher)
            }

            val statusColor = when {
                character.status.equals("alive", ignoreCase = true) -> R.color.status_alive
                character.status.equals("dead", ignoreCase = true) -> R.color.status_dead
                else -> R.color.status_unknown
            }
            statusTextView.setTextColor(ContextCompat.getColor(itemView.context, statusColor))

            // 点击整个卡片进入角色详情页。
            itemView.setOnClickListener { onCharacterClick(character) }
        }
    }

    private class CharacterDiffCallback : DiffUtil.ItemCallback<Character>() {
        override fun areItemsTheSame(oldItem: Character, newItem: Character): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Character, newItem: Character): Boolean {
            return oldItem == newItem
        }
    }
}
