package com.interview.assessment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.interview.assessment.data.repository.RepositoryProvider
import com.interview.assessment.ui.character.CharacterAdapter
import com.interview.assessment.ui.character.CharacterUiState
import com.interview.assessment.ui.character.CharacterViewModel
import com.interview.assessment.ui.character.CharacterViewModelFactory
import com.interview.assessment.ui.detail.CharacterDetailActivity
import kotlinx.coroutines.launch

/** 角色列表主页面。 */
class MainActivity : AppCompatActivity() {

    // 使用工厂注入 Repository，ViewModel 不直接负责创建数据层对象。
    private val viewModel: CharacterViewModel by lazy {
        val factory = CharacterViewModelFactory(RepositoryProvider.characterRepository)
        ViewModelProvider(this, factory)[CharacterViewModel::class.java]
    }

    private lateinit var characterAdapter: CharacterAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var loadMoreProgressBar: ProgressBar
    private lateinit var errorContainer: View
    private lateinit var errorTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = getString(R.string.character_list_title)
        bindViews()
        setupCharacterList()
        setupRetryButton()
        observeUiState()
    }

    /** 获取布局中的控件引用。 */
    private fun bindViews() {
        recyclerView = findViewById(R.id.characterRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        loadMoreProgressBar = findViewById(R.id.loadMoreProgressBar)
        errorContainer = findViewById(R.id.errorContainer)
        errorTextView = findViewById(R.id.errorTextView)
    }

    /** 配置 RecyclerView、点击事件和滚动分页。 */
    private fun setupCharacterList() {
        val layoutManager = LinearLayoutManager(this)
        characterAdapter = CharacterAdapter { character ->
            val intent = CharacterDetailActivity.createIntent(this, character.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = characterAdapter
        recyclerView.setHasFixedSize(true)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // 只在向下滚动且距离底部不足 5 项时触发下一页请求。
                if (dy <= 0) return
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                if (lastVisiblePosition >= characterAdapter.itemCount - 5) {
                    viewModel.loadNextPage()
                }
            }
        })
    }

    /** 失败页面中的按钮重新请求第一页。 */
    private fun setupRetryButton() {
        findViewById<MaterialButton>(R.id.retryButton).setOnClickListener {
            viewModel.refresh()
        }
    }

    /** 只在页面可见时收集 StateFlow，避免后台页面继续更新 UI。 */
    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderUiState)
            }
        }
    }

    /** 根据 ViewModel 状态切换加载、成功和错误界面。 */
    private fun renderUiState(state: CharacterUiState) {
        when (state) {
            CharacterUiState.Loading -> {
                loadingProgressBar.visibility = View.VISIBLE
                loadMoreProgressBar.visibility = View.GONE
                recyclerView.visibility = View.GONE
                errorContainer.visibility = View.GONE
            }

            is CharacterUiState.Success -> {
                loadingProgressBar.visibility = View.GONE
                errorContainer.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                loadMoreProgressBar.visibility = if (state.isLoadingMore) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                characterAdapter.submitList(state.characters)

                state.loadMoreError?.let { message ->
                    // Snackbar 保留当前列表，并允许用户直接重试失败的下一页。
                    Snackbar.make(
                        recyclerView,
                        getString(R.string.load_more_failed_format, message),
                        Snackbar.LENGTH_LONG
                    ).setAction(R.string.retry) {
                        viewModel.loadNextPage()
                    }.show()
                    viewModel.consumeLoadMoreError()
                }
            }

            is CharacterUiState.Error -> {
                loadingProgressBar.visibility = View.GONE
                loadMoreProgressBar.visibility = View.GONE
                recyclerView.visibility = View.GONE
                errorContainer.visibility = View.VISIBLE
                errorTextView.text = state.message

                // 基本要求：加载失败时显示 Toast 提示。
                Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
