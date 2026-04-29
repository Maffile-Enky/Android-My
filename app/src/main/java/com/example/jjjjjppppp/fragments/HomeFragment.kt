package com.example.jjjjjppppp.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.jjjjjppppp.R

class HomeFragment : Fragment() {

    private lateinit var rvBanner: RecyclerView
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var ibSettings: ImageButton
    private lateinit var indicatorContainer: LinearLayout

    private val announcements = ArrayList<Announcement>()
    private var currentPosition = 0
    private val BANNER_DELAY = 3000L // 3秒切换一次

    private var handler: Handler? = null
    private val bannerRunnable = object : Runnable {
        override fun run() {
            if (::rvBanner.isInitialized && announcements.size > 1) {
                currentPosition = (currentPosition + 1) % announcements.size
                rvBanner.smoothScrollToPosition(currentPosition)
                bannerAdapter.currentPosition = currentPosition
                updateIndicators()
                handler?.postDelayed(this, BANNER_DELAY)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvBanner = view.findViewById(R.id.rvBanner)
        ibSettings = view.findViewById(R.id.ibSettings)
        indicatorContainer = view.findViewById(R.id.indicatorContainer)

        // 初始化 Handler
        handler = Handler(Looper.getMainLooper())

        // 初始化公告数据
        initAnnouncements()

        // 加载保存的公告
        loadAnnouncements()

        // 设置适配器
        bannerAdapter = BannerAdapter(announcements)
        rvBanner.adapter = bannerAdapter

        // 设置RecyclerView为水平滑动
        rvBanner.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        // 添加PageSnapHelper实现翻页效果
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvBanner)

        // 滑动监听
        rvBanner.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                    if (firstVisiblePosition >= 0) {
                        currentPosition = firstVisiblePosition
                        bannerAdapter.currentPosition = currentPosition
                        updateIndicators()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // 不需要实现
            }
        })

        // 开始轮播
        startBannerRotation()

        // 设置按钮监听
        setupButtons(view)
    }

    private fun initAnnouncements() {
        announcements.clear()
        // 第一页为logo
        announcements.add(Announcement(
            title = getString(R.string.welcome_title),
            description = getString(R.string.welcome_desc),
            imageRes = R.mipmap.ic_launcher,
            isLogoPage = true
        ))
    }

    private fun loadAnnouncements() {
        try {
            val activity = requireActivity()
            val prefs = activity.getSharedPreferences("AnnouncementPrefs", 0)
            val savedAnnouncements = prefs.getString("announcements", "")

            if (!savedAnnouncements.isNullOrEmpty()) {
                val parts = savedAnnouncements.split("|||")
                for (part in parts) {
                    if (part.isNotEmpty()) {
                        val items = part.split("|")
                        if (items.size >= 2) {
                            announcements.add(Announcement(
                                title = items[0],
                                description = items[1],
                                imageRes = R.mipmap.ic_launcher,
                                isLogoPage = false
                            ))
                        }
                    }
                }
                bannerAdapter?.notifyDataSetChanged()
                updateIndicators()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveAnnouncements() {
        try {
            val activity = requireActivity()
            val prefs = activity.getSharedPreferences("AnnouncementPrefs", 0)
            val editor = prefs.edit()

            // 跳过第一页logo，只保存实际公告
            val actualAnnouncements = announcements.drop(1)
            val announcementString = actualAnnouncements.joinToString("|||") { "${it.title}|${it.description}" }

            editor.putString("announcements", announcementString)
            editor.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startBannerRotation() {
        // 确保handler已初始化
        if (handler != null) {
            handler?.removeCallbacks(bannerRunnable)
            handler?.postDelayed(bannerRunnable, BANNER_DELAY)
        }
    }

    private fun stopBannerRotation() {
        handler?.removeCallbacks(bannerRunnable)
    }

    private fun setupButtons(view: View) {
        // 设置按钮
        ibSettings.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }

        // 新增公告按钮
        view.findViewById<android.widget.Button>(R.id.btnAddAnnouncement).setOnClickListener {
            showAddAnnouncementDialog()
        }

        // 删除公告按钮
        view.findViewById<android.widget.Button>(R.id.btnDeleteAnnouncement).setOnClickListener {
            deleteCurrentAnnouncement()
        }
    }

    private fun showAddAnnouncementDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_announcement, null)
        val etTitle = dialogView.findViewById<android.widget.EditText>(R.id.etAnnouncementTitle)
        val etDescription = dialogView.findViewById<android.widget.EditText>(R.id.etAnnouncementDescription)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_announcement_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()

                if (title.isNotEmpty() && description.isNotEmpty()) {
                    addAnnouncement(title, description)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.fill_complete_info), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun addAnnouncement(title: String, description: String) {
        announcements.add(Announcement(
            title = title,
            description = description,
            imageRes = R.mipmap.ic_launcher,
            isLogoPage = false
        ))
        bannerAdapter?.notifyDataSetChanged()
        saveAnnouncements()
        updateIndicators()
        Toast.makeText(requireContext(), getString(R.string.announcement_added), Toast.LENGTH_SHORT).show()
    }

    private fun deleteCurrentAnnouncement() {
        if (currentPosition > 0 && currentPosition < announcements.size) {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_announcement))
                .setMessage(getString(R.string.delete_announcement_confirm))
                .setPositiveButton(getString(R.string.delete)) { _, _ ->
                    announcements.removeAt(currentPosition)
                    currentPosition = 0
                    rvBanner.smoothScrollToPosition(0)
                    bannerAdapter?.notifyDataSetChanged()
                    saveAnnouncements()
                    updateIndicators()
                    Toast.makeText(requireContext(), getString(R.string.announcement_deleted), Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.cannot_delete_logo), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateIndicators() {
        indicatorContainer.removeAllViews()

        for (i in announcements.indices) {
            val indicator = View(requireContext())
            val size = if (i == currentPosition) 10 else 8
            val backgroundColor = if (i == currentPosition) {
                android.graphics.Color.parseColor("#6200EE")
            } else {
                android.graphics.Color.parseColor("#CCCCCC")
            }

            indicator.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val drawable = android.graphics.drawable.GradientDrawable()
            drawable.shape = android.graphics.drawable.GradientDrawable.OVAL
            drawable.setColor(backgroundColor)
            indicator.background = drawable

            val params = ViewGroup.MarginLayoutParams(size, size)
            params.setMargins(4, 4, 4, 4)
            indicator.layoutParams = params

            indicatorContainer.addView(indicator)
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次Fragment显示时重新开始轮播
        startBannerRotation()
    }

    override fun onPause() {
        super.onPause()
        // Fragment不可见时停止轮播
        stopBannerRotation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 清理资源
        stopBannerRotation()
        handler = null
    }

    data class Announcement(
        val title: String,
        val description: String,
        val imageRes: Int,
        val isLogoPage: Boolean
    )
}