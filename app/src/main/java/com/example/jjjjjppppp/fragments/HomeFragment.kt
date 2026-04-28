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
            title = "欢迎使用工具箱",
            description = "实用工具，简单生活",
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
            showSettingsDrawer()
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

    private fun showSettingsDrawer() {
        try {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.drawer_settings, null)

            val builder = AlertDialog.Builder(requireContext())
            builder.setView(dialogView)
            builder.setCancelable(true)

            val dialog = builder.create()
            dialog.show()

            dialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.5).toInt(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // 设置关闭按钮
            val ivClose = dialogView.findViewById<ImageButton>(R.id.ivCloseSettings)
            ivClose?.setOnClickListener {
                dialog.dismiss()
            }

            // 设置功能按钮
            setupSettingsOptions(dialogView)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "设置加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSettingsOptions(dialogView: View) {
        try {
            // 检查更新
            val llCheckUpdate = dialogView.findViewById<LinearLayout>(R.id.llCheckUpdate)
            llCheckUpdate?.setOnClickListener {
                Toast.makeText(requireContext(), "已是最新版本 v1.0.0", Toast.LENGTH_SHORT).show()
            }

            // 关于我们
            val llAbout = dialogView.findViewById<LinearLayout>(R.id.llAbout)
            llAbout?.setOnClickListener {
                Toast.makeText(requireContext(), "工具箱 v1.0.0\n简单实用的工具集合", Toast.LENGTH_LONG).show()
            }

            // 意见反馈
            val llFeedback = dialogView.findViewById<LinearLayout>(R.id.llFeedback)
            llFeedback?.setOnClickListener {
                Toast.makeText(requireContext(), "感谢您的反馈！", Toast.LENGTH_SHORT).show()
            }

            // 清除缓存
            val llClearCache = dialogView.findViewById<LinearLayout>(R.id.llClearCache)
            llClearCache?.setOnClickListener {
                Toast.makeText(requireContext(), "缓存已清除", Toast.LENGTH_SHORT).show()
            }

            // 主题设置
            val llTheme = dialogView.findViewById<LinearLayout>(R.id.llTheme)
            llTheme?.setOnClickListener {
                Toast.makeText(requireContext(), "主题功能开发中", Toast.LENGTH_SHORT).show()
            }

            // 语言设置
            val llLanguage = dialogView.findViewById<LinearLayout>(R.id.llLanguage)
            llLanguage?.setOnClickListener {
                Toast.makeText(requireContext(), "语言功能开发中", Toast.LENGTH_SHORT).show()
            }

            // 隐私政策
            val llPrivacy = dialogView.findViewById<LinearLayout>(R.id.llPrivacy)
            llPrivacy?.setOnClickListener {
                Toast.makeText(requireContext(), "隐私政策：保护用户隐私是我们的责任", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showAddAnnouncementDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_announcement, null)
        val etTitle = dialogView.findViewById<android.widget.EditText>(R.id.etAnnouncementTitle)
        val etDescription = dialogView.findViewById<android.widget.EditText>(R.id.etAnnouncementDescription)

        AlertDialog.Builder(requireContext())
            .setTitle("新增公告")
            .setView(dialogView)
            .setPositiveButton("添加") { _, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()

                if (title.isNotEmpty() && description.isNotEmpty()) {
                    addAnnouncement(title, description)
                } else {
                    Toast.makeText(requireContext(), "请填写完整信息", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
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
        Toast.makeText(requireContext(), "公告已添加", Toast.LENGTH_SHORT).show()
    }

    private fun deleteCurrentAnnouncement() {
        if (currentPosition > 0 && currentPosition < announcements.size) {
            AlertDialog.Builder(requireContext())
                .setTitle("删除公告")
                .setMessage("确定要删除这条公告吗？")
                .setPositiveButton("删除") { _, _ ->
                    announcements.removeAt(currentPosition)
                    currentPosition = 0
                    rvBanner.smoothScrollToPosition(0)
                    bannerAdapter?.notifyDataSetChanged()
                    saveAnnouncements()
                    updateIndicators()
                    Toast.makeText(requireContext(), "公告已删除", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("取消", null)
                .show()
        } else {
            Toast.makeText(requireContext(), "无法删除Logo页面", Toast.LENGTH_SHORT).show()
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