package com.interview.assessment.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.google.android.material.button.MaterialButton
import com.interview.assessment.R
import com.interview.assessment.data.model.Character
import com.interview.assessment.data.repository.RepositoryProvider
import kotlinx.coroutines.launch

/** 点击列表项后打开的角色详情页。 */
class CharacterDetailActivity : AppCompatActivity() {

    // 使用工厂注入 Repository，并保证配置变更后复用原有 ViewModel。
    private val viewModel: CharacterDetailViewModel by lazy {
        val factory = CharacterDetailViewModelFactory(RepositoryProvider.characterRepository)
        ViewModelProvider(this, factory)[CharacterDetailViewModel::class.java]
    }

    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var contentContainer: View
    private lateinit var errorContainer: View
    private lateinit var errorTextView: TextView
    private var characterId: Int = INVALID_CHARACTER_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_detail)

        title = getString(R.string.character_detail_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        characterId = intent.getIntExtra(EXTRA_CHARACTER_ID, INVALID_CHARACTER_ID)
        if (characterId == INVALID_CHARACTER_ID) {
            Toast.makeText(this, R.string.invalid_character, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        bindViews()
        findViewById<MaterialButton>(R.id.detailRetryButton).setOnClickListener {
            viewModel.loadCharacter(characterId, forceRefresh = true)
        }
        observeUiState()
        viewModel.loadCharacter(characterId)
    }

    private fun bindViews() {
        loadingProgressBar = findViewById(R.id.detailLoadingProgressBar)
        contentContainer = findViewById(R.id.detailContentContainer)
        errorContainer = findViewById(R.id.detailErrorContainer)
        errorTextView = findViewById(R.id.detailErrorTextView)
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderUiState)
            }
        }
    }

    private fun renderUiState(state: CharacterDetailUiState) {
        when (state) {
            CharacterDetailUiState.Idle,
            CharacterDetailUiState.Loading -> showLoading()

            is CharacterDetailUiState.Success -> showCharacter(state.character)
            is CharacterDetailUiState.Error -> showError(state.message)
        }
    }

    private fun showLoading() {
        loadingProgressBar.visibility = View.VISIBLE
        contentContainer.visibility = View.GONE
        errorContainer.visibility = View.GONE
    }

    /** 将单角色接口返回的数据展示到详情卡片。 */
    private fun showCharacter(character: Character) {
        loadingProgressBar.visibility = View.GONE
        contentContainer.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE

        findViewById<ImageView>(R.id.detailImageView).load(character.image) {
            crossfade(true)
            placeholder(R.color.image_placeholder)
            error(R.mipmap.ic_launcher)
        }
        findViewById<TextView>(R.id.detailNameTextView).text = character.name

        val statusTextView = findViewById<TextView>(R.id.detailStatusTextView)
        statusTextView.text = getString(R.string.character_status_format, character.status)
        val statusColor = when {
            character.status.equals("alive", ignoreCase = true) -> R.color.status_alive
            character.status.equals("dead", ignoreCase = true) -> R.color.status_dead
            else -> R.color.status_unknown
        }
        statusTextView.setTextColor(ContextCompat.getColor(this, statusColor))

        findViewById<TextView>(R.id.detailSpeciesTextView).text =
            getString(R.string.character_species_format, character.species)
        findViewById<TextView>(R.id.detailGenderTextView).text =
            getString(R.string.character_gender_format, character.gender)
        findViewById<TextView>(R.id.detailTypeTextView).text =
            getString(
                R.string.character_type_format,
                character.type.ifBlank { getString(R.string.unknown_value) }
            )
        findViewById<TextView>(R.id.detailOriginTextView).text =
            getString(R.string.character_origin_format, character.origin.name)
        findViewById<TextView>(R.id.detailLocationTextView).text =
            getString(R.string.character_location_format, character.location.name)
    }

    private fun showError(message: String) {
        loadingProgressBar.visibility = View.GONE
        contentContainer.visibility = View.GONE
        errorContainer.visibility = View.VISIBLE
        errorTextView.text = message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        private const val EXTRA_CHARACTER_ID = "extra_character_id"
        private const val INVALID_CHARACTER_ID = -1

        /** 统一创建详情页 Intent，避免其他页面写错参数名。 */
        fun createIntent(context: Context, characterId: Int): Intent {
            return Intent(context, CharacterDetailActivity::class.java)
                .putExtra(EXTRA_CHARACTER_ID, characterId)
        }
    }
}
