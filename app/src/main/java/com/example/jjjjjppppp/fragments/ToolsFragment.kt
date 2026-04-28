package com.example.jjjjjppppp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.jjjjjppppp.BmiCalculatorActivity
import com.example.jjjjjppppp.CalculatorActivity
import com.example.jjjjjppppp.CounterActivity
import com.example.jjjjjppppp.NotesActivity
import com.example.jjjjjppppp.R
import com.example.jjjjjppppp.RandomNumberActivity
import com.example.jjjjjppppp.StopwatchActivity
import com.example.jjjjjppppp.TimerActivity
import com.example.jjjjjppppp.TodoActivity
import com.example.jjjjjppppp.UnitConverterActivity
import com.example.jjjjjppppp.WordCounterActivity
import androidx.cardview.widget.CardView

class ToolsFragment : Fragment() {

    private lateinit var toolsContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tools, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolsContainer = view.findViewById(R.id.toolsContainer)

        // 初始化工具列表
        setupTools()
    }

    private fun setupTools() {
        val tools = listOf(
            ToolInfo(
                name = getString(R.string.tool_counter),
                description = "简单实用的计数工具，支持数据保存",
                icon = R.mipmap.ic_launcher,
                activityClass = CounterActivity::class.java
            ),
            ToolInfo(
                name = getString(R.string.tool_calculator),
                description = "基础计算器，支持四则运算",
                icon = R.drawable.ic_calculator,
                activityClass = CalculatorActivity::class.java
            ),
            ToolInfo(
                name = getString(R.string.tool_stopwatch),
                description = "精准秒表，支持计次功能",
                icon = R.drawable.ic_stopwatch,
                activityClass = StopwatchActivity::class.java
            ),
            ToolInfo(
                name = getString(R.string.tool_timer),
                description = "倒计时工具，设置提醒",
                icon = R.drawable.ic_timer,
                activityClass = TimerActivity::class.java
            ),
            ToolInfo(
                name = getString(R.string.tool_todo),
                description = "待办事项管理，记录日常任务",
                icon = R.drawable.ic_todo,
                activityClass = TodoActivity::class.java
            ),
            ToolInfo(
                name = getString(R.string.tool_random),
                description = "生成指定范围的随机数，支持掷骰子",
                icon = R.drawable.ic_random,
                activityClass = RandomNumberActivity::class.java
            ),
            ToolInfo(
                name = getString(R.string.tool_converter),
                description = "长度、重量、温度单位换算",
                icon = R.drawable.ic_convert,
                activityClass = UnitConverterActivity::class.java
            ),
            ToolInfo(
                name = getString(R.string.tool_notes),
                description = "随时记录想法和重要信息",
                icon = R.drawable.ic_note,
                activityClass = NotesActivity::class.java
            ),
            ToolInfo(
                name = getString(R.string.tool_bmi),
                description = "计算身体质量指数，评估体重状态",
                icon = R.drawable.ic_bmi,
                activityClass = BmiCalculatorActivity::class.java
            ),
            ToolInfo(
                name = getString(R.string.tool_word_count),
                description = "统计文本的字符数、中文数和单词数",
                icon = R.drawable.ic_word_count,
                activityClass = WordCounterActivity::class.java
            )
        )

        // 每两个工具放在一行
        for (i in tools.indices step 2) {
            val row = createRowLayout()

            // 添加第一个工具
            val tool1View = createToolCard(tools[i])
            row.addView(tool1View)

            // 如果有第二个工具，添加它
            if (i + 1 < tools.size) {
                val tool2View = createToolCard(tools[i + 1])
                row.addView(tool2View)
            }

            toolsContainer.addView(row)
        }
    }

    private fun createRowLayout(): LinearLayout {
        val row = LinearLayout(requireContext())
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        row.orientation = LinearLayout.HORIZONTAL
        row.weightSum = 2f
        row.setPadding(0, 8, 0, 8)
        return row
    }

    private fun createToolCard(tool: ToolInfo): View {
        val cardView = layoutInflater.inflate(R.layout.item_tool_card, null) as CardView

        val ivToolIcon = cardView.findViewById<android.widget.ImageView>(R.id.ivToolIcon)
        val tvToolName = cardView.findViewById<TextView>(R.id.tvToolName)
        val tvToolDescription = cardView.findViewById<TextView>(R.id.tvToolDescription)

        ivToolIcon.setImageResource(tool.icon)
        tvToolName.text = tool.name
        tvToolDescription.text = tool.description

        // 设置卡片权重为1，每行两个卡片
        val params = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.weight = 1f
        params.setMargins(8, 0, 8, 0)
        cardView.layoutParams = params

        cardView.setOnClickListener {
            val intent = Intent(requireContext(), tool.activityClass)
            startActivity(intent)
        }

        return cardView
    }

    data class ToolInfo(
        val name: String,
        val description: String,
        val icon: Int,
        val activityClass: Class<*>
    )
}