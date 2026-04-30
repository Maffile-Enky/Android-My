package com.example.jjjjjppppp.fragments

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.jjjjjppppp.*
import com.example.jjjjjppppp.R
import com.example.jjjjjppppp.network.dto.BannerItemDto
import com.example.jjjjjppppp.network.dto.FeaturedCardItemDto
import com.example.jjjjjppppp.network.dto.NoticeItemDto
import com.example.jjjjjppppp.utils.ThemeManager
import com.example.jjjjjppppp.viewmodel.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    // Views
    private lateinit var rvBanner: RecyclerView
    private lateinit var indicatorContainer: LinearLayout
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var llFeaturedContainer: LinearLayout
    private lateinit var llNoticesContainer: LinearLayout
    private lateinit var scrollContent: View
    private lateinit var progressLoading: View
    private lateinit var llError: View
    private lateinit var tvError: TextView
    private lateinit var btnRetry: View
    private lateinit var tvFeaturedTitle: TextView
    private lateinit var tvNoticesTitle: TextView

    // Banner auto-rotation
    private val bannerHandler = Handler(Looper.getMainLooper())
    private var bannerPosition = 0
    private lateinit var bannerRunnable: Runnable
    private var bannerItems: List<BannerItemDto> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind views
        rvBanner = view.findViewById(R.id.rvBanner)
        indicatorContainer = view.findViewById(R.id.indicatorContainer)
        llFeaturedContainer = view.findViewById(R.id.llFeaturedContainer)
        llNoticesContainer = view.findViewById(R.id.llNoticesContainer)
        scrollContent = view.findViewById(R.id.scrollContent)
        progressLoading = view.findViewById(R.id.progressLoading)
        llError = view.findViewById(R.id.llError)
        tvError = view.findViewById(R.id.tvError)
        btnRetry = view.findViewById(R.id.btnRetry)
        tvFeaturedTitle = view.findViewById(R.id.tvFeaturedTitle)
        tvNoticesTitle = view.findViewById(R.id.tvNoticesTitle)

        // Settings button
        view.findViewById<View>(R.id.ibSettings).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }

        // Retry button
        btnRetry.setOnClickListener { viewModel.loadConfig() }

        // Setup banner RecyclerView
        setupBanner()

        // Setup auto-rotation
        bannerRunnable = Runnable { rotateBanner() }

        // Observe ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { loading ->
                if (loading) {
                    progressLoading.visibility = View.VISIBLE
                    scrollContent.visibility = View.GONE
                    llError.visibility = View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collectLatest { error ->
                if (error != null) {
                    progressLoading.visibility = View.GONE
                    scrollContent.visibility = View.GONE
                    llError.visibility = View.VISIBLE
                    tvError.text = error
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.config.collectLatest { config ->
                if (config != null) {
                    progressLoading.visibility = View.GONE
                    llError.visibility = View.GONE
                    scrollContent.visibility = View.VISIBLE
                    applyConfig(config)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (bannerItems.size > 1) {
            bannerHandler.postDelayed(bannerRunnable, 3000)
        }
    }

    override fun onPause() {
        super.onPause()
        bannerHandler.removeCallbacks(bannerRunnable)
    }

    private fun applyConfig(config: com.example.jjjjjppppp.network.dto.HomeConfigDto) {
        buildBanners(config.banners)
        buildFeaturedCards(config.featuredCards)
        buildNotices(config.notices)
    }

    // ==================== Banner ====================

    private fun setupBanner() {
        bannerAdapter = BannerAdapter { }
        rvBanner.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvBanner.adapter = bannerAdapter
        PagerSnapHelper().attachToRecyclerView(rvBanner)

        rvBanner.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                    val pos = (layoutManager?.findFirstVisibleItemPosition() ?: 0) % bannerItems.size.coerceAtLeast(1)
                    bannerPosition = pos
                    updateIndicators(bannerItems.size, pos)
                }
            }
        })
    }

    private fun buildBanners(banners: List<BannerItemDto>) {
        bannerItems = banners
        if (banners.isEmpty()) {
            rvBanner.visibility = View.GONE
            indicatorContainer.visibility = View.GONE
            return
        }
        rvBanner.visibility = View.VISIBLE
        indicatorContainer.visibility = if (banners.size > 1) View.VISIBLE else View.GONE

        bannerAdapter.setItems(banners.map { it.title to it.content })
        bannerPosition = 0
        // Start in the middle for infinite scroll illusion
        if (banners.size > 1) {
            val midStart = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % banners.size)
            rvBanner.scrollToPosition(midStart)
        }
        updateIndicators(banners.size, 0)
    }

    private fun rotateBanner() {
        if (bannerItems.size <= 1) return
        bannerPosition = (bannerPosition + 1) % bannerItems.size
        val layoutManager = rvBanner.layoutManager as? LinearLayoutManager ?: return
        val currentVisible = layoutManager.findFirstVisibleItemPosition()
        rvBanner.smoothScrollToPosition(currentVisible + 1)
        updateIndicators(bannerItems.size, bannerPosition)
        bannerHandler.postDelayed(bannerRunnable, 3000)
    }

    private fun updateIndicators(count: Int, current: Int) {
        indicatorContainer.removeAllViews()
        for (i in 0 until count) {
            val dot = View(requireContext())
            val size = if (i == current) 10 else 8
            val color = if (i == current) {
                ThemeManager.getColorThemePrimaryColor(requireContext())
            } else {
                0xFFCCCCCC.toInt()
            }
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
                setSize(size, size)
            }
            dot.background = drawable
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(4, 0, 4, 0)
            }
            dot.layoutParams = params
            indicatorContainer.addView(dot)
        }
    }

    // ==================== Featured Cards ====================

    private fun buildFeaturedCards(cards: List<FeaturedCardItemDto>) {
        llFeaturedContainer.removeAllViews()
        if (cards.isEmpty()) {
            tvFeaturedTitle.visibility = View.GONE
            llFeaturedContainer.visibility = View.GONE
            return
        }
        tvFeaturedTitle.visibility = View.VISIBLE
        llFeaturedContainer.visibility = View.VISIBLE

        for (card in cards) {
            val cardView = CardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 12 }
                radius = 12f
                cardElevation = 2f
                setCardBackgroundColor(0xFFFFFFFF.toInt())
                setContentPadding(20, 16, 20, 16)
            }

            val contentLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
            }

            val titleView = TextView(requireContext()).apply {
                text = card.title
                textSize = 16f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(0xFF333333.toInt())
            }

            val descView = TextView(requireContext()).apply {
                text = card.description
                textSize = 13f
                setTextColor(0xFF999999.toInt())
                setPadding(0, 6, 0, 0)
            }

            contentLayout.addView(titleView)
            contentLayout.addView(descView)
            cardView.addView(contentLayout)

            // Handle click — simulate bottom nav tab press for identical behavior
            if (card.actionType.isNotBlank()) {
                cardView.isClickable = true
                cardView.isFocusable = true
                cardView.setOnClickListener {
                    val destId = when (card.actionType) {
                        "community" -> R.id.nav_community
                        "tools" -> R.id.nav_tools
                        else -> null
                    }
                    if (destId != null) {
                        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                        bottomNav.selectedItemId = destId
                    }
                }
            }

            llFeaturedContainer.addView(cardView)
        }
    }

    // ==================== Notices ====================

    private fun buildNotices(notices: List<NoticeItemDto>) {
        llNoticesContainer.removeAllViews()
        if (notices.isEmpty()) {
            tvNoticesTitle.visibility = View.GONE
            llNoticesContainer.visibility = View.GONE
            return
        }
        tvNoticesTitle.visibility = View.VISIBLE
        llNoticesContainer.visibility = View.VISIBLE

        val primaryColor = ThemeManager.getColorThemePrimaryColor(requireContext())

        for (notice in notices) {
            val item = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 10, 0, 10)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Dot indicator
            val dot = View(requireContext()).apply {
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(primaryColor)
                    setSize(8, 8)
                }
                background = drawable
                layoutParams = LinearLayout.LayoutParams(8, 8).apply {
                    marginEnd = 12
                }
            }
            item.addView(dot)

            // Notice content
            val contentLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val titleView = TextView(requireContext()).apply {
                text = notice.title
                textSize = 14f
                setTextColor(0xFF333333.toInt())
            }

            val timeView = TextView(requireContext()).apply {
                text = notice.createdAt
                textSize = 11f
                setTextColor(0xFF999999.toInt())
            }

            contentLayout.addView(titleView)
            contentLayout.addView(timeView)
            item.addView(contentLayout)

            // Click to show notice detail
            item.isClickable = true
            item.isFocusable = true
            item.setBackgroundResource(android.R.drawable.list_selector_background)
            item.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle(notice.title)
                    .setMessage(notice.content + "\n\n" + notice.createdAt)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }

            // Separator line
            val separator = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1
                ).apply { topMargin = 10 }
                setBackgroundColor(0xFFEEEEEE.toInt())
            }

            llNoticesContainer.addView(item)
            llNoticesContainer.addView(separator)
        }
    }
}
